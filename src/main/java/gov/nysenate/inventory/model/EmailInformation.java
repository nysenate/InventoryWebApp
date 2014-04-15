/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.model;

/**
 *
 * @author Brian Heitner
 */
public class EmailInformation
{
   private String naemail = null;
   private String naemailName = null;
   private String errorMessage = null;
   private StackTraceElement[] errorStackTrace = null;
  
   public void setNaemail(String naemail) {
       this.naemail = naemail;
   }
   
   public String getNaemail() {
     return this.naemail;
   }
   
   public void setNaemailName(String naemailName) {
       this.naemailName = naemailName;
   }
   
   public String getNaemailName() {
     return this.naemailName;
   }
   
   public void setErrorStackTrace(StackTraceElement[] errorStackTrace) {
      this.errorStackTrace = errorStackTrace;
   }

   public StackTraceElement[] getErrorStackTrace() {
      return this.errorStackTrace;
   }
   
   public void setErrorMessage(String errorMessage) {
       this.errorMessage = errorMessage;
   }
   
   public String getErrorMessage() {
     return this.errorMessage;
   }
}
