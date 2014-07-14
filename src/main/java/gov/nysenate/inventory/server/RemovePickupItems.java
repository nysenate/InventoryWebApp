package gov.nysenate.inventory.server;

import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.security.InvalidParameterException;
import java.sql.SQLException;
import java.util.Arrays;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "RemovePickupItems", urlPatterns = { "/RemovePickupItems" })
public class RemovePickupItems extends HttpServlet {

    private static final Logger log = Logger.getLogger(RemovePickupItems.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String nuxrpdString = request.getParameter("nuxrpd");
        String[] items = request.getParameterValues("items[]");
        log.info("Removing items for pickup nuxrpd = " + nuxrpdString + ", items = " + Arrays.toString(items));
        if (nuxrpdString == null || items == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.info("Cannot remote items because nuxrpd or items are null.");
            return;
        }

        int nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            db.removeDeliveryItems(nuxrpd, items);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Cancel Pickup Exception: ", ex);
        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Error getting oracle jdbc driver: ", e);
        } catch (InvalidParameterException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error("Invalid Param, remove delivery item.", e);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
