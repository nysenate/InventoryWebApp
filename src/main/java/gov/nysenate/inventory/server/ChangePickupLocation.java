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

@WebServlet(name = "ChangePickupLocation", urlPatterns = { "/ChangePickupLocation" })
public class ChangePickupLocation extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(CancelPickup.class.getName());

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        String nuxrpdStr = request.getParameter("nuxrpd");
        String cdLoc = request.getParameter("cdloc");
        if (cdLoc == null || nuxrpdStr == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int nuxrpd = Integer.valueOf(nuxrpdStr);
        try {
            db.changePickupLocation(nuxrpd, cdLoc);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Change Pickup Location Exception: ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
