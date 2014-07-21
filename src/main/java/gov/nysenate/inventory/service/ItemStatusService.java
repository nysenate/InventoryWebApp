package gov.nysenate.inventory.service;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.ItemStatusDAO;
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
        ItemStatusDAO dao = new ItemStatusDAO();

        boolean isInactive;
        boolean inTransit;
        boolean pendingRemoval;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            isInactive = dao.isItemInActive(conn, item.getId());
            inTransit = dao.isItemInTransit(conn, item.getBarcode());
            pendingRemoval = dao.isItemPendingRemoval(conn, item.getId());
        } finally {
            DbUtils.close(conn);
        }

        log.info("isincative= " + isInactive + " intransit= " + inTransit + " pending= " + pendingRemoval);
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
