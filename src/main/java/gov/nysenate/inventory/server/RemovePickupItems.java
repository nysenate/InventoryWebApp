package gov.nysenate.inventory.server;

import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "RemovePickupItems", urlPatterns = { "/RemovePickupItems" })
public class RemovePickupItems extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Logger log = Logger.getLogger(RemovePickupItems.class.getName());

        PrintWriter out = null;
        DbConnect db = null;
        try {
            out = response.getWriter();
            db = HttpUtils.getHttpSession(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        // Temporary fix to abide by current session checking functionality.
        if (out.toString().contains("Session timed out")) {
            response.setStatus(HttpUtils.SC_SESSION_TIMEOUT);
        }

        String nuxrpdString = request.getParameter("nuxrpd");
        String[] items = request.getParameterValues("items[]");
        log.info("RemovePickupItems nuxrpd = " + nuxrpdString);
        log.info("RemovePickupItems items = " + Arrays.toString(items));
        if (nuxrpdString == null || items == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            db.removeDeliveryItems(nuxrpd, items);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Cancel Pickup Exception: ", ex);
        }
        log.info("RemovePickupItems end.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}