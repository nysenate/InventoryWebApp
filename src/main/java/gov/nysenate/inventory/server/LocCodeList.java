package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "LocCodeList", urlPatterns = {"/LocCodeList"})
public class LocCodeList extends HttpServlet {

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
                System.out.println ("****SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.ipAddr + "|" + "****SESSION NOT FOUND LocCodeList.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.ipAddr + "|" + "****SESSION NOT FOUND LocCodeList.processRequest could not process Fallback Username. Generic Username will be used instead.");                
                }             }
            else {
                System.out.println ("SESSION FOUND!!!!");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
                
            }
            db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(LocCodeList.class.getName()).info(db.ipAddr+"|"+"Servlet LocCodeList : start");
            String natype;
            try {
                natype = request.getParameter("NATYPE");
            } catch (Exception e) {
                natype = "ALL";
                Logger.getLogger(LocCodeList.class.getName()).info(db.ipAddr+"|"+"Servlet LocCodeList : " + "NATYPE SET TO ALL DUE TO EXCEPTION");
                System.out.println("NATYPE SET TO ALL DUE TO EXCEPTION");
            }
            if (natype == null) {
                natype = "ALL";
                System.out.println("NATYPE SET TO ALL DUE TO NULL");
            } else {
                System.out.println("NATYPE=" + natype);
            }

            ArrayList<String> LocCodeList = new ArrayList<String>();
         
            LocCodeList = db.getLocCodes(natype, userFallback);

            if (LocCodeList.size() == 0) {
                System.out.println("NO LOCATION CODES FOUND");
            }

            String json = new Gson().toJson(LocCodeList);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

            out.print(json);
            Logger.getLogger(LocCodeList.class.getName()).info(db.ipAddr+"|"+"Servlet LocCodeList : end");
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
