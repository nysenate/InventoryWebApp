package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.RemovalRequest;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RemovalRequestDAO extends DbManager
{

    // TODO: dont need status, sysdate, user stuff?

    private String SELECT_ALL_PENDING =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdadjusted, deadjust, cdinvreqstatm \n" +
            "FROM fm12invadjreq requests\n" +
            "INNER JOIN fl12adjustcd ON requests.cdadjusted = fl12adjustcd.cdadjust\n" +
            "WHERE requests.cdstatus = 'A'\n" +
            "AND requests.cdinvreqstatm = 'PE'";

    public List<RemovalRequest> getPendingRequests(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> requests;
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            requests = run.query(conn, SELECT_ALL_PENDING, new RemovalRequestHandler());
        } finally {
            DbUtils.close(conn);
        }
        return requests;
    }

    private String UPDATE_REMOVAL_REQUEST =
            "INSERT INTO fm12invadjreq (nuinvadjreq, nauser, dtinvadjreq, cdadjusted, cdinvreqstatm, \n" +
            "cdstatus, dttxnorigin, dttxnupdate, natxnorguser, natxnupduser)\n" +
            "VALUES(NUINVADJREQ_SQNC.nextval, USER, SYSDATE, ?, ?, 'A', SYSDATE, SYSDATE, USER, USER)";

    public void updateRmovalRequest(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            run.update(conn, UPDATE_REMOVAL_REQUEST, rr.getAdjustCode().getCode(), rr.getStatus());
        } finally {
            DbUtils.close(conn);
        }
    }

    private String DELETE_REMOVAL_REQUEST =
            "INSERT INTO fm12invadjreq (cdstatus) VALUES('I')";

    public void deleteRemovalRequest(DbConnect db, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            run.update(conn, DELETE_REMOVAL_REQUEST);
        } finally {
            DbUtils.close(conn);
        }
    }

    private class RemovalRequestHandler implements ResultSetHandler<List<RemovalRequest>> {

        @Override
        public List<RemovalRequest> handle(ResultSet rs) throws SQLException {
            List<RemovalRequest> requests = new ArrayList<RemovalRequest>();
            while(rs.next()) {
                RemovalRequest req = new RemovalRequest(rs.getString("nauser"));
                req.setTransactionNum(rs.getInt("nuinvadjreq"));
                req.setDate(rs.getDate("dtinvadjreq"));
                req.setStatus(rs.getString("cdinvreqstatm"));

                // TODO: do adjustcode dao separately?
                AdjustCode code = new AdjustCode(rs.getString("cdadjusted"), rs.getString("deadjust"));
                req.setAdjustCode(code);

                requests.add(req);
            }
            return requests;
        }
    }
}
