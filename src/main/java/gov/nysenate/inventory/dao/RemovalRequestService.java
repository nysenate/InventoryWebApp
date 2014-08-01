package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RemovalRequestService {

    public RemovalRequest getRemovalRequest(DbConnect db, int id) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            return getRemovalRequest(conn, id);
        } finally {
            DbUtils.close(conn);
        }
    }

    public RemovalRequest getRemovalRequest(Connection conn, int id) throws SQLException, ClassNotFoundException {
        RemovalRequest rr;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();
        rr = rrDao.getRemovalRequst(conn, id);
        rr = populateRequest(conn, rr);

        return rr;
    }

    public List<RemovalRequest> getPending(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getPendingRequests(conn);
            rrs = populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public List<RemovalRequest> getShallowPending(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getPendingRequests(conn);
            rrs = shallowPopulateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public List<RemovalRequest> getSubmittedToInventoryControl(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getSubmittedToInventoryControl(conn);
            rrs = populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public List<RemovalRequest> getSubmittedToManagement(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getSubmittedToManagement(conn);
            rrs = populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public List<RemovalRequest> getApproved(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getApproved(conn);
            rrs = populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public List<RemovalRequest> getRejected(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getRejected(conn);
            rrs = populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public void insertRemovalRequest(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        RemovalRequestDAO dao = new RemovalRequestDAO();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rr.setTransactionNum(dao.getNextIdValue(conn));
            dao.insertRemovalRequest(conn, rr);
            updateRemovalRequestItems(conn, rr);
        } finally {
            DbUtils.close(conn);
        }
    }

    private void updateRemovalRequestItems(Connection conn, RemovalRequest rr) throws SQLException {
        List<Item> persistedItems = new ItemService().getItemsInRemovalRequest(conn, rr.getTransactionNum());
        RemovalRequestDAO dao = new RemovalRequestDAO();

        for (Item i : rr.getItems()) {
            if (itemIsNew(i, persistedItems)) {
                dao.insertRemovalRequestItem(conn, rr, i);
            } else if (itemWasDeleted(i, persistedItems)) {
                dao.deleteRemovalRequestItem(conn, rr, i);
            }
        }
    }

    private boolean itemIsNew(Item item, List<Item> persistedItems) {
        boolean isNew = true;
        for (Item i: persistedItems) {
            if (i.getId() == item.getId()) {
                isNew = false;
            }
        }
        return isNew;
    }

    private boolean itemWasDeleted(Item item, List<Item> persistedItems) {
        for (Item persisted : persistedItems) {
            if (persisted.getId() == item.getId() && !persisted.equals(item) && item.getStatus().equals(ItemStatus.INACTIVE)) {
                return true;
            }
        }
        return false;
    }

    // TODO: if all items removed, logically delete master record also.
    public void updateRemovalRequest(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            RemovalRequest persistedRequest = getRemovalRequest(conn, rr.getTransactionNum());
            if (!rr.equals(persistedRequest)) {
                new RemovalRequestDAO().updateRemovalRequest(conn, rr);
            }
            updateRemovalRequestItems(conn, rr);
        } finally {
            DbUtils.close(conn);
        }
    }

    public void deleteRemovalRequest(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            new RemovalRequestDAO().deleteRemovalRequest(conn, rr);
        } finally {
            DbUtils.close(conn);
        }
    }

    private List<RemovalRequest> populateRequestList(List<RemovalRequest> rrs, Connection conn) throws SQLException {
        for (RemovalRequest rr : rrs) {
            populateRequest(conn, rr);
        }
        return rrs;
    }

    private RemovalRequest populateRequest(Connection conn, RemovalRequest rr) throws SQLException {
        rr.setItems(new ItemService().getItemsInRemovalRequest(conn, rr.getTransactionNum()));
        rr.setAdjustCode(new AdjustCodeService().getRemovalRequestAdjustCode(conn, rr.getTransactionNum()));
        return rr;
    }

    private List<RemovalRequest> shallowPopulateRequestList(List<RemovalRequest> rrs, Connection conn) throws SQLException {
        for (RemovalRequest rr : rrs) {
            shallowPopulateRequest(conn, rr);
        }
        return rrs;
    }

    private RemovalRequest shallowPopulateRequest(Connection conn, RemovalRequest rr) throws SQLException {
        rr.setItems(new ItemService().getShallowItemsInRemovalRequest(conn, rr.getTransactionNum()));
        rr.setAdjustCode(new AdjustCodeService().getRemovalRequestAdjustCode(conn, rr.getTransactionNum()));
        return rr;
    }
}
