package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(name = "GetPickup", urlPatterns = { "/GetPickup" })
public class GetPickup extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(CancelPickup.class.getName());
        response.setContentType("text/html;charset=UTF-8");

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

        int nuxrpd;
        Pickup pickup = null;
        ArrayList<InvItem> items;
        String userFallback = request.getParameter("userFallback");
        String nuxrpdString = request.getParameter("nuxrpd");
        if (nuxrpdString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            pickup = db.getPickupInfo(nuxrpd);
            items = db.getDeliveryDetails(nuxrpdString, userFallback);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetPickup: SQL Exception: ", ex);
            return;
        }

        if (pickup == null) {
            log.error("GetPickup: Error Parsing pickup");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        pickup.setPickupItems(items);
        out.print(new Gson().toJson(pickup));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
