package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.ArrayList;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

import com.google.gson.Gson;

@WebServlet(name = "GetPickup", urlPatterns = { "/GetPickup" })
public class GetPickup extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(CancelPickup.class.getName());
        response.setContentType("text/html;charset=UTF-8");

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        int nuxrpd;
        Transaction pickup = null;
        String userFallback = request.getParameter("userFallback");
        String nuxrpdString = request.getParameter("nuxrpd");
        if (nuxrpdString == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        TransactionMapper mapper = new TransactionMapper();
        nuxrpd = Integer.parseInt(nuxrpdString);
        try {
            pickup = mapper.queryTransaction(db, nuxrpd);
        } catch (SQLException ex) {
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            log.error("GetPickup: SQL Exception: ", ex);
            return;
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }

        if (pickup == null) {
            log.error("GetPickup: Error Parsing pickup");
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            return;
        }

        out.print(new Gson().toJson(pickup));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
