/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.server.EmailMoveReceipt;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Brian Heitner
 */

/*
 *  Handle E-mails was created as a possible solution if needed. 
 * If there is a problem e-mailing from multiple threads we can call 
 * HandleEmails in a thread and then handle e-mails sequentially. 
 * Currently, this code is not implemented.
 */
public class HandleEmails implements Runnable {
    Transaction pickup;
    String type; 
    HttpServletRequest request;
    String testingModeParam;
    DbConnect db;
    private static final Logger log = Logger.getLogger(HandleEmails.class.getName());    
            
    public HandleEmails(Transaction pickup, String type, HttpServletRequest request, String testingModeParam, DbConnect db) {
        this.pickup = pickup;
        this.type = type; 
        this.request = request;
        this.testingModeParam = testingModeParam;
        this.db = db;
    }
    
    public void run() {
  try {
        System.out.println("Before E-mail Receipt");
        HttpSession httpSession = request.getSession(false);
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");        
        try {
          EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "pickup" ,pickup);
        }
        catch (Exception e) {
            log.error(null, e);
        }

//        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: comment code below back in");
        System.out.println("RIGHT Before E-mail Receipt");
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Generating Email for Pickup Part");
        //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
        //Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
        //threadEmailMoveReceipt.start();
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: PickupPart Email Started");
        
        /*
         * If user is doing a pickup of a remote delivery, we need to also send the paperwork
         * for the remote delivery at the time if pickup. The remote delivery paperwork will be
         * printed and sent to the remote location for signature.
         * 
         */
        
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: *******pickup remote delivery commented out for now");        
       
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: *******pickup remote:"+pickup.isRemote()+", shiptype:"+pickup.getShipType()+", RemoteType:"+pickup.getRemoteType()+", Origin Remote:"+pickup.getOrigin().isRemote()+", Destination Remote:"+pickup.getDestination().isRemote()+", Dest City:"+pickup.getDestination().getAdcity());
        if (pickup.getRemoteType().equalsIgnoreCase("RDL")) {
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Generating Email for Remote Delivery Part");
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Generating Email for Remote Delivery Part");
            
            if (db==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email db is NULL!!");              
            }
            
            Transaction remoteDelivery = null;
            TransactionMapper transactionMapper = new TransactionMapper();
            try {
                remoteDelivery = transactionMapper.queryTransaction(db, pickup.getNuxrpd());
                if (remoteDelivery==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(a): Remote Delivery Part Email remoteDelivery==NULL!!");              
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            } 
                if (remoteDelivery==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(b): Remote Delivery Part Email remoteDelivery==NULL!!");              
                }
            //remoteDelivery = db.getDelivery(pickup.getNuxrpd()); 
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(b): Remote Delivery Part user:"+user+", pwd:"+pwd);                          
           try {
                EmailMoveReceipt emailRemoteDeliveryReceipt = new EmailMoveReceipt(request, user, pwd, "delivery" ,remoteDelivery);
           }
           catch (Exception e) {
               log.error(null, e);
           }
            //Thread threadEmailRemoteDeliveryReceipt = new Thread(emailRemoteDeliveryReceipt);
            //threadEmailRemoteDeliveryReceipt.start();
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email Started");
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email Started");
        }
        user = null;
        pwd = null;       
      } catch (Exception e) {
          e.printStackTrace();
          log.error(null, e);
      }        
        
    }
    
}
