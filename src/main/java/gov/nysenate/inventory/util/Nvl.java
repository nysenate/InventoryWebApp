package gov.nysenate.inventory.util;

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
