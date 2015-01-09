package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.util.HttpUtils;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "LocCodeList", urlPatterns = {"/LocCodeList"})
public class LocCodeList extends HttpServlet {

    private static final Logger log = Logger.getLogger(LocCodeList.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(request, HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        log.info("Requesting list of all locations.");

        try {
            ArrayList<Location> locations = new ArrayList<Location>();
            locations = db.getLocCodes();

            log.info("Received info for " + locations.size() + " locations");

            String json = new Gson().toJson(locations);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.write(json);

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
