package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.TransactionMapper;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HandleEmails;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 *
 * @author Patil
 */
@WebServlet(name = "DeliveryConfirmation", urlPatterns = {"/DeliveryConfirmation"})
public class DeliveryConfirmation extends HttpServlet {

    private static final Logger log = Logger.getLogger(DeliveryConfirmation.class.getName());
    protected DbConnect db = null;

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        Transaction delivery = null;
        try {
            String deliveryJson = request.getParameter("Delivery");
            log.info("Completing delivery: " + deliveryJson);
            TransactionMapper mapper = new TransactionMapper();
            try {
                delivery = Serializer.deserialize(deliveryJson, Transaction.class).get(0);
                String cdshiptyp = delivery.getShipType();
                if ((cdshiptyp != null && cdshiptyp.trim().length() > 0)
                        || (delivery.getShipTypeDesc() == null || delivery.getShipTypeDesc().trim().length() == 0)) {
                    try {
                        delivery.setShipTypeDesc(db.getShipTypeDesc(cdshiptyp));
                    } catch (ClassNotFoundException ex) {
                        log.warn(null, ex);
                    } catch (SQLException ex) {
                        log.warn(null, ex);
                    }
                }

                mapper.completeDelivery(db, delivery);
            } catch (SQLException e) {
                out.println("Database not updated");
                log.error("SQL Exception ", e);
                return;
            } catch (ClassNotFoundException e) {
                log.error("Error getting oracle jdbc driver: ", e);
            } catch (JsonSyntaxException e) {
                log.error("DeliveryConfirmation Json Syntax Exception: ", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
            HandleEmails handleEmails = new HandleEmails(delivery, HandleEmails.DELIVERYTRANSACTION, request, response, db);
            handleEmails.sendEmails("delivery");

            //emailDeliveryReceipt(out, "Database updated successfully", delivery, request);
        } finally {
            out.close();
        }
    }

    public void emailDeliveryReceipt(PrintWriter out, String msg, Transaction delivery, HttpServletRequest request) {
        int emailReceiptStatus = 0;
        try {
            HttpSession httpSession = request.getSession(false);
            String user = (String) httpSession.getAttribute("user");
            String pwd = (String) httpSession.getAttribute("pwd");

            EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", delivery, "delivery");
            Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
            threadEmailMoveReceipt.start();

            //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(delivery);
            //if (emailReceiptStatus==0) {
            out.println("Database updated successfully");

            if (delivery.getRemoteType().equalsIgnoreCase("RPK") && delivery.getShipType() != null && delivery.getShipType().trim().length() > 0) {
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Generating Email for Remote Pickup Part");
                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Generating Email for Remote Pickup Part");

                /*if (db == null) {
                 log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation: Remote Pickup Part Email db is NULL!!");
                 }*/

                Transaction remotePickup = null;
                TransactionMapper transactionMapper = new TransactionMapper();
                try {
                    remotePickup = transactionMapper.queryTransaction(db, delivery.getNuxrpd());
                    /*if (remotePickup == null) {
                     log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation(a): Remote Pickup Part Email remotePickup==NULL!!");
                     }*/
                } catch (Exception e) {
                    e.printStackTrace();
                }
                /*if (remotePickup == null) {
                 log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation(b): Remote Pickup Part Email remotePickup==NULL!!");
                 }*/
                //remoteDelivery = db.getDelivery(pickup.getNuxrpd()); 
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation(b): Remote Pickup Part user:" + user + ", pwd:" + pwd);
                EmailMoveReceipt emailRemotePickupReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", remotePickup);
                Thread threadEmailRemotePickupReceipt = new Thread(emailRemotePickupReceipt);
                threadEmailRemotePickupReceipt.start();
                //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation: Remote Pickup Part Email Started");
                //System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE DeliveryConfirmation: Remote Pickup Part Email Started");
            }
            /*}
             else {
             out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:"+emailReceiptStatus+").");
             }*/
        } catch (Exception e) {
            e.printStackTrace();
            System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).[" + e.getMessage() + ":" + e.getStackTrace()[0].toString() + "]");
            out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
            log.error("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2). ");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }
}
