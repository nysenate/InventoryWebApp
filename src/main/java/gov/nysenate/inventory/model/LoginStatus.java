/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.model;

import com.google.gson.annotations.Expose;
import com.google.gson.annotations.SerializedName;
import java.util.Date;

/**
 *
 * @author Brian Heitner
 */
public class LoginStatus
{
 public final int VALID = 1; // Username/Password have been validated, password is not going to expire soon
 public final int PASSWORD_EXPIRED = 2; // Username/Password have been validated but password expired
 public final int PASSWORD_EXPIRES_SOON = 3; // Username/Password have been validated but password will expire soon
 public final int NO_ACCESS = 4;  // Username/Password have been validated but user does not have access to App.
 public final int PASSWORD_RULE_FAILURE = 5; // Password fails the rules setup for valid passwords
 public final int INVALID_USERNAME_OR_PASSWORD = 99;  // Invalid Username and/or Password
 public final int INVALID = 100;  // Invalid Username/password and the default value, covers Oracle Generic Login Errors
  
 @Expose private String nauser = "";
 @Expose private String destatus = "";
 @Expose private String cdseclevel = "";
 @Expose private int nustatus = INVALID;
 @Expose private int sqlErrorCode = -1;
 @Expose private Date dtpasswdexp = null; 
 @Expose private boolean usernamePasswordValid = false;
   
  public void setNauser(String nauser) {
    this.nauser = nauser;
  }
  
  public String getNauser() {
     return this.nauser;
  }

  public void setDestatus(String destatus) {
    this.destatus = destatus;
  }
  
  public String getDestatus() {
     return this.destatus;
  }
  
  public void setNustatus(int nustatus) {
    this.nustatus = nustatus;
    switch (this.nustatus) {
      case INVALID:
      case INVALID_USERNAME_OR_PASSWORD:
        this.usernamePasswordValid = false;
        break;
      default:
        this.usernamePasswordValid = true;
        break;
    }
  }
  
  public int getNustatus() {
     return this.nustatus;
  }

  public void setDtpasswdexp(Date dtpasswdexp) {
    this.dtpasswdexp = dtpasswdexp;
  }
  
  public Date getDtpasswdexp() {
     return this.dtpasswdexp;
  }
  
  public void setCdseclevel(String cdseclevel) {
    this.cdseclevel = cdseclevel;
  }
  
  public String getCdseclevel() {
     return this.cdseclevel;
  } 

  public void setSQLErrorCode(int sqlErrorCode) {
    this.sqlErrorCode = sqlErrorCode;
  }
  
  public int getSQLErrorCode() {
     return this.sqlErrorCode;
  }
  
  public boolean isUsernamePasswordValid () {
      return this.usernamePasswordValid;
  }  
  
}
