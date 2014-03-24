package gov.nysenate.inventory.server;

import org.apache.log4j.Logger;

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
 * @author Patil
 */
@WebServlet(name = "Search", urlPatterns = {"/Search"})
public class Search extends HttpServlet
{

    private static final Logger log = Logger.getLogger(Search.class.getName());

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
                log.info("****SESSION NOT FOUND Search.processRequest ");
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info("****SESSION NOT FOUND Search.processRequest could not process Fallback Username. Generic Username will be used instead.");
                } 
                out.println("Session timed out");
                return;
            }
            else {
                long  lastAccess = (System.currentTimeMillis() - httpSession.getLastAccessedTime());
                System.out.println ("SESSION FOUND!!!!LAST ACCESSED:"+this.convertTime(lastAccess));
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
                
            }
            Logger.getLogger(Search.class.getName()).info("Servlet Search : start");
            String barcode_num = request.getParameter("barcode_num");
            System.out.println("Search Servlet  barcode_num " + barcode_num);
            
            String details = db.getDetails(barcode_num, userFallback);
            String commodityCode = db.getItemCommodityCode(barcode_num, userFallback);

            if (details.equals("no")) {
                out.println("Does not exist in system");
            }
            else {
                details += "|" + commodityCode;
                String model[] = details.split("\\|");
                String deadjust = "";
                if (model.length>11) {
                    deadjust = model[11].replaceAll("\"", "&#34;");
                }
                
                System.out.println("DETAILS:"+details+" MODEL LENGTH:"+model.length);
                // out.println(" Model   :  "+model[0]+"\n Location :  "+model[1]+"\n Manufacturer : "+model[2]+"\n Signed By  :    "+model[3]);
                //V_NUSENATE,V_NUXREFSN,V_NUSERIAL,V_DTISSUE,V_CDLOCATTO,V_CDLOCTYPETO,V_CDCATEGORY,V_DECOMMODITYF
                //out.println(" Barcode   :  "+model[0]+"\n NUXREFSN :  "+model[1]+"\n NUSERIAL : "+model[2]+"\n DTISSUE  :    "+model[3]+"\n CDLOCATTO  :    "+model[4]+"\n CDLOCTYPETO :    "+model[5]+"\n CDCATEGORY  :    "+model[6]+"\n DECOMMODITYF  :    "+model[7]);

                //Psuedo JSON for now               

                out.println("{\"nusenate\":\"" + model[0] + "\",\"nuxrefsn\":\"" + model[1] + "\",\"dtissue\":\"" + model[3] + "\",\"cdlocatto\":\"" + model[4] + "\",\"cdloctypeto\":\"" + model[5]
                        + "\",\"cdcategory\":\"" + model[6] + "\",\"adstreet1to\":\"" + model[7].replaceAll("\"", "&#34;") + "\",\"decommodityf\":\"" + model[8].replaceAll("\"", "&#34;")
                        + "\",\"cdstatus\":\"" + model[10] + "\",\"deadjust\":\"" + deadjust + "\",\"dtlstinvntry\":\"" + model[13] + "\",\"commodityCd\":\"" + model[14] +  "\",\"nuserial\":\"" + model[2] + "\"}");
                System.out.println("**Details** " + details);
             }


            Logger.getLogger(Search.class.getName()).info("Servlet Search : end");

        } finally {
            out.close();
        }
    } // processRequest()
    public String convertTime(long time) {
        long secDiv = 1000;        
        long minDiv = 1000 * 60;
        long hourDiv = 1000 * 60 *60;
        long minutes = time % hourDiv;
        long seconds = minutes % minDiv;
        int hoursConverted = (int)(time/hourDiv);
        int minutesConverted = (int)(minutes/minDiv);
        int secondsConverted = (int)(seconds/secDiv);
      
        StringBuffer  returnTime = new StringBuffer();
        if (hoursConverted>0) {
            returnTime.append("Hours:");
            returnTime.append(hoursConverted);
            returnTime.append(" ");
        }
        if (hoursConverted>0||minutesConverted>0) {
            returnTime.append("Minutes:");
            returnTime.append(minutesConverted);
            returnTime.append(" ");
        }
        returnTime.append("Seconds:");
        returnTime.append(secondsConverted);
        returnTime.append(" ");
        
        return returnTime.toString();
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
    } // doGet()



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
    } // doPost()



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
