package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.SimpleListItem;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

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
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
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

            List<SimpleListItem> pickupSearchByList = Collections.synchronizedList(new ArrayList<SimpleListItem>());

            pickupSearchByList = db.getPickupSearchByList();

            if (pickupSearchByList.size() == 0) {
                System.out.println("NO LOCATION CODES FOUND");
            }

            String json = Serializer.serialize(pickupSearchByList);
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
