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

            String barcode_num = request.getParameter("barcode_num");
            DbConnect db = new DbConnect();
            String details = db.getInvLocDetails(barcode_num);


            if (details.equals("no")) {

                out.println("Does not exist in system");
            } else {
                String model[] = details.split("\\|");

                //P_LOC_CODE||','||V_DELOCAT||','||V_ADSTREET1||','||V_ADSTREET2||','||V_ADCITY||','||V_ADSTATE||','||V_ADZIPCODE;
                out.println(" Location Code   :  " + model[0] + "\n V_DELOCAT :  " + model[1] + "\n V_ADSTREET1 : " + model[2] + "\n V_ADSTREET2  :    " + model[3] + "\n V_ADCITY  :    " + model[4] + "\n V_ADSTATE :    " + model[5] + "\n V_ADZIPCODE  :    " + model[6]);
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
