/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.model;

import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.util.MapFormat;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.mail.internet.InternetAddress;
import gov.nysenate.inventory.exception.InvalidParameterException;
import gov.nysenate.inventory.exception.ParameterNotUsedException;

/**
 *
 * @author Brian Heitner
 */
public class EmailData
{

 private String message;
 private ArrayList<InternetAddress> recipients;
 private Map map = new HashMap(); 
 private DbConnect db;
 private String cdemail; 
 private boolean emailFoundInDBA = false;
 private ArrayList<String> validEmailParams = new ArrayList<String>();

 public EmailData(String cdemail) {
   this.db = new DbConnect();
   this.cdemail = cdemail;
   this.pullEmailInfoFromDatabase();
 }
 
 public EmailData(DbConnect db, String cdemail) {
   if (db==null) {
     this.db = new DbConnect();
   }
   else {
     this.db = db;
   }
   this.cdemail = cdemail;
   this.pullEmailInfoFromDatabase();
 }
  
  public void setMessage(String message) {
    this.message = message;
  }
  
  public String getMessage() {
    return this.message;
  }

  public String getFormattedMessage() {
    System.out.println("MAP SIZE:"+map.size());
    return MapFormat.format(this.message, map);
  }
  
  public void addRecipient(InternetAddress recipient) {
    if (recipients==null) {
      recipients = new  ArrayList<InternetAddress>();
    }
    recipients.add(recipient);
  }
  
  public void addRecipients(InternetAddress[] recipients) {
    for (int x=0;x<recipients.length;x++) {
      addRecipient(recipients[x]);
    }
  }
  
  public ArrayList<InternetAddress> getRecipients() {
     return this.recipients;
  }
  
  public void addValidParameter(String parameter) {
      if (this.validEmailParams.indexOf(parameter)==-1) {
        this.validEmailParams.add(parameter);
      }
  }
  
  public void resetValidParameters() throws ClassNotFoundException, SQLException {
    this.validEmailParams = db.pullEmailParams();
  }

  public void addRecipients(ArrayList<InternetAddress> recipients) {
      addRecipients((InternetAddress[]) recipients.toArray());
  }
  
  public void setRecipients(ArrayList<InternetAddress> recipients) {
    this.recipients = recipients;
  }
     
  public ArrayList<String> getValidEmailParams() {
      return this.validEmailParams;
  }
  
  public void put(Object key, Object value) throws InvalidParameterException, ParameterNotUsedException {
      if (this.validEmailParams == null || this.validEmailParams.indexOf(key)==-1) {
          throw new InvalidParameterException((String)key);
      }
      if (message==null) {
          throw new ParameterNotUsedException((String)key);
      }
      else {
        String skey = "{"+(String)key+"}";
        if (this.message.indexOf(skey)==-1) {
          throw new ParameterNotUsedException((String)key);
        }
      }
      map.put(key, value);
  }
  
  private void pullEmailInfoFromDatabase() {
   try {
     this.message = null;
     this.emailFoundInDBA = false;
     resetValidParameters();
     this.message = db.pullEmailMessage(cdemail);
     if (this.message!=null) {
        this.emailFoundInDBA = true;
     }
   } catch (ClassNotFoundException ex) {
     Logger.getLogger(EmailData.class.getName()).log(Level.WARNING, null, ex);
   } catch (SQLException ex) {
     Logger.getLogger(EmailData.class.getName()).log(Level.WARNING, null, ex);
   }
  }
  
}


