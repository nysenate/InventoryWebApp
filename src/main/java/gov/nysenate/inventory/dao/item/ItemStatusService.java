package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import org.apache.commons.dbutils.DbUtils;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.SQLException;

public class ItemStatusService
{
    private static final Logger log = Logger.getLogger(ItemStatusService.class.getName());

    public ItemStatus getItemStatus(DbConnect db, Item item) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            return getItemStatus(conn, item);
        } finally {
            DbUtils.close(conn);
        }
    }

    // TODO: check for deleted status?

    public ItemStatus getItemStatus(Connection conn, Item item) throws SQLException {
        if (item.getId() == 0) {
            return ItemStatus.DOES_NOT_EXIST;
        }

        ItemStatusDAO dao = new ItemStatusDAO();
        boolean isInactive = dao.isItemInactive(conn, item.getId());
        boolean inTransit = dao.isItemInTransit(conn, item.getBarcode());
        boolean pendingRemoval = dao.isItemPendingRemoval(conn, item.getId());

        ItemStatus status;
        if (isInactive) {
            status = ItemStatus.INACTIVE;
        } else if (inTransit) {
            status = ItemStatus.IN_TRANSIT;
        } else if (pendingRemoval) {
            status = ItemStatus.PENDING_REMOVAL;
        } else {
            status = ItemStatus.ACTIVE;
        }

        return status;
    }
}
