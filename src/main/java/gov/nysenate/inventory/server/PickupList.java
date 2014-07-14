package gov.nysenate.inventory.server;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import com.google.gson.Gson;
import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.model.SimpleListItem;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "PickupList", urlPatterns = {"/PickupList"})
public class PickupList extends HttpServlet {

    String[] searchByTypes = {"cdlocatfrom", "cdlocatto", "napickupby", "dttxnorigin"};
    private static final Logger log = Logger.getLogger(PickupList.class.getName());
  
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);
        ArrayList<SimpleListItem> searchBy = new ArrayList<SimpleListItem>();

        try {
            String loc_code = request.getParameter("loc_code");
            log.info("Getting pickup list for " + loc_code);

            for (int x=0;x<searchByTypes.length;x++) {
              try {
                String natype = null;
                String navalue = null;
                navalue = request.getParameter(searchByTypes[x]);
                natype =  searchByTypes[x];
                if (navalue!=null && navalue.trim().length()>0 && natype!=null && natype.trim().length()>0) {
                  SimpleListItem simpleListItem = new SimpleListItem();
                  simpleListItem.setNatype(natype);
                  simpleListItem.setNavalue(navalue);
                  searchBy.add(simpleListItem);
                }
              }
              catch (Exception e) {
              }
            }
           
            List<PickupGroup> pickupList = Collections.synchronizedList(new ArrayList<PickupGroup>());
            pickupList = db.getPickupList(searchBy);
            String json = new Gson().toJson(pickupList);
            log.info("Pickup list info = " + json);
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
