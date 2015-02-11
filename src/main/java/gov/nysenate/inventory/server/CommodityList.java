/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.item.CommodityService;
import gov.nysenate.inventory.model.Commodity;
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
import java.sql.SQLException;
import java.util.List;

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

        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
            String keywords = request.getParameter("keywords");
            log.info("Get commodity info for keywords: " + keywords);

            //TODO: should prob check for keywords here, how should app respond if there are none?

            CommodityService service = new CommodityService();
            List<Commodity> commodityResults = service.getCommoditiesByKeywords(db, keywords.trim());

            log.info("Commodity results size = " + commodityResults.size());
            String json = Serializer.serialize(commodityResults);
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
