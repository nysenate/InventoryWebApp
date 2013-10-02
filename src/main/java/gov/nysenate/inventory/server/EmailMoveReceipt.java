/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.Delivery;
import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.model.ReportNotGeneratedException;
import static gov.nysenate.inventory.server.PickupServlet.bytesFromUrlWithJavaIO;
import static gov.nysenate.inventory.server.PickupServlet.error;
import java.io.ByteArrayInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.UnsupportedEncodingException;
import java.net.MalformedURLException;
import java.sql.SQLException;
import java.text.SimpleDateFormat;
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

/**
 *
 * @author senateuser
 */
public class EmailMoveReceipt
{
  Properties properties = new Properties();
  InputStream in;
  String receiptPath = "C:\\";
  String dbaUrl = "";
  String serverOS = "Windows"; // Default to Windows OS
  String pathDelimeter = "\\";   
  private String naemailFrom = null; 
  private String naemailNameFrom = null; 
  private String naemailTo1 = null;
  private String naemailNameTo1 = null;
  private String naemailTo2 = null;
  private String naemailNameTo2 = null;
  private String testingModeProperty = null;
  private String testingModeParam = null;
  private Employee signingEmployee = null;
  private String username = null;
  private String password = null;
  private DbConnect db = null;
  private String userFallback = null;
  final int PICKUP = -101, DELIVERY = -102;
  private Pickup pickup = null;
  private Delivery delivery = null;
  private Date dtreceipt = new Date();
  private String receiptFilename = null;
  private String napickupbyName = null;
  private String pickupAddress = null;  
  private String nadeliverbyName = null;
  private String deliverAddress = null;
  private Employee pickupEmployee;
  private Employee deliverEmployee;
  
  public EmailMoveReceipt(String username, String password, Pickup pickup) {
        this.username = username;
        this.password = password;
        this.pickup = pickup;
        userFallback = username; // userfallback is not really being used
                                 // but it needs to be passed so it is being
                                 // set to username (which should be set)
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
            //System.out.println ("EmailMoveReceipt Receipt Location:"+receiptPath);
            dbaUrl = properties.getProperty("dbaUrl");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
}

public EmailMoveReceipt(String username, String password, Delivery delivery) {
        this.username = username;
        this.password = password;
        this.delivery = delivery;
        userFallback = username; // userfallback is not really being used
                                 // but it needs to be passed so it is being
                                 // set to username (which should be set)
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
            //System.out.println ("EmailMoveReceipt Receipt Location:"+receiptPath);
            dbaUrl = properties.getProperty("dbaUrl");
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(EmailMoveReceipt.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }        
}
  
  /*
   * Pickup Specific function serves as the initial setup code for the sendEmail(int emailType)
   * which handles both Pickup and Delivery
   */
  
  
  public int sendEmailReceipt(Pickup pickup)
  {
    int emailType = PICKUP;
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
    }
    try {
      db.setLocationInfo(pickup.getDestination());
      deliverAddress = pickup.getDestination().getAdstreet1()+" "+pickup.getDestination().getAdcity()+", "+pickup.getDestination().getAdstate()+", "+pickup.getDestination().getAdzipcode();
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
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
    }
    int emailReturnStatus = sendEmailReceipt(emailType);
    
    return emailReturnStatus;
  }  
  
  /*
   * Delivery Specific function serves as the initial setup code for the sendEmailReceipt(int emailType)
   * which handles both Pickup and Delivery
   */
  
  public int sendEmailReceipt(Delivery delivery)
  {
    int emailType = DELIVERY;
    this.delivery = delivery;
    
    try {
      db.setLocationInfo(delivery.getDestination());
      deliverAddress = delivery.getDestination().getAdstreet1()+" "+delivery.getDestination().getAdcity()+", "+delivery.getDestination().getAdstate()+", "+delivery.getDestination().getAdzipcode();
    } catch (SQLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex);
    }

 
    // Get the employee who signed the Release 
    signingEmployee = db.getEmployeeWhoSigned(delivery.getNuxrAccptSign(), false, userFallback);
    signingEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
    
    // Get the employee who picked up the items
    try {
      deliverEmployee  = db.getEmployee(delivery.getNadeliverby());
      deliverEmployee.setEmployeeNameOrder(signingEmployee.FIRST_MI_LAST_SUFFIX);
      this.nadeliverbyName = deliverEmployee.getEmployeeName().trim();
    }
    catch (SQLException sqle) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "***WARNING: Exception occured when trying to get Pickup Employee for (USER:"+pickup.getNapickupby()+") ("+sqle.getMessage()+")");
      pickupEmployee = new Employee();
      this.napickupbyName = "N/A";
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
      
    StringBuilder sb = new StringBuilder();
    boolean testingMode = false;
    byte[] attachment = null;
    String msgBody = "";
    receiptFilename = nuxrpd+"_"+formatDate(dtreceipt, "yyMMddhh24mmss");
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

    Properties props = new Properties();
    props.setProperty("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props, null);
 
    properties.getProperty("pickupEmailTo2");
    naemailFrom = null; 
    naemailFrom = properties.getProperty("pickupEmailFrom");
    naemailNameFrom = null; 
    naemailNameFrom = properties.getProperty("pickupEmailNameFrom");
    
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
    
    if (testingModeParam!=null && testingModeParam.trim().length()>0) {
      if (testingModeParam.toUpperCase().indexOf("T")>-1) {
        testingMode = true;
        Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "****testingModeParam has a T, so Testing Mode is set to TRUE Pickup.processRequest ");
      }
      else {
        testingMode = false;
      }
    }
    else if (testingModeProperty==null || testingModeProperty.toUpperCase().contains("T")) {
      testingMode = true;
      Logger.getLogger(EmailMoveReceipt.class.getName()).info(db.ipAddr + "|" + "***Testing Mode is set to TRUE Pickup.processRequest ");
    }
    
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
      sb.append("picked ");
    }
    sb.append("up by ");
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
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE FILE TO WRITE:"+receiptPath+nuxrpd+"_"+formatDate(new Date(), "yyMMddhh24mmss"));
       // If the Attachment does not return a pdf, then it will be null since it expects a PDF, so we can tag on .pdf as a filename
      attachment = bytesFromUrlWithJavaIO(receiptURL + nuxrpd, receiptPath+receiptFilename); // +"&destype=CACHE&desformat=PDF
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER GETTING ATTACHMENT");

      // Attachment needs to be checked to ensure that there were no issues with the Reports Server
      // and the PDF was generated properly. Otherwise the PDF sent is garbage.  We need to e-mail
      // STSBAC and possibly others that an issue occured and/or try to generate the PDF again
      
      //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
      //System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((attachment.length) / 1024.0) + "KB");
    } catch (MalformedURLException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ReportNotGeneratedException ex) {
      if (returnStatus == 0) {
        returnStatus = 2;
      }
      Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, "There was an issue with Oracle Reports Server. Please contact STS/BAC.", ex);    
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR ReportNotGeneratedException1");
      emailError(emailType);
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR ReportNotGeneratedException1");
     }
    if (attachment == null) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 1");
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was null Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 4;
      }
      emailError(emailType);

      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 2");
    }
    else if (attachment.length==0) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment.length==0 1");
      Logger.getLogger(EmailMoveReceipt.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 5;
      }
      emailError(emailType);
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR attachment.length==0 1");
    }
    
    //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT");
    MimeMultipart mimeMultipart = new MimeMultipart();
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
            
            return new ByteArrayInputStream(bytesFromUrlWithJavaIO(receiptURL + nuxrpd));
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

    try {
      in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
      properties.load(in);
 
      msgBody = sb.toString();
      MimeMessage msg = new MimeMessage(session);
      //System.out.println("EMAILING FROM:" + naemailFrom + ":" + naemailNameFrom);
      msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
      if (testingMode) {
          System.out.println("TESTINGMODE Would have sent (BUT DID NOT) TO:" + signingEmployee.getNaemail() + " (" + signingEmployee.getEmployeeName()+")");
        if (naemailTo1!=null && naemailTo1.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + naemailTo1 + ":" + naemailNameTo1);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
        }
        if (naemailTo2!=null && naemailTo2.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + naemailTo2 + ":" + naemailNameTo2);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
        }
      }
      else {
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(signingEmployee.getNaemail(), signingEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
      }
      
      //System.out.println("EMAILING BEFORE SUBJECT");
      msg.setSubject("Equipment Pickup Receipt");
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
              Transport.send(msg);
              //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT AFTER SENDING E-MAIL");
          }
      }
      System.out.println("E-mail sent with no errors.");

    } catch (AddressException e) {
      if (returnStatus == 0) {
        returnStatus = 10;
      }
      
      e.printStackTrace();
    } catch (MessagingException e) {
      if (returnStatus == 0) {
        returnStatus = 11;
      }
      e.printStackTrace();
    } catch (Exception e) {
      if (returnStatus == 0) {
        returnStatus = 12;
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
  
 /*
  * New EmailError
  */
  
 public void emailError(int emailType) {
   
      Properties props = new Properties();
      String smtpServer = properties.getProperty("smtpServer");
      props.setProperty("mail.smtp.host", smtpServer);
      Session session = Session.getDefaultInstance(props, null);
      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));

         // Set To: header field of the header.
        if (naemailTo1!=null && naemailTo1.trim().length()>0){
          message.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
        }
        if (naemailTo2!=null && naemailTo2.trim().length()>0){
          message.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
          }

         // Set Subject: header field
         message.setSubject("Oracle Report Server Unable to Generate Pickup Receipt. Contact STS/BAC.");

         // Now set the actual message
         message.setText(error, "utf-8", "html");

         // Send message
         Transport.send(message);
         System.out.println("Sent error message successfully....");
      }catch (MessagingException mex) {
         mex.printStackTrace();
      } catch (UnsupportedEncodingException ex1) {
        Logger.getLogger(EmailMoveReceipt.class.getName()).log(Level.WARNING, null, ex1);
      }    
  }    
  

}
