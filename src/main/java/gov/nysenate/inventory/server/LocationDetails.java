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
@WebServlet(name = "LocationDetails", urlPatterns = {"/LocationDetails"})
public class LocationDetails extends HttpServlet {

    private static final Logger log = Logger.getLogger(LocationDetails.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);
            String location = request.getParameter("barcode_num"); // barcode_num = location code.
            log.info("Getting location details for " + location);

            String details = db.getInvLocDetails(location);

            log.info("Location details = " + details);
            if (details.equals("no")) {
                out.println("{\"cdlocat\":\"" + "" + "\",\"delocat\":\"" + "Does not exist in system" + "\",\"adstreet1\":\"" + "" + "\",\"adstreet2\":\"" + "" + "\",\"adcity\":\"" + "" + "\",\"adstate\":\"" + "" + "\",\"adzipcode\":\"" + "" + "\",\"nucount\":\"" + "" + "\",\"cdrespctrhd\":\"" + "" + "\"}");

            } else {
                String model[] = details.split("\\|");
                
                //Psuedo JSON for now
                out.println("{\"cdlocat\":\"" + model[0] + "\",\"delocat\":\"" + model[1].replaceAll("\"", "&34;") + "\",\"adstreet1\":\"" + model[2].replaceAll("\"", "&34;") + "\",\"adstreet2\":\"" + model[3].replaceAll("\"", "&34;") + "\",\"adcity\":\"" + model[4].replaceAll("\"", "&34;") + "\",\"adstate\":\"" + model[5] + "\",\"adzipcode\":\"" + model[6].replaceAll("\"", "&#34;") + "\",\"nucount\":\"" + model[7] + "\",\"cdrespctrhd\":\"" + model[8] + "\"}");
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
