package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.JsonParseException;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.RemovalRequestService;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.RemovalRequestParser;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

/**
 * Servlet that handles all http requests related to {@link RemovalRequest}.
 */
@WebServlet (name = "RemovalRequest", urlPatterns = {"/RemovalRequest"})
public class RemovalRequestServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(RemovalRequestServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        super.doGet(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);
        String json = request.getParameter("RemovalRequest");

        RemovalRequest rr = null;
        try {
            rr = RemovalRequestParser.parseRemovalRequest(json);
        } catch (JsonParseException e) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            log.error("Removal Reqeust json was invalid: " + json);
            return;
        }

        RemovalRequestService service = new RemovalRequestService();
        try {
            if (rr.getTransactionNum() == 0) {
                service.insertRemovalRequest(db, rr);
            } else {
                service.updateRemovalRequest(db, rr);
            }
        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            log.error(e.getMessage(), e);
        }

        out.write(new Gson().toJson(rr));
    }

}
