package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.location.LocationDAO;
import gov.nysenate.inventory.model.Item;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class ItemDAO
{
    private String SELECT_ITEM_BY_ID_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial \n" +
            "FROM fm12senxref items INNER JOIN fd12issue ON \n" +
            "items.nuxrefsn = fd12issue.nuxrefsn \n" +
            "WHERE items.nuxrefsn = ?";

    protected Item getItemById(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        Item item = run.query(conn, SELECT_ITEM_BY_ID_SQL, new ItemHandler(), id);
        getItemLocHistory(conn, item);
        return item;
    }

    private String SELECT_ITEM_BY_BARCODE_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial \n" +
            "FROM fm12senxref items INNER JOIN fd12issue ON \n" +
            "items.nuxrefsn = fd12issue.nuxrefsn \n" +
            "WHERE items.nusenate = ?";

    protected Item getItemByBarcode(Connection conn, String barcode) throws SQLException {
        QueryRunner run = new QueryRunner();
        Item item = run.query(conn, SELECT_ITEM_BY_BARCODE_SQL, new ItemHandler(), barcode);
        getItemLocHistory(conn, item);
        return item;
    }

    private String SELECT_ITEMS_BY_LOCATION_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial\n" +
            "FROM fm12senxref items INNER JOIN fd12issue issued ON \n" +
            "items.nuxrefsn = issued.nuxrefsn INNER JOIN sl16location loc ON \n" +
            "(loc.cdlocat = issued.cdlocatto AND loc.cdloctype = issued.cdloctypeto)\n" +
            "WHERE items.cdstatus = 'A' AND issued.cdstatus = 'A' AND cdlocat = ? AND cdloctype = ? ";

    protected List<Item> getItemsAtLocation(Connection conn, String locCode, String locType) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Item> items = run.query(conn, SELECT_ITEMS_BY_LOCATION_SQL, new ItemListHandler(), locCode, locType);
        for (Item item : items) {
            getItemLocHistory(conn, item);
        }
        return items;
    }

    private String SELECT_ITEMS_BY_REMOVAL_REQUEST_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial\n" +
            "FROM fm12senxref items INNER JOIN fd12issue issued ON \n" +
            "items.nuxrefsn = issued.nuxrefsn INNER JOIN fd12invadjreq rr\n" +
            "ON rr.nuxrefsn = items.nuxrefsn\n" +
            "WHERE rr.cdstatus = 'A' AND rr.nuxriareq = ?";


    protected List<Item> getItemsInRemovalRequest(Connection conn, int removalRequestNum) throws SQLException {

        QueryRunner run = new QueryRunner();
        List<Item> items = run.query(conn, SELECT_ITEMS_BY_REMOVAL_REQUEST_SQL, new ItemListHandler(), removalRequestNum);
        for (Item item : items) {
            getItemLocHistory(conn, item);
        }
        return items;
    }

    private void  getItemLocHistory(Connection conn, Item item) throws SQLException {

        if (item == null || item.getId() < 1) {
            return;
        }

        item.setLocations(new LocationDAO().getLocationsOfItem(conn, item.getId()));

    }

    private class ItemListHandler implements ResultSetHandler<List<Item>> {

        @Override
        public List<Item> handle(ResultSet rs) throws SQLException {
            List<Item> items = new ArrayList<Item>();
            while (rs.next()) {
                Item item = new Item(rs.getInt("nuxrefsn"), rs.getString("nusenate"));
                item.setSerialNumber(rs.getString("nuserial"));
                items.add(item);
            }
            return items;
        }
    }

    private class ItemHandler implements ResultSetHandler<Item> {

        /**
         * Returns null if no item is found.
         */
        @Override
        public Item handle(ResultSet rs) throws SQLException {
            Item item = new Item();
            while (rs.next()) {
                item.setId(rs.getInt("nuxrefsn"));
                item.setBarcode(rs.getString("nusenate"));
                item.setSerialNumber(rs.getString("nuserial"));
            }
            return item;
        }
    }
}
