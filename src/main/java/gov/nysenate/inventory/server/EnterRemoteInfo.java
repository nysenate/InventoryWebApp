package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.db.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.log4j.Logger;

@WebServlet(name = "EnterRemoteInfo", urlPatterns = { "/EnterRemoteInfo" })
public class EnterRemoteInfo extends HttpServlet {

    private static final Logger log = Logger.getLogger(EnterRemoteInfo.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String transJson = request.getParameter("trans");
        log.info("Entering remote info for transaction: " + transJson);

        Transaction trans = null;
        TransactionMapper mapper = new TransactionMapper();
        if (transJson == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            log.warn("Cannot enter remote info, transaction json was null");
            return;
        }

        try {
            trans = TransactionParser.parseTransaction(transJson);
            mapper.insertRemoteInfo(db, trans);
            HttpSession httpSession = request.getSession(false);
            String user = (String) httpSession.getAttribute("user");
            String pwd = (String) httpSession.getAttribute("pwd");
            EmailMoveReceipt emailMoveReceipt = null;

            if (trans.isRemoteDelivery()) {
                log.info("Entering remote delivery info");
                mapper.insertRemoteDeliveryRemoteUserInfo(db, trans);
                emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "delivery", trans);
            } else {
                log.info("Entering remote pickup info");
                mapper.insertRemotePickupRemoteUserInfo(db, trans);
                emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "pickup", trans);
            }
            if (emailMoveReceipt!=null) {
                Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
                threadEmailMoveReceipt.start();
            }
            
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error Inserting Remote Info: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        } catch (JsonSyntaxException e) {
            log.error("EnterRemote Json Syntax Exception: ", e);
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
