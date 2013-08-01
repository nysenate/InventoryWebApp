package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.logging.Logger;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Patil
 */
@WebServlet(name = "VerificationReports", urlPatterns = {"/VerificationReports"})
public class VerificationReports extends HttpServlet {

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
                log.info(db.ipAddr + "|" + "****SESSION NOT FOUND VerificationReports.processRequest ");                
               try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.ipAddr + "|" + "****SESSION NOT FOUND VerificationReports.processRequest could not process Fallback Username. Generic Username will be used instead.");                
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
            db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(VerificationReports.class.getName()).info(db.ipAddr+"|"+"Servlet VerificationReports : start");
            String jsonString = request.getParameter("barcodes");
            String cdlocat = request.getParameter("loc_code");
            String cdloctype = null;
            
            try {
              cdloctype = request.getParameter("cdloctype");
              /*
               * If the cdloctype that was passess is larger than one character, the value must
               * be wrong, so clear it out so the database procedure will simply look at location code
               * which is one to one in most cases..  Future enhancement might be to pass back an informational 
               * message to the client.
               */
              if (cdloctype!=null && cdloctype.trim().length()>1) {
                log.warn(db.ipAddr + "|" + "****Parameter cdloctype was passed by the Client with a value of "+cdloctype+" which is larger than 1 character. IGNORING. to VerificationReports.processRequest ");                
                cdloctype = null;
              }              
            }
            catch (Exception e) {
              /*
               * If Parameter cdloctype was not passed by the client, then simply pass null for the
               * value, the database procedure which is eventually called will handle nulls.
              */
              log.info(db.ipAddr + "|" + "****Parameter cdloctype was not passed by the Client to VerificationReports.processRequest ");                
              cdloctype = null;
            }
            
            System.out.println("json string from server = " + jsonString);
            // ArrayList<Integer> a= new ArrayList<Integer>();
            //         a= new Gson().fromJson(jsonString,ArrayList<Integer>);
            //   g.toJson(jsonString);
            //new Gson.
     /*        jstest a = new jstest();
             a.print(jsonString);
             JsonParser jsonParser = new JsonParser();
             JsonObject jo = (JsonObject)jsonParser.parse(jsonString);
             String barcodes=jo.getAsString();     
             a.print(barcodes);  */

            String barcodes[] = jsonString.split(",");
            
            int result = db.setBarcodesInDatabase(cdlocat, cdloctype, barcodes, userFallback);
            
            if (result == 0) {
                out.println("Database updated successfully");
                Logger.getLogger(VerificationReports.class.getName()).info(db.ipAddr+"|"+"Servlet VerificationReports : Database updated successfully");
            } else {
                out.println("Database not updated");
                Logger.getLogger(VerificationReports.class.getName()).info(db.ipAddr+"|"+"Servlet VerificationReports : Database not updated");
            }

            Logger.getLogger(VerificationReports.class.getName()).info(db.ipAddr+"|"+"Servlet VerificationReports : end");
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
