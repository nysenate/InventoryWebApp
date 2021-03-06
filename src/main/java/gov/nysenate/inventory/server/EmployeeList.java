package gov.nysenate.inventory.server;


import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;

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
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
            String employeeName = request.getParameter("employeeName");
            String cdempstatus = request.getParameter("cdempstatus");
            // Only show Active Employees if no Employee Status is passed.
            if (cdempstatus == null || cdempstatus.length() == 0) {
                cdempstatus = "A";
            }
            log.info("Requesting list of employees with status = " + cdempstatus);

            ArrayList<Employee> employeeList = db.getEmployeeList(employeeName, cdempstatus);

            log.info("Found " + employeeList.size() + " valid employees.");
            String json = Serializer.serialize(employeeList);
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
