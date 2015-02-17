package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.item.ItemService;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.util.HttpUtils;
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

/**
 *
 * @author Patil
 */
@WebServlet(name = "ItemDetails", urlPatterns = {"/ItemDetails"})
public class ItemDetails extends HttpServlet {

    private static final Logger log = Logger.getLogger(ItemDetails.class);

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String barcode = request.getParameter("barcode_num");
        log.info("Searching for item with barcode: " + barcode);

        Item item = retrieveItem(db, barcode);
        out.write(Serializer.serialize(item));
    }

    private Item retrieveItem(DbConnect db, String barcode) {
        Item item = null;
        try {
            item = new ItemService().getItemByBarcode(db, barcode);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error searching for item.", ex);
        }
        return item;
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
