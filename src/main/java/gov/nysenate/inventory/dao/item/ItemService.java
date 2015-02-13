package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.location.LocationService;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.Location;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class ItemService
{
    /**
     * Attempts to retrieve an item by its id.
     * @param id The item's id.
     * @return An item
     * @throws SQLException
     * @throws ClassNotFoundException
     * @see gov.nysenate.inventory.model.Item
     */
    public Item getItemById(DbConnect db, int id) throws SQLException, ClassNotFoundException {
        Item item;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            item = new ItemDAO().getItemById(conn, id);
            item.setStatus(serveStatus(conn, item));
            populateItemDetails(item, conn);
        } finally {
            DbUtils.close(conn);
        }
        return item;
    }

    /**
     * Attempts to retrieve an item by its barcode number.
     * @param barcode The item's barcode.
     * @return An item
     * @throws SQLException
     * @throws ClassNotFoundException
     * @see gov.nysenate.inventory.model.Item
     */
    public Item getItemByBarcode(DbConnect db, String barcode) throws SQLException, ClassNotFoundException {
        Item item;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            item = new ItemDAO().getItemByBarcode(conn, barcode);
            item.setStatus(serveStatus(conn, item));
            populateItemDetails(item, conn);
        } finally {
            DbUtils.close(conn);
        }
        return item;
    }

    /**
     * Populates the {@link gov.nysenate.inventory.model.Commodity} and
     * {@link gov.nysenate.inventory.model.Location} info for item's that exist
     * <p>Does nothing to an item which does not exist.</p>
     * @param item
     * @param conn
     * @throws SQLException
     */
    private void populateItemDetails(Item item, Connection conn) throws SQLException {
        if (item.getStatus() != ItemStatus.DOES_NOT_EXIST) {
            item.setCommodity(serveCommodity(conn, item.getId()));
            item.setLocation(serveLocation(conn, item.getId()));
        }
    }

    /**
     * Retrieves {@link gov.nysenate.inventory.model.Item Item's}
     * contained in a {@link gov.nysenate.inventory.model.RemovalRequest RemovalRequest}
     * @param conn
     * @param transactionNum The transaction number of the removal request.
     * @return List of items in removal request.
     * @throws SQLException
     */
    public List<Item> getItemsInRemovalRequest(Connection conn, int transactionNum) throws SQLException {
        List<Item> items = new ItemDAO().getItemsInRemovalRequest(conn, transactionNum);

        for (Item item : items) {
            item.setCommodity(serveCommodity(conn, item.getId()));
            item.setLocation(serveLocation(conn, item.getId()));
            item.setStatus(serveStatus(conn, item));
        }
        return items;
    }

    /**
     * Functions similar to {@link #getItemsInRemovalRequest(java.sql.Connection, int)}
     * except only the {@link gov.nysenate.inventory.model.Item} {@code id}, {@code barcode}
     * and {@code serialNumber} are retrieved.
     */
    public List<Item> getShallowItemsInRemovalRequest(Connection conn, int transactionNum) throws SQLException {
        List<Item> items = new ItemDAO().getItemsInRemovalRequest(conn, transactionNum);
        return items;
    }

    private ItemStatus serveStatus(Connection conn, Item item) throws SQLException {
        return new ItemStatusService().getItemStatus(conn, item);
    }

    private Location serveLocation(Connection conn, int id) throws SQLException {
        return new LocationService().getLocationOfItem(conn, id);
    }

    private Commodity serveCommodity(Connection conn, int id) throws SQLException {
        return new CommodityService().getCommodityByItemId(conn, id);
    }
}
