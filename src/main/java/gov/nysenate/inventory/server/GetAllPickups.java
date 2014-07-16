package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.dao.TransactionMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.Collection;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(name = "GetAllPickups", urlPatterns = { "/GetAllPickups" })
public class GetAllPickups extends HttpServlet {

    private static final Logger log = Logger.getLogger(GetAllPickups.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);
        log.info("Getting info for all pickups");

        boolean wantIncompleteRemotes = false;
        String incRemotes = request.getParameter("incompleteRemote");
        if (incRemotes != null) {
            wantIncompleteRemotes = Boolean.valueOf(incRemotes);
            log.info("Include remote transactions? = " + incRemotes);
        }

        Collection<Transaction> trans = null;
        TransactionMapper transMap = new TransactionMapper();
        try {
            if (wantIncompleteRemotes) {
                trans = transMap.queryDeliveriesMissingRemoteInfo(db);
            } else {
                trans = transMap.queryAllValidTransactions(db);
            }
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetAllPickups Exception: ", ex);
        } catch (ClassNotFoundException e) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("Error getting oracle jdbc driver: ", e);
        }

        log.info("Recieved info for " + trans.size() + " pickups.");
        Gson gson = new Gson();
        out.print(gson.toJson(trans));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
