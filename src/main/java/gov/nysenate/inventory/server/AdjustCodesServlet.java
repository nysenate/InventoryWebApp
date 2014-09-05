package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import gov.nysenate.inventory.dao.AdjustCodeService;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import java.util.List;

@WebServlet(name = "AdjustCodeServlet", urlPatterns = { "/AdjustCodeServlet" })
public class AdjustCodesServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(AdjustCodesServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        List<AdjustCode> adjustCodes = null;
        try {
            adjustCodes = new AdjustCodeService().getAdjustCodes(db);
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
        }

        out.print(new Gson().toJson(adjustCodes));
        out.close();
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
