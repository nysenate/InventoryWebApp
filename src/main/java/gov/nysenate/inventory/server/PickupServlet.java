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
@WebServlet(name = "Pickup", urlPatterns = {"/Pickup"})
public class PickupServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(PickupServlet.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        String pickupJson = request.getParameter("pickup");
        log.info("Attempting to complete pickup: " + pickupJson);

        TransactionMapper mapper = new TransactionMapper();
        try {
            Transaction pickup = Serializer.deserialize(pickupJson, Transaction.class).get(0);
            db.setLocationInfo(pickup.getOrigin());
            db.setLocationInfo(pickup.getDestination());
            int pickupId = mapper.insertPickup(db, pickup);
            sendEmails(request, response, pickup, out, db, pickupId);
        } catch (SQLException | ClassNotFoundException | JsonSyntaxException ex) {
            log.error(ex.getMessage(), ex);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }

        log.info("Servlet Pickup : end");
    }

    private void sendEmails(HttpServletRequest request, HttpServletResponse response, Transaction pickup, PrintWriter out, DbConnect db, int pickupId) throws IOException {
        ifRemoteSetShipTypeDesc(pickup, db, pickupId);
        if (pickupId > -1) {
            HandleEmails handleEmails = new HandleEmails(pickup, HandleEmails.PICKUPTRANSACTION, request, response,  db);
            handleEmails.sendEmails();
        } else {
            out.println("Database not updated");
        }
    }

    private void ifRemoteSetShipTypeDesc(Transaction pickup, DbConnect db, int pickupId) {
        pickup.setNuxrpd(pickupId);
        String cdshiptyp = pickup.getShipType();
        if ((cdshiptyp != null && cdshiptyp.trim().length() > 0) || (pickup.getShipTypeDesc() == null || pickup.getShipTypeDesc().trim().length() == 0)) {
            try {
                pickup.setShipTypeDesc(db.getShipTypeDesc(cdshiptyp));
            } catch (ClassNotFoundException | SQLException ex) {
                log.warn("Error setting the pickup ship type", ex);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }


    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

}
