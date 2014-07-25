package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.RemovalRequest;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class RemovalRequestService
{
    public List<RemovalRequest> getPending(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs;
        RemovalRequestDAO rrDao = new RemovalRequestDAO();

        Connection conn = null;
        try {
            conn = db.getDbConnection();
            rrs = rrDao.getPendingRequests(conn);
            populateRequestList(rrs, conn);
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
            populateRequestList(rrs, conn);
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
            populateRequestList(rrs, conn);
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
            populateRequestList(rrs, conn);
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
            populateRequestList(rrs, conn);
        } finally {
            DbUtils.close(conn);
        }

        return rrs;
    }

    public void update(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            new RemovalRequestDAO().update(conn, rr);
        } finally {
            DbUtils.close(conn);
        }
    }

    public void delete(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            new RemovalRequestDAO().delete(conn, rr);
        } finally {
            DbUtils.close(conn);
        }
    }


    private void populateRequestList(List<RemovalRequest> rrs, Connection conn) throws SQLException {
        for (RemovalRequest rr : rrs) {
            populateRequest(conn, rr);
        }
    }

    private void populateRequest(Connection conn, RemovalRequest rr) throws SQLException {
        rr.setItems(new ItemService().getItemsByRemovalRequest(conn, rr.getTransactionNum()));
        rr.setAdjustCode(new AdjustCodeDAO().getRemovalRequestAdjustCode(conn, rr.getTransactionNum()));
    }}
