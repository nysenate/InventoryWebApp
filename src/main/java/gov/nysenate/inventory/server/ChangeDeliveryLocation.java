package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.TransactionMapper;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HandleEmails;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

@WebServlet(name = "ChangeDeliveryLocation", urlPatterns = { "/ChangeDeliveryLocation" })
public class ChangeDeliveryLocation extends HttpServlet {

    private static final Logger log = Logger.getLogger(ChangeDeliveryLocation.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(request, HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String nuxrpdStr = request.getParameter("nuxrpd");
        String cdLoc = request.getParameter("cdloc");
        log.info("Changing Delivery location for nuxrpd = " + nuxrpdStr + " to " + cdLoc);

        if (cdLoc == null || nuxrpdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Unable to change delivery location, nuxrpd or cdloc was null.");
            return;
        }

        int nuxrpd = Integer.valueOf(nuxrpdStr);
        try {
            db.changeDeliveryLocation(nuxrpd, cdLoc);
            Transaction pickup = new Transaction();
            TransactionMapper transactionMapper = new TransactionMapper();
            pickup = transactionMapper.queryTransaction(db, nuxrpd);
            HandleEmails handleEmails = new HandleEmails(pickup, HandleEmails.PICKUPTRANSACTION, request, response, db);
            handleEmails.sendEmails("CHANGEDELIVERYLOCATION");            
            
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Change Delivery Location Exception: ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
