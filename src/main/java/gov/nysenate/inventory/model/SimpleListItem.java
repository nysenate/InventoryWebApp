/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.model;

import com.google.gson.annotations.Expose;

/**
 *
 * @author senateuser
 */
public class SimpleListItem
{
  @Expose String natype;
  @Expose String navalue;

  public void setNatype (String natype){
    this.natype = natype;
  }
 
  public void setNavalue(String navalue) {
    this.natype = natype;
  }
  
  public String getNatype() {
    return natype;
  }
  
  public String getNavalue() {
    return navalue;
  }  
  
}
