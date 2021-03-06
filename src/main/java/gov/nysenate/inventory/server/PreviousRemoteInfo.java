package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.dao.TransactionMapper;
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

@WebServlet(name = "PreviousRemoteInfo", urlPatterns = { "/PreviousRemoteInfo" })
public class PreviousRemoteInfo extends HttpServlet {

    private static final Logger log = Logger.getLogger(PreviousRemoteInfo.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String nuxrpd = request.getParameter("nuxrpd");
        log.info("Requesting previous remote info for nuxrpd = " + nuxrpd);
        if (nuxrpd == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.info("Could not get previous remote info, nuxrpd was null.");
            return;
        }

        int orig = 0;
        TransactionMapper mapper = new TransactionMapper();
        Transaction original = null;
        try {
            orig = db.getOriginalTransaction(Integer.valueOf(nuxrpd));
            if (orig != 0) {
                original = mapper.queryTransaction(db, orig);
                out.write(Serializer.serialize(original));
                log.info("Previous transaction: " + Serializer.serialize(original));
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