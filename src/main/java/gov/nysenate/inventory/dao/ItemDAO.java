package gov.nysenate.inventory.dao;

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
        List<Item> items = run.query(conn, SELECT_ITEM_BY_ID_SQL, new ItemHandler(), id);
        return items.get(0);
    }

    private String SELECT_ITEM_BY_BARCODE_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial \n" +
            "FROM fm12senxref items INNER JOIN fd12issue ON \n" +
            "items.nuxrefsn = fd12issue.nuxrefsn \n" +
            "WHERE items.nusenate = ?";

    protected Item getItemByBarcode(Connection conn, String barcode) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Item> items = run.query(conn, SELECT_ITEM_BY_BARCODE_SQL, new ItemHandler(), barcode);
        return items.get(0);
    }

    private String SELECT_ITEMS_BY_LOCATION_SQL =
            "SELECT items.nuxrefsn, items.nusenate, nuserial\n" +
            "FROM fm12senxref items INNER JOIN fd12issue issued ON \n" +
            "items.nuxrefsn = issued.nuxrefsn INNER JOIN sl16location loc ON \n" +
            "(loc.cdlocat = issued.cdlocatto AND loc.cdloctype = issued.cdloctypeto)\n" +
            "WHERE items.cdstatus = 'A' AND issued.cdstatus = 'A' AND cdlocat = ? AND cdloctype = ? ";

    protected List<Item> getItemsAtLocation(Connection conn, String locCode, String locType) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Item> items = run.query(conn, SELECT_ITEMS_BY_LOCATION_SQL, new ItemHandler(), locCode, locType);
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
        List<Item> items = run.query(conn, SELECT_ITEMS_BY_REMOVAL_REQUEST_SQL, new ItemHandler(), removalRequestNum);
        return items;
    }

    private class ItemHandler implements ResultSetHandler<List<Item>> {

        @Override
        public List<Item> handle(ResultSet rs) throws SQLException {
            List<Item> items = new ArrayList<Item>();
            while (rs.next()) {
                int id = rs.getInt("nuxrefsn");
                String barcode = rs.getString("nusenate");
                Item item = new Item(id, barcode);

                String serialNum = rs.getString("nuserial");
                if (serialNum != null) {
                    item.setSerialNumber(serialNum);
                }

                items.add(item);
            }
            return items;
        }
    }
}
