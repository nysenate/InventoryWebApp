/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "KeepSessionAlive", urlPatterns = {"/KeepSessionAlive"})
public class KeepSessionAlive extends HttpServlet {

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
        DbConnect db;             
        try {
            HttpSession httpSession = request.getSession(false);
            String userFallback = null;
            db = new DbConnect();   
            if (httpSession==null) {
                    System.out.println ("****SESSION NOT FOUND");
                    log.info(db.ipAddr + "|" + "****SESSION NOT FOUND KeepSessionAlive.processRequest ");                
                    out.println("Session timed out");
            }
            else {
                    String user = (String)httpSession.getAttribute("user");
                    String pwd = (String)httpSession.getAttribute("pwd");
                    db = new DbConnect(user, pwd);                
                    System.out.println ("SESSION FOUND!!!");
                     log.info(db.ipAddr + "|" + "****SESSION FOUND KeepAlive USER:"+user);                
                    /* TODO output your page here. You may use following sample code. */
                     out.println("<!DOCTYPE html>");
                     out.println("<html>");
                     out.println("<head>");
                     out.println("<title>Servlet KeepSessionAlive</title>");            
                     out.println("</head>");
                     out.println("<body>");
                     out.println("<h1>Servlet KeepSessionAlive at " + request.getContextPath() + "</h1>");
                     out.println("</body>");
                     out.println("</html>");
            }
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
