package gov.nysenate.inventory.dao;

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
    public Item getItemById(DbConnect db, int id) throws SQLException, ClassNotFoundException {
        Item item;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            item = new ItemDAO().getItemById(conn, id);

            item.setCommodity(serveCommodity(conn, item.getId()));
            item.setLocation(serveLocation(conn, item.getId()));
            item.setStatus(serveStatus(conn, item));
        } finally {
            DbUtils.close(conn);
        }
        return item;
    }

    public Item getItemByBarcode(DbConnect db, String barcode) throws SQLException, ClassNotFoundException {
        Item item;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            item = new ItemDAO().getItemByBarcode(conn, barcode);

            item.setCommodity(serveCommodity(conn, item.getId()));
            item.setLocation(serveLocation(conn, item.getId()));
            item.setStatus(serveStatus(conn, item));
        } finally {
            DbUtils.close(conn);
        }
        return item;
    }

    public List<Item> getItemsInRemovalRequest(Connection conn, int transactionNum) throws SQLException {
        List<Item> items = new ItemDAO().getItemsInRemovalRequest(conn, transactionNum);

        for (Item item : items) {
            item.setCommodity(serveCommodity(conn, item.getId()));
            item.setLocation(serveLocation(conn, item.getId()));
            item.setStatus(serveStatus(conn, item));
        }
        return items;
    }

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
