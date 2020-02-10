package gov.nysenate.inventory.server;

import com.google.gson.JsonParseException;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.removalrequest.RemovalRequestService;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.http.HttpStatus;
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
import java.util.*;

/**
 * Servlet that handles all http requests related to {@link RemovalRequest}.
 */
@WebServlet (name = "RemovalRequest", urlPatterns = {"/RemovalRequest"})
public class RemovalRequestServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(RemovalRequestServlet.class.getName());

    /**
     * Handles querying of Removal Requests.
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        // Keeping the code below commented out for future testing purposes as needed
        //System.out.println("RemovalRequest GET");

/*        Map params = request.getParameterMap();
        Iterator i = params.keySet().iterator();
        while ( i.hasNext() ) {
            String key = (String) i.next();
            String value = ((String[]) params.get(key))[0];
            Logger.getLogger(this.getClass()).info("         RemovalRequest Parameter: " +key + " : " + value );
            System.out.println("         RemovalRequest Parameter: " +key + " : " + value );
        }*/

//        Logger.getLogger(this.getClass()).info("RemovalRequest status:"+request.getParameter("status") +" " + "id: "+request.getParameter("id"));
//        System.out.println("RemovalRequest status:"+request.getParameter("status") +" " + "id: "+request.getParameter("id"));

        if (request.getParameter("status") != null) {
           //Logger.getLogger("RemovalRequest "+"status:"+request.getParameter("status")+" ");
            //System.out.println("RemovalRequest "+"status:"+request.getParameter("status")+" ");

            getRemovalRequestsByStatus(db, request, response, out);
        } else if (request.getParameter("id") != null) {
            //System.out.println("RemovalRequest "+("id: "+request.getParameter("id")+" "));
            //Logger.getLogger("RemovalRequest "+("id: "+request.getParameter("id")+" "));
            getRemovalRequestById(db, request, response, out);
        }
    }

    private void getRemovalRequestsByStatus(DbConnect db, HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        List<RemovalRequest> rrs = new ArrayList<RemovalRequest>();
        String[] status = request.getParameterValues("status");
        String user = request.getParameter("user");
        try {
            for (String stat : status) {
                List<RemovalRequest> rr;
                rr = getRemovalRequests(stat, user, db);
                rrs.addAll(rr);
            }
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        out.write(Serializer.serialize(rrs));
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
            rrs = onlyCurrentUsersRequests(user, rrs);
        }

        return rrs;
    }

    private List<RemovalRequest> onlyCurrentUsersRequests(String user, List<RemovalRequest> rrs) {
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

        out.write(Serializer.serialize(rr));
    }

    /**
     * Handles updating of Removal Requests.
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        String json = request.getParameter("RemovalRequest");

        // Keeping the code below commented out for future testing purposes as needed
        RemovalRequest rr = null;

        try {
            rr = Serializer.deserialize(json, RemovalRequest.class).get(0);
        } catch (JsonParseException e) {
            response.setStatus(HttpStatus.SC_BAD_REQUEST);
            log.error("Removal Reqeust json was invalid: " + json);
            return;
        }

        RemovalRequestService service = new RemovalRequestService();

        try {
            if (rr.getTransactionNum() == 0) {
                service.insertRemovalRequest(db, rr);
            } else if (allItemsDeleted(rr)){
                service.deleteRemovalRequest(db, rr);
            } else {
                service.updateRemovalRequest(db, rr);
            }
            response.setStatus(HttpStatus.SC_OK);
        } catch (SQLException | ClassNotFoundException e) {
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
            log.error("RemovalRequestServlet.doPost error:"+e.getMessage(), e);
        }

        out.write(Serializer.serialize(rr));
    }

    private boolean allItemsDeleted(RemovalRequest rr) {
        for (Item i: rr.getItems()) {
            if (i.getStatus() != ItemStatus.INACTIVE) {
                return false;
            }
        }
        return true;
    }

    private void testParameters (HttpServletRequest req) {
        Enumeration<String> parameterNames = req.getParameterNames();

        while (parameterNames.hasMoreElements()) {

            String paramName = parameterNames.nextElement();
            System.out.println("Param: "+paramName);

            String[] paramValues = req.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                String paramValue = paramValues[i];
                System.out.println("      " + paramValue);
            }

        }
    }

}
