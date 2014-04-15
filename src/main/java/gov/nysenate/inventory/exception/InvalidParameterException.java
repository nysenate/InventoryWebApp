/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.exception;

/**
 *
 * @author senateuser
 */
public class InvalidParameterException extends Exception
{
 String parameter;
  public InvalidParameterException(String parameter) {
    super(parameter+" is Invalid.");
    this.parameter = parameter;
  }
  
  public String getParameter(){
     return this.parameter;
  }
}
