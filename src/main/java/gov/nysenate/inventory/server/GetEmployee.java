package gov.nysenate.inventory.server;

import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "GetEmployee", urlPatterns = { "/GetEmployee" })
public class GetEmployee extends HttpServlet {

    private static final Logger log = Logger.getLogger(GetEmployee.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String[] empInfo = new String[3];
        String nalast = request.getParameter("nalast");
        log.info("Getting employee info for: " + nalast);

        if (nalast == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Cannot get employee info, nalast was null.");
            return;
        }

        try {
            empInfo = db.getEmployeeInfo(nalast);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetPickup: SQL Exception: ", ex);
            return;
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
        String json = ("{" + "\"nafirst\":" + empInfo[0] + "," + "\"nalast\":" + empInfo[1] + "," + "\"cdrespctrhd\":" + empInfo[2] + "}");
        out.print(json);
        log.info("Employee info = " + json);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
