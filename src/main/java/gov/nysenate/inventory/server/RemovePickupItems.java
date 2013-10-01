package gov.nysenate.inventory.server;

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
        log.info("RemovePickupItems start.");
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
            new DbConnect().removeDeliveryItems(nuxrpd, items);
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
