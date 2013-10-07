/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.model.InvSerialNumber;
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
@WebServlet(name = "SerialList", urlPatterns = {"/SerialList"})
public class SerialList extends HttpServlet
{

   int numaxResults = 500;
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
          throws ServletException, IOException
  {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;  
            String userFallback = null;
            if (httpSession==null) {
                System.out.println ("****SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.ipAddr + "|" + "****SESSION NOT FOUND SerialList.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.ipAddr + "|" + "****SESSION NOT FOUND SerialList.processRequest could not process Fallback Username. Generic Username will be used instead.");                
                }    
                out.println("Session timed out");
                return;                
            }
            else {
                System.out.println ("SESSION FOUND!!!!");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
            }
            
            String nuserial = request.getParameter("nuserial");
            
            String maxResults = request.getParameter("maxResults");
            
            if (maxResults!=null) {
               try {
                this.numaxResults = Integer.valueOf(maxResults);
               }
               catch (Exception e) {
                 Logger.getLogger(SerialList.class.getName()).warn(db.ipAddr+"|"+"Servlet SerialList could not convert maxResults ("+maxResults+") to a number, defaulting to "+maxResults);
               }
            }
            
            db.ipAddr=request.getRemoteAddr();
            Gson gson = new GsonBuilder()
                  .excludeFieldsWithoutExposeAnnotation()
                  .create();                 
            //Logger.getLogger(SerialList.class.getName()).info(db.ipAddr+"|"+"Servlet SerialList : start");

            List<InvSerialNumber> serialList = Collections.synchronizedList(new ArrayList<InvSerialNumber>());
            
            serialList = db.getNuSerialList(nuserial, numaxResults, userFallback);

            if (serialList.size() == 0) {
                System.out.println("NO SERIAL#s FOUND");
            }

            String json = gson.toJson(serialList);
            System.out.println ("SERIAL LIST RESULTS:"+json);
            log.info("SERIAL LIST RESULTS:"+json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            out.print(json);
            //Logger.getLogger(SerialList.class.getName()).info(db.ipAddr+"|"+"Servlet SerialList : end");
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
