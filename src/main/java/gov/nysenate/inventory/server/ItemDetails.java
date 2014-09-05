package gov.nysenate.inventory.server;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "ItemDetails", urlPatterns = {"/ItemDetails"})
public class ItemDetails extends HttpServlet {

    private static final Logger log = Logger.getLogger(ItemDetails.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);
            String barcode_num = request.getParameter("barcode_num");
            log.info("Getting item info for barcode = " + barcode_num);

            String details = db.getDetails(barcode_num);

            log.info("Item details for " + barcode_num + " = " + details);
            if (details.equals("no")) {
                out.println("Does not exist in system");
            } else {
                String model[] = details.split("\\|");

                //Psuedo JSON for now
                out.println("{\"nusenate\":\"" + model[0] + "\",\"nuxrefsn\":\"" + model[1] + "\",\"dtissue\":\"" + model[3] + "\",\"cdlocatto\":\"" + model[4] + "\",\"cdloctypeto\":\"" + model[5] + "\",\"cdcategory\":\"" + model[6] + "\",\"adstreet1to\":\"" + model[7].replaceAll("\"", "&#34;") + "\",\"decommodityf\":\"" + model[8].replaceAll("\"", "&#34;")  + "\",\"cdlocatfrom\":\"" + model[9] + "\",\"cdstatus\":\"" + model[10] + "\",\"cdintransit\":\"" + model[12] + "\",\"pending_removal\":\"" + model[14] + "\"}");
            }

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
