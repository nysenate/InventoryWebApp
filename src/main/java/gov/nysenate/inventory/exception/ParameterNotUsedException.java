/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.exception;

/**
 *
 * @author senateuser
 */
public class ParameterNotUsedException extends Exception
{
  String parameter;
  public ParameterNotUsedException(String parameter) {
    super(parameter+" was not used.");
    this.parameter = parameter;
  }
  
  public String getParameter(){
     return this.parameter;
  }
  
}
