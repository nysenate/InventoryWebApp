package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.TransactionMapper;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HandleEmails;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.log4j.Logger;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

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
        HttpSession session = request.getSession(false);
        db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String transJson = request.getParameter("trans");
        log.info("Updating remote status, pickup: " + transJson);

        TransactionMapper mapper = new TransactionMapper();

        if (transJson != null) {
            try {
                trans = Serializer.deserialize(transJson, Transaction.class).get(0);
                int nuxrpd = trans.getNuxrpd();
                transOrg = mapper.queryTransaction(db, nuxrpd);
                compareRemoteInfo(trans, transOrg, response, out);
                mapper.updateTransaction(db, trans);
                processEmails(trans, transOrg);
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
        if (trans == null || transOrg == null) {
            emailActionNeeded = NO_ACTION_NEEDED;
            return;
        }

        if (transOrg.isRemoteDelivery() && !trans.isRemoteDelivery()) {
            /*
             * Remote Delivery changed to a standard Delivery. No email needs to be sent because on delivery
             * the user will have to get the employee to sign. We can ignore the printed signed paperwork.
             */
            emailActionNeeded = NO_ACTION_NEEDED;
        } else if (!transOrg.isRemoteDelivery() && trans.isRemoteDelivery()) {
            /*
             * Standard Delivery changed to a Remote Delivery. We need to send a Remote Delivery E-mail to the
             * user so he/she can send it to the user for signoff.
             */
            emailActionNeeded = SEND_DELIVERY_EMAIL;
        } else if (transOrg.isRemotePickup() != trans.isRemotePickup()) {
            /*
             * Pickup Remote flag is changed. This should not be allowed due
             * to data integrity.
             */
            log.warn("Pickup Remote flag cannot be changed after pickup was initiated.");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        } else if (trans.isRemoteDelivery() && !trans.getShipType().equalsIgnoreCase(transOrg.getShipType())) {
            /*
             * Remote flags were not changed but ShipType has changed for Delivery. We will later need to send
             * Remote Delivery e-mail out with updated shiptype. 
             */
            emailActionNeeded = SEND_DELIVERY_EMAIL;
        } else if (trans.isRemotePickup() && !trans.getShipType().equalsIgnoreCase(transOrg.getShipType())) {
            /*
             * Remote flags were not changed but ShipType has changed for Pickup. We will later need to send
             * Remote Pickup e-mail out with updated shiptype. 
             */
            emailActionNeeded = SEND_PICKUP_EMAIL;
        } else {
            /*
             * We don't need to send e-mails for any other conditions. EX: Changing remote comments since
             * the attachment will not display them until it has been delivered or later and the e-mail does
             * not display the comments.
             */
            emailActionNeeded = NO_ACTION_NEEDED;
        }
    }

    private void processEmails(Transaction trans, Transaction transOrg) throws IOException {
        switch (emailActionNeeded) {
            case SEND_IGNORE_REMOTE_DELIVERY_EMAIL:
                break;
            case SEND_DELIVERY_EMAIL:
                sendEmailsFromPickupTransaction(HandleEmails.DELIVERYEMAIL);
                break;
            case SEND_PICKUP_EMAIL:
                sendEmailsFromPickupTransaction(HandleEmails.PICKUPEMAIL);
                break;
            case NO_ACTION_NEEDED:
                break;
        }
    }

    private void sendEmailsFromPickupTransaction(int emailType) throws IOException {
        HandleEmails handleEmails = new HandleEmails(trans, HandleEmails.PICKUPTRANSACTION, request, response, db);
        handleEmails.sendEmails(emailType);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
