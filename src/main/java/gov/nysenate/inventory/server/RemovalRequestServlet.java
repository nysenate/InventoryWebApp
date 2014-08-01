package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
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
import java.util.ArrayList;
import java.util.List;

/**
 * Servlet that handles all http requests related to {@link RemovalRequest}.
 */
@WebServlet (name = "RemovalRequest", urlPatterns = {"/RemovalRequest"})
public class RemovalRequestServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(RemovalRequestServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        if (request.getParameter("status") != null) {
            getRemovalRequestsByStatus(db, request, response, out);
        } else if (request.getParameter("id") != null) {
            getRemovalRequestById(db, request, response, out);
        }
    }

    private void getRemovalRequestsByStatus(DbConnect db, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        List<RemovalRequest> rrs = null;
        String status = request.getParameter("status");
        String user = request.getParameter("user");
        try {
            rrs = getRemovalRequests(status, user, db);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        out.write(new Gson().toJson(rrs));
    }

    private List<RemovalRequest> getRemovalRequests(String status, String user, DbConnect db) throws SQLException, ClassNotFoundException {
        RemovalRequestService service = new RemovalRequestService();
        List<RemovalRequest> rrs = null;
        switch (status) {
            case "PE": rrs = service.getShallowPending(db); break;
            case "SI": rrs =  service.getSubmittedToInventoryControl(db); break;
            case "SM": rrs =  service.getSubmittedToManagement(db); break;
            case "AP": rrs =  service.getApproved(db); break;
            case "RJ": rrs =  service.getRejected(db); break;
            case "CA": rrs =  null; // TODO: implement cancelled Removal Requests
        }

        if (rrs != null && user != null) {
            rrs = deleteThoseByOtherUsers(user, rrs);
        }

        return rrs;
    }

    private List<RemovalRequest> deleteThoseByOtherUsers(String user, List<RemovalRequest> rrs) {
        List<RemovalRequest> deleteList = new ArrayList<RemovalRequest>();
        for (RemovalRequest rr : rrs) {
            if (!rr.getEmployee().equalsIgnoreCase(user)) {
                deleteList.add(rr);
            }
        }
        rrs.removeAll(deleteList);
        return rrs;
    }

    private void getRemovalRequestById(DbConnect db, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        RemovalRequest rr = null;
        String id = request.getParameter("id");
        RemovalRequestService service = new RemovalRequestService();
        try {
            rr = service.getRemovalRequest(db, Integer.valueOf(id));
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }

        out.write(new Gson().toJson(rr));
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
