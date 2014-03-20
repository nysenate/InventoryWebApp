/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.exception;

/**
 *
 * @author senateuser
 */
public class BlankMessageException extends Exception
{
  private String msgAdd = "";
  public BlankMessageException() {
    super("Message was null or blank.");
  }
  public BlankMessageException(String msgAdd)  {
    super("Message was null or blank ("+msgAdd+").");
  }
     
}
