/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.model.Commodity;
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
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            String keywords = request.getParameter("keywords");
            log.info("Get commodity info for keywords: " + keywords);

            List<Commodity> commodityResults = Collections.synchronizedList(new ArrayList<Commodity>());
            commodityResults = db.getCommodityList(keywords.trim());

            log.info("Commodity results = " + commodityResults);
            String json = gson.toJson(commodityResults);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(json);
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
