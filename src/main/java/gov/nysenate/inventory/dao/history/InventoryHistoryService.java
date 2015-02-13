package gov.nysenate.inventory.dao.history;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Item;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.Date;

public class InventoryHistoryService {

    /**
     * @param db
     * @param item
     * @return The date an {@link gov.nysenate.inventory.model.Item} was last inventoried.
     * @throws SQLException
     * @throws ClassNotFoundException
     */
    public Date getDateItemLastInventoried(DbConnect db, Item item) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            return new InventoryHistoryDao().getDateItemLastInventoried(conn, item.getId());
        } finally {
            DbUtils.close(conn);
        }
    }
}
