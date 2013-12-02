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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(CancelPickup.class.getName());
        response.setContentType("text/html;charset=UTF-8");

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        String[] empInfo = new String[3];
        String nalast = request.getParameter("nalast");
        if (nalast == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
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
        System.out.println(json);
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
