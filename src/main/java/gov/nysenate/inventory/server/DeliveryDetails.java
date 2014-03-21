package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import com.google.gson.reflect.TypeToken;
import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.IOException;
import java.io.PrintWriter;
import java.lang.reflect.Type;
import java.util.ArrayList;
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
 * @author Patil
 */
@WebServlet(name = "DeliveryDetails", urlPatterns = {"/DeliveryDetails"})
public class DeliveryDetails extends HttpServlet {

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
                log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND DeliveryDetails.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.clientIpAddr + "|" + "****SESSION NOT FOUND DeliveryDetails.processRequest could not process Fallback Username. Generic Username will be used instead.");                
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
            Logger.getLogger(DeliveryDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet DeliveryDetails : Start");

            String nuxrpd = request.getParameter("NUXRPD");
            System.out.println("nuxrpickup : " + nuxrpd);

            // populate the list from the database and also get the details like other activities
            
            ArrayList<InvItem> deliveryDetails = new ArrayList<InvItem>();
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();
            Type listOfTestObject = new TypeToken<List<InvItem>>(){}.getType();
            
            deliveryDetails = db.getDeliveryDetails(nuxrpd, userFallback);
            String json = gson.toJson(deliveryDetails);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            System.out.println("json : " + json);
            out.print(json);
            Logger.getLogger(DeliveryDetails.class.getName()).info(db.clientIpAddr+"|"+"Servlet DeliveryDetails : end");
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
