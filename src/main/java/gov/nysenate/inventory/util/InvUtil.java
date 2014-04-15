/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.util;

/**
 *
 * @author senateuser
 */
public class InvUtil
{

    public String stackTraceAsMsg (Exception e) {
        StringBuffer sb = new StringBuffer();
        StackTraceElement[] stackTrace = e.getStackTrace();
        if (stackTrace==null) {
            return "";
        }
        else {
            for (int x=0;x<stackTrace.length;x++) {
                sb.append("<br/>&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;&nbsp;");
                sb.append(stackTrace[x].toString());
            }
        }
        return sb.toString();
    }    
}


