/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.server.EmailMoveReceipt;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Brian Heitner
 */

/*
 *  Handle E-mails was created as a possible solution if needed. 
 */
public class HandleEmails {

    Transaction trans = null;
    public static final int PICKUPTRANSACTION = 101, DELIVERYTRANSACTION = 102;
    public static final int ALLEMAILS = 201, PICKUPEMAIL = 202, DELIVERYEMAIL = 203;    
    int currentTransType = PICKUPTRANSACTION;
    HttpServletRequest request = null;
    boolean testingModeParam = false;
    DbConnect db = null;
    private static final Logger log = Logger.getLogger(HandleEmails.class.getName());
    private PrintWriter out = null;
    private HttpServletResponse response = null;

    public HandleEmails(Transaction trans, int currentTransType, HttpServletRequest request, HttpServletResponse response, boolean testingModeParam, DbConnect db) throws IOException {
        this.trans = trans;
        this.currentTransType = currentTransType;
        this.request = request;
        this.response = response;
        out = response.getWriter();
        this.testingModeParam = testingModeParam;
        this.db = db;
        switch (currentTransType) {
            case PICKUPTRANSACTION:
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from PICKUPTRANSACTION");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from PICKUPTRANSACTION");
                break;                
            case DELIVERYTRANSACTION:
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from DELIVERYTRANSACTION");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from DELIVERYTRANSACTION");
                break;
            default: 
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from UNKNOWN:"+currentTransType);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: initialized from UNKNOWN:"+currentTransType);
        }
    }

   public int sendEmails() {
       return sendEmails("", ALLEMAILS);
   }
   
   public int sendEmails(String calledBy) {
       return sendEmails(calledBy, ALLEMAILS);
   }

   public int sendEmails(int sendEmailType) {
       return sendEmails("", sendEmailType);
   }
   
   public int sendEmails(String calledBy, int sendEmailType) {
        switch (currentTransType) {
            case PICKUPTRANSACTION:
                sendPickupTransactionEmails(calledBy, sendEmailType);
                return HttpServletResponse.SC_OK;
            case DELIVERYTRANSACTION:
                sendDeliveryTransactionEmails(calledBy, sendEmailType);
                return HttpServletResponse.SC_OK;
            default:
                return HttpServletResponse.SC_BAD_REQUEST;
        }
    }

    public void sendPickupTransactionEmails() {
        sendPickupTransactionEmails("", ALLEMAILS);
    }
    
    public void sendPickupTransactionEmails(String calledBy) {
        sendPickupTransactionEmails(calledBy, ALLEMAILS);
    }
    
    public void sendPickupTransactionEmails(int sendEmailType) {
        sendPickupTransactionEmails("", sendEmailType);
    }
    
    public void sendPickupTransactionEmails(String calledBy, int sendEmailType) {
        int emailReceiptStatus = 0;
        try {
            HttpSession httpSession = request.getSession(false);
            String user = (String) httpSession.getAttribute("user");
            String pwd = (String) httpSession.getAttribute("pwd");

           if (sendEmailType==ALLEMAILS||sendEmailType==PICKUPEMAIL) {
           EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", trans, calledBy);

            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing pickup email");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing pickup email");
            //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
            Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
            threadEmailMoveReceipt.start();
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") pickup email started");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") pickup email started");

           }
           
            /*
             * If user is doing a pickup of a remote delivery, we need to also send the paperwork
             * for the remote delivery at the time if pickup. The remote delivery paperwork will be
             * printed and sent to the remote location for signature.
             * 
             */


            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote type:"+trans.getRemoteType());
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote type:"+trans.getRemoteType());
            if (trans.getRemoteType().equalsIgnoreCase("RDL") && (sendEmailType==ALLEMAILS||sendEmailType==DELIVERYEMAIL)) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote type:"+trans.getRemoteType()+" Generating email for Remote Delivery");
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote type:"+trans.getRemoteType()+" Generating email for Remote Delivery");

                if (db == null) {
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Remote Delivery Part Email db is NULL!!");
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Remote Delivery Part Email db is NULL!!");
               }

                Transaction remoteDelivery = null;
                TransactionMapper transactionMapper = new TransactionMapper();
                try {
                    remoteDelivery = transactionMapper.queryTransaction(db, trans.getNuxrpd());
                    if (remoteDelivery == null) {
                        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(a): Remote Delivery Part Email remoteDelivery==NULL!!");
                        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(a): Remote Delivery Part Email remoteDelivery==NULL!!");
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
                if (remoteDelivery == null) {
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(b): Remote Delivery Part Email remoteDelivery==NULL!!");
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(b): Remote Delivery Part Email remoteDelivery==NULL!!");
                }

                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing remote delivery email");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing remote delivery email");
                
                //remoteDelivery = db.getDelivery(pickup.getNuxrpd()); 
                EmailMoveReceipt emailRemoteDeliveryReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", remoteDelivery);
                Thread threadEmailRemoteDeliveryReceipt = new Thread(emailRemoteDeliveryReceipt);
                threadEmailRemoteDeliveryReceipt.start();
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote delivery email started");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") remote delivery email started");
            }
            user = null;
            pwd = null;
            //System.out.println("emailReceiptStatus:" + emailReceiptStatus);

//        if (emailReceiptStatus == 0) {
            //System.out.println("Database updated successfully");
            out.println("Database updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
            out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
        }

    }

    public void sendDeliveryTransactionEmails() {
        sendDeliveryTransactionEmails("", ALLEMAILS);
    }

    public void sendDeliveryTransactionEmails(String calledBy) {
        sendDeliveryTransactionEmails(calledBy, ALLEMAILS);
    }
    
    public void sendDeliveryTransactionEmails(int sendEmailType) {
        sendDeliveryTransactionEmails("", sendEmailType);
    }
    
    public void sendDeliveryTransactionEmails(String calledBy, int sendEmailType) {
        {
            int emailReceiptStatus = 0;
            try {
                HttpSession httpSession = request.getSession(false);
                String user = (String) httpSession.getAttribute("user");
                String pwd = (String) httpSession.getAttribute("pwd");
                
                if (sendEmailType==ALLEMAILS||sendEmailType==DELIVERYEMAIL) {

                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing delivery email");
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing delivery email");
                
                    EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", trans, calledBy);
                    Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
                    threadEmailMoveReceipt.start();
                }

                //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(delivery);
                //if (emailReceiptStatus==0) {
                out.println("Database updated successfully");

                if (trans.getRemoteType().equalsIgnoreCase("RPK") && trans.getShipType() != null && trans.getShipType().trim().length() > 0 && (sendEmailType==ALLEMAILS||sendEmailType==PICKUPEMAIL)) {
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing remote pickup email with verification info");
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing remote pickup email with verification info");

                    if (db == null) {
                        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Remote Pickup Part Email db is NULL!!");
                        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: Remote Pickup Part Email db is NULL!!");
                    }

                    Transaction remotePickup = null;
                    TransactionMapper transactionMapper = new TransactionMapper();
                    try {
                        remotePickup = transactionMapper.queryTransaction(db, trans.getNuxrpd());
                        if (remotePickup == null) {
                            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(a): Remote Pickup Part Email remotePickup==NULL!!");
                            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(a): Remote Pickup Part Email remotePickup==NULL!!");
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    if (remotePickup == null) {
                        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(b): Remote Pickup Part Email remotePickup==NULL!!");
                        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails(b): Remote Pickup Part Email remotePickup==NULL!!");
                    }
                    //remoteDelivery = db.getDelivery(pickup.getNuxrpd()); 
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing pickup email with verification info");
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") preparing pickup email with verification info");
                    EmailMoveReceipt emailRemotePickupReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", remotePickup);
                    Thread threadEmailRemotePickupReceipt = new Thread(emailRemotePickupReceipt);
                    threadEmailRemotePickupReceipt.start();
                    System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") pickup email with verification info started");
                    log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendDeliveryTransactionEmails("+calledBy+") pickup email with verification info started");
                }
                /*}
                 else {
                 out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:"+emailReceiptStatus+").");
                 }*/
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
                out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
            }
        }
    }
}
