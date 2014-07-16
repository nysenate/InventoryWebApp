package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "CancelPickup", urlPatterns = { "/CancelPickup" })
public class CancelPickup extends HttpServlet {

    private static final Logger log = Logger.getLogger(CancelPickup.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String nuxrpdString = request.getParameter("nuxrpd");
        log.info("Canceling pickup with nuxrpd = " + nuxrpdString);

        if (nuxrpdString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Can't cancel pickup, nuxrpd was null");
            return;
        }

        int nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            db.cancelPickup(nuxrpd);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Cancel Pickup Exception: ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
