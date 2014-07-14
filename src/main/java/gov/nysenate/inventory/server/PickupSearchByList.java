package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.model.SimpleListItem;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "PickupSearchByList", urlPatterns = {"/PickupSearchByList"})
public class PickupSearchByList extends HttpServlet
{

  private static final Logger log = Logger.getLogger(PickupSearchByList.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);
        try {
            Gson gson = new GsonBuilder()
                  .excludeFieldsWithoutExposeAnnotation()
                  .create();                 
            String natype;
            try {
                natype = request.getParameter("NATYPE");
            } catch (Exception e) {
                natype = "ALL";
                Logger.getLogger(PickupSearchByList.class.getName()).info("Servlet PickupSearchByList : " + "NATYPE SET TO ALL DUE TO EXCEPTION");
                System.out.println("NATYPE SET TO ALL DUE TO EXCEPTION");
            }
            if (natype == null) {
                natype = "ALL";
                System.out.println("NATYPE SET TO ALL DUE TO NULL");
            } else {
                System.out.println("NATYPE=" + natype);
            }

            log.info("Getting pickup search by list where natype = " + natype);

            List<SimpleListItem> PickupSearchByList = Collections.synchronizedList(new ArrayList<SimpleListItem>());

            PickupSearchByList = db.getPickupSearchByList();

            if (PickupSearchByList.size() == 0) {
                System.out.println("NO LOCATION CODES FOUND");
            }

            String json = gson.toJson(PickupSearchByList);
            log.info("PickupSearchByList results: " + json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            out.print(json);
            Logger.getLogger(PickupSearchByList.class.getName()).info("Servlet PickupSearchByList : end");
        } finally {
            out.close();
        }
    }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

}
