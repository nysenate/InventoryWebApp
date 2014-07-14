/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.exception.BlankMessageException;
import gov.nysenate.inventory.exception.InvalidParameterException;
import gov.nysenate.inventory.exception.ParameterNotUsedException;
import gov.nysenate.inventory.model.EmailData;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.exception.ReportNotGeneratedException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.model.EmailRecord;
import gov.nysenate.inventory.util.EmailValidator;
import gov.nysenate.inventory.util.InvUtil;
import gov.nysenate.inventory.db.TransactionMapper;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.net.URL;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.Properties;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Address;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.http.HttpServletRequest;

import org.apache.commons.io.IOUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author senateuser
 */
public class EmailMoveReceipt implements Runnable {

    Properties properties = new Properties();
    InputStream in;
    String receiptPath = "C:\\";
    String dbaUrl = "";
    String serverOS = "Windows"; // Default to Windows OS
    String pathDelimeter = "\\";
    private String error = null;
    private String naemailFrom = null;
    private String naemailNameFrom = null;
    private String naemailTo1 = null;
    private String naemailNameTo1 = null;
    private String naemailTo2 = null;
    private String naemailNameTo2 = null;
    private String testingModeProperty = null;
    private String[] naemailErrorTo = null;
    private String[] naemailErrorNameTo = null;
    private String[] naemailGenTo = null;
    private String[] naemailGenNameTo = null;
    private String testingModeParam = null;
    private Employee signingEmployee = null;
    private Employee remoteUser = null;
    private String username = null;
    private String password = null;
    private DbConnect db = null;
    private String userFallback = null;
    final int PICKUP = -101, DELIVERY = -102;
    private Transaction pickup = null;
    private Transaction delivery = null;
    private Date dtreceipt = new Date();
    private String receiptFilename = null;
    private String napickupbyName = null;
    private String pickupAddress = null;
    private String nadeliverbyName = null;
    private String deliverAddress = null;
    private boolean testingMode = false;
    private Employee pickupEmployee;
    private Employee deliveryEmployee;
    private final int REPORTRETRYLIMITDEFAULT = 5;   // Default Report Retry Limit
    private final int REPORTWAITINTERVALDEFAULT = 120; // Default Wait Time between Retries in Seconds
    private int emailType;
    private String reportRetryLimitString = String.valueOf(REPORTRETRYLIMITDEFAULT); // (String Representation) Max number of Retries
    private String reportWaitIntervalString = String.valueOf(REPORTWAITINTERVALDEFAULT); // (String Represenation) In Seconds between retries
    private int reportRetryLimit = REPORTRETRYLIMITDEFAULT;
    private int reportWaitInterval = REPORTWAITINTERVALDEFAULT;
    private int retryCounter = 0;  // Default to 0 since the initial try will be counted as part of the retries
    // Ex: Max Retry Interval of 5 would be the first try plus two more. 
    private int nuxrpd = 0;
    private MimeBodyPart attachmentPart;
    private String receiptURL = null;
    private String transTypeParam;
    private String serverInfo = "";
    private ArrayList<EmailRecord> problemEmailAddrs = new ArrayList<>();
    EmailValidator emailValidator = new EmailValidator();
    InvUtil invUtil = new InvUtil();
    HttpServletRequest request = null;
    private String subjectAddText = "";
    private static final Logger log = Logger.getLogger(EmailMoveReceipt.class.getName());
    private String paperworkType = null;
    private Transaction initialPickup = null;
    private Employee remoteVerByEmployee = null;
    boolean remoteDeliveryNoSigDelivered = false;
    private String calledBy = "";

    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, Transaction trans) {
        this(request, username, password, type, (String) null, trans);
    }

    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, Transaction trans, String calledBy) {
        this(request, username, password, type, (String) null, trans, calledBy);
    }

    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, String paperworkType, Transaction trans) {
        this(request, username, password, type, paperworkType, trans, "");
    }

    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, String paperworkType, Transaction trans, String calledBy) {
        this.request = request;
        this.paperworkType = paperworkType;
        this.calledBy = calledBy;

        if (this.calledBy == null) {
            this.calledBy = "";
        }

        System.setProperty("java.net.preferIPv4Stack", "true");   // added for test purposes only

        String verificationMethod = trans.getVerificationMethod();

        switch (type) {
            case "pickup":
                this.emailType = PICKUP;
                this.username = username;
                this.password = password;
                this.pickup = trans;
                userFallback = username; // userfallback is not really being used

                if (this.paperworkType == null || this.paperworkType.trim().length() == 0 && this.pickup != null) {
                    this.paperworkType = this.pickup.getRemoteType();
                }
                // but it needs to be passed so it is being
                // set to username (which should be set)
                attachmentPart = null;
                transTypeParam = "&p_transtype=PICKUP";
                db = new DbConnect(request, username, password);

                this.serverInfo = "";
                this.subjectAddText = "";

                if (verificationMethod != null && !verificationMethod.equals("") && this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {
                    int remoteVerEmpNuxrefem = trans.getEmployeeId();
                    if (remoteVerEmpNuxrefem > 0) {
                        String remoteVerEmpNuxrefemStr = new Integer(remoteVerEmpNuxrefem).toString();
                        remoteVerByEmployee = db.getEmployee(remoteVerEmpNuxrefemStr, false, userFallback);
                        try {
                            remoteVerByEmployee.setEmployeeNameOrder(remoteVerByEmployee.FIRST_MI_LAST_SUFFIX);
                        } catch (Exception e) {
                            log.warn("**WARNING: Could not set Remote Pickup Verification By Employee Name order", e);
                        }
                    }
                }

                if (db.serverName.toUpperCase().contains("PROD")) {
                    if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {
                        this.subjectAddText = " (REMOTE)";
                    } else {
                        this.subjectAddText = "";
                    }
                    this.serverInfo = "";
                } else {
                    if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {
                        this.subjectAddText = " (REMOTE) (" + db.serverName + ")";
                    } else {
                        this.subjectAddText = " (" + db.serverName + ")";
                    }
                    this.serverInfo = "<b>SERVER: " + db.serverName + " (" + db.serverIpAddr + ")</b><br/><br/><br/>";
                }

                properties = new Properties();
                in = getClass().getClassLoader().getResourceAsStream("config.properties");
                serverOS = System.getProperty("os.name");
                if (serverOS.toUpperCase().indexOf("WINDOWS") == -1) {
                    pathDelimeter = "/";
                }
                try {
                    properties.load(in);
                    receiptPath = properties.getProperty("receiptPath");
                    if (!receiptPath.trim().endsWith(pathDelimeter)) {
                        receiptPath = receiptPath.trim() + pathDelimeter;
                    }

                    dbaUrl = properties.getProperty("dbaUrl");
                    testingModeParam = properties.getProperty("testingMode");
                    testingModeCheck();
                    initializeEmailTo();
                } catch (IOException ex) {
                    log.warn(null, ex);
                }
                break;
            case "delivery":
                this.emailType = DELIVERY;
                this.username = username;
                this.password = password;
                this.delivery = trans;

                if (this.paperworkType == null || this.paperworkType.trim().length() == 0 && this.delivery != null) {
                    this.paperworkType = this.delivery.getRemoteType();
                }
                
                userFallback = username; // userfallback is not really being used
                // but it needs to be passed so it is being
                // set to username (which should be set)
                transTypeParam = "&p_transtype=DELIVERY";
                attachmentPart = null;
                db = new DbConnect(request, username, password);
                this.serverInfo = "";
                this.subjectAddText = "";

                if (verificationMethod != null && !verificationMethod.equals("") && this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RDL")) {
                    int remoteVerEmpNuxrefem = trans.getEmployeeId();
                    if (remoteVerEmpNuxrefem > 0) {
                        String remoteVerEmpNuxrefemStr = new Integer(remoteVerEmpNuxrefem).toString();
                        remoteVerByEmployee = db.getEmployee(remoteVerEmpNuxrefemStr, false, userFallback);
                        try {
                            remoteVerByEmployee.setEmployeeNameOrder(remoteVerByEmployee.FIRST_MI_LAST_SUFFIX);
                        } catch (Exception e) {
                            log.warn("**WARNING: Could not set Remote Delivery Verification By Employee Name order", e);
                        }
                    }
                }

                if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RDL")) {
                    TransactionMapper transactionMapper = new TransactionMapper();

                    /*
                     * If the transaction is a Remote Delivery which was called from 
                     * the Delivery SERVLET (this.calledBy==DELIVERY) and Remote Verification still 
                     * has not been entered (verificationMethod==null||verificationMethod.equals(""))
                     * then the e-mail needs to specify that it has been delivered.
                     * 
                     */

                    if (this.calledBy != null && this.calledBy.equalsIgnoreCase("delivery") && (verificationMethod == null || verificationMethod.equals(""))) {
                        remoteDeliveryNoSigDelivered = true;
                    }
                    try {
                        if (this.delivery != null && this.delivery.getNuxrpd() > 0) {
                            this.initialPickup = transactionMapper.queryTransaction(db, this.delivery.getNuxrpd());
                        }
                    } catch (SQLException ex) {
                        log.warn(null, ex);
                    } catch (ClassNotFoundException ex) {
                        log.warn(null, ex);
                    }
                }

                if (db.serverName.toUpperCase().contains("PROD")) {
                    if (this.paperworkType.equalsIgnoreCase("RDL")) {
                        this.subjectAddText = " (REMOTE)";
                    } else {
                        this.subjectAddText = "";
                    }
                    this.serverInfo = "";
                } else {
                    if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RDL")) {
                        this.subjectAddText = " (REMOTE) (" + db.serverName + ")";
                    } else {
                        this.subjectAddText = " (" + db.serverName + ")";
                    }
                    this.serverInfo = "<b>SERVER: " + db.serverName + " (" + db.serverIpAddr + ")</b><br/><br/><br/>";
                }
                System.setProperty("java.net.preferIPv4Stack", "true");   // added for test purposes only
                properties = new Properties();
                in = getClass().getClassLoader().getResourceAsStream("config.properties");
                serverOS = System.getProperty("os.name");
                if (serverOS.toUpperCase().indexOf("WINDOWS") == -1) {
                    pathDelimeter = "/";
                }
                try {
                    properties.load(in);
                    receiptPath = properties.getProperty("receiptPath");
                    if (!receiptPath.trim().endsWith(pathDelimeter)) {
                        receiptPath = receiptPath.trim() + pathDelimeter;
                    }
                    testingModeParam = properties.getProperty("testingMode");
                    dbaUrl = properties.getProperty("dbaUrl");
                    testingModeCheck();
                    initializeEmailTo();
                } catch (IOException ex) {
                    log.warn(null, ex);
                }
                break;
        }
    }

    public void testingModeCheck() {
        //System.out.println ("TESTINGMODEPARAM:"+testingModeParam);
        if (testingModeParam != null && testingModeParam.trim().length() > 0) {
            if (testingModeParam.toUpperCase().indexOf("T") > -1) {
                testingMode = true;
                log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****testingModeParam has a T, so Testing Mode is set to TRUE Pickup.processRequest ");
            } else {
                testingMode = false;
            }
        } else if (testingModeProperty == null || testingModeProperty.toUpperCase().contains("T")) {
            testingMode = true;
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ***Testing Mode is set to TRUE Pickup.processRequest ");
        }
    }

    private void initializeEmailTo() {
        if (properties == null) {
            return;
        }

        String naemailGenToS = properties.getProperty("report.gen.email_to");
        String naemailGenNameToS = properties.getProperty("report.gen.email_name_to");
        String naemailErrorToS = properties.getProperty("report.error.email_to");
        String naemailErrorNameToS = properties.getProperty("report.error.email_name_to");

        if (naemailGenToS == null) {
            this.naemailGenTo = null;
        } else {
            this.naemailGenTo = naemailGenToS.split("\\|");
        }

        if (naemailGenNameToS == null) {
            this.naemailGenNameTo = null;
        } else {
            this.naemailGenNameTo = naemailGenNameToS.split("\\|");
        }

        if (naemailErrorToS == null) {
            this.naemailErrorTo = null;
        } else {
            this.naemailErrorTo = naemailErrorToS.split("\\|");
        }

        if (naemailErrorNameToS == null) {
            this.naemailGenNameTo = null;
        } else {
            this.naemailErrorNameTo = naemailErrorNameToS.split("\\|");
        }

        try {
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") " + "initializeEmailTo: Length: " + this.naemailErrorTo.length + "Name Length: " + this.naemailGenNameTo.length);
        } catch (NullPointerException e) {
            e.printStackTrace();
        }
    }

    /*
     * Pickup Specific function serves as the initial setup code for the sendEmail(int emailType)
     * which handles both Pickup and Delivery
     */
    public int sendPickupEmailReceipt(Transaction pickup) {

        if (emailType != PICKUP) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "***WARNING: Email Type was not set to PICKUP!!! Not emailing Pickup receipt.");
            return 30;
        }
        this.pickup = pickup;
//    String napickupby = pickup.getNapickupby();
        String originLocation = pickup.getOrigin().getCdlocat();
        String destinationLocation = pickup.getDestination().getCdlocat();

        String naemployeeTo = "";

        try {
            db.setLocationInfo(pickup.getOrigin());
            pickupAddress = pickup.getOrigin().getAdstreet1() + " " + pickup.getOrigin().getAdcity() + ", " + pickup.getOrigin().getAdstate() + ", " + pickup.getOrigin().getAdzipcode();
        } catch (SQLException ex) {
            log.warn(null, ex);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        try {
            db.setLocationInfo(pickup.getDestination());
            deliverAddress = pickup.getDestination().getAdstreet1() + " " + pickup.getDestination().getAdcity() + ", " + pickup.getDestination().getAdstate() + ", " + pickup.getDestination().getAdzipcode();
        } catch (SQLException ex) {
            log.warn(null, ex);
        } catch (ClassNotFoundException e) {
            e.printStackTrace();
        }

        try {
            pickupEmployee = db.getEmployee(pickup.getNapickupby(), false);
            pickupEmployee.setEmployeeNameOrder(pickupEmployee.FIRST_MI_LAST_SUFFIX);
            this.napickupbyName = pickupEmployee.getEmployeeName().trim();
        } catch (SQLException sqle) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Pickup Employee for " + pickup.getNapickupby(), sqle);
            pickupEmployee = new Employee();
            this.napickupbyName = "N/A";
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }

        // Get the employee who signed the Release
        if (pickup.getNuxrrelsign() == null || pickup.getNuxrrelsign().trim().length() == 0) {
            remoteUser = this.pickupEmployee;
        } else {
            try {
                signingEmployee = db.getEmployeeWhoSigned(pickup.getNuxrrelsign(), false, userFallback);
                signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
            } catch (Exception e) {
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Pickup SigningEmployee");
            }
        }
        // Get the employee who picked up the items

        int emailReturnStatus = sendEmailReceipt(emailType);

        return emailReturnStatus;
    }

    /*
     * Delivery Specific function serves as the initial setup code for the sendEmailReceipt(int emailType)
     * which handles both Pickup and Delivery
     */
    public int sendDeliveryEmailReceipt(Transaction delivery) {
        if (emailType != DELIVERY) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Email Type was not set to DELIVERY!!! Not emailing Delivery receipt.");
            return 31;
        }
        this.delivery = delivery;

        try {
            db.setLocationInfo(delivery.getDestination());
            deliverAddress = delivery.getDestination().getAdstreet1() + " " + delivery.getDestination().getAdcity() + ", " + delivery.getDestination().getAdstate() + ", " + delivery.getDestination().getAdzipcode();
        } catch (SQLException ex) {
            log.warn(null, ex);
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
        // Get the employee who signed the Release

        // Get the employee who picked up the items
        if (delivery.getNadeliverby() == null || delivery.getNadeliverby().trim().length() == 0) {
            deliveryEmployee = new Employee();
            this.nadeliverbyName = "N/A";

            try {
                pickupEmployee = db.getEmployee(delivery.getNapickupby(), false);
                pickupEmployee.setEmployeeNameOrder(pickupEmployee.FIRST_MI_LAST_SUFFIX);
                this.napickupbyName = pickupEmployee.getEmployeeName().trim();
            } catch (SQLException sqle) {
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Delivery Employee for " + delivery.getNadeliverby(), sqle);
                pickupEmployee = new Employee();
                this.napickupbyName = "N/A";
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }
        } else {
            try {

                deliveryEmployee = db.getEmployee(delivery.getNadeliverby(), false);
                deliveryEmployee.setEmployeeNameOrder(deliveryEmployee.FIRST_MI_LAST_SUFFIX);
                this.nadeliverbyName = deliveryEmployee.getEmployeeName().trim();
            } catch (SQLException sqle) {
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Delivery Employee for " + delivery.getNadeliverby(), sqle);
                deliveryEmployee = new Employee();
                this.nadeliverbyName = "N/A";
            } catch (ClassNotFoundException e) {
                // TODO Auto-generated catch block
                e.printStackTrace();
            }

        }

        if (delivery.getNuxraccptsign() == null || delivery.getNuxraccptsign().trim().length() == 0) {
            if (this.deliveryEmployee != null && this.deliveryEmployee.getNaemail() != null && this.deliveryEmployee.getNaemail().trim().length() > 0) {
                remoteUser = this.deliveryEmployee;
            } else if (this.pickupEmployee != null && this.pickupEmployee.getNaemail() != null && this.pickupEmployee.getNaemail().trim().length() > 0) {
                remoteUser = this.pickupEmployee;
            }
        } else {
            try {
                signingEmployee = db.getEmployeeWhoSigned(delivery.getNuxraccptsign(), false, userFallback);
                signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
            } catch (Exception e) {
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Delivery SigningEmployee");
            }

        }

        int emailReturnStatus = sendEmailReceipt(emailType);

        return emailReturnStatus;
    }

    /*
     * Method that handles both Pickup and Delivery but called fron the sendEmailReceipt(Pickup) which only
     * handles the Pickup and sendEmailReceipt(Delivery) which handles only the Delivery.
     * 
     */
    private int sendEmailReceipt(int emailType) {
        String emailTypeString = "";

        int nuxrpdOrig = -1;
        switch (emailType) {
            case PICKUP:
                nuxrpdOrig = pickup.getNuxrpd();
                emailTypeString = "P";
                break;
            case DELIVERY:
                nuxrpdOrig = delivery.getNuxrpd();
                emailTypeString = "D";
                break;
        }

        final int nuxrpd = nuxrpdOrig;
        this.nuxrpd = nuxrpd;

        StringBuilder sbTestMsg = new StringBuilder();
        byte[] attachment = null;
        String msgBody = "";
        receiptFilename = nuxrpd + "_" + formatDate(dtreceipt, "yyMMddHHmmss") + emailTypeString;
        int returnStatus = 0;

        InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(in);
        } catch (IOException ex) {
            log.error(null, ex);
            returnStatus = 1;
        }

        String smtpServer = properties.getProperty("smtpServer");
        final String receiptURL = properties.getProperty("pickupReceiptURL");
        this.receiptURL = receiptURL;

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(props, null);

        this.naemailTo2 = properties.getProperty("pickupEmailTo2");
        naemailFrom = null;
        naemailFrom = properties.getProperty("pickupEmailFrom");
        naemailNameFrom = null;
        naemailNameFrom = properties.getProperty("pickupEmailNameFrom");
        reportRetryLimitString = properties.getProperty("report.gen.retry_limit");
        reportWaitIntervalString = properties.getProperty("report.gen.wait_interval");
        if (reportRetryLimitString == null || reportWaitIntervalString.isEmpty()) {
            reportRetryLimit = REPORTRETRYLIMITDEFAULT;
        } else {
            try {
                reportRetryLimit = Integer.parseInt(reportRetryLimitString);
            } catch (Exception e) {
                reportRetryLimit = REPORTRETRYLIMITDEFAULT;
                e.printStackTrace();
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.retry_limit was found with an invalid numeric value of (" + reportRetryLimitString + ") in config.properties file defaulting to " + reportRetryLimit + ".[{3}] at {4}", e);
            }
        }

        if (reportWaitIntervalString == null || reportWaitIntervalString.isEmpty()) {
            reportWaitInterval = REPORTWAITINTERVALDEFAULT;
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.wait_interval was not found in config.properties file defaulting to " + reportWaitInterval);
        } else {
            try {
                reportWaitInterval = Integer.parseInt(reportWaitIntervalString);
            } catch (Exception e) {
                e.printStackTrace();
                reportWaitInterval = REPORTWAITINTERVALDEFAULT;
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.wait_interval was found with an invalid numeric value in config.properties", e);
            }
        }

        try {
            naemailTo1 = properties.getProperty("pickupEmailTo1");
        } catch (NullPointerException e) {
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailTo1 NOT FOUND Pickup.processRequest ");
        } catch (Exception e) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
        }

        try {
            naemailNameTo1 = properties.getProperty("pickupEmailNameTo1");
        } catch (NullPointerException e) {
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailNameTo1 NOT FOUND Pickup.processRequest ");
        } catch (Exception e) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailNameTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
        }

        try {
            naemailTo2 = properties.getProperty("pickupEmailTo2");
        } catch (NullPointerException e) {
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailTo2 NOT FOUND Pickup.processRequest ");
        } catch (Exception e) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
        }

        try {
            naemailNameTo2 = properties.getProperty("pickupEmailNameTo2");
        } catch (NullPointerException e) {
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailNameTo2 NOT FOUND Pickup.processRequest ");
        } catch (Exception e) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER pickupEmailNameTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
        }

        try {
            testingModeProperty = properties.getProperty("testingMode").toUpperCase();
        } catch (NullPointerException e) {
            // Could not find the Testing Mode Property so assume that we are in testing mode, this will
            // at least alert someone if no one is getting receipts..
            testingModeProperty = "TRUE";
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ****PARAMETER testingMode was NOT FOUND  TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ");
        } catch (Exception e) {
            testingModeProperty = "TRUE";
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to find testingMode Property ({1}) TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ", e);
        }

        /*
         *  If either E-mail to field is filled, then the server is meant to e-mail that specific user
         * instead of the user that should be e-mailed. This would mean that the server is in testing mode.
         */

        if (testingMode) {
            sbTestMsg.append("<b>TESTINGMODE</b>: E-mail under normal circumstances would have been sent to:");
            if (signingEmployee != null && signingEmployee.getNaemail() != null) {
                sbTestMsg.append(signingEmployee.getNaemail());
            } else if (remoteUser != null && remoteUser.getNaemail() != null) {
                sbTestMsg.append(remoteUser.getNaemail());
                if (this.remoteVerByEmployee != null && remoteVerByEmployee.getNaemail() != null) {
                    sbTestMsg.append(", ");
                    sbTestMsg.append(remoteVerByEmployee.getNaemail());
                }
            }
            sbTestMsg.append("<br /><br />");
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ***Testing Mode add testing information:" + sbTestMsg);
        }

        EmailData emailData = null;

        switch (this.emailType) {
            case PICKUP:
                /*
                 * Pickup Receipt for Remote Pickup 
                 */

                if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {
                    if (this.remoteVerByEmployee == null || this.remoteVerByEmployee.getNaemail() == null || this.remoteVerByEmployee.getNaemail().trim().length() == 0) {
                        emailData = new EmailData(db, "RMTPCKPPICKUPRCPT");
                    } else {
                        emailData = new EmailData(db, "RMTPCKPPCKPVERRCPT");
                    }
                    try {
                        emailData.put("ShipType", pickup.getShipType());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        //log.info(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }

                    try {

                        emailData.put("ShipTypeDesc", pickup.getShipTypeDesc());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        //log.info(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }
                } /*
                 * Pickup Receipt for Remote Delivery 
                 */ else if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RDL")) {

                    /*
                     * BH 4/9/14. 
                     *     Discussed with SJG.. Suggestion was to send normal Pickup E-mail
                     *  even with a Remote Delivery. There will be a special instructions
                     *  in the Remote Delivery E-mail
                     */

                    emailData = new EmailData(db, "PICKUPRCPT");
                    /*    emailData = new EmailData(db, "RMTDLRYPICKUPRCPT");
                     try {
                     emailData.put("ShipType", pickup.getShipType());
                     } catch (InvalidParameterException ex) {
                     log.error(null, ex);
                     } catch (ParameterNotUsedException ex) {
                     //log.info(null, ex);
                     } catch (BlankMessageException ex) {
                     log.error(null, ex);
                     }

                     try {
                     emailData.put("ShipTypeDesc", pickup.getShipTypeDesc());
                     } catch (InvalidParameterException ex) {
                     log.error(null, ex);
                     } catch (ParameterNotUsedException ex) {
                     //log.error(null, ex);
                     } catch (BlankMessageException ex) {
                     log.error(null, ex);
                     }*/
                } /*
                 * Pickup Receipt for normal Pickup/Delivery (Neither are remote) 
                 */ else {
                    emailData = new EmailData(db, "PICKUPRCPT");
                }

                try {
                    if (testingMode) {
                        emailData.setPreMessage(sbTestMsg.toString());
                    }
                    if (signingEmployee != null && signingEmployee.getEmployeeName() != null && signingEmployee.getEmployeeName().trim().length() > 0) {
                        emailData.put("Employee", signingEmployee.getEmployeeName());
                    } else if (this.remoteVerByEmployee != null && remoteVerByEmployee.getNaemail() != null) {
                        emailData.put("Employee", remoteVerByEmployee.getEmployeeName());
                    } else if (remoteUser != null && remoteUser.getEmployeeName() != null && remoteUser.getEmployeeName().trim().length() > 0) {
                        emailData.put("Employee", remoteUser.getEmployeeName());
                    } else {
                        //log.warn("***WARNING: Both signing employee and remote user employee names are blank. {Employee} cannot be set.");
                        /*
                         *  Dear {Employee} should be either the Signing Employee Name or the Remote User
                         * (ie: the user who logged into the tablet to send the paperwork for the remote office)
                         *  If neither are filled, then we will just default to a Generic Name
                         */
                        emailData.put("Employee", "Senate Employee");
                    }

                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("FromAddress", pickupAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("FromLocat", pickup.getOrigin().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("PickupDate", formatDate(dtreceipt, "dd-MMM-yy"));
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("RefDoc", receiptFilename);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("ToAddress", deliverAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("ToLocat", pickup.getDestination().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("UserID", pickup.getNapickupby());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("UserName", napickupbyName);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    if (serverInfo != null) {
                        emailData.put("ServerInfo", serverInfo);
                    } else {
                        emailData.put("ServerInfo", "");
                    }
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                break;
            case DELIVERY:
                /*
                 * Delivery Receipt for Remote Delivery
                 */

                if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RDL")) {
                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** SEND  remoteDeliveryNoSigDelivered:" + remoteDeliveryNoSigDelivered);
                    if (remoteDeliveryNoSigDelivered) {
                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery No Signature E-mail");
                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery No Signature E-mail");
                        emailData = new EmailData(db, "RMTDLRYDLRYNSRCPT");
                    } else {
                        if (this.remoteVerByEmployee == null || this.remoteVerByEmployee.getNaemail() == null || this.remoteVerByEmployee.getNaemail().trim().length() == 0) {
                            String add = "";
                            if (this.remoteVerByEmployee == null) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Standard E-mail Remote Ver Emp Info: NULL");
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Standard E-mail Remote Ver Emp Info: NULL");
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Standard E-mail Remote Ver Emp Info: xref:" + this.remoteVerByEmployee.getEmployeeXref() + ", e-mail:" + this.remoteVerByEmployee.getNaemail() + " Name:" + this.remoteVerByEmployee.getEmployeeName());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Standard E-mail Remote Ver Emp Info: xref:" + this.remoteVerByEmployee.getEmployeeXref() + ", e-mail:" + this.remoteVerByEmployee.getNaemail() + " Name:" + this.remoteVerByEmployee.getEmployeeName());
                            }
                            emailData = new EmailData(db, "RMTDLRYDELIVERYRCPT");
                        } else {
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Verified E-mail");
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE SEND Remote Delivery Verified E-mail");
                            emailData = new EmailData(db, "RMTDLRYDLRYVERRCPT");
                        }
                    }
                    try {
                        emailData.put("ShipType", delivery.getShipType());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        //log.info(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }

                    try {
                        emailData.put("ShipTypeDesc", delivery.getShipTypeDesc());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        //log.info(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }

                    try {
                        if (initialPickup != null && initialPickup.getPickupDate() != null) {
                            emailData.put("PickupDate", formatDate(initialPickup.getPickupDate(), "dd-MMM-yy"));
                        } else {
                            emailData.put("PickupDate", formatDate(dtreceipt, "dd-MMM-yy"));
                        }
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        //log.info(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }

                    if (initialPickup != null && initialPickup.getOrigin() != null) {
                        try {
                            if (initialPickup.getOrigin().getCdlocat() != null) {
                                emailData.put("FromLocat", initialPickup.getOrigin().getCdlocat());
                            }
                        } catch (InvalidParameterException ex) {
                            log.error(null, ex);
                        } catch (ParameterNotUsedException ex) {
                            //log.info(null, ex);
                        } catch (BlankMessageException ex) {
                            log.error(null, ex);
                        }

                        try {
                            if (initialPickup.getOrigin().getFullAddress() != null) {
                                emailData.put("FromAddress", initialPickup.getOrigin().getFullAddress());
                            }
                        } catch (InvalidParameterException ex) {
                            log.error(null, ex);
                        } catch (ParameterNotUsedException ex) {
                            //log.info(null, ex);
                        } catch (BlankMessageException ex) {
                            log.error(null, ex);
                        }
                    }
                } /*
                 * Delivery Receipt for Remote Pickup
                 */ else if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {

                    /*
                     * BH 4/9/14. 
                     *     Discussed with SJG.. Suggestion was to send normal Delivery E-mail
                     *  even with a Remote Pickup. There will be a special instructions
                     *  in the Remote Pickup E-mail
                     */

                    emailData = new EmailData(db, "DELIVERYRCPT");

                } /*
                 * Delivery Receipt for normal Pickup/Delivery (Neither are remote) 
                 */ else {
                    emailData = new EmailData(db, "DELIVERYRCPT");
                }

                try {
                    if (testingMode) {
                        emailData.setPreMessage(sbTestMsg.toString());
                    }
                    emailData.put("DeliveryDate", formatDate(dtreceipt, "dd-MMM-yy"));
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    if (signingEmployee != null && signingEmployee.getEmployeeName() != null) {
                        emailData.put("Employee", signingEmployee.getEmployeeName());
                    } else if (this.remoteVerByEmployee != null && remoteVerByEmployee.getNaemail() != null) {
                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** Remote Verification Employee Info:" + remoteVerByEmployee.getEmployeeName() + " E-mail Addr:" + remoteVerByEmployee.getNaemail());
                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** Remote Verification Employee Info:" + remoteVerByEmployee.getEmployeeName() + " E-mail Addr:" + remoteVerByEmployee.getNaemail());
                        emailData.put("Employee", remoteVerByEmployee.getEmployeeName());
                    } else if (remoteUser != null && remoteUser.getEmployeeName() != null) {
                        emailData.put("Employee", remoteUser.getEmployeeName());
                    } else {
                        /*
                         *  Dear {Employee} should be either the Signing Employee Name or the Remote User
                         * (ie: the user who logged into the tablet to send the paperwork for the remote office)
                         *  If neither are filled, then we will just default to a Generic Name
                         */
                        emailData.put("Employee", "Senate Employee");
                    }
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                } catch (NullPointerException ex) {
                    log.info(null, ex);
                }

                try {
                    emailData.put("RefDoc", receiptFilename);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("ToAddress", deliverAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("ToLocat", delivery.getDestination().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("UserID", delivery.getNadeliverby());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("UserName", nadeliverbyName);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    if (serverInfo != null) {
                        emailData.put("ServerInfo", serverInfo);
                    } else {
                        emailData.put("ServerInfo", "");
                    }

                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.info(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                break;
        }
        String error = null;

        try {
            attachment = bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam, receiptPath + receiptFilename); // +"&destype=CACHE&desformat=PDF

            // Attachment needs to be checked to ensure that there were no issues with the Reports Server
            // and the PDF was generated properly. Otherwise the PDF sent is garbage.  We need to e-mail
            // STSBAC and possibly others that an issue occured and/or try to generate the PDF again

            //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
        } catch (MalformedURLException ex) {
            log.error(null, ex);
            if (returnStatus == 0) {
                returnStatus = 2;
            }
            error = invUtil.stackTraceAsMsg(ex);
            emailError(emailType, "<html><body>Email Error URL was MALFORMED: <b>" + receiptURL + nuxrpd + transTypeParam + "</b><br/><br/></body></html>");

            return returnStatus;
        } catch (IOException ex) {
            log.error(null, ex);
        } catch (ReportNotGeneratedException ex) {
            if (returnStatus == 0) {
                returnStatus = 2;
            }
            log.error("There was an issue with Oracle Reports Server. Please contact STS/BAC.", ex);
            error = invUtil.stackTraceAsMsg(ex);

            emailError(emailType);
            return returnStatus;
        }

        if (attachment == null) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "****ATTACHMENT was null Pickup.processRequest ");
            if (returnStatus == 0) {
                returnStatus = 4;
            }
            error = "<br/> Error: Null Attachment";
            emailError(emailType);
            return returnStatus;

        } else if (attachment.length == 0) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
            if (returnStatus == 0) {
                returnStatus = 5;
            }
            error = "<br/> Error: Attachment with a length of 0";

            emailError(emailType);
            return returnStatus;
        }

        MimeMultipart mimeMultipart = new MimeMultipart();
        attachmentPart = getOracleReportResponse(receiptURL, nuxrpd);

        try {
            attachmentPart.setFileName(receiptFilename + ".pdf");
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

            msgBody = emailData.getFormattedMessage();

            MimeMessage msg = new MimeMessage(session);
            try {
                msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
            } catch (UnsupportedEncodingException | MessagingException e) {
                e.printStackTrace();
            }

            int recipientCount = 0;
            recipientCount = addDistributionRecipients(msg);
            recipientCount = recipientCount + addEmailSupervisors(msg);
            if (this.emailType == PICKUP) {
                if (!testingMode) {
                    if (pickupEmployee != null && pickupEmployee.getEmployeeName() != null) {
                        try {
                            if (emailValidator.validate(pickupEmployee.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Employee:"+pickupEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Employee:"+pickupEmployee.getNaemail());
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Employee Invalid E-mail Address:"+pickupEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Employee Invalid E-mail Address:"+pickupEmployee.getNaemail());
                                addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                            }
                            if (this.remoteVerByEmployee != null && remoteVerByEmployee.getNaemail() != null) {
                                try {
                                    if (emailValidator.validate(remoteVerByEmployee.getNaemail())) {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                        msg.addRecipient(Message.RecipientType.TO,
                                                new InternetAddress(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                        recipientCount++;
                                    } else {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                        addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                                    }
                                } catch (UnsupportedEncodingException | MessagingException e2) {
                                    addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), e2.getStackTrace(), e2.getMessage());
                                }
                            }
                        } catch (UnsupportedEncodingException | MessagingException e) {
                            addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                        }
                    } else if (remoteUser != null && remoteUser.getEmployeeName() != null) {
                        try {
                            if (emailValidator.validate(remoteUser.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote User:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote User:"+remoteUser.getNaemail());
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(remoteUser.getNaemail(), remoteUser.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote User Invalid E-mail Address:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote User Invalid E-mail Address:"+remoteUser.getNaemail());                                
                                addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), null, "Invalid E-mail Address");
                            }
                            if (this.remoteVerByEmployee != null && remoteVerByEmployee.getEmployeeName() != null) {
                                try {
                                    if (emailValidator.validate(remoteVerByEmployee.getNaemail())) {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Pickup Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                        msg.addRecipient(Message.RecipientType.TO,
                                                new InternetAddress(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                        recipientCount++;
                                    } else {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (PICKUP) addrecipient Pickup Pickup Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                        addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                                    }
                                } catch (UnsupportedEncodingException | MessagingException e2) {
                                    addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), e2.getStackTrace(), e2.getMessage());
                                }
                            }
                        } catch (UnsupportedEncodingException | MessagingException e) {
                            addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), e.getStackTrace(), e.getMessage());
                        }
                    } else if (pickupEmployee == null) {
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Pickup Employee was null so can''t add Pickup Employee as recipient.");
                    } else if (pickupEmployee.getNaemail() == null) {
                        addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Pickup Employee " + pickupEmployee.getEmployeeName() + " E-mail Field was null so can''t add Pickup Employee as recipient.");
                    }
                }
            } else if (this.emailType == DELIVERY) {
                if (!testingMode) {
                    if (deliveryEmployee != null && deliveryEmployee.getEmployeeName() != null && deliveryEmployee.getEmployeeName().trim().length() > 0) {
                        try {
                            if (emailValidator.validate(deliveryEmployee.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Employee:"+deliveryEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Employee:"+deliveryEmployee.getNaemail());
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Employee Invalid E-mail Address:"+deliveryEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Employee Invalid E-mail Address:"+deliveryEmployee.getNaemail());
                                addProblemEmailAddr(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                            }
                        } catch (UnsupportedEncodingException | MessagingException e) {
                            addProblemEmailAddr(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                        }
                        if (this.remoteVerByEmployee != null && remoteVerByEmployee.getEmployeeName() != null) {
                            try {
                                if (emailValidator.validate(remoteVerByEmployee.getNaemail())) {
                                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote Ver Employee(1):"+remoteVerByEmployee.getNaemail());
                                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Pickup Delivery Remote Ver Employee(1):"+remoteVerByEmployee.getNaemail());
                                    msg.addRecipient(Message.RecipientType.TO,
                                            new InternetAddress(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                    recipientCount++;
                                } else {
                                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote Ver Employee(1) Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote Ver Employee(1) Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                    addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                                }
                            } catch (UnsupportedEncodingException | MessagingException e2) {
                                addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), e2.getStackTrace(), e2.getMessage());
                            }
                        }
                    } else if (remoteUser != null && remoteUser.getEmployeeName() != null) {
                        try {
                            if (emailValidator.validate(remoteUser.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote User:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote User:"+remoteUser.getNaemail());
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(remoteUser.getNaemail(), remoteUser.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote User Invalid E-mail Address:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote User Invalid E-mail Address:"+remoteUser.getNaemail());
                                addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), null, "Invalid E-mail Address");
                            }
                        } catch (UnsupportedEncodingException | MessagingException e) {
                            addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), e.getStackTrace(), e.getMessage());
                        }
                        if (this.remoteVerByEmployee != null && remoteVerByEmployee.getEmployeeName() != null) {
                            try {
                                if (emailValidator.validate(remoteVerByEmployee.getNaemail())) {
                                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Ver Employee:"+remoteVerByEmployee.getNaemail());
                                    msg.addRecipient(Message.RecipientType.TO,
                                            new InternetAddress(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                    recipientCount++;
                                } else {
                                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** (DELIVERY) addrecipient Delivery Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());                                    
                                    addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                                }
                            } catch (UnsupportedEncodingException | MessagingException e2) {
                                addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), e2.getStackTrace(), e2.getMessage());
                            }
                        }
                    } else if (deliveryEmployee == null) {
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Delivery Employee was null so can''t add Delivery Employee as recipient.");
                    } else if (deliveryEmployee.getNaemail() == null) {
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Delivery Employee " + deliveryEmployee.getEmployeeName() + " E-mail Field was null so can''t add Delivery Employee as recipient.");
                    }
                }
            }
            if (testingMode) {
                if (naemailNameTo1 != null && naemailNameTo1.trim().length() > 0) {
                    try {
//                        System.out.println("(" + this.dbaUrl + ") TESTINGMODE EMAILING TO:" + naemailTo1 + ":" + naemailNameTo1);
                        if (emailValidator.validate(naemailTo1)) {
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL1:"+naemailTo1);
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL1:"+naemailTo1);
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL1 Invalid E-mail Address:"+naemailTo1);
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL1 Invalid E-mail Address:"+naemailTo1);
                            addProblemEmailAddr(naemailTo1, naemailNameTo1, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
                    }
                }
                if (naemailNameTo2 != null && naemailNameTo2.trim().length() > 0) {
                    try {
                        if (emailValidator.validate(naemailTo2)) {
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL2:"+naemailTo2);
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL2:"+naemailTo2);                            
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL2 Invalid E-mail Address:"+naemailTo2);
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient TESTING EMAIL2 Invalid E-mail Address:"+naemailTo2);    
                            addProblemEmailAddr(naemailTo2, naemailNameTo2, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
                    }

                }
            } else {

                try {
                    if ((this.paperworkType == null)
                            || (this.paperworkType.trim().length() == 0)
                            || (this.emailType == this.PICKUP && (!this.paperworkType.equalsIgnoreCase("RPK")))
                            || (this.emailType == this.DELIVERY && (!this.paperworkType.equalsIgnoreCase("RDL")))) {
                        if (signingEmployee != null && signingEmployee.getEmployeeName() != null) {
                            if (emailValidator.validate(signingEmployee.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Signing Employee:"+signingEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Signing Employee:"+signingEmployee.getNaemail());                            
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(signingEmployee.getNaemail(), signingEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Signing Employee Invalid E-mail Address:"+signingEmployee.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Signing Employee Invalid E-mail Address:"+signingEmployee.getNaemail());                            
                                addProblemEmailAddr(signingEmployee.getNaemail(), signingEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                            }

                        } else if (remoteUser != null && remoteUser.getEmployeeName() != null) {
                            if (emailValidator.validate(remoteUser.getNaemail())) {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote User:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote User:"+remoteUser.getNaemail());                            
                                msg.addRecipient(Message.RecipientType.TO,
                                        new InternetAddress(remoteUser.getNaemail(), remoteUser.getEmployeeName()));  //naemailTo, naemployeeTo
                                recipientCount++;
                            } else {
                                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote User Invalid E-mail Address:"+remoteUser.getNaemail());
                                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote User Invalid E-mail Address:"+remoteUser.getNaemail());                            
                                addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), null, "Invalid E-mail Address");
                            }
                            if (this.remoteVerByEmployee != null && remoteVerByEmployee.getEmployeeName() != null) {
                                try {
                                    if (emailValidator.validate(remoteVerByEmployee.getNaemail())) {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote Ver Employee:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote Ver Employee:"+remoteVerByEmployee.getNaemail());                            
                                        msg.addRecipient(Message.RecipientType.TO,
                                                new InternetAddress(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                                        recipientCount++;
                                    } else {
                                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());
                                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** addrecipient REAL Remote Ver Employee Invalid E-mail Address:"+remoteVerByEmployee.getNaemail());                            
                                        addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                                    }
                                } catch (UnsupportedEncodingException | MessagingException e2) {
                                    addProblemEmailAddr(remoteVerByEmployee.getNaemail(), remoteVerByEmployee.getEmployeeName(), e2.getStackTrace(), e2.getMessage());
                                }
                            }
                        }
                    }
                } catch (UnsupportedEncodingException | MessagingException e) {
                    if (signingEmployee != null && signingEmployee.getNaemail() != null) {
                        System.out.println("(" + this.dbaUrl + ") EXCEPTION REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                        log.warn("(" + this.dbaUrl + ") EXCEPTION REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                        addProblemEmailAddr(signingEmployee.getNaemail(), signingEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                    } else if (remoteUser != null && remoteUser.getNaemail() != null) {
                        System.out.println("(" + this.dbaUrl + ") EXCEPTION Remote User  addRecipient: email:" + remoteUser.getNaemail() + ", Email Name:" + remoteUser.getEmployeeName());
                        log.warn("(" + this.dbaUrl + ") EXCEPTION Remote User  addRecipient: email:" + remoteUser.getNaemail() + ", Email Name:" + remoteUser.getEmployeeName());
                        addProblemEmailAddr(remoteUser.getNaemail(), remoteUser.getEmployeeName(), e.getStackTrace(), e.getMessage());
                    }
                }
            }

            if (emailType == DELIVERY) {
                msg.setSubject("Equipment Delivery Receipt" + subjectAddText);
            } else {
                msg.setSubject("Equipment Pickup Receipt" + subjectAddText);
            }

            //msg.setText(msgBody, "utf-8", "html");
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(msgBody);
            mbp1.setContent(msgBody, "text/html");
            mimeMultipart.addBodyPart(mbp1);
            mimeMultipart.addBodyPart(attachmentPart);
            msg.setContent(mimeMultipart);

            if (attachmentPart == null || attachmentPart.getSize() == 0) {
                System.out.println("(" + this.dbaUrl + ") ***E-mail NOT sent because attachment was malformed.");
                if (returnStatus == 0) {
                    returnStatus = 8;
                }
            } else {
                if (attachmentPart.getContent() == null) {
                    if (returnStatus == 0) {
                        returnStatus = 9;
                    }
                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** NO EMAIL-ATTACHMENT MALFORMED");
                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** NO EMAIL-ATTACHMENT MALFORMED");                            

                    System.out.println("(" + this.dbaUrl + ") ***E-mail NOT sent because attachment was malformed(2).");
                } else {

                    if (recipientCount == 0) {
                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** NO EMAIL-NO EMAIL RECIPIENTS");
                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** NO EMAIL-NO EMAIL RECIPIENTS");                            
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: There were no e-mail recipients for a Report. No e-mail will be sent!!!");
                    } else {
                        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** EMAILING....");
                        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** EMAILING....");    
                        try {
                            //System.out.println("BEFORE EMAIL ATTACHMENT java.net.preferIPv4Stack:"+System.getProperty("java.net.preferIPv4Stack"));
                            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE **** SEND EMAIL:"+msgBody);
                            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE **** SEND EMAIL:"+msgBody);                            

                            Transport.send(msg);
                        } catch (javax.mail.SendFailedException e) {
                            this.sendToValidAddresses(e, msg);
                        }
                    }
                    //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** CALL EMAIL WARNING....");
                    //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** CALL EMAIL WARNING....");                            
                    emailWarning(emailType);
                }
            }
            System.out.println("(" + this.dbaUrl + ") E-mail sent with no errors.");

        } catch (AddressException e) {
            if (returnStatus == 0) {
                returnStatus = 10;
                try {
                    error = invUtil.stackTraceAsMsg(e);
                    emailError(emailType, "(" + this.dbaUrl + ") ADDRESS EXCEPTION:+" + e.getMessage() + "<br />" + error);
                    emailWarning(emailType);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return returnStatus;
            }

            e.printStackTrace();
        } catch (MessagingException e) {
            if (returnStatus == 0) {
                returnStatus = 11;
                try {
                    error = invUtil.stackTraceAsMsg(e);
                    emailError(emailType, "(" + this.dbaUrl + ") MESSAGING EXCEPTION:+" + e.getMessage() + "<br />" + error);
                    emailWarning(emailType);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return returnStatus;
            }
            e.printStackTrace();
        } catch (Exception e) {
            if (returnStatus == 0) {
                returnStatus = 20;
                try {
                    error = invUtil.stackTraceAsMsg(e);
                    emailError(emailType, "(" + this.dbaUrl + ") GENERAL EXCEPTION:+" + e.getMessage() + "<br />" + error);
                    emailWarning(emailType);
                } catch (Exception e2) {
                    e2.printStackTrace();
                }
                return returnStatus;
            }
            e.printStackTrace();
        }
        return returnStatus;
    }

    public void sendToValidAddresses(javax.mail.SendFailedException e, MimeMessage message) throws MessagingException {
        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses start");
        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses start");                            
        try {
            Address[] validAddresses = e.getValidUnsentAddresses();
            Address[] invalidAddresses = e.getInvalidAddresses();
            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses invalidAddress count:"+invalidAddresses.length);
            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses invalidAddress count:"+invalidAddresses.length);                            
            for (int x = 0; x < invalidAddresses.length; x++) {
                addProblemEmailAddr(((InternetAddress) invalidAddresses[x]).getAddress(), invalidAddresses[x].toString(), e.getStackTrace(), "Invalid E-mail Address");
            }
            if (validAddresses != null && validAddresses.length > 0) {
                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses validAddress count:"+validAddresses.length);
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses validAddress count:"+validAddresses.length);                            
                //System.out.println("BEFORE sendToValidAddresses java.net.preferIPv4Stack:"+System.getProperty("java.net.preferIPv4Stack"));
                Transport.send(message, validAddresses);
            } else {
                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses-NO VALID EMAIL RECIPIENTS");
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE *** sendToValidAddresses-NO VALID EMAIL RECIPIENTS");                            
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: (sendToValidAddresses)There were no valid e-mail recipients for a Report. No e-mail will be sent!!!");
            }
        } catch (javax.mail.SendFailedException sfe) {
            sendToValidAddresses(sfe, message);
        }
    }

    public String formatDate(Date d, String format) {
        if (d == null) {
            return "";
        }
        SimpleDateFormat sdf = new SimpleDateFormat(format);
        return sdf.format(d);
    }

    private MimeBodyPart getOracleReportResponse(final String receiptURL, final int nuxrpd) {
        MimeBodyPart attachmentPart = new MimeBodyPart();
        try {

            attachmentPart.setDataHandler(
                    new DataHandler(
                    new DataSource() {
                @Override
                public String getContentType() {
                    return "application/pdf";
                }

                @Override
                public InputStream getInputStream() throws IOException {
                    try {

                        return new ByteArrayInputStream(bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam));
                    } catch (ReportNotGeneratedException e) {
                        log.warn("Oracle Reports Server failed to generate a PDF Report for the Pickup Receipt. Please contact STS/BAC.", e);
                        return new ByteArrayInputStream(new byte[0]);
                    }
                }

                @Override
                public String getName() {
                    log.info("{0}" + "|" + "DataSource.getName() called. Returning:" + receiptFilename);
                    return receiptFilename + ".pdf";
                }

                @Override
                public OutputStream getOutputStream() throws IOException {
                    return null;
                }
            }));

        } catch (MessagingException ex) {
            log.error(null, ex);
        }
        return attachmentPart;
    }

    /*
     * New EmailError
     */
    public void emailError(int emailType) {
        emailError(emailType, null);
    }

    public void emailError(int emailType, String msgOverride) {
        Properties props = new Properties();
        String smtpServer = properties.getProperty("smtpServer");
        props.setProperty("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(props, null);
        int recipientCount = 0;
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));

            // Set To: header field of the header.
            if (testingMode) {
                recipientCount = addErrorRecipients(message);
                if (naemailTo1 != null && naemailTo1.trim().length() > 0) {
                    try {
                        if (emailValidator.validate(naemailTo1)) {
                            message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(naemailTo1, naemailNameTo1, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
                    }
                }
                if (naemailTo2 != null && naemailTo2.trim().length() > 0) {
                    try {
                        if (emailValidator.validate(naemailTo2)) {
                            message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(naemailTo2, naemailNameTo2, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
                    }
                }
            } else {
                recipientCount = addErrorRecipients(message);
            }

            if (recipientCount == 0) {
                log.warn("{0}" + "|" + "**WARNING: There were no e-mail recipients for a Report Genration error. No error e-mail will be sent!!!");
                return;
            }
            log.warn("{0}" + "|" + "!!!!EMAILERROR BEFORE SUBJECT");
            // Set Subject: header field

            if (emailType == PICKUP) {
                message.setSubject("!!ERROR: Oracle Report Server Unable to Generate Pickup Receipt. Contact STS/BAC." + subjectAddText);
            } else if (emailType == DELIVERY) {
                message.setSubject("!!ERROR: Oracle Report Server Unable to Generate Delivery Receipt. Contact STS/BAC." + subjectAddText);
            }

            log.warn("{0}" + "|" + "!!!!EMAILERROR BEFORE MESSAGE HEADER");
            String sEmailType = "";
            if (emailType == PICKUP) {
                sEmailType = "PICKUP";
            } else if (emailType == DELIVERY) {
                sEmailType = "DELIVERY";
            } else {
                sEmailType = "UNKNOWN EMAIL TYPE:" + emailType;
            }

            String msgHeader = null; //"<html><body><b>URL:<a href='" + receiptURL + nuxrpd + "'>" + receiptURL + nuxrpd + "</a> (" + sEmailType + ") Try#:" + retryCounter + " failed to generated and came back with the following response...<br /><br /> </body></html>";

            /*
             * 
             * "<html><body><b>URL:<a href='" + receiptURL + nuxrpd + "'>" + receiptURL + nuxrpd + "</a> (" + sEmailType + ") Try#:" + retryCounter + " failed to generated and came back with the following response...<br /><br /> </body></html>";

             * 
             */

            EmailData errorEmailData = new EmailData(db, "EMAILERROR");
            try {
                /*if (testingMode) {
                 emailData.setPreMessage(sbTestMsg.toString());
                 }*/
                //log.warn("(AA)!!!!EMAILERROR BEFORE SET MESSAGE ERROR:" + error);
                //System.out.println("(AB)!!!!!!!!!!!!!EMAILERROR BEFORE SET MESSAGE ERROR:" + error);
                errorEmailData.put("EmailType", sEmailType);
                errorEmailData.put("ReceiptURL", receiptURL + nuxrpd);
                errorEmailData.put("ReceiptURL", receiptURL + nuxrpd);
                errorEmailData.put("RetryNumber", new Integer(retryCounter).toString());
                if (error == null) {
                    error = "<Error not Specified>";
                }
                errorEmailData.put("ErrorMessage", error);
                if (serverInfo != null) {
                    errorEmailData.put("ServerInfo", serverInfo);
                } else {
                    errorEmailData.put("ServerInfo", "");
                }
            } catch (InvalidParameterException ex) {
                log.error(null, ex);
            } catch (ParameterNotUsedException ex) {
                //log.error(null, ex);
            } catch (BlankMessageException ex) {
                log.error(null, ex);
            }

            msgHeader = errorEmailData.getFormattedMessage();

            //log.warn("(B)!!!!EMAILERROR BEFORE SET MESSAGE:" + msgHeader);
            //System.out.println("(A)!!!!!!!!!!!!!EMAILERROR BEFORE SET MESSAGE:" + msgHeader);
            // Now set the actual message
            if (msgOverride == null) {
                message.setText(msgHeader, "utf-8", "html");
            } else {
                if (error == null) {
                    message.setText(msgOverride, "utf-8", "html");
                } else {
                    message.setText(msgOverride + error, "utf-8", "html");
                }
            }
            //log.warn("{0}" + "|" + "!!!!EMAILERROR AFTER SET MESSAGE");
            //log.info("{0}| EMAIL ERRORR MSG: " + message);
            // Send message
            System.out.println("BEFORE EMAIL ERROR java.net.preferIPv4Stack:" + System.getProperty("java.net.preferIPv4Stack"));

            Transport.send(message);
            System.out.println("Sent error message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
            log.warn(null, mex);
        } catch (UnsupportedEncodingException ex1) {
            log.warn(null, ex1);
        }
    }

    /*
     * New emailWarning
     */
    public void emailWarning(int emailType) {
        emailWarning(emailType, null);
    }

    public void emailWarning(int emailType, String msgOverride) {
        if (problemEmailAddrs == null || problemEmailAddrs.size() == 0) {
            //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** EMAILWARNING:  No problematic emails found, so no warning needed.");
            //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** EMAILWARNING:  No problematic emails found, so no warning needed.");
            return;
        }
        //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** EMAILWARNING:  problematic emails found:" + problemEmailAddrs.size() + ": FIRST PROBLEM:" + problemEmailAddrs.get(0).getNaemailName());
        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ** EMAILWARNING:  problematic emails found:" + problemEmailAddrs.size() + ": FIRST PROBLEM:" + problemEmailAddrs.get(0).getNaemailName());

        Properties props = new Properties();
        String smtpServer = properties.getProperty("smtpServer");
        props.setProperty("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(props, null);
        int recipientCount = 0;
        try {
            // Create a default MimeMessage object.
            MimeMessage message = new MimeMessage(session);

            // Set From: header field of the header.
            message.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));

            // Set To: header field of the header.
            if (testingMode) {
                recipientCount = addErrorRecipients(message);
                if (naemailTo1 != null && naemailTo1.trim().length() > 0) {
                    try {
                        if (emailValidator.validate(naemailTo1)) {
                            message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(naemailTo1, naemailNameTo1, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
                    }
                }
                if (naemailTo2 != null && naemailTo2.trim().length() > 0) {
                    try {
                        if (emailValidator.validate(naemailTo2)) {
                            message.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(naemailTo2, naemailNameTo2, null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
                    }
                }
            } else {
                recipientCount = addErrorRecipients(message);
            }

            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") !!!!EMAILWARNING BEFORE SUBJECT");
            // Set Subject: header field

            String sEmailType = "";
            if (emailType == PICKUP) {
                message.setSubject("***WARNING: Pickup Receipt Recipient(s) E-mail Address Problems. Contact STS/BAC." + subjectAddText);
                sEmailType = "PICKUP";
            } else if (emailType == DELIVERY) {
                message.setSubject("***WARNING: Delivery Receipt Recipient(s) E-mail Address Problems. Contact STS/BAC." + subjectAddText);
                sEmailType = "DELIVERY";
            }

            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") !!!!EMAILWARNING BEFORE MESSAGE HEADER");

            EmailData warningEmailData = new EmailData(db, "EMAILWARNING");
            try {
                warningEmailData.put("EmailType", sEmailType);
                warningEmailData.put("ReceiptURL", receiptURL + nuxrpd);
                //warningEmailData.put("ReceiptURL", receiptURL + nuxrpd);
                warningEmailData.put("ProblemRecipients", this.getProblemEmailString());
                if (serverInfo != null) {
                    System.out.println("NOT NULL serverInfo:" + serverInfo);
                    //Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.INFO, "", null);
                    warningEmailData.put("ServerInfo", serverInfo);
                } else {
                    warningEmailData.put("ServerInfo", "");
                    System.out.println("!!!!!NULL serverInfo:" + serverInfo);
                }
            } catch (InvalidParameterException ex) {
                log.warn(null, ex);
            } catch (ParameterNotUsedException ex) {
                //log.warn(null, ex);
            } catch (BlankMessageException ex) {
                log.warn(null, ex);
            }
            String msgHeader = warningEmailData.getFormattedMessage();

            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") !!!!EMAILWARNING BEFORE SET MESSAGE: " + msgHeader);
            System.out.println("!!!!!!!!!!!!!EMAILWARNING BEFORE SET MESSAGE:" + msgHeader);
            // Now set the actual message
            if (msgOverride == null) {
                message.setText(msgHeader, "utf-8", "html");
            } else {
                message.setText(msgHeader + msgOverride, "utf-8", "html");
            }
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") !!!!EMAILWARNING AFTER SET MESSAGE");
            log.info("{0}|(" + this.dbaUrl + ")  EMAIL WARNING MSG ");
            System.out.println("BEFORE EMAIL WARNING java.net.preferIPv4Stack:" + System.getProperty("java.net.preferIPv4Stack"));
            // Send message
            Transport.send(message);
            System.out.println("(" + this.dbaUrl + ") Sent warning message successfully....");
        } catch (MessagingException mex) {
            mex.printStackTrace();
            log.warn(null, mex);
        } catch (UnsupportedEncodingException ex1) {
            log.warn(null, ex1);
        }
    }

    @Override
    public void run() {
        int returnStatus = -1;
        retryCounter = 0;
        switch (emailType) {
            case PICKUP:
                if (pickup == null) {
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: E-mail Receipt type was set to PICKUP but pickup object was NULL. No e-mail will be generated!!!");
                } else {
                    log.info("{0}" + "|" + "(" + this.dbaUrl + ")Asynchronouly generating a Pickup E-mail Receipt ");
                }
                do {
                    retryCounter++;
                    returnStatus = sendPickupEmailReceipt(pickup);
                    if (returnStatus != 0) {
                        log.info("{0}" + "|" + "(" + this.dbaUrl + ")Pickup receipt generated a returnStatus=" + returnStatus + ". Will retry to generate the Delivery receipt after " + this.reportWaitInterval + " seconds.");
                        try {
                            Thread.sleep(this.reportWaitInterval * 1000);
                        } catch (InterruptedException ex) {
                            log.warn(null, ex);
                        }
                    }
                } while (returnStatus != 0 && retryCounter <= reportRetryLimit);
                break;
            case DELIVERY:
                if (delivery == null) {
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: E-mail Receipt type was set to DELIVERY but delivery object was NULL. No e-mail will be generated!!!");
                } else {
                    log.info("{0}" + "|" + "(" + this.dbaUrl + ") Asynchronouly generating a Delivery E-mail Receipt ");
                }

                do {
                    retryCounter++;
                    returnStatus = sendDeliveryEmailReceipt(delivery);
                    if (returnStatus != 0) {
                        log.info("{0}" + "|" + "(" + this.dbaUrl + ") Delivery receipt generated a returnStatus=" + returnStatus + ". Will retry to generate the Delivery receipt after " + this.reportWaitInterval + " seconds.");
                        try {
                            Thread.sleep(this.reportWaitInterval * 1000);
                        } catch (InterruptedException ex) {
                            log.warn(null, ex);
                        }
                    }
                } while (returnStatus != 0 && retryCounter <= reportRetryLimit);
                // TODO
                // Need an E-mail if it could not generate after the max number of retries          
                break;
            default:
                log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: E-mail Receipt type not set to PICKUP or DELIVERY. No e-mail will be generated!!!");
                break;
        }
    }

    private int addEmailSupervisors(MimeMessage msg) throws MessagingException, UnsupportedEncodingException, ClassNotFoundException {
        int cnt = 0;
        String curNaemailErrorTo = null;
        String curNameErrorTo = null;

        /*
         * If a second Remote Delivery E-mail is being sent and information has not
         * been entered then only send the e-mail to the user (no one else) as discussed
         * with Sheila 4/24/14. We don't need to worry about siging employee since there
         * is no signature. We don't need to worry about Remote Verified By Employee because
         * the information has not been entered for the Remote Verified By Employee.
         * 
         */

        if (this.naemailErrorTo == null || remoteDeliveryNoSigDelivered) {
            //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients NO RECIPIENTS");
            return cnt;
        }

        ArrayList<Employee> emailSupervisors = null;

        try {
            emailSupervisors = db.getEmailSupervisors(username);
        } catch (SQLException ex) {
            log.error(null, ex);
        }

        for (int x = 0; x < emailSupervisors.size(); x++) {
            Employee currentEmailSupervisor = emailSupervisors.get(x);
            currentEmailSupervisor.setEmployeeNameOrder(currentEmailSupervisor.FIRST_MI_LAST_SUFFIX);

            curNameErrorTo = currentEmailSupervisor.getEmployeeName();
            curNaemailErrorTo = currentEmailSupervisor.getNaemail();

            //System.out.println("(" + this.dbaUrl + ") " + x + ": EMAIL:" + curNaemailErrorTo + " NAME:" + curNameErrorTo);
            try {
                if (emailValidator.validate(curNaemailErrorTo)) {
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(curNaemailErrorTo, curNameErrorTo));  //naemailTo, naemployeeTo
                } else {
                    addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, null, "Invalid E-mail Address");
                }
            } catch (UnsupportedEncodingException | MessagingException e) {
                addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, e.getStackTrace(), e.getMessage());
            }
            cnt++;
        }
        return cnt;
    }

    private int addDistributionRecipients(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        int cnt = 0;

        /*
         * If a second Remote Delivery E-mail is being sent and information has not
         * been entered then only send the e-mail to the user (no one else) as discussed
         * with Sheila 4/24/14. We don't need to worry about siging employee since there
         * is no signature. We don't need to worry about Remote Verified By Employee because
         * the information has not been entered for the Remote Verified By Employee.
         * 
         */

        if (this.naemailGenTo == null || remoteDeliveryNoSigDelivered) {
            return cnt;
        }
        for (int x = 0; x < naemailGenTo.length; x++) {
            try {
                if (emailValidator.validate(naemailGenTo[x])) {
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(naemailGenTo[x], getName(x, this.naemailGenNameTo)));  //naemailTo, naemployeeTo
                    cnt++;
                } else {
                    addProblemEmailAddr(naemailGenTo[x], getName(x, this.naemailGenNameTo), null, "Invalid E-mail Address");

                }
            } catch (UnsupportedEncodingException | MessagingException e) {
                addProblemEmailAddr(naemailGenTo[x], getName(x, this.naemailGenNameTo), e.getStackTrace(), e.getMessage());
            }
        }
        return cnt;
    }

    private int addErrorRecipients(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
        int cnt = 0;
        String curNaemailErrorTo = null;
        String curNameErrorTo = null;

        if (this.naemailErrorTo == null) {
            return cnt;
        }

        for (int x = 0; x < naemailErrorTo.length; x++) {
            curNaemailErrorTo = naemailErrorTo[x];
            curNameErrorTo = getName(x, this.naemailErrorNameTo);

            try {
                if (emailValidator.validate(curNaemailErrorTo)) {
                    msg.addRecipient(Message.RecipientType.TO,
                            new InternetAddress(curNaemailErrorTo, curNameErrorTo));  //naemailTo, naemployeeTo
                    cnt++;
                } else {
                    addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, null, "Invalid E-mail Address");

                }
            } catch (UnsupportedEncodingException | MessagingException e) {
                addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, e.getStackTrace(), e.getMessage());
            }

        }
        return cnt;
    }

    private String getName(int row, String[] nameList) {
        if (nameList == null || nameList.length < row) {
            return "";
        } else {
            return nvl(nameList[row], "");
        }
    }

    private String nvl(String value, String nullReturn) {
        if (value == null) {
            return nullReturn;
        } else {
            return value;
        }
    }

    public byte[] bytesFromUrlWithJavaIO(String fileUrl) throws MalformedURLException, IOException, ReportNotGeneratedException {
        return bytesFromUrlWithJavaIO(fileUrl, null);
    }

// Using Java IO
    public synchronized byte[] bytesFromUrlWithJavaIO(String fileUrl, String nafile)
            throws MalformedURLException, IOException, ReportNotGeneratedException {
        //System.out.println("bytesFromUrlWithJavaIO " + fileUrl);
        String nafileext = ".pdf";
        BufferedInputStream in = null;
        ByteArrayOutputStream bout;
        error = null;
        byte[] returnBytes = null;
        bout = new ByteArrayOutputStream();
        try {
            in = new BufferedInputStream(new URL(fileUrl).openStream());
            returnBytes = IOUtils.toByteArray(in);
        } finally {

            if (in != null) {
                in.close();
            }
        }

        String decoded = new String(returnBytes, "UTF-8");
        if (decoded.toUpperCase().startsWith("%PDF-")) {
            try {
                if (nafile != null && nafile.trim().length() > 0) {
                    try (FileOutputStream fos = new FileOutputStream(nafile + nafileext)) {
                        fos.write(returnBytes);
                        fos.flush();
                    }
                }
            } catch (Exception e) {
                e.printStackTrace();
            }
        } else {
            if (decoded.toUpperCase().contains("<HTML>") || decoded.toUpperCase().contains("<BODY>")) {
                nafileext = ".html";
                try {
                    if (nafile != null && nafile.trim().length() > 0) {
                        try (FileOutputStream fos = new FileOutputStream(nafile + nafileext)) {
                            fos.write(returnBytes);
                            fos.flush();
                        }
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            } else {
                nafileext = ".???";
            }
            try {
                try (FileOutputStream fos = new FileOutputStream(nafile + nafileext)) {
                    fos.write(returnBytes);
                    fos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            error = decoded;

            throw new ReportNotGeneratedException("Reports Server was unable to generate a receipt.");
        }

        return returnBytes;
    }

    public void addProblemEmailAddr(String naemail, String naemailName, StackTraceElement[] errorStackTrace, String errorMessage) {
        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") !!!!addProblemEmailAddr naemail:" + naemail + ", naemailName:" + naemailName + ", errorMessage:" + errorMessage);
        System.out.println("(" + this.dbaUrl + ") !!!!addProblemEmailAddr naemail:" + naemail + ", naemailName:" + naemailName + ", errorMessage:" + errorMessage);

        EmailRecord emailRecord = new EmailRecord();
        emailRecord.setNaemail(naemail);
        emailRecord.setNaemailName(naemailName);
        emailRecord.setErrorStackTrace(errorStackTrace);
        emailRecord.setErrorMessage(errorMessage);
        problemEmailAddrs.add(emailRecord);
    }

    public String getProblemEmailString() {
        StringBuilder returnString = new StringBuilder();
        if (problemEmailAddrs == null || problemEmailAddrs.isEmpty()) {
            return "<b>PROBLEM E-MAIL INFORMATION NOT AVAILABLE</b>";
        } else {
            EmailData emailAddrErrorInfo = new EmailData(db, "EMAILADDRERRORINFO");
            for (int x = 0; x < problemEmailAddrs.size(); x++) {
                /*
                 * Clear the puts from previous values in the looping mechanism
                 */
                if (x > 0) {
                    emailAddrErrorInfo.clearValues();
                }
                EmailRecord emailRecord = problemEmailAddrs.get(x);
                try {
                    if (emailRecord.getNaemailName() == null) {
                        emailAddrErrorInfo.put("EmailName", "Null");
                    } else {
                        emailAddrErrorInfo.put("EmailName", emailRecord.getNaemailName());
                    }
                } catch (InvalidParameterException ex) {
                    log.warn(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.warn(null, ex);
                } catch (BlankMessageException ex) {
                    log.warn(null, ex);
                }

                try {
                    if (emailRecord.getNaemail() == null) {
                        emailAddrErrorInfo.put("Email", "Null");
                    } else {
                        emailAddrErrorInfo.put("Email", emailRecord.getNaemail());
                    }
                } catch (InvalidParameterException ex) {
                    log.warn(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.warn(null, ex);
                } catch (BlankMessageException ex) {
                    log.warn(null, ex);
                }

                try {
                    /*
                     * N/A used instead of Null when the value is null because
                     * it makes more sense here to use N/A, the nulls where used
                     * above for Email and Email Address because null would be an
                     * important clue for those values but not for an Error Msg.
                     */
                    if (emailRecord.getErrorMessage() == null) {
                        emailAddrErrorInfo.put("ErrorMessage", "<N/A>");
                    } else {
                        emailAddrErrorInfo.put("ErrorMessage", emailRecord.getErrorMessage());
                    }
                } catch (InvalidParameterException ex) {
                    log.warn(null, ex);
                } catch (ParameterNotUsedException ex) {
                    //log.warn(null, ex);
                } catch (BlankMessageException ex) {
                    log.warn(null, ex);
                }

                StringBuffer stackTraceString = new StringBuffer();
                StackTraceElement[] errorStackTrace = emailRecord.getErrorStackTrace();
                if (errorStackTrace != null) {
                    for (int y = 0; y < errorStackTrace.length; y++) {
                        if (y > 0) {
                            stackTraceString.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                        }
                        stackTraceString.append(errorStackTrace[y].toString());
                    }
                }
                try {
                    if (stackTraceString == null) {
                        emailAddrErrorInfo.put("StackTrace", "N/A");
                    } else {
                        emailAddrErrorInfo.put("StackTrace", stackTraceString.toString());
                    }
                } catch (InvalidParameterException ex) {
                    log.warn(null, ex);;
                } catch (ParameterNotUsedException ex) {
                    //log.warn(null, ex);
                } catch (BlankMessageException ex) {
                    log.warn(null, ex);
                }
                returnString.append(emailAddrErrorInfo.getFormattedMessage());
                //returnString.append("<br/>");
            }
        }
        return returnString.toString();
    }
}
