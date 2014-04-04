/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

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
import java.util.logging.Level;
import javax.activation.DataHandler;
import javax.activation.DataSource;
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
    
    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, Transaction trans) {
            this(request, username, password, type, (String)null, trans);
    }

    public EmailMoveReceipt(HttpServletRequest request, String username, String password, String type, String paperworkType, Transaction trans) {
        this.request = request;
        this.paperworkType = paperworkType;

        switch (type) {
            case "pickup":
                this.emailType = PICKUP;
                this.username = username;
                this.password = password;
                this.pickup = trans;
                userFallback = username; // userfallback is not really being used
                // but it needs to be passed so it is being
                // set to username (which should be set)
                attachmentPart = null;
                transTypeParam = "&p_transtype=PICKUP";
                System.setProperty("java.net.preferIPv4Stack", "true");   // added for test purposes only
                db = new DbConnect(request, username, password);

                this.serverInfo = "";
                this.subjectAddText = "";

                if (db.serverName.toUpperCase().contains("PROD")) {
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE BEFORE PAPERWORK (PICKUP) PROD");                    
                    if (this.paperworkType!=null && this.paperworkType.equalsIgnoreCase("RPK")) {
                        this.subjectAddText = " (REMOTE)";
                    }
                    else {
                        this.subjectAddText = "";
                    }
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE YES!!! AFTER PAPERWORK (PICKUP) PROD");                    
                    this.serverInfo = "";
                } else {
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE BEFORE PAPERWORK (PICKUP)");                    
                    if (this.paperworkType!=null && this.paperworkType.equalsIgnoreCase("RPK")) {
                        this.subjectAddText = " (REMOTE) (" + db.serverName + ")";
                    }
                    else {
                        this.subjectAddText = " (" + db.serverName + ")";
                    }
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE YES!!! AFTER PAPERWORK (PICKUP)");                    
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
                userFallback = username; // userfallback is not really being used
                // but it needs to be passed so it is being
                // set to username (which should be set)
                transTypeParam = "&p_transtype=DELIVERY";
                attachmentPart = null;
                db = new DbConnect(request, username, password);
                this.serverInfo = "";
                this.subjectAddText = "";

                if (db.serverName.toUpperCase().contains("PROD")) {
                    if (this.paperworkType.equalsIgnoreCase("RDL")) {
                        this.subjectAddText = " (REMOTE)";
                    }
                    else {
                        this.subjectAddText = "";
                    }
                    this.serverInfo = "";
                } else {
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE BEFORE PAPERWORK (DELIVERY)");
                    if (this.paperworkType!=null && this.paperworkType.equalsIgnoreCase("RDL")) {
                        this.subjectAddText = " (REMOTE) (" + db.serverName + ")";
                    }
                    else {
                        this.subjectAddText = " (" + db.serverName + ")";
                    }
                    System.out.println("=-=-=-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE YES!!! AFTER PAPERWORK (DELIVERY)");
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

        System.out.println("(" + this.dbaUrl + ") EmailMoveReciept: pickup.getNuxrrelsign:" + pickup.getNuxrrelsign());
        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "EmailMoveReciept: pickup.getNuxrrelsign:" + pickup.getNuxrrelsign() + " " + pickup.getNapickupby());
        // Get the employee who signed the Release 
        signingEmployee = db.getEmployeeWhoSigned(pickup.getNuxrrelsign(), false, userFallback);
        signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);

        // Get the employee who picked up the items
        try {
            pickupEmployee = db.getEmployee(pickup.getNapickupby());
            pickupEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
            this.napickupbyName = pickupEmployee.getEmployeeName().trim();
        } catch (SQLException sqle) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Pickup Employee for " + pickup.getNapickupby(), sqle);
            pickupEmployee = new Employee();
            this.napickupbyName = "N/A";
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
        }
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
        signingEmployee = db.getEmployeeWhoSigned(delivery.getNuxraccptsign(), false, userFallback);
        signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);

        // Get the employee who picked up the items
        try {
            deliveryEmployee = db.getEmployee(delivery.getNadeliverby());
            deliveryEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
            this.nadeliverbyName = deliveryEmployee.getEmployeeName().trim();
        } catch (SQLException sqle) {
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Exception occured when trying to get Pickup Employee for " + pickup.getNapickupby(), sqle);
            pickupEmployee = new Employee();
            this.napickupbyName = "N/A";
        } catch (ClassNotFoundException e) {
            // TODO Auto-generated catch block
            e.printStackTrace();
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
        int nuxrpdOrig = -1;
        switch (emailType) {
            case PICKUP:
                nuxrpdOrig = pickup.getNuxrpd();
                break;
            case DELIVERY:
                nuxrpdOrig = delivery.getNuxrpd();
                break;
        }

        final int nuxrpd = nuxrpdOrig;
        this.nuxrpd = nuxrpd;

        StringBuilder sbTestMsg = new StringBuilder();
        byte[] attachment = null;
        String msgBody = "";
        receiptFilename = nuxrpd + "_" + formatDate(dtreceipt, "yyMMddHHmmss");
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
        log.info("{0}" + "|" + "(" + this.dbaUrl + ") pickupEmailFrom: " + naemailNameFrom);
        if (reportRetryLimitString == null || reportWaitIntervalString.isEmpty()) {
            reportRetryLimit = REPORTRETRYLIMITDEFAULT;
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.retry_limit was not found in config.properties file defaulting to " + reportRetryLimit);
        } else {
            try {
                reportRetryLimit = Integer.parseInt(reportRetryLimitString);
            } catch (Exception e) {
                reportRetryLimit = REPORTRETRYLIMITDEFAULT;
                e.printStackTrace();
                log.info("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.retry_limit was found with an invalid numeric value of (" + reportRetryLimitString + ") in config.properties file defaulting to " + reportRetryLimit + ".[{3}] at {4}", e);
            }
        }

        if (reportWaitIntervalString == null || reportWaitIntervalString.isEmpty()) {
            reportWaitInterval = REPORTWAITINTERVALDEFAULT;
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.wait_interval was not found in config.properties file defaulting to " + reportWaitInterval);
        } else {
            try {
                reportWaitInterval = Integer.parseInt(reportWaitIntervalString);
            } catch (Exception e) {
                e.printStackTrace();
                reportWaitInterval = REPORTWAITINTERVALDEFAULT;
                log.info("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: report.gen.wait_interval was found with an invalid numeric value in config.properties", e);
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
            sbTestMsg.append(signingEmployee.getNaemail());
            sbTestMsg.append("<br /><br />");
            log.info("{0}" + "|" + "(" + this.dbaUrl + ") ***Testing Mode add testing information");
        }

        EmailData emailData = null;

        switch (this.emailType) {
            case PICKUP:
                if (this.paperworkType != null && this.paperworkType.equalsIgnoreCase("RPK")) {
                    emailData = new EmailData(db, "REMOTEPICKUPRCPT");
                    try {
                        emailData.put("ShipType", pickup.getShipType());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        log.error(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }
                    
                    try {
                        emailData.put("ShipTypeDesc", pickup.getShipTypeDesc());
                    } catch (InvalidParameterException ex) {
                        log.error(null, ex);
                    } catch (ParameterNotUsedException ex) {
                        log.error(null, ex);
                    } catch (BlankMessageException ex) {
                        log.error(null, ex);
                    }
                }
                else {
                    emailData = new EmailData(db, "PICKUPRCPT");
                }
                try {
                    if (testingMode) {
                        emailData.setPreMessage(sbTestMsg.toString());
                    }
                    emailData.put("Employee", signingEmployee.getEmployeeName());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("FromAddress", pickupAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("FromLocat", pickup.getOrigin().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("PickupDate", formatDate(dtreceipt, "dd-MMM-yy"));
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("RefDoc", receiptFilename);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("ToAddress", deliverAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("ToLocat", pickup.getDestination().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("UserID", pickup.getNapickupby());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("UserName", napickupbyName);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
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
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                break;
            case DELIVERY:
                emailData = new EmailData(db, "DELIVERYRCPT");
                try {
                    if (testingMode) {
                        emailData.setPreMessage(sbTestMsg.toString());
                    }
                    emailData.put("DeliveryDate", formatDate(dtreceipt, "dd-MMM-yy"));
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }

                try {
                    emailData.put("Employee", signingEmployee.getEmployeeName());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("RefDoc", receiptFilename);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("ToAddress", deliverAddress);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("ToLocat", delivery.getDestination().getCdlocat());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("UserID", delivery.getNapickupby());
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                try {
                    emailData.put("UserName", nadeliverbyName);
                } catch (InvalidParameterException ex) {
                    log.error(null, ex);
                } catch (ParameterNotUsedException ex) {
                    log.error(null, ex);
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
                    log.error(null, ex);
                } catch (BlankMessageException ex) {
                    log.error(null, ex);
                }
                break;
        }
        String error = null;
        /*    sb.append("Dear ");
         sb.append(signingEmployee.getEmployeeName());
         sb.append(",");
         sb.append("<br/><br/>You are receiving this email as a receipt and confirmation that you signed off on the ");
         if (emailType==DELIVERY) {
         sb.append("Delivery ");
         }
         else {
         sb.append("Pickup ");
         }
         sb.append("of the Senate Inventory Equipment on ");
         sb.append(formatDate(dtreceipt, "dd-MMM-yy"));
         sb.append(". <br/><br/>The Inventoried equipment was ");
         if (emailType==DELIVERY) {
         sb.append("delivered ");
         }
         else {
         sb.append("picked up ");
         }
         sb.append(" by ");
         if (emailType==DELIVERY) {
         sb.append(delivery.getNadeliverby());
         }
         else {
         sb.append(pickup.getNapickupby());
         }
         sb.append(" [");
         if (emailType==DELIVERY) {
         sb.append(nadeliverbyName);
         sb.append("] to ");
         sb.append (delivery.getDestination().getCdlocat());
         sb.append(" [");
         sb.append(deliverAddress);
         }
         else {
         sb.append(napickupbyName);
         sb.append("] from ");
         sb.append (pickup.getOrigin().getCdlocat());
         sb.append(" [");
         sb.append(pickupAddress);
         sb.append("] with an intended destination of ");
         sb.append (pickup.getDestination().getCdlocat());
         sb.append(" [");
         sb.append(deliverAddress);
         }
         sb.append("].<br/><br/>");
         sb.append("<br /><br />To view the details of the <b>");
         if (emailType==DELIVERY) {
         sb.append("DELIVERY");
         }
         else {
         sb.append("PICKUP");
         }
         sb.append("</b> on the <b>Senate Equipment Request and Issue Receipt</b>, please open the PDF attachment in this email.");
         sb.append("<br /><br />If you believe that you have received this email in error, please contact Senate Inventory Control Office @ 518-455-3233. Reference Doc#:");
         sb.append(receiptFilename);*/
        //Logger.getLogger(EmailMoveReceipt.class.getName()).info("***E-mail body added ");
        //System.out.println ("***EMAIL:+"+sb.toString());
        //Logger.getLogger(EmailMoveReceipt.class.getName()).info("***EMAIL:+"+sb.toString());

        try {
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE nuxrpd: "+ nuxrpd);
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE FILE TO WRITE:"+receiptPath+nuxrpd+"_"+formatDate(new Date(), "yyMMddHHmmss"));
            // If the Attachment does not return a pdf, then it will be null since it expects a PDF, so we can tag on .pdf as a filename
            attachment = bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam, receiptPath + receiptFilename); // +"&destype=CACHE&desformat=PDF
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER GETTING ATTACHMENT");

            // Attachment needs to be checked to ensure that there were no issues with the Reports Server
            // and the PDF was generated properly. Otherwise the PDF sent is garbage.  We need to e-mail
            // STSBAC and possibly others that an issue occured and/or try to generate the PDF again

            //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
            //System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((attachment.length) / 1024.0) + "KB");
        } catch (MalformedURLException ex) {
            log.error(null, ex);
            if (returnStatus == 0) {
                returnStatus = 2;
            }
            System.out.println("call email error (1)");
            log.info("call email error (1A)");
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
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR ReportNotGeneratedException1");
            System.out.println("call email error (2)");
            log.info("call email error (2A)");
            error = invUtil.stackTraceAsMsg(ex);

            emailError(emailType);
            return returnStatus;
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR ReportNotGeneratedException1");
        }
        if (attachment == null) {
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 1");
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "****ATTACHMENT was null Pickup.processRequest ");
            if (returnStatus == 0) {
                returnStatus = 4;
            }
            System.out.println("call email error (3)");
            log.info("call email error (3A)");
            error = "<br/> Error: Null Attachment";
            emailError(emailType);
            return returnStatus;

            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 2");
        } else if (attachment.length == 0) {
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment.length==0 1");
            log.warn("{0}" + "|" + "(" + this.dbaUrl + ") " + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
            if (returnStatus == 0) {
                returnStatus = 5;
            }
            error = "<br/> Error: Attachment with a length of 0";
            System.out.println("call email error (4)");
            log.info("call email error (4A)");

            emailError(emailType);
            return returnStatus;
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR attachment.length==0 1");
        }

        //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT");
        MimeMultipart mimeMultipart = new MimeMultipart();
        attachmentPart = getOracleReportResponse(receiptURL, nuxrpd);


        try {
            System.out.println("(" + this.dbaUrl + ") -=-=-=-=-=-=-=-=-=TRACE ATTACHMENT (before) FILENAME:" + attachmentPart.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }
        try {
            attachmentPart.setFileName(receiptFilename + ".pdf");
            System.out.println("(" + this.dbaUrl + ") -=-=-=-=-=-=-=-=-=TRACE ATTACHMENT(after) FILENAME:" + attachmentPart.getFileName());
        } catch (Exception e) {
            e.printStackTrace();
        }

        try {
            in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

            msgBody = emailData.getFormattedMessage();
            System.out.println("-=-=-=-=-=-=-=-=-=TRACE EMAIL BODY:" + msgBody);

            MimeMessage msg = new MimeMessage(session);
            //System.out.println("EMAILING FROM:" + naemailFrom + ":" + naemailNameFrom);
            try {
                msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
            } catch (UnsupportedEncodingException | MessagingException e) {
            }
            int recipientCount = 0;
            recipientCount = addDistributionRecipients(msg);
            recipientCount = recipientCount + addEmailSupervisors(msg);
            if (this.emailType == PICKUP) {
                if (pickupEmployee != null && pickupEmployee.getNaemail() != null) {
                    try {
                        if (emailValidator.validate(pickupEmployee.getNaemail())) {
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                    }
                } else if (pickupEmployee == null) {
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Pickup Employee was null so can''t add Pickup Employee as recipient.");
                } else if (pickupEmployee.getNaemail() == null) {
                    addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Pickup Employee " + pickupEmployee.getEmployeeName() + " E-mail Field was null so can''t add Pickup Employee as recipient.");
                }
            } else if (this.emailType == DELIVERY) {
                if (deliveryEmployee != null && deliveryEmployee.getNaemail() != null) {
                    try {
                        if (emailValidator.validate(deliveryEmployee.getNaemail())) {
                            msg.addRecipient(Message.RecipientType.TO,
                                    new InternetAddress(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                            recipientCount++;
                        } else {
                            addProblemEmailAddr(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                        }
                    } catch (UnsupportedEncodingException | MessagingException e) {
                        addProblemEmailAddr(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                    }
                } else if (deliveryEmployee == null) {
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Delivery Employee was null so can''t add Delivery Employee as recipient.");
                } else if (deliveryEmployee.getNaemail() == null) {
                    log.warn("{0}" + "|" + "(" + this.dbaUrl + ") ***WARNING: Delivery Employee " + deliveryEmployee.getEmployeeName() + " E-mail Field was null so can''t add Delivery Employee as recipient.");
                }
            }

            if (testingMode) {
                System.out.println("(" + this.dbaUrl + ") TESTINGMODE Would have sent (BUT DID NOT) TO:" + signingEmployee.getNaemail() + " (" + signingEmployee.getEmployeeName() + ")");
                if (naemailTo1 != null && naemailTo1.trim().length() > 0) {
                    try {
                        System.out.println("(" + this.dbaUrl + ") TESTINGMODE EMAILING TO:" + naemailTo1 + ":" + naemailNameTo1);
                        if (emailValidator.validate(naemailTo1)) {
                            msg.addRecipient(Message.RecipientType.TO,
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
                        System.out.println("(" + this.dbaUrl + ") TESTINGMODE EMAILING TO:" + naemailTo2 + ":" + naemailNameTo2);
                        log.warn("(" + this.dbaUrl + ") TESTINGMODE EMAILING TO:" + naemailTo2 + ":" + naemailNameTo2);
                        if (emailValidator.validate(naemailTo2)) {
                            msg.addRecipient(Message.RecipientType.TO,
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
                try {
                    System.out.println("(" + this.dbaUrl + ") REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                    log.warn("(" + this.dbaUrl + ") REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                    if (emailValidator.validate(signingEmployee.getNaemail())) {
                        msg.addRecipient(Message.RecipientType.TO,
                                new InternetAddress(signingEmployee.getNaemail(), signingEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
                        recipientCount++;
                    } else {
                        addProblemEmailAddr(signingEmployee.getNaemail(), signingEmployee.getEmployeeName(), null, "Invalid E-mail Address");
                    }
                    System.out.println("(" + this.dbaUrl + ") ADDED REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                    log.warn("(" + this.dbaUrl + ") ADDED REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                } catch (UnsupportedEncodingException | MessagingException e) {
                    System.out.println("(" + this.dbaUrl + ") EXCEPTION REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                    log.warn("(" + this.dbaUrl + ") EXCEPTION REAL addRecipient: email:" + signingEmployee.getNaemail() + ", Email Name:" + signingEmployee.getEmployeeName());
                    addProblemEmailAddr(signingEmployee.getNaemail(), signingEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
                }
            }

            //System.out.println("EMAILING BEFORE SUBJECT");
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
                    System.out.println("(" + this.dbaUrl + ") ***E-mail NOT sent because attachment was malformed(2).");
                } else {
                    //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT BEFORE SENDING E-MAIL");

                    if (recipientCount == 0) {
                        log.warn("{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: There were no e-mail recipients for a Report. No e-mail will be sent!!!");
                    } else {
                        Transport.send(msg);
                        //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT AFTER SENDING E-MAIL");
                    }
                    emailWarning(emailType);
                }
            }
            System.out.println("(" + this.dbaUrl + ") E-mail sent with no errors.");

        } catch (AddressException e) {
            if (returnStatus == 0) {
                returnStatus = 10;
                try {
                    error = invUtil.stackTraceAsMsg(e);
                    System.out.println("call email error (5)");
                    log.info("call email error (5A)");
                    emailError(emailType, "(" + this.dbaUrl + ") ADDRESS EXCEPTION:+" + e.getMessage() + " [" + e.getStackTrace()[0].toString() + "]");
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
                    //System.out.println("call email error (6)");
                    //Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.INFO, "call email error (6A)");
                    emailError(emailType, "(" + this.dbaUrl + ") MESSAGING EXCEPTION:+" + e.getMessage());
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
                    System.out.println("call email error (7)");
                    log.info("call email error (7A)");
                    emailError(emailType, "(" + this.dbaUrl + ") GENERAL EXCEPTION:+" + e.getMessage());
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

            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ADD ATTACHMENT");
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
                        ////System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream()");

                        return new ByteArrayInputStream(bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam));
                    } catch (ReportNotGeneratedException e) {
                        ////System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream() ReportNotGeneratedException");
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
            //System.out.println ("EMAILMOVERECEIPT ATTACHMENT NAME:"+attachmentPart.getDataHandler().getName());
            //System.out.println ("EMAILMOVERECEIPT ATTACHMENT NAME(2):"+attachmentPart.getDataHandler().getDataSource().getName());

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
                log.warn("(AA)!!!!EMAILERROR BEFORE SET MESSAGE ERROR:" + error);
                System.out.println("(AB)!!!!!!!!!!!!!EMAILERROR BEFORE SET MESSAGE ERROR:" + error);
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
                log.error(null, ex);
            } catch (BlankMessageException ex) {
                log.error(null, ex);
            }

            msgHeader = errorEmailData.getFormattedMessage();

            log.warn("(B)!!!!EMAILERROR BEFORE SET MESSAGE:" + msgHeader);
            System.out.println("(A)!!!!!!!!!!!!!EMAILERROR BEFORE SET MESSAGE:" + msgHeader);
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
            log.warn("{0}" + "|" + "!!!!EMAILERROR AFTER SET MESSAGE");
            log.info("{0}| EMAIL ERRORR MSG: " + message);
            // Send message
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
            return;
        }
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

            /*if (recipientCount == 0) {
             Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, "{0}" + "|" + "(" + this.dbaUrl + ") **WARNING: There were no e-mail recipients for a Report Genration error. No warning e-mail will be sent!!!");
             if (this.problemEmailAddrs != null && this.problemEmailAddrs.size() > 0) {
             this.emailWarning(emailType);
             }
             return;
             }*/
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
                /*if (testingMode) {
                 emailData.setPreMessage(sbTestMsg.toString());
                 }*/
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
                log.warn(null, ex);
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
            /*if (this.problemEmailAddrs != null && this.problemEmailAddrs.size() > 0) {
             this.emailWarning(emailType);
             }*/
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

        if (this.naemailErrorTo == null) {
            //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients NO RECIPIENTS");
            return cnt;
        }

        ArrayList<Employee> emailSupervisors = null;

        try {
            emailSupervisors = db.getEmailSupervisors(username);
        } catch (SQLException ex) {
            log.error(null, ex);
        }

        //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients "+naemailErrorTo.length+" RECIPIENTS");

        for (int x = 0; x < emailSupervisors.size(); x++) {
            Employee currentEmailSupervisor = emailSupervisors.get(x);
            currentEmailSupervisor.setEmployeeNameOrder(currentEmailSupervisor.FIRST_MI_LAST_SUFFIX);

            curNameErrorTo = currentEmailSupervisor.getEmployeeName();
            curNaemailErrorTo = currentEmailSupervisor.getNaemail();

            System.out.println("(" + this.dbaUrl + ") " + x + ": EMAIL:" + curNaemailErrorTo + " NAME:" + curNameErrorTo);
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

        if (this.naemailGenTo == null) {
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
        //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients");

        if (this.naemailErrorTo == null) {
            //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients NO RECIPIENTS");
            return cnt;
        }

        //Logger.getLogger(EmailMoveReceipt.class.getName()).info("addErrorRecipients "+naemailErrorTo.length+" RECIPIENTS");

        for (int x = 0; x < naemailErrorTo.length; x++) {
            curNaemailErrorTo = naemailErrorTo[x];
            curNameErrorTo = getName(x, this.naemailErrorNameTo);

            //Logger.getLogger(EmailMoveReceipt.class.getName()).info("EMAIL ERRORR TO:" + curNaemailErrorTo + " NAME:"+curNameErrorTo);
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
    public byte[] bytesFromUrlWithJavaIO(String fileUrl, String nafile)
            throws MalformedURLException, IOException, ReportNotGeneratedException {
        //System.out.println("bytesFromUrlWithJavaIO " + fileUrl);
        String nafileext = ".pdf";
        BufferedInputStream in = null;
        ByteArrayOutputStream bout;
        error = null;
        byte[] returnBytes = null;
        bout = new ByteArrayOutputStream();
        try {
            //System.out.println("bytesFromUrlWithJavaIO READ " + fileUrl);
            in = new BufferedInputStream(new URL(fileUrl).openStream());
            //System.out.println("bytesFromUrlWithJavaIO READ DONE " + fileUrl);
            returnBytes = IOUtils.toByteArray(in);
            //System.out.println("bytesFromUrlWithJavaIO returnBytes:" + returnBytes.length);
        } finally {

            if (in != null) {
                in.close();
            }
        }

        String decoded = new String(returnBytes, "UTF-8");
        //System.out.println("****URL:" + fileUrl);
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
                //decoded = insertTextInto(decoded, "src=\"^", );
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
            //System.out.println("PickupServlet.bytesFromUrlWithJavaIO writing file:" + nafile + nafileext);
            try {
                try (FileOutputStream fos = new FileOutputStream(nafile + nafileext)) {
                    fos.write(returnBytes);
                    fos.flush();
                }
            } catch (Exception e) {
                e.printStackTrace();
            }

            //System.out.println("****REPORT DOES NOT CONTAIN %PDF- STARTS WITH: " + decoded.substring(0, 100));
            error = decoded;

            throw new ReportNotGeneratedException("Reports Server was unable to generate a receipt.");
        }
        //System.out.println(decoded);

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
                    if (emailRecord.getNaemailName()==null) {
                        emailAddrErrorInfo.put("EmailName", "Null");
                    }
                    else {
                        emailAddrErrorInfo.put("EmailName", emailRecord.getNaemailName());
                    }
                } catch (InvalidParameterException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (ParameterNotUsedException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (BlankMessageException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                }

                try {
                    if (emailRecord.getNaemail()==null) {
                        emailAddrErrorInfo.put("Email", "Null");
                    }
                    else {
                        emailAddrErrorInfo.put("Email", emailRecord.getNaemail());
                    }
                } catch (InvalidParameterException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (ParameterNotUsedException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (BlankMessageException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                }

                try {
                    /*
                     * N/A used instead of Null when the value is null because
                     * it makes more sense here to use N/A, the nulls where used
                     * above for Email and Email Address because null would be an
                     * important clue for those values but not for an Error Msg.
                     */
                    if (emailRecord.getErrorMessage()==null) {
                        emailAddrErrorInfo.put("ErrorMessage", "<N/A>");
                    }
                    else {
                        emailAddrErrorInfo.put("ErrorMessage", emailRecord.getErrorMessage());
                    }
                } catch (InvalidParameterException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (ParameterNotUsedException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (BlankMessageException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                }

                /*returnString.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Email:</b> ");
                 returnString.append(emailRecord.getNaemail());
                 returnString.append("&nbsp;&nbsp;&nbsp<b>Name:</b> ");
                 returnString.append(emailRecord.getNaemailName());
                 returnString.append("&nbsp;&nbsp;&nbsp<b>Error Message:</b> ");
                 returnString.append(emailRecord.getErrorMessage());*/
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
                    if (stackTraceString==null) {
                       emailAddrErrorInfo.put("StackTrace", "N/A");
                    }
                    else {
                       emailAddrErrorInfo.put("StackTrace", stackTraceString.toString());
                    }
                } catch (InvalidParameterException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (ParameterNotUsedException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                } catch (BlankMessageException ex) {
                    java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
                }
                returnString.append(emailAddrErrorInfo.getFormattedMessage());
                //returnString.append("<br/>");
            }
        }
        return returnString.toString();
    }
}
