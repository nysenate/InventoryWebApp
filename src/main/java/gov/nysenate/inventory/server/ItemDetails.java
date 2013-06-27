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
            DbConnect db = new DbConnect();
             db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(ItemDetails.class.getName()).info(db.ipAddr+"|"+"Servlet ItemDetails : start");
            //System.out.println("ItemDetails Servlet: getParameter");
            String barcode_num = request.getParameter("barcode_num");
            //System.out.println("ItemDetails Servlet: getParameter");
            //int barcode = Integer.valueOf(barcode_num);
        
            String details = db.getDetails(barcode_num);
            //System.out.println("ItemDetails Servlet: details:" + details);


            if (details.equals("no")) {

                out.println("Does not exist in system");
            } else {
                String model[] = details.split("\\|");

//                System.out.println(model.length+": CDINVTRANS:"+model[model.length-1]);
                
                // out.println(" Model   :  "+model[0]+"\n Location :  "+model[1]+"\n Manufacturer : "+model[2]+"\n Signed By  :    "+model[3]);
                //V_NUSENATE,V_NUXREFSN,V_NUSERIAL,V_DTISSUE,V_CDLOCATTO,V_CDLOCTYPETO,V_CDCATEGORY,V_DECOMMODITYF
                //out.println(" " + model[0] + " : " + model[8]);
                //Psuedo JSON for now
                out.println("{\"nusenate\":\"" + model[0] + "\",\"nuxrefsn\":\"" + model[1] + "\",\"dtissue\":\"" + model[3] + "\",\"cdlocatto\":\"" + model[4] + "\",\"cdloctypeto\":\"" + model[5] + "\",\"cdcategory\":\"" + model[6] + "\",\"adstreet1to\":\"" + model[7].replaceAll("\"", "&#34;") + "\",\"decommodityf\":\"" + model[8].replaceAll("\"", "&#34;")  + "\",\"cdlocatfrom\":\"" + model[9] + "\",\"cdintransit\":\"" + model[12] + "\"}");
                Logger.getLogger(ItemDetails.class.getName()).info(db.ipAddr+"|"+"Servlet ItemDetails : end");
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
