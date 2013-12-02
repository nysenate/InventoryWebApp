package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;

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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(GetAllPickups.class.getName());

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        TransactionMapper transMap = new TransactionMapper();
        Collection<Transaction> trans = null;
        try {
            trans = transMap.queryAllValidTransactions(db);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetAllPickups Exception: ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }

        Gson gson = new Gson();
        out.print(gson.toJson(trans));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
