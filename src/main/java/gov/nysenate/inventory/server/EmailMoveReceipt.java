/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.ReportNotGeneratedException;
import gov.nysenate.inventory.model.EmailInformation;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.model.EmailRecord;

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
import java.util.logging.Logger;
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

import org.apache.commons.io.IOUtils;

/**
 *
 * @author senateuser
 */
public class EmailMoveReceipt implements Runnable
{
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
  private ArrayList<EmailRecord> problemEmailAddrs = new ArrayList<EmailRecord>();
  
  public EmailMoveReceipt(String username, String password, String type, Transaction trans) {
      if (type.equals("pickup")) {
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
        db = new DbConnect(username, password);
        properties = new Properties();
        in = getClass().getClassLoader().getResourceAsStream("config.properties");
        serverOS = System.getProperty("os.name");
        if (serverOS.toUpperCase().indexOf("WINDOWS")==-1) {
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
            java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
      }
      else if (type.equals("delivery")) {
          this.emailType = DELIVERY;
          this.username = username;
          this.password = password;
          this.delivery = trans;
          userFallback = username; // userfallback is not really being used
                                   // but it needs to be passed so it is being
                                   // set to username (which should be set)
          transTypeParam = "&p_transtype=DELIVERY";
          attachmentPart = null;
          db = new DbConnect(username, password);
          System.setProperty("java.net.preferIPv4Stack", "true");   // added for test purposes only
          properties = new Properties();
          in = getClass().getClassLoader().getResourceAsStream("config.properties");
          serverOS = System.getProperty("os.name");
          if (serverOS.toUpperCase().indexOf("WINDOWS")==-1) {
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
              java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
          }  
      }
  }

  public void testingModeCheck()
  {
    //System.out.println ("TESTINGMODEPARAM:"+testingModeParam);
    if (testingModeParam != null && testingModeParam.trim().length() > 0) {
      if (testingModeParam.toUpperCase().indexOf("T") > -1) {
        testingMode = true;
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****testingModeParam has a T, so Testing Mode is set to TRUE Pickup.processRequest ");
      } else {
        testingMode = false;
      }
    } else if (testingModeProperty == null || testingModeProperty.toUpperCase().contains("T")) {
      testingMode = true;
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "***Testing Mode is set to TRUE Pickup.processRequest ");
    }
  }

 private void initializeEmailTo () {
       if (properties==null) {
          return;
       } 
       String naemailGenToS = properties.getProperty("report.gen.email_to");
       String naemailGenNameToS = properties.getProperty("report.gen.email_name_to");
       String naemailErrorToS = properties.getProperty("report.error.email_to");
       String naemailErrorNameToS = properties.getProperty("report.error.email_name_to");
       if (naemailGenToS == null) {
         this.naemailGenTo = null;
       }
       else {
          this.naemailGenTo = naemailGenToS.split("\\|");
       }
       
       if (naemailGenNameToS == null) {
         this.naemailGenNameTo = null;
       }
       else {
         this.naemailGenNameTo = naemailGenNameToS.split("\\|");
       }

       if (naemailErrorToS == null) {
         this.naemailErrorTo = null;
       }
       else {
         this.naemailErrorTo = naemailErrorToS.split("\\|");
       }
       
       if (naemailErrorNameToS == null) {
         this.naemailGenNameTo = null;
       }
       else {
         this.naemailErrorNameTo = naemailErrorNameToS.split("\\|");
       }
       
      try {
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "initializeEmailTo: Length:"+this.naemailErrorTo.length+" Name Length:"+this.naemailGenNameTo.length);
      }
      catch (NullPointerException e) {
          e.printStackTrace();
      }
 }
  
  /*
   * Pickup Specific function serves as the initial setup code for the sendEmail(int emailType)
   * which handles both Pickup and Delivery
   */
  
  public int sendPickupEmailReceipt(Transaction pickup)
  {
    if (emailType!=PICKUP) {
       Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Email Type was not set to PICKUP!!! Not emailing Pickup receipt.");
       return 30;
    }
    this.pickup = pickup;
//    String napickupby = pickup.getNapickupby();
    String originLocation = pickup.getOrigin().getCdlocat();
    String destinationLocation = pickup.getDestination().getCdlocat();  
    
    String naemployeeTo = "";

    
    try {
      db.setLocationInfo(pickup.getOrigin());
      pickupAddress = pickup.getOrigin().getAdstreet1()+" "+pickup.getOrigin().getAdcity()+", "+pickup.getOrigin().getAdstate()+", "+pickup.getOrigin().getAdzipcode();
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    try {
      db.setLocationInfo(pickup.getDestination());
      deliverAddress = pickup.getDestination().getAdstreet1()+" "+pickup.getDestination().getAdcity()+", "+pickup.getDestination().getAdstate()+", "+pickup.getDestination().getAdzipcode();
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
 
    // Get the employee who signed the Release 
    signingEmployee = db.getEmployeeWhoSigned(pickup.getNuxrrelsign(), false, userFallback);
    signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
    
    // Get the employee who picked up the items
    try {
      pickupEmployee  = db.getEmployee(pickup.getNapickupby());
      pickupEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
      this.napickupbyName = pickupEmployee.getEmployeeName().trim();
    }
    catch (SQLException sqle) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Exception occured when trying to get Pickup Employee for (USER:"+pickup.getNapickupby()+") ("+sqle.getMessage()+")");
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
  
  public int sendDeliveryEmailReceipt(Transaction delivery)
  {
    if (emailType!=DELIVERY) {
       Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Email Type was not set to DELIVERY!!! Not emailing Delivery receipt.");
       return 31;
    }
    this.delivery = delivery;
    
    try {
      db.setLocationInfo(delivery.getDestination());
      deliverAddress = delivery.getDestination().getAdstreet1()+" "+delivery.getDestination().getAdcity()+", "+delivery.getDestination().getAdstate()+", "+delivery.getDestination().getAdzipcode();
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

 
    // Get the employee who signed the Release 
    signingEmployee = db.getEmployeeWhoSigned(delivery.getNuxraccptsign(), false, userFallback);
    signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
    
    // Get the employee who picked up the items
    try {
      deliveryEmployee  = db.getEmployee(delivery.getNadeliverby());
      deliveryEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
      this.nadeliverbyName = deliveryEmployee.getEmployeeName().trim();
    }
    catch (SQLException sqle) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Exception occured when trying to get Pickup Employee for (USER:"+pickup.getNapickupby()+") ("+sqle.getMessage()+")");
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
        nuxrpdOrig =  pickup.getNuxrpd();
        break;
      case DELIVERY:
        nuxrpdOrig = delivery.getNuxrpd();
        break;
    }
    
    final int nuxrpd = nuxrpdOrig;
    this.nuxrpd = nuxrpd;
      
    StringBuilder sb = new StringBuilder();
    byte[] attachment = null;
    String msgBody = "";
    receiptFilename = nuxrpd+"_"+formatDate(dtreceipt, "yyMMddHHmmss");
    int returnStatus = 0;    
    
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
    try {
      properties.load(in);
    } catch (IOException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
      returnStatus = 1;
    }   
    
    String smtpServer = properties.getProperty("smtpServer");
    final String receiptURL = properties.getProperty("pickupReceiptURL");
    this.receiptURL = receiptURL;

    Properties props = new Properties();
    props.setProperty("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props, null);
 
    this.naemailTo2 =  properties.getProperty("pickupEmailTo2");
    naemailFrom = null; 
    naemailFrom = properties.getProperty("pickupEmailFrom");    
    naemailNameFrom = null; 
    naemailNameFrom = properties.getProperty("pickupEmailNameFrom");
    reportRetryLimitString = properties.getProperty("report.gen.retry_limit");
    reportWaitIntervalString = properties.getProperty("report.gen.wait_interval");
    Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "pickupEmailFrom:"+naemailNameFrom);
    if (reportRetryLimitString==null||reportWaitIntervalString.isEmpty()) {
        reportRetryLimit = REPORTRETRYLIMITDEFAULT;
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "**WARNING: report.gen.retry_limit was not found in config.properties file defaulting to "+reportRetryLimit+".");
    }
    else {
      try {
        reportRetryLimit = Integer.parseInt(reportRetryLimitString);
      }
      catch (Exception e) {
        reportRetryLimit = REPORTRETRYLIMITDEFAULT;
        e.printStackTrace();
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "**WARNING: report.gen.retry_limit was found with an invalid numeric value of ("+reportRetryLimitString+") in config.properties file defaulting to "+reportRetryLimit+".["+e.getMessage()+"] at "+e.getStackTrace()[0].toString());
      }
    }
    
    if (reportWaitIntervalString==null||reportWaitIntervalString.isEmpty()) {
        reportWaitInterval = REPORTWAITINTERVALDEFAULT;  
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "**WARNING: report.gen.wait_interval was not found in config.properties file defaulting to "+reportWaitInterval+".");
    }
    else {
      try {
        reportWaitInterval = Integer.parseInt(reportWaitIntervalString);
      }
      catch (Exception e) {
        e.printStackTrace();
        reportWaitInterval = REPORTWAITINTERVALDEFAULT;
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "**WARNING: report.gen.wait_interval was found with an invalid numeric value of ("+reportWaitIntervalString+") in config.properties file defaulting to "+reportWaitInterval+".["+e.getMessage()+"] at "+e.getStackTrace()[0].toString());
      }      
    }

    try {
        naemailTo1 = properties.getProperty("pickupEmailTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
       
    try {
        naemailNameTo1 = properties.getProperty("pickupEmailNameTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }

    try {
        naemailTo2 = properties.getProperty("pickupEmailTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
        naemailNameTo2 = properties.getProperty("pickupEmailNameTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
      testingModeProperty =  properties.getProperty("testingMode").toUpperCase();
     }
    catch (NullPointerException e) {
      // Could not find the Testing Mode Property so assume that we are in testing mode, this will
      // at least alert someone if no one is getting receipts..
      testingModeProperty = "TRUE";
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****PARAMETER testingMode was NOT FOUND  TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ");
    }
    catch (Exception e) {
      testingModeProperty = "TRUE";
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Exception occured when trying to find testingMode Property ("+e.getMessage()+") TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ");
    }
          
    /*
     *  If either E-mail to field is filled, then the server is meant to e-mail that specific user
     * instead of the user that should be e-mailed. This would mean that the server is in testing mode.
     */
    
   
    if (testingMode) {
      sb.append("<b>TESTINGMODE</b>: E-mail under normal circumstances would have been sent to:");
      sb.append(signingEmployee.getNaemail());
      sb.append("<br /><br />");
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "***Testing Mode add testing information");
    }
    
    String error = null;
    sb.append("Dear ");
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
    sb.append(receiptFilename);
    //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "***E-mail body added ");
    //System.out.println ("***EMAIL:+"+sb.toString());
    //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "***EMAIL:+"+sb.toString());
  
    try {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE nuxrpd: "+ nuxrpd);
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE FILE TO WRITE:"+receiptPath+nuxrpd+"_"+formatDate(new Date(), "yyMMddHHmmss"));
       // If the Attachment does not return a pdf, then it will be null since it expects a PDF, so we can tag on .pdf as a filename
      attachment = bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam, receiptPath+receiptFilename); // +"&destype=CACHE&desformat=PDF
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER GETTING ATTACHMENT");

      // Attachment needs to be checked to ensure that there were no issues with the Reports Server
      // and the PDF was generated properly. Otherwise the PDF sent is garbage.  We need to e-mail
      // STSBAC and possibly others that an issue occured and/or try to generate the PDF again
      
      //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
      //System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((attachment.length) / 1024.0) + "KB");
    } catch (MalformedURLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
      if (returnStatus == 0) {
        returnStatus = 2;
      }
      emailError(emailType, "<html><body>Email Error URL was MALFORMED: <b>"+receiptURL + nuxrpd + transTypeParam+"</b><br/><br/></body></html>");
      return returnStatus;
    } catch (IOException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ReportNotGeneratedException ex) {
      if (returnStatus == 0) {
        returnStatus = 2;
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, "There was an issue with Oracle Reports Server. Please contact STS/BAC.", ex);    
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR ReportNotGeneratedException1");
      emailError(emailType);
      return returnStatus;
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR ReportNotGeneratedException1");
     }
    if (attachment == null) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 1");
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was null Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 4;
      }
      emailError(emailType);
      return returnStatus;

      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 2");
    }
    else if (attachment.length==0) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment.length==0 1");
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 5;
      }
      emailError(emailType);
      return returnStatus;
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR attachment.length==0 1");
    }
    
    //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT");
    MimeMultipart mimeMultipart = new MimeMultipart();
    attachmentPart = getOracleReportResponse(receiptURL, nuxrpd);
    
    
    try{
    System.out.println("-=-=-=-=-=-=-=-=-=TRACE ATTACHMENT (before) FILENAME:"+attachmentPart.getFileName());
    }
    catch (Exception e) {
        e.printStackTrace();
    }
    try{
    attachmentPart.setFileName(receiptFilename+".pdf");
    System.out.println("-=-=-=-=-=-=-=-=-=TRACE ATTACHMENT(after) FILENAME:"+attachmentPart.getFileName());
    }
    catch (Exception e) {
        e.printStackTrace();
    }    
    
   try {
      in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
      properties.load(in);
 
      msgBody = sb.toString();
      MimeMessage msg = new MimeMessage(session);
      //System.out.println("EMAILING FROM:" + naemailFrom + ":" + naemailNameFrom);
      try {
        msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
      }
      catch (Exception e) {
          
      }
      int recipientCount = 0;
      recipientCount = addDistributionRecipients(msg);
      recipientCount = recipientCount + addEmailSupervisors(msg);
      if (this.emailType==PICKUP) {
          if (pickupEmployee!=null && pickupEmployee.getNaemail()!=null) {
            try {
              msg.addRecipient(Message.RecipientType.TO,
                      new InternetAddress(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
              recipientCount++;
            } catch (Exception e) {
              addProblemEmailAddr(pickupEmployee.getNaemail(), pickupEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
            }
          }
          else if (pickupEmployee==null) {
             Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Pickup Employee was null so can't add Pickup Employee as recipient.");
          }
          else if (pickupEmployee.getNaemail()==null) {
             Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Pickup Employee ("+pickupEmployee.getEmployeeName()+") E-mail Field was null so can't add Pickup Employee as recipient.");
          }
      }
      else if (this.emailType==DELIVERY) {
         if (deliveryEmployee!=null && deliveryEmployee.getNaemail()!=null) {
          try {
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(deliveryEmployee.getNaemail(), deliveryEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
          }
        }
         else if (deliveryEmployee==null) {
             Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Delivery Employee was null so can't add Delivery Employee as recipient.");
         }
         else if (deliveryEmployee.getNaemail()==null) {
             Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Delivery Employee ("+deliveryEmployee.getEmployeeName()+") E-mail Field was null so can't add Delivery Employee as recipient.");
         }
      }

      if (testingMode) {
        System.out.println("TESTINGMODE Would have sent (BUT DID NOT) TO:" + signingEmployee.getNaemail() + " (" + signingEmployee.getEmployeeName()+")");
        if (naemailTo1!=null && naemailTo1.trim().length()>0){
          try {
            System.out.println("TESTINGMODE EMAILING TO:" + naemailTo1 + ":" + naemailNameTo1);
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
          }
        }
        if (naemailTo2!=null && naemailTo2.trim().length()>0){
          try {
            System.out.println("TESTINGMODE EMAILING TO:" + naemailTo2 + ":" + naemailNameTo2);
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
          }

        }
      }
      else {
        try {
          msg.addRecipient(Message.RecipientType.TO,
                  new InternetAddress(signingEmployee.getNaemail(), signingEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
          recipientCount++;
        } catch (Exception e) {
          addProblemEmailAddr(signingEmployee.getNaemail(), signingEmployee.getEmployeeName(), e.getStackTrace(), e.getMessage());
        }          
      }
      
      //System.out.println("EMAILING BEFORE SUBJECT");
      if (emailType==DELIVERY) {
         msg.setSubject("Equipment Delivery Receipt");
      }
      else {
         msg.setSubject("Equipment Pickup Receipt");
      }
      //msg.setText(msgBody, "utf-8", "html");
      MimeBodyPart mbp1 = new MimeBodyPart();
      mbp1.setText(msgBody);
      mbp1.setContent(msgBody, "text/html");
      mimeMultipart.addBodyPart(mbp1);
      mimeMultipart.addBodyPart(attachmentPart);
      msg.setContent(mimeMultipart);
      if (attachmentPart==null||attachmentPart.getSize()==0) {
          System.out.println("***E-mail NOT sent because attachment was malformed.");
          if (returnStatus==0) {
              returnStatus= 8;
          }            
      }
      else {
          if (attachmentPart.getContent()==null) {
              if (returnStatus==0) {
                returnStatus= 9;
              }            
              System.out.println("***E-mail NOT sent because attachment was malformed(2).");
          }
          else {
              //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT BEFORE SENDING E-MAIL");

              if (recipientCount==0) {
                  Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: There were no e-mail recipients for a Report. No e-mail will be sent!!!");
              }
              else {
                  Transport.send(msg);
                  //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT AFTER SENDING E-MAIL");
              }
          }
      }
      System.out.println("E-mail sent with no errors.");

    } catch (AddressException e) {
      if (returnStatus == 0) {
        returnStatus = 10;
        try {
          emailError(emailType, "ADDRESS EXCEPTION:+" +e.getMessage()+" ["+e.getStackTrace()[0].toString()+"]");
        }
        catch (Exception e2) {
          e2.printStackTrace();
        }
        return returnStatus;
      }
      
      e.printStackTrace();
    } catch (MessagingException e) {
      if (returnStatus == 0) {
        returnStatus = 11;
        try {
          emailError(emailType, "MESSAGING EXCEPTION:+" +e.getMessage());
        }
        catch (Exception e2) {
          e2.printStackTrace();
        }
        return returnStatus;
      }
      e.printStackTrace();
    } catch (Exception e) {
      if (returnStatus == 0) {
        returnStatus = 20;
        try {
          emailError(emailType, "GENERAL EXCEPTION:+" +e.getMessage());
        }
        catch (Exception e2) {
          e2.printStackTrace();
        }
        return returnStatus;
      }
      e.printStackTrace();
    }    
    return returnStatus;
  }
  
  public String formatDate(Date d, String format) {
    if (d==null) {
      return "";
    }
    SimpleDateFormat sdf = new SimpleDateFormat(format);
    return sdf.format(d);
  }
  
  private MimeBodyPart getOracleReportResponse(final String receiptURL , final int nuxrpd) {
    MimeBodyPart attachmentPart = new MimeBodyPart();
    try {

      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ADD ATTACHMENT");
      attachmentPart.setDataHandler(
              new DataHandler(
              new DataSource()
      {
        @Override
        public String getContentType()
        {
          return "application/pdf";
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
          try {
            ////System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream()");
            
            return new ByteArrayInputStream(bytesFromUrlWithJavaIO(receiptURL + nuxrpd + transTypeParam));
          }
          catch (ReportNotGeneratedException e) {
            ////System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream() ReportNotGeneratedException");
            Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, "Oracle Reports Server failed to generate a PDF Report for the Pickup Receipt. Please contact STS/BAC.", e);                  
            return new ByteArrayInputStream(new byte[0]);
          }
        }

        @Override
        public String getName()
        {
         Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "DataSource.getName() called. Returning:"+receiptFilename+".pdf");
          return receiptFilename+".pdf";
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
          return null;
        }
      }));
      //System.out.println ("EMAILMOVERECEIPT ATTACHMENT NAME:"+attachmentPart.getDataHandler().getName());
      //System.out.println ("EMAILMOVERECEIPT ATTACHMENT NAME(2):"+attachmentPart.getDataHandler().getDataSource().getName());
      
    } catch (MessagingException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
    }
    return attachmentPart;
  }
  
 /*
  * New EmailError
  */

 public void emailError(int emailType) {
    emailError(emailType, null);
 }
  
 public void emailError(int emailType,String msgOverride) {
      Properties props = new Properties();
      String smtpServer = properties.getProperty("smtpServer");
      props.setProperty("mail.smtp.host", smtpServer);
      Session session = Session.getDefaultInstance(props, null);
      int recipientCount = 0;
      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
         
      // Set To: header field of the header.
      if (testingMode) {
        recipientCount = addErrorRecipients(message);
        if (naemailTo1!=null && naemailTo1.trim().length()>0){
          try {
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
          }        
        }
        if (naemailTo2!=null && naemailTo2.trim().length()>0){
          try {
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
          }       
          }
      }
      else {
         recipientCount = addErrorRecipients(message);
      }

      if (recipientCount==0) {
          Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: There were no e-mail recipients for a Report Genration error. No error e-mail will be sent!!!");
          return;
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILERROR BEFORE SUBJECT");
      // Set Subject: header field
      
      if (emailType==PICKUP) {
          message.setSubject("Oracle Report Server Unable to Generate Pickup Receipt. Contact STS/BAC.");
      } else if (emailType==DELIVERY) {
          message.setSubject("Oracle Report Server Unable to Generate Delivery Receipt. Contact STS/BAC.");
      }
      
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILERROR BEFORE MESSAGE HEADER");
      String sEmailType = "";
      if (emailType==PICKUP) {
          sEmailType = "PICKUP";
      } 
      else if (emailType==DELIVERY) {
          sEmailType = "DELIVERY";
      }
      else {
          sEmailType = "UNKNOWN EMAIL TYPE:"+emailType;
      }
      
      String msgHeader = "<html><body><b>URL:<a href='"+receiptURL + nuxrpd+"'>"+receiptURL + nuxrpd+"</a> ("+sEmailType+") Try#:"+retryCounter+" failed to generated and came back with the following response...<br /><br /> </body></html>"; 
         
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILERROR BEFORE SET MESSAGE:"+msgHeader+error);
      // Now set the actual message
      if (msgOverride==null) {
        message.setText(msgHeader+error, "utf-8", "html");
      }
      else {
        if (error==null) {
            message.setText(msgOverride, "utf-8", "html");
        }
        else {
            message.setText(msgOverride+msgHeader+error, "utf-8", "html");
        }
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILERROR AFTER SET MESSAGE");
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| EMAIL ERRORR MSG:" + message);
      // Send message
      Transport.send(message);
      System.out.println("Sent error message successfully....");
    }  catch (MessagingException mex) {
          mex.printStackTrace();
        Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, mex);
    } catch (UnsupportedEncodingException ex1) {
        Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex1);
    }    
  }    
 
 /*
  * New emailWarning
  */

 public void emailWarning(int emailType) {
    emailWarning(emailType, null);
 }
  
 public void emailWarning(int emailType,String msgOverride) {
      Properties props = new Properties();
      String smtpServer = properties.getProperty("smtpServer");
      props.setProperty("mail.smtp.host", smtpServer);
      Session session = Session.getDefaultInstance(props, null);
      int recipientCount = 0;
      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
         
      // Set To: header field of the header.
      if (testingMode) {
        recipientCount = addErrorRecipients(message);
        if (naemailTo1 != null && naemailTo1.trim().length() > 0) {
          try {
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo1, naemailNameTo1, e.getStackTrace(), e.getMessage());
          }
        }
        if (naemailTo2 != null && naemailTo2.trim().length() > 0) {
          try {
            message.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
            recipientCount++;
          } catch (Exception e) {
            addProblemEmailAddr(naemailTo2, naemailNameTo2, e.getStackTrace(), e.getMessage());
          }
        }
      }
      else {
         recipientCount = addErrorRecipients(message);
      }

      if (recipientCount==0) {
          Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: There were no e-mail recipients for a Report Genration error. No warning e-mail will be sent!!!");
          if (this.problemEmailAddrs!=null && this.problemEmailAddrs.size()>0) {
             this.emailWarning(emailType);
         }
          return;
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILWARNING BEFORE SUBJECT");
      // Set Subject: header field
      
      if (emailType==PICKUP) {
          message.setSubject("Pickup Receipt Recipient(s) Errors. Contact STS/BAC.");
      } else if (emailType==DELIVERY) {
          message.setSubject("Delivery Receipt Recipient(s) Errors. Contact STS/BAC.");
      }
      
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILWARNING BEFORE MESSAGE HEADER");
      String sEmailType = "";
      if (emailType==PICKUP) {
          sEmailType = "PICKUP";
      } 
      else if (emailType==DELIVERY) {
          sEmailType = "DELIVERY";
      }
      else {
          sEmailType = "UNKNOWN EMAIL TYPE:"+emailType;
      }
      
      String msgHeader = "<html><body><b>URL:<a href='"+receiptURL + nuxrpd+"'>"+receiptURL + nuxrpd+"</a> ("+sEmailType+") Try#:"+retryCounter+" was unable to generate e-mails to the following recipients..."+this.getProblemEmailString()+"</body></html>"; 
         
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILWARNING BEFORE SET MESSAGE:"+msgHeader);
      // Now set the actual message
      if (msgOverride==null) {
        message.setText(msgHeader, "utf-8", "html");
      }
      else {
         message.setText(msgHeader+msgOverride, "utf-8", "html");
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "!!!!EMAILWARNING AFTER SET MESSAGE");
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| EMAIL WARNING MSG:" + message);
      if (this.problemEmailAddrs!=null && this.problemEmailAddrs.size()>0) {
          this.emailWarning(emailType);
      }
      // Send message
      Transport.send(message);
      System.out.println("Sent warning message successfully....");
    }  catch (MessagingException mex) {
          mex.printStackTrace();
        Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, mex);
    } catch (UnsupportedEncodingException ex1) {
        Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex1);
    }    
  } 

  @Override
  public void run()
  {
      int returnStatus = -1;
      retryCounter = 0;
      switch (emailType) {
        case PICKUP:
          if (pickup==null) {
              Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: E-mail Receipt type was set to PICKUP but pickup object was NULL. No e-mail will be generated!!!");
          } 
          else {
              Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "Asynchronouly generating a Pickup E-mail Receipt ");
          }
          do {
            retryCounter++;
            returnStatus = sendPickupEmailReceipt(pickup);
            if (returnStatus != 0) {
              Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" +  "Pickup receipt generated a returnStatus="+returnStatus+". Will retry to generate the Pickup receipt after "+this.reportWaitInterval+" seconds.");
              try {
                Thread.sleep(this.reportWaitInterval*1000);
              } catch (InterruptedException ex) {
                Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
              }
            } 
          }
          while (returnStatus !=0 && retryCounter<=reportRetryLimit);
          break;
        case DELIVERY:
          if (delivery==null) {
              Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: E-mail Receipt type was set to DELIVERY but delivery object was NULL. No e-mail will be generated!!!");
          } 
          else {
              Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "Asynchronouly generating a Delivery E-mail Receipt ");
          }
          do {
            retryCounter++;
            returnStatus = sendDeliveryEmailReceipt(delivery);
            if (returnStatus != 0) {
              Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" +  "Delivery receipt generated a returnStatus="+returnStatus+". Will retry to generate the Delivery receipt after "+this.reportWaitInterval+" seconds.");
              try {
                Thread.sleep(this.reportWaitInterval*1000);
              } catch (InterruptedException ex) {
                Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
              }
            } 
          }
          while (returnStatus !=0 && retryCounter<=reportRetryLimit);
          // TODO
          // Need an E-mail if it could not generate after the max number of retries          
          break;
        default: 
          Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "**WARNING: E-mail Receipt type not set to PICKUP or DELIVERY. No e-mail will be generated!!!");
          break;
      }
  }
  
    private int addEmailSupervisors(MimeMessage msg) throws MessagingException, UnsupportedEncodingException, ClassNotFoundException {
    int cnt = 0;
    String curNaemailErrorTo = null;
    String curNameErrorTo = null;
    
    if (this.naemailErrorTo==null) {
      //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| addErrorRecipients NO RECIPIENTS");
      return cnt;
    }
    
    ArrayList<Employee> emailSupervisors = null;
    
    try {
      emailSupervisors = db.getEmailSupervisors(username);
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
    }
    
   //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| addErrorRecipients "+naemailErrorTo.length+" RECIPIENTS");
           
    for (int x=0;x<emailSupervisors.size();x++) {
       Employee currentEmailSupervisor = emailSupervisors.get(x);
       currentEmailSupervisor.setEmployeeNameOrder(currentEmailSupervisor.FIRST_MI_LAST_SUFFIX);

       curNameErrorTo = currentEmailSupervisor.getEmployeeName();
       curNaemailErrorTo = currentEmailSupervisor.getNaemail();
       
       System.out.println(x+": EMAIL:"+curNaemailErrorTo+" NAME:"+curNameErrorTo);
       try {            
           msg.addRecipient(Message.RecipientType.TO,
                new InternetAddress(curNaemailErrorTo, curNameErrorTo));  //naemailTo, naemployeeTo
         }
         catch (Exception e) {
            addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, e.getStackTrace(), e.getMessage());        
          }            
       cnt++;
    }
    return cnt;
   }    
  
  private int addDistributionRecipients(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
    int cnt = 0;
    
    if (this.naemailGenTo==null) {
      return cnt;
    }
    for (int x=0;x<naemailGenTo.length;x++) {
      try {
       msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailGenTo[x], getName(x, this.naemailGenNameTo) ));  //naemailTo, naemployeeTo
       cnt++;       
         }
      catch (Exception e) {
           addProblemEmailAddr(naemailGenTo[x], getName(x, this.naemailGenNameTo), e.getStackTrace(), e.getMessage());        
       }          
    }
    return cnt;    
  }
  
  private int addErrorRecipients(MimeMessage msg) throws MessagingException, UnsupportedEncodingException {
    int cnt = 0;
    String curNaemailErrorTo = null;
    String curNameErrorTo = null;
    //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| addErrorRecipients");
    
    if (this.naemailErrorTo==null) {
      //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| addErrorRecipients NO RECIPIENTS");
      return cnt;
    }
    
   //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| addErrorRecipients "+naemailErrorTo.length+" RECIPIENTS");
           
    for (int x=0;x<naemailErrorTo.length;x++) {
       curNaemailErrorTo = naemailErrorTo[x];
       curNameErrorTo = getName(x, this.naemailErrorNameTo);
       
       //Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "| EMAIL ERRORR TO:" + curNaemailErrorTo + " NAME:"+curNameErrorTo);
       try {      
       msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(curNaemailErrorTo, curNameErrorTo ));  //naemailTo, naemployeeTo
       cnt++;
         }
      catch (Exception e) {
           addProblemEmailAddr(curNaemailErrorTo, curNameErrorTo, e.getStackTrace(), e.getMessage());        
       }          
       
    }
    return cnt;
  }
  
  private String getName(int row, String[] nameList) {
    if (nameList==null||nameList.length<row) {
        return "";
    }
    else {
        return nvl(nameList[row], "");
    }
  }
  
  private String nvl(String value, String nullReturn) {
    if (value==null) {
        return nullReturn;
    }
    else {
        return value;
    }
  }
  
  public byte[] bytesFromUrlWithJavaIO(String fileUrl) throws MalformedURLException, IOException, ReportNotGeneratedException
  {
    return bytesFromUrlWithJavaIO(fileUrl, null);
  }

// Using Java IO
  public byte[] bytesFromUrlWithJavaIO(String fileUrl, String nafile)
          throws MalformedURLException, IOException, ReportNotGeneratedException
  {
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
          if (nafile!=null && nafile.trim().length()>0) {
            //System.out.println("PickupServlet.bytesFromUrlWithJavaIO writing file:" + nafile + nafileext);
            FileOutputStream fos = new FileOutputStream(nafile + nafileext);
            fos.write(returnBytes);
            fos.flush();
            fos.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
    } else {
      if (decoded.toUpperCase().contains("<HTML>") || decoded.toUpperCase().contains("<BODY>")) {
        nafileext = ".html";
        //decoded = insertTextInto(decoded, "src=\"^", );
        try {
          if (nafile!=null && nafile.trim().length()>0) {
            //System.out.println("PickupServlet.bytesFromUrlWithJavaIO writing file:" + nafile + nafileext);
            FileOutputStream fos = new FileOutputStream(nafile + nafileext);
            fos.write(returnBytes);
            fos.flush();
            fos.close();
          }
        } catch (Exception e) {
          e.printStackTrace();
        }
      } else {
        nafileext = ".???";
      }
      //System.out.println("PickupServlet.bytesFromUrlWithJavaIO writing file:" + nafile + nafileext);
      try {
        FileOutputStream fos = new FileOutputStream(nafile + nafileext);
        fos.write(returnBytes);
        fos.flush();
        fos.close();
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
    EmailRecord emailInformation = new EmailRecord();
    emailInformation.setNaemail(naemail);
    emailInformation.setNaemailName(naemailName);
    emailInformation.setErrorStackTrace(errorStackTrace);
    emailInformation.setErrorMessage(errorMessage);
    problemEmailAddrs.add(emailInformation);
  }
  
  public String getProblemEmailString() {
    StringBuffer returnString = new StringBuffer();
      if (problemEmailAddrs==null||problemEmailAddrs.size()==0) {
        return null;
      }
      else {
        for (int x=0;x<problemEmailAddrs.size();x++) {
            EmailRecord emailInformation = problemEmailAddrs.get(x);
            returnString.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;<b>Email:</b> ");
            returnString.append(emailInformation.getNaemail());
            returnString.append("&nbsp;&nbsp;&nbsp<b>Name:</b> ");
            returnString.append(emailInformation.getNaemailName());
            returnString.append("&nbsp;&nbsp;&nbsp<b>Error Message:</b> ");
            returnString.append(emailInformation.getErrorMessage());
            StackTraceElement[] errorStackTrace = emailInformation.getErrorStackTrace();
            if (errorStackTrace!=null) {
                for (int y=0;y<errorStackTrace.length;y++) {
                     returnString.append("&nbsp;&nbsp;&nbsp&nbsp;&nbsp;&nbsp");
                     returnString.append(errorStackTrace[y].toString());
                }
            }
           returnString.append("<br/>");            
        }
      }
      return returnString.toString();
  }  
}
