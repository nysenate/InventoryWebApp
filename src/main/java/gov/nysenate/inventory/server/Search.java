package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.history.InventoryHistoryService;
import gov.nysenate.inventory.dao.item.ItemService;
import gov.nysenate.inventory.dto.SearchDto;
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
import java.util.Date;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Search", urlPatterns = {"/Search"})
public class Search extends HttpServlet
{
    private static final Logger log = Logger.getLogger(Search.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException {
        response.setContentType("application/json");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        String barcode = request.getParameter("barcode_num");
        log.info("Searching for item with barcode: " + barcode);

        Item item = retrieveItem(db, barcode);
        Date lastInventoried = retrieveLastInventoried(db, item);
        SearchDto dto = new SearchDto(item, lastInventoried);
        out.write(Serializer.serialize(dto));
    }

    private Item retrieveItem(DbConnect db, String barcode) {
        ItemService itemService = new ItemService();
        Item item = null;
        try {
            item = itemService.getItemByBarcode(db, barcode);
        } catch (SQLException | ClassNotFoundException ex) {
            log.error("Error searching for item.", ex);
        }
        return item;
    }

    private Date retrieveLastInventoried(DbConnect db, Item item) {
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
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException
    {
        processRequest(request, response);
    }

}
