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
@WebServlet(name = "ItemDetails", urlPatterns = {"/ItemDetails"})
public class ItemDetails extends HttpServlet {

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
        //System.out.println("ItemDetails Servlet: start");
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;
            String userFallback = null;
            if (httpSession==null) {
                System.out.println ("****SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND ItemDetails.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND ItemDetails.processRequest could not process Fallback Username. Generic Username will be used instead.");                
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
            Logger.getLogger(ItemDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet ItemDetails : start");
            String barcode_num = request.getParameter("barcode_num");
            String details = db.getDetails(barcode_num, userFallback);

            if (details.equals("no")) {

                out.println("Does not exist in system");
            } else {
                String model[] = details.split("\\|");

                //Psuedo JSON for now
                out.println("{\"nusenate\":\"" + model[0] + "\",\"nuxrefsn\":\"" + model[1] + "\",\"dtissue\":\"" + model[3] + "\",\"cdlocatto\":\"" + model[4] + "\",\"cdloctypeto\":\"" + model[5] + "\",\"cdcategory\":\"" + model[6] + "\",\"adstreet1to\":\"" + model[7].replaceAll("\"", "&#34;") + "\",\"decommodityf\":\"" + model[8].replaceAll("\"", "&#34;")  + "\",\"cdlocatfrom\":\"" + model[9] + "\",\"cdstatus\":\"" + model[10] + "\",\"cdintransit\":\"" + model[12] + "\"}");
                Logger.getLogger(ItemDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet ItemDetails : end");
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
