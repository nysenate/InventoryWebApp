package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "LocationDetails", urlPatterns = {"/LocationDetails"})
public class LocationDetails extends HttpServlet {

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
                log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND LocationDetails.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND LocationDetails.processRequest could not process Fallback Username. Generic Username will be used instead.");                
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
            db.clientIpAddr=request.getRemoteAddr();
            Logger.getLogger(LocationDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet LocationDetails : start");
            String barcode_num = request.getParameter("barcode_num");
            System.out.println ("barcode_num:"+barcode_num);
          
            String details = db.getInvLocDetails(barcode_num, userFallback);
            System.out.println ("After getInvLocDetails:"+details);

            if (details.equals("no")) {
                out.println("{\"cdlocat\":\"" + "" + "\",\"delocat\":\"" + "Does not exist in system" + "\",\"adstreet1\":\"" + "" + "\",\"adstreet2\":\"" + "" + "\",\"adcity\":\"" + "" + "\",\"adstate\":\"" + "" + "\",\"adzipcode\":\"" + "" + "\",\"nucount\":\"" + "" + "\",\"cdrespctrhd\":\"" + "" + "\"}");

            } else {
                String model[] = details.split("\\|");
                
                //Psuedo JSON for now
                out.println("{\"cdlocat\":\"" + model[0] + "\",\"delocat\":\"" + model[1].replaceAll("\"", "&34;") + "\",\"adstreet1\":\"" + model[2].replaceAll("\"", "&34;") + "\",\"adstreet2\":\"" + model[3].replaceAll("\"", "&34;") + "\",\"adcity\":\"" + model[4].replaceAll("\"", "&34;") + "\",\"adstate\":\"" + model[5] + "\",\"adzipcode\":\"" + model[6].replaceAll("\"", "&#34;") + "\",\"nucount\":\"" + model[7] + "\",\"cdrespctrhd\":\"" + model[8] + "\"}");
            }

            Logger.getLogger(LocationDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet LocationDetails : end");
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
