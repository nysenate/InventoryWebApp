package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Location;
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
import java.sql.SQLException;
import java.util.List;

/**
 *
 * @author Patil
 */
@WebServlet(name = "LocCodeList", urlPatterns = {"/LocCodeList"})
public class LocCodeList extends HttpServlet {

    private static final Logger log = Logger.getLogger(LocCodeList.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        log.info("Requesting list of all locations.");
        List<Location> locations = null;
        try {
            locations = db.getLocCodes(db.getDbConnection());
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error getting Connection", e);
        }
        log.info("Received info for " + locations.size() + " locations");
        response.setContentType("application/json");
        out.write(Serializer.serialize(locations));
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
