package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Pickup;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(name = "GetAllPickups", urlPatterns = { "/GetAllPickups" })
public class GetAllPickups extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) {
        Logger log = Logger.getLogger(GetAllPickups.class.getName());

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

        List<Pickup> pickups = null;
        try {
            pickups = db.getAllValidPickups();
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetAllPickups Exception: ", ex);
        }

        Gson gson = new Gson();
        out.print(gson.toJson(pickups));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) {
        doGet(request, response);
    }
}
