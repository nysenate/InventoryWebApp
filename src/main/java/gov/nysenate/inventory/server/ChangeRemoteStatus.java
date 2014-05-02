package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HandleEmails;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "ChangeRemoteStatus", urlPatterns = {"/ChangeRemoteStatus"})
public class ChangeRemoteStatus extends HttpServlet {

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ChangeRemoteStatus.class.getName());
    private Transaction trans = null;
    private Transaction transOrg = null;
    private final int NO_ACTION_NEEDED = 100, SEND_PICKUP_EMAIL = 101, SEND_DELIVERY_EMAIL = 102, SEND_PICKUP_AND_DELIVERY_EMAIL = 103, SEND_IGNORE_REMOTE_DELIVERY_EMAIL = 104;
    private int emailActionNeeded = NO_ACTION_NEEDED;
    private DbConnect db = null;
    private HttpServletRequest request = null;
    private HttpServletResponse response = null;

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        this.request = request;
        this.response = response;
        db = HttpUtils.getHttpSession(request, response, out);

        String transJson = request.getParameter("trans");
        log.info("Updating remote status, pickup: " + transJson);

        TransactionMapper mapper = new TransactionMapper();
        TransactionMapper mapperOrg = new TransactionMapper();

        if (transJson != null) {
            try {
                trans = TransactionParser.parseTransaction(transJson);
                int nuxrpd = trans.getNuxrpd();
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we query original transaction for nuxrpd:" + nuxrpd);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we query original transaction for nuxrpd:" + nuxrpd);
                transOrg = mapperOrg.queryTransaction(db, nuxrpd);
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we compare transaction changes for nuxrpd:" + nuxrpd);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we compare transaction changes for nuxrpd:" + nuxrpd);
                compareRemoteInfo(trans, transOrg, response, out);
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we update changes for nuxrpd:" + nuxrpd);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we update changes for nuxrpd:" + nuxrpd);
                mapper.updateTransaction(db, trans);
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we process emails for nuxrpd:" + nuxrpd);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: before we process emails  for nuxrpd:" + nuxrpd);
                processEmails(trans, transOrg);
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: after we process emails for nuxrpd:" + nuxrpd);
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE ChangeRemoteStatus: after we process emails  for nuxrpd:" + nuxrpd);
            } catch (ClassNotFoundException | SQLException e) {
                log.error("Error updating remote status: ", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                log.error("ChangeRemote Json Syntax Exception: ", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            log.warn("Unable to update remote status, pickup json was null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

    }

    private void compareRemoteInfo(Transaction trans, Transaction transOrg, HttpServletResponse response, PrintWriter out) {
        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: start");
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: start");

        if (transOrg == null) {
            emailActionNeeded = NO_ACTION_NEEDED;
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: original transaction is null");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: original transaction is null");
            return;
        }

        if (trans == null) {
            emailActionNeeded = NO_ACTION_NEEDED;
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: new transaction is null");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: new transaction is null");
            return;
        }

        System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: before compare " + transOrg.isRemoteDelivery() + ", " + trans.isRemoteDelivery());
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: before compare " + transOrg.isRemoteDelivery() + ", " + trans.isRemoteDelivery());

        if (transOrg.isRemoteDelivery() && !trans.isRemoteDelivery()) {

            /*
             * Remote Delivery changed to a standard Delivery. No email needs to be sent because on delivery
             * the user will have to get the employee to sign. We can ignore the printed signed paperwork.
             */
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: No longer a REMOTE DELIVERY");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: No longer a REMOTE DELIVERY");


            emailActionNeeded = NO_ACTION_NEEDED;

        } else if (!transOrg.isRemoteDelivery() && trans.isRemoteDelivery()) {

            /*
             * Standard Delivery changed to a Remote Delivery. We need to send a Remote Delivery E-mail to the
             * user so he/she can send it to the user for signoff.
             */

            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: STANDARD DELIVERY changed to  REMOTE DELIVERY");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: STANDARD DELIVERY changed to  REMOTE DELIVERY");

            emailActionNeeded = SEND_DELIVERY_EMAIL;

        } else if (transOrg.isRemotePickup() != trans.isRemotePickup()) {

            /*
             * Pickup Remote flag is changed. This should not be allowed due
             * to data integrity.
             */
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE FLAG ON PICKUP CHANGED!!! NO NO!!");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE FLAG ON PICKUP CHANGED!!! NO NO!!");

            log.warn("Pickup Remote flag cannot be changed after pickup was initiated.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if (trans.isRemoteDelivery() && !trans.getShipType().equalsIgnoreCase(transOrg.getShipType())) {

            /*
             * Remote flags were not changed but ShipType has changed for Delivery. We will later need to send
             * Remote Delivery e-mail out with updated shiptype. 
             */

            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE DELIVERY SHIPTYPE CHANGED");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE DELIVERY SHIPTYPE CHANGED");
            emailActionNeeded = SEND_DELIVERY_EMAIL;

        } else if (trans.isRemotePickup() && !trans.getShipType().equalsIgnoreCase(transOrg.getShipType())) {

            /*
             * Remote flags were not changed but ShipType has changed for Pickup. We will later need to send
             * Remote Pickup e-mail out with updated shiptype. 
             */
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE PICKUP SHIPTYPE CHANGED");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: REMOTE PICKUP SHIPTYPE CHANGED");

            emailActionNeeded = SEND_PICKUP_EMAIL;

        } else {

            /*
             * We don't need to send e-mails for any other conditions. EX: Changing remote comments since
             * the attachment will not display them until it has been delivered or later and the e-mail does
             * not display the comments.
             */

            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: NO IMPORTANT REMOTE INFO CHANGED");
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE compareRemoteInfo: NO IMPORTANT REMOTE INFO CHANGED");
            emailActionNeeded = NO_ACTION_NEEDED;

        }

    }

    private void processEmails(Transaction trans, Transaction transOrg) throws IOException {
        switch (emailActionNeeded) {
            case SEND_IGNORE_REMOTE_DELIVERY_EMAIL:
                break;
            case SEND_DELIVERY_EMAIL:
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE processEmails: sendEmailsFromPickupTransaction(ONLY DELIVERY EMAIL)");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE processEmails: sendEmailsFromPickupTransaction(ONLY DELIVERY EMAIL)");
                sendEmailsFromPickupTransaction(HandleEmails.DELIVERYEMAIL);
            case SEND_PICKUP_EMAIL:
                System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE processEmails: sendEmailsFromPickupTransaction(ONLY PICKUP EMAIL)");
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE processEmails: sendEmailsFromPickupTransaction(ONLY PICKUP EMAIL)");
                sendEmailsFromPickupTransaction(HandleEmails.PICKUPEMAIL);
                break;
            case NO_ACTION_NEEDED:
                break;
        }
    }

    private void sendEmailsFromPickupTransaction() throws IOException {
        sendEmailsFromPickupTransaction(HandleEmails.ALLEMAILS);
    }

    private void sendEmailsFromPickupTransaction(int emailType) throws IOException {
        HandleEmails handleEmails = new HandleEmails(trans, HandleEmails.PICKUPTRANSACTION, request, response, false, db);
        handleEmails.sendEmails(emailType);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
