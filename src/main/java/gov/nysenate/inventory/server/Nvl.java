/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

/**
 *
 * @author Heitner
 */
public class Nvl {
    
    public String value (String val, String returnIfNull) {
        if (val==null) {
            return returnIfNull;    
        }
        else {
            return val;    
            
        }
        
    }
    
}
