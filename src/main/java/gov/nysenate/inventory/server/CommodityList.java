/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.dao.CommodityDAO;
import gov.nysenate.inventory.dao.CommodityService;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Commodity;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
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
@WebServlet(name = "CommodityList", urlPatterns = {"/CommodityList"})
public class CommodityList extends HttpServlet
{

  private static final Logger log = Logger.getLogger(CommodityList.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);
            Gson gson = new GsonBuilder().create();
            String keywords = request.getParameter("keywords");
            log.info("Get commodity info for keywords: " + keywords);

            //TODO: should prob check for keywords here, how should app respond if there are none?

            CommodityService service = new CommodityService();
            List<Commodity> commodityResults = service.getCommoditiesByKeywords(db, keywords.trim());

            log.info("Commodity results size = " + commodityResults.size());
            String json = gson.toJson(commodityResults);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(json);
        } catch (ClassNotFoundException | SQLException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
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
