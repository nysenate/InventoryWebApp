/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

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
  

public int sendPickupEmail(PickupServlet pickupServlet, Pickup pickup)
  {
    return sendPickupEmail(pickupServlet, pickup.getNaPickupBy(), pickup.getOrigin().getCdLoc(), pickup.getDestination().getCdLoc(), pickup.getNuxrpd());
  }

  @SuppressWarnings("empty-statement")
  public int sendPickupEmail(PickupServlet pickupServlet, String NAPICKUPBY, String originLocation, String destinationLocation, final int nuxrpd)
  {
    String naemployeeTo = "";
    String msgBody = "";
    byte[] attachment = null;
    int returnStatus = 0;
    
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
    try {
      properties.load(in);
    } catch (IOException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
      returnStatus = 1;
    }

    String smtpServer = properties.getProperty("smtpServer");
    final String pickupReceiptURL = properties.getProperty("pickupReceiptURL");

    Properties props = new Properties();
    props.setProperty("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props, null);
    StringBuilder sb = new StringBuilder();
    properties.getProperty("pickupEmailTo2");
    pickupServlet.naemailFrom = null; 
    pickupServlet.naemailFrom = properties.getProperty("pickupEmailFrom");
    pickupServlet.naemailNameFrom = null; 
    pickupServlet.naemailNameFrom = properties.getProperty("pickupEmailNameFrom");
    
    try {
        pickupServlet.naemailTo1 = properties.getProperty("pickupEmailTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
       
    try {
        pickupServlet.naemailNameTo1 = properties.getProperty("pickupEmailNameTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }

    try {
        pickupServlet.naemailTo2 = properties.getProperty("pickupEmailTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
        pickupServlet.naemailNameTo1 = properties.getProperty("pickupEmailNameTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
      pickupServlet.testingModeProperty =  properties.getProperty("testingMode").toUpperCase();
     }
    catch (NullPointerException e) {
      // Could not find the Testing Mode Property so assume that we are in testing mode, this will
      // at least alert someone if no one is getting receipts..
      pickupServlet.testingModeProperty = "TRUE";
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****PARAMETER testingMode was NOT FOUND  TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ");
    }
    catch (Exception e) {
      pickupServlet.testingModeProperty = "TRUE";
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "***WARNING: Exception occured when trying to find testingMode Property ("+e.getMessage()+") TESTING MODE WILL BE DEFAULTED TO TRUE Pickup.processRequest ");
    }
    
    // Get the employee who signed the Release 
    pickupServlet.currentEmployee = pickupServlet.db.getEmployeeWhoSigned(pickupServlet.pickup.getNuxrRelSign(), false, pickupServlet.userFallback);
    pickupServlet.currentEmployee.setEmployeeNameOrder(pickupServlet.currentEmployee.FIRST_MI_LAST_SUFFIX);
    
    boolean testingMode = false;
    
    /*
     *  If either E-mail to field is filled, then the server is meant to e-mail that specific user
     * instead of the user that should be e-mailed. This would mean that the server is in testing mode.
     */
    
    if (pickupServlet.testingModeParam!=null && pickupServlet.testingModeParam.trim().length()>0) {
      if (pickupServlet.testingModeParam.toUpperCase().indexOf("T")>-1) {
        testingMode = true;
        Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "****testingModeParam has a T, so Testing Mode is set to TRUE Pickup.processRequest ");
      }
      else {
        testingMode = false;
      }
    }
    else if (pickupServlet.testingModeProperty==null || pickupServlet.testingModeProperty.toUpperCase().contains("T")) {
      testingMode = true;
      Logger.getLogger(PickupServlet.class.getName()).info(pickupServlet.db.ipAddr + "|" + "***Testing Mode is set to TRUE Pickup.processRequest ");
    }
    
    if (testingMode) {
      sb.append("<b>TESTINGMODE</b>: E-mail under normal circumstances would have been sent to:");
      sb.append(pickupServlet.currentEmployee.getNaemail());
      sb.append("<br /><br />");
    }
    
    String error = null;
    sb.append("Dear ");
    sb.append(pickupServlet.currentEmployee.getEmployeeName());
    sb.append(",");
    sb.append("<br/><br/> Equipment was picked up by <b>" + NAPICKUPBY + "</b> from <b>" + originLocation + "</b> with the destination of <b>" + destinationLocation + "</b>");
    sb.append("<br /><br />To view Equipment Pickup Receipt, please open the PDF attachment included in this e-mail.");
  
    try {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE ATTACHMENT");
      attachment = bytesFromUrlWithJavaIO(pickupReceiptURL + nuxrpd); // +"&destype=CACHE&desformat=PDF
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER ATTACHMENT");

      // Attachment needs to be checked to ensure that there were no issues with the Reports Server
      // and the PDF was generated properly. Otherwise the PDF sent is garbage.  We need to e-mail
      // STSBAC and possibly others that an issue occured and/or try to generate the PDF again
      
      //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
      System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((attachment.length) / 1024.0) + "KB");
    } catch (MalformedURLException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    } catch (ReportNotGeneratedException ex) {
      if (returnStatus == 0) {
        returnStatus = 2;
      }
      Logger.getLogger(PickupServlet.class.getName()).log(Level.WARNING, "There was an issue with Oracle Reports Server. Please contact STS/BAC.", ex);    
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR ReportNotGeneratedException1");
      emailError(pickupServlet);
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE AFTER E-MAIL ERROR ReportNotGeneratedException1");
     }
    if (attachment == null) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 1");
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****ATTACHMENT was null Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 4;
      }
      emailError(pickupServlet);

      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment == null 2");
    }
    else if (attachment.length==0) {
      //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ERROR attachment.length==0 1");
      Logger.getLogger(PickupServlet.class.getName()).warning(pickupServlet.db.ipAddr + "|" + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
      if (returnStatus == 0) {
        returnStatus = 5;
      }
      emailError(pickupServlet);
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
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream()");
            
            return new ByteArrayInputStream(bytesFromUrlWithJavaIO(pickupReceiptURL + nuxrpd));
          }
          catch (ReportNotGeneratedException e) {
            //System.out.println("-=-=-=-=-=-=-=-=-=TRACE BEFORE E-MAIL ATTACHMENT getInputStream() ReportNotGeneratedException");
            Logger.getLogger(PickupServlet.class.getName()).log(Level.WARNING, "Oracle Reports Server failed to generate a PDF Report for the Pickup Receipt. Please contact STS/BAC.", e);                  
            return new ByteArrayInputStream(new byte[0]);
          }
        }

        @Override
        public String getName()
        {
          return "pickup_receipt.pdf";
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
          return null;
        }
      }));
    } catch (MessagingException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    try {
      in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
      properties.load(in);
 
      msgBody = sb.toString();
      MimeMessage msg = new MimeMessage(session);
      System.out.println("EMAILING FROM:" + pickupServlet.naemailFrom + ":" + pickupServlet.naemailNameFrom);
      msg.setFrom(new InternetAddress(pickupServlet.naemailFrom, pickupServlet.naemailNameFrom));
      if (testingMode) {
          System.out.println("TESTINGMODE Would have sent (BUT DID NOT) TO:" + pickupServlet.currentEmployee.getNaemail() + " (" + pickupServlet.currentEmployee.getEmployeeName()+")");
        if (pickupServlet.naemailTo1!=null && pickupServlet.naemailTo1.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + pickupServlet.naemailTo1 + ":" + pickupServlet.naemailNameTo1);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(pickupServlet.naemailTo1, pickupServlet.naemailNameTo1));  //naemailTo, naemployeeTo
        }
        if (pickupServlet.naemailTo2!=null && pickupServlet.naemailTo2.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + pickupServlet.naemailTo2 + ":" + pickupServlet.naemailNameTo2);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(pickupServlet.naemailTo2, pickupServlet.naemailNameTo2));  //naemailTo, naemployeeTo
        }
      }
      else {
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(pickupServlet.currentEmployee.getNaemail(), pickupServlet.currentEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
      }
      
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
  
 public void emailError(PickupServlet pickupServlet) {
      Properties props = new Properties();
      String smtpServer = properties.getProperty("smtpServer");
      props.setProperty("mail.smtp.host", smtpServer);
      Session session = Session.getDefaultInstance(props, null);
      try{
         // Create a default MimeMessage object.
         MimeMessage message = new MimeMessage(session);

         // Set From: header field of the header.
         message.setFrom(new InternetAddress(pickupServlet.naemailFrom, pickupServlet.naemailNameFrom));

         // Set To: header field of the header.
        if (pickupServlet.naemailTo1!=null && pickupServlet.naemailTo1.trim().length()>0){
          message.addRecipient(Message.RecipientType.TO,
            new InternetAddress(pickupServlet.naemailTo1, pickupServlet.naemailNameTo1));  //naemailTo, naemployeeTo
        }
        if (pickupServlet.naemailTo2!=null && pickupServlet.naemailTo2.trim().length()>0){
          message.addRecipient(Message.RecipientType.TO,
            new InternetAddress(pickupServlet.naemailTo2, pickupServlet.naemailNameTo2));  //naemailTo, naemployeeTo
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
        Logger.getLogger(PickupServlet.class.getName()).log(Level.WARNING, null, ex1);
      }    
  }  
}
