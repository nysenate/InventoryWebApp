package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.history.InventoryHistoryService;
import gov.nysenate.inventory.dao.item.ItemService;
import gov.nysenate.inventory.dto.ItemInventoriedDetails;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.Serializer;
import org.apache.http.HttpStatus;
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
import java.util.Date;

/**
 * Api for retrieving information on an {@link gov.nysenate.inventory.model.Item}.
 *
 * <p>Required Request Parameteres:
 * <ul>
 *     <li>barcode (String) - The item's barcode number</li>
 * </ul>
 * Optional Request Parameters:
 * <ul>
 *     <li>inventoried_date (boolean) (default:false) - If true, a {@link gov.nysenate.inventory.dto.ItemInventoriedDetails}
 *     is returned containing item info and the date it was inventoried.</li>
 * </ul>
 * </p>
 */
@WebServlet(name = "Item", urlPatterns = {"/Item"})
public class ItemServlet extends HttpServlet
{
    private static final Logger log = Logger.getLogger(ItemServlet.class.getName());

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String barcode = request.getParameter("barcode");
        boolean requestedInventoriedDate = Boolean.valueOf(request.getParameter("inventoried_date"));

        Item item = retrieveItem(response, db, barcode);
        if (requestedInventoriedDate) {
            Date inventoriedDate = retrieveInventoriedDate(db, item);
            out.write(Serializer.serialize(new ItemInventoriedDetails(item, inventoriedDate)));
        }
        else {
            out.write(Serializer.serialize(item));
        }
    }

    private Item retrieveItem(HttpServletResponse response, DbConnect db, String barcode) {
        Item item = null;
        try {
            item =  new ItemService().getItemByBarcode(db, barcode);
        } catch (SQLException | ClassNotFoundException e) {
            log.error("Error getting info for item with barcode = " + barcode, e);
            response.setStatus(HttpStatus.SC_INTERNAL_SERVER_ERROR);
        }
        return item;
    }

    private Date retrieveInventoriedDate(DbConnect db, Item item) {
        Date lastInventoried = null;
        InventoryHistoryService inventoryHistoryService = new InventoryHistoryService();
        try {
            lastInventoried = inventoryHistoryService.getDateItemLastInventoried(db, item);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error getting last inventoried date.", ex);
        }
        return lastInventoried;
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        doGet(request, response);
    }
}
