package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.dao.TransactionMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(name = "GetPickup", urlPatterns = { "/GetPickup" })
public class GetPickup extends HttpServlet {

    private static final Logger log = Logger.getLogger(GetPickup.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        int nuxrpd;
        Transaction pickup = null;
        String nuxrpdString = request.getParameter("nuxrpd");
        log.info("Getting pickup for nuxrpd = " + nuxrpdString);
        if (nuxrpdString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Unable to get pickup info, nuxrpd was null");
            return;
        }

        TransactionMapper mapper = new TransactionMapper();
        nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            pickup = mapper.queryTransaction(db, nuxrpd);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetPickup: SQL Exception: ", ex);
            return;
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }

        if (pickup == null) {
            log.error("GetPickup: Error Parsing pickup");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        String json = new Gson().toJson(pickup);
        log.info("Pickup info received: " + json);
        PrintWriter out = response.getWriter();
        out.print(json);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
