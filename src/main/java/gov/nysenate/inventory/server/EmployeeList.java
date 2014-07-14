package gov.nysenate.inventory.server;


import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.model.Employee;
import com.google.gson.Gson;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author HEITNER
 */
@WebServlet(urlPatterns = {"/EmployeeList"})
public class EmployeeList extends HttpServlet {

    private static final Logger log = Logger.getLogger(EmployeeList.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        
        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);

            String employeeName = request.getParameter("employeeName");
            String cdempstatus = request.getParameter("cdempstatus");
            // Only show Active Employees if no Employee Status is passed.
            if (cdempstatus == null || cdempstatus.length() == 0) {
                cdempstatus = "A";
            }
            log.info("Requesting list of employees with status = " + cdempstatus);

            ArrayList<Employee> employeeList = db.getEmployeeList(employeeName, cdempstatus);

            log.info("Found " + employeeList.size() + " valid employees.");
            String json = new Gson().toJson(employeeList);
            out.println(json);
        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
