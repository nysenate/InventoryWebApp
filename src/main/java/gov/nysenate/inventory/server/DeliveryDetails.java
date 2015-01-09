package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "DeliveryDetails", urlPatterns = {"/DeliveryDetails"})
public class DeliveryDetails extends HttpServlet {

    private static final Logger log = Logger.getLogger(DeliveryDetails.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(request, HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        try {
            String nuxrpd = request.getParameter("NUXRPD");
            log.info("Getting delivery info for nuxrpd = " + nuxrpd);

            ArrayList<InvItem> deliveryDetails = new ArrayList<InvItem>();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            deliveryDetails = db.getDeliveryDetails(nuxrpd);
            String json = gson.toJson(deliveryDetails);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(json);
            log.info("The details for delivery with nuxrpd = " + nuxrpd + " are: " + json);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
