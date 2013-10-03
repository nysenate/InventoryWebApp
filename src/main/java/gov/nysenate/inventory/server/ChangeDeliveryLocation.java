package gov.nysenate.inventory.server;

import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "ChangeDeliveryLocation", urlPatterns = { "/ChangeDeliveryLocation" })
public class ChangeDeliveryLocation extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Logger log = Logger.getLogger(CancelPickup.class.getName());

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

        String nuxrpdStr = request.getParameter("nuxrpd");
        String cdLoc = request.getParameter("cdloc");
        if (cdLoc == null || nuxrpdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int nuxrpd = Integer.valueOf(nuxrpdStr);
        try {
            db.changeDeliveryLocation(nuxrpd, cdLoc);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Change Delivery Location Exception: ", ex);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }

}
