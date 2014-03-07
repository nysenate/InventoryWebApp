package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;

@WebServlet(name = "PreviousRemoteInfo", urlPatterns = { "/PreviousRemoteInfo" })
public class PreviousRemoteInfo extends HttpServlet {

    private Logger log = Logger.getLogger(PreviousRemoteInfo.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        String nuxrpd = request.getParameter("nuxrpd");
        if (nuxrpd == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        int orig = 0;
        TransactionMapper mapper = new TransactionMapper();
        Transaction original = null;
        try {
            orig = db.getOriginalTransaction(Integer.valueOf(nuxrpd));
            if (orig != 0) {
                original = mapper.queryTransaction(db, orig);
                out.write(original.toJson());
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error getting original nuxrpd: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

    }

    @Override
    protected void doPut(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}