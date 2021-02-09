/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.util;

import gov.nysenate.inventory.dao.TransactionMapper;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.dao.DbConnect;
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
    DbConnect db = null;
    private static final Logger log = Logger.getLogger(HandleEmails.class.getName());
    private PrintWriter out = null;
    private HttpServletResponse response = null;

    public HandleEmails(Transaction trans, int currentTransType, HttpServletRequest request, HttpServletResponse response, DbConnect db) throws IOException {
        this.trans = trans;
        this.currentTransType = currentTransType;
        this.request = request;
        this.response = response;
        out = response.getWriter();
        this.db = db;
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

            if (sendEmailType == ALLEMAILS || sendEmailType == PICKUPEMAIL) {
                EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", trans, calledBy);

                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing pickup email");
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") preparing pickup email");
                //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
                Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
                threadEmailMoveReceipt.start();
                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") pickup email started");
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE HandleEmails: sendPickupTransactionEmails("+calledBy+") pickup email started");

            }

            /*
             * If user is doing a pickup of a remote delivery, we need to also send the paperwork
             * for the remote delivery at the time if pickup. The remote delivery paperwork will be
             * printed and sent to the remote location for signature.
             * 
             */

            if (trans.getRemoteType().equalsIgnoreCase("RDL") && (sendEmailType == ALLEMAILS || sendEmailType == DELIVERYEMAIL)) {

                if (db == null) {
                }

                Transaction remoteDelivery = null;
                TransactionMapper transactionMapper = new TransactionMapper();
                try {
                    remoteDelivery = transactionMapper.queryTransaction(db, trans.getNuxrpd());
                } catch (Exception e) {
                    e.printStackTrace();
                    log.error("Problem with querying remoteDelivery.[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
                }
                EmailMoveReceipt emailRemoteDeliveryReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", remoteDelivery);
                Thread threadEmailRemoteDeliveryReceipt = new Thread(emailRemoteDeliveryReceipt);
                threadEmailRemoteDeliveryReceipt.start();
            }
            user = null;
            pwd = null;

            out.println("Database updated successfully");
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
            out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
            log.error("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
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

                if (sendEmailType == ALLEMAILS || sendEmailType == DELIVERYEMAIL) {

                    EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", trans, calledBy);
                    Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
                    threadEmailMoveReceipt.start();
                }

                out.println("Database updated successfully");

                if (trans.getRemoteType().equalsIgnoreCase("RPK") && trans.getShipType() != null && trans.getShipType().trim().length() > 0 && (sendEmailType == ALLEMAILS || sendEmailType == PICKUPEMAIL)) {

                    Transaction remotePickup = null;
                    TransactionMapper transactionMapper = new TransactionMapper();
                    try {
                        remotePickup = transactionMapper.queryTransaction(db, trans.getNuxrpd());
                    } catch (Exception e) {
                        e.printStackTrace();
                    }
                    EmailMoveReceipt emailRemotePickupReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", remotePickup);
                    Thread threadEmailRemotePickupReceipt = new Thread(emailRemotePickupReceipt);
                    threadEmailRemotePickupReceipt.start();
                }
            } catch (Exception e) {
                e.printStackTrace();
                System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
                out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
                log.error("Problem with remotePickup. (E-MAIL ERROR#:\" + emailReceiptStatus + \"-2). [" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
            }
        }
    }
}
