package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author HEITNER
 */
@WebServlet(urlPatterns = {"/EmployeeList"})
public class EmployeeList extends HttpServlet {

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
             DbConnect db = new DbConnect();
              db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(EmployeeList.class.getName()).info(db.ipAddr+"|"+"Servlet EmployeeList : start");
            String employeeName = request.getParameter("employeeName");
            String cdempstatus = request.getParameter("cdempstatus");
            // Only show Active Employees if no Employee Status is passed.
            if (cdempstatus == null || cdempstatus.length() == 0) {
                cdempstatus = "A";
            }
           
            ArrayList<Employee> employeeList = db.getEmployeeList(employeeName, cdempstatus);
            String json = new Gson().toJson(employeeList);
            out.println(json);
            Logger.getLogger(EmployeeList.class.getName()).info(db.ipAddr+"|"+"Servlet EmployeeList : end");
        } catch (Exception e) {
            out.println("Failure " + e.getMessage());
            Logger.getLogger(EmployeeList.class.getName()).fatal(request.getRemoteAddr()+"|"+"Exception in Servlet EmployeeList : " + e.getMessage());
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
