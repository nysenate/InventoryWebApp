/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.model.Commodity;
import static gov.nysenate.inventory.server.DbConnect.log;
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
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "CommodityList", urlPatterns = {"/CommodityList"})
public class CommodityList extends HttpServlet
{

  /**
   * Processes requests for both HTTP
   * <code>GET</code> and
   * <code>POST</code> methods.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;
            String userFallback = null;
            if (httpSession==null) {
                System.out.println ("****SESSION NOT FOUND(CommodityList)");
                db = new DbConnect();
                log.info("****SESSION NOT FOUND CommodityList.processRequest ");
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info("****SESSION NOT FOUND CommodityList.processRequest could not process Fallback Username. Generic Username will be used instead.");
                } 
                out.println("Session timed out");
                return;            
            }
            else {
                long  lastAccess = (System.currentTimeMillis() - httpSession.getLastAccessedTime());
                System.out.println ("SESSION FOUND!!!!(CommodityList)");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
            }
           Gson gson = new GsonBuilder()
                .excludeFieldsWithoutExposeAnnotation()
                .create();            
            //System.out.println("Before Logger");
            Logger.getLogger(CommodityList.class.getName()).info("Servlet CommodityList : Start");
            //System.out.println("Before keywords");
            String keywords = request.getParameter("keywords");
           
            //System.out.println("Before commodityResults List Setup");
            List<Commodity> commodityResults = Collections.synchronizedList(new ArrayList<Commodity>());
            //System.out.println("Before commodityResults List Setup LOGGER");
            Logger.getLogger(CommodityList.class.getName()).info("db.getCommodityList(+'"+keywords.trim()+"', '"+userFallback+"')");
            //System.out.println("Before actual commodityResults:"+keywords);
            commodityResults = db.getCommodityList(keywords.trim(), userFallback);
            //System.out.println("After actual commodityResults:"+keywords);
            Logger.getLogger(CommodityList.class.getName()).info("commodityResults:"+commodityResults);
            //System.out.println("Before JSON:"+keywords);
            String json = gson.toJson(commodityResults);
            //System.out.println("Before JSON setContentType:"+keywords);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            //System.out.println("Before JSON getWriter:"+keywords);
            response.getWriter().write(json);
            //System.out.println("after JSON getWriter retrning:"+json);

            Logger.getLogger(CommodityList.class.getName()).info("returning:"+json);
            out.print(json);


            Logger.getLogger(CommodityList.class.getName()).info("Servlet CommodityList : end");
        } finally {

            out.close();
        }
    }

  // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
  /**
   * Handles the HTTP
   * <code>GET</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Handles the HTTP
   * <code>POST</code> method.
   *
   * @param request servlet request
   * @param response servlet response
   * @throws ServletException if a servlet-specific error occurs
   * @throws IOException if an I/O error occurs
   */
  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  /**
   * Returns a short description of the servlet.
   *
   * @return a String containing servlet description
   */
  @Override
  public String getServletInfo()
  {
    return "Short description";
  }// </editor-fold>
}
