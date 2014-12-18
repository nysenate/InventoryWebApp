package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.item.ItemService;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.http.HttpStatus;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

@WebServlet(name = "Item", urlPatterns = {"/Item"})
public class ItemServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ItemServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

        String barcode = request.getParameter("barcode");

        ItemService service = new ItemService();
        try {
            Item item = service.getItemByBarcode(db, barcode);
            out.write(new Gson().toJson(item));
        } catch (SQLException | ClassNotFoundException e) {
            log.error(e.getMessage(), e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
