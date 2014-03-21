package gov.nysenate.inventory.server;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(RemovePickupItems.class.getName());

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

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
        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Error getting oracle jdbc driver: ", e);
        } catch (InvalidParameterException e) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.error("Invalid Param, remove delivery item.", e);
        }
        log.info("RemovePickupItems end.");
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
