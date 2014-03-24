package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
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
import gov.nysenate.inventory.model.SimpleListItem;
import gov.nysenate.inventory.server.DbConnect;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "PickupList", urlPatterns = {"/PickupList"})
public class PickupList extends HttpServlet {

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
    String[] searchByTypes = {"cdlocatfrom", "cdlocatto", "napickupby", "dttxnorigin"};
  
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        Logger log = Logger.getLogger(PickupList.class.getName());

        ArrayList<SimpleListItem> searchBy = new ArrayList<SimpleListItem>();
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;
            String userFallback = null;
            if (httpSession==null) {
                System.out.println ("****SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND PickupList.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND PickupList.processRequest could not process Fallback Username. Generic Username will be used instead.");                
                } 
                out.println("Session timed out");
                return;            
            }
            else {
                long  lastAccess = (System.currentTimeMillis() - httpSession.getLastAccessedTime());
                System.out.println ("SESSION FOUND!!!!");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
                
            }
            db.clientIpAddr=request.getRemoteAddr();
            Logger.getLogger(PickupList.class.getName()).info(db.clientIpAddr+"|"+"Servlet PickupList : Start");
            String loc_code = request.getParameter("loc_code");

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
            pickupList = db.getPickupList(searchBy, userFallback);
            String json = new Gson().toJson(pickupList);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            out.print(json);


            Logger.getLogger(PickupList.class.getName()).info(db.clientIpAddr+"|"+"Servlet PickupList : end");
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
            throws ServletException, IOException {
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
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
