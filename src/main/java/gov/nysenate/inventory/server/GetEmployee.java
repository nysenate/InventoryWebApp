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

        PrintWriter out = null;
        DbConnect db = null;
        try {
            out = response.getWriter();
            db = HttpUtils.getHttpSession(request, out);
        } catch (IOException e) {
            e.printStackTrace();
        }
        if (out.toString().contains("Session timed out")) {
            response.setStatus(HttpUtils.SC_SESSION_TIMEOUT);
        }

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
