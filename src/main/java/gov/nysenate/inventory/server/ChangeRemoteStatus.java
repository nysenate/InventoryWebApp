package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.log4j.Logger;

@WebServlet(name = "ChangeRemoteStatus", urlPatterns = { "/ChangeRemoteStatus" })
public class ChangeRemoteStatus extends HttpServlet{

    private static final long serialVersionUID = 1L;
    private static final Logger log = Logger.getLogger(ChangeRemoteStatus.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String transJson = request.getParameter("trans");
        log.info("Updating remote status, pickup: " + transJson);

        Transaction trans = null;
        TransactionMapper mapper = new TransactionMapper();

        if (transJson != null) {
            try {
                trans = TransactionParser.parseTransaction(transJson);
                mapper.updateTransaction(db, trans);
            } catch (ClassNotFoundException | SQLException e) {
                log.error("Error updating remote status: ", e);
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
            } catch (JsonSyntaxException e) {
                log.error("ChangeRemote Json Syntax Exception: ", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            log.warn("Unable to update remote status, pickup json was null");
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
