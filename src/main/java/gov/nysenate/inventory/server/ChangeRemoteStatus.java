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

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(ChangeRemoteStatus.class.getName());

        DbConnect db = null;
        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        String transJson = request.getParameter("trans");
        String appUser = request.getParameter("user");

        Transaction trans = null;
        TransactionMapper mapper = new TransactionMapper();

        if (transJson != null && appUser != null) {
            try {
                trans = TransactionParser.parseTransaction(transJson);
                mapper.updateTransaction(db, trans, appUser);
            } catch (ClassNotFoundException | SQLException e) {
                log.error("Error updating transaction: ", e);
            } catch (JsonSyntaxException e) {
                log.error("ChangeRemote Json Syntax Exception: ", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }

}
