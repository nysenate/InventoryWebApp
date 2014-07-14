package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;

import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "ItemsList", urlPatterns = {"/ItemsList"})
public class ItemsList extends HttpServlet {

    private static final Logger log = Logger.getLogger(ItemsList.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        try {
            String loc_code = request.getParameter("loc_code");
            //log.info("Getting item details for location " + loc_code);

            ArrayList<VerList> itemList = new ArrayList<VerList>();
            itemList = db.getLocationItemList(loc_code);
            String json = new Gson().toJson(itemList);
            //log.info("Item details = " + json);

            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            out.print(json);
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
