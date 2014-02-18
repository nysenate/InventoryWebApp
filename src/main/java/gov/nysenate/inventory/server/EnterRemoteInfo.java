package gov.nysenate.inventory.server;

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

@WebServlet(name = "EnterRemoteInfo", urlPatterns = { "/EnterRemoteInfo" })
public class EnterRemoteInfo extends HttpServlet {

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws IOException {
        Logger log = Logger.getLogger(EnterRemoteInfo.class.getName());
        PrintWriter out = response.getWriter();
        DbConnect db = null;
        db = HttpUtils.getHttpSession(request, response, out);

        String transJson = request.getParameter("trans");
        Transaction trans = null;
        TransactionMapper mapper = new TransactionMapper();

        if (transJson == null) {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            return;
        }

        trans = TransactionParser.parseTransaction(URLDecoder.decode(transJson, "UTF-8"));
        try {
            mapper.insertRemoteInfo(db, trans);
            if (trans.isRemoteDelivery()) {
                mapper.insertRemoteDeliveryRemoteUserInfo(db, trans);
            } else {
                mapper.insertRemotePickupRemoteUserInfo(db, trans);
            }
        } catch (ClassNotFoundException | SQLException e) {
            log.error("Error Inserting Remote Info: ", e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws IOException {
        doGet(request, response);
    }
}
