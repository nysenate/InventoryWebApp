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
    private String SELECT_ALL_PENDING_SQL =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdinvreqstatm\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'PE'";

    protected List<RemovalRequest> getPendingRequests(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_PENDING_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_SUBMITTED_TO_INVENTORY_CONTROL_SQL =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdinvreqstatm\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'SI'";

    protected List<RemovalRequest> getSubmittedToInventoryControl(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_SUBMITTED_TO_INVENTORY_CONTROL_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_SUBMITTED_TO_MANAGEMENT_SQL =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdinvreqstatm\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'SM'";

    protected List<RemovalRequest> getSubmittedToManagement(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_SUBMITTED_TO_MANAGEMENT_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_APPROVED_SQL =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdinvreqstatm\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'AP'";

    protected List<RemovalRequest> getApproved(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_APPROVED_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_REJECTED_SQL =
            "SELECT nuinvadjreq, nauser, dtinvadjreq, cdinvreqstatm\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'RJ'";

    protected List<RemovalRequest> getRejected(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_REJECTED_SQL, new RemovalRequestHandler());
        return requests;
    }

    // TODO: dont need status, sysdate, user stuff?
    private String UPDATE_REMOVAL_REQUEST_SQL =
            "INSERT INTO fm12invadjreq (nuinvadjreq, nauser, dtinvadjreq, cdadjusted, cdinvreqstatm, \n" +
            "cdstatus, dttxnorigin, dttxnupdate, natxnorguser, natxnupduser)\n" +
            "VALUES(NUINVADJREQ_SQNC.nextval, USER, SYSDATE, ?, ?, 'A', SYSDATE, SYSDATE, USER, USER)";

    protected void update(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, UPDATE_REMOVAL_REQUEST_SQL, rr.getAdjustCode().getCode(), rr.getStatus());
    }

    private String DELETE_REMOVAL_REQUEST_SQL =
            "INSERT INTO fm12invadjreq (cdstatus) VALUES('I')\n" +
            "WHERE nuinvadjreq = ?";

    protected void delete(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, DELETE_REMOVAL_REQUEST_SQL, rr.getTransactionNum());
    }

    private class RemovalRequestHandler implements ResultSetHandler<List<RemovalRequest>> {

        @Override
        public List<RemovalRequest> handle(ResultSet rs) throws SQLException {
            List<RemovalRequest> requests = new ArrayList<RemovalRequest>();
            while(rs.next()) {
                RemovalRequest req = new RemovalRequest(rs.getString("nauser"), rs.getDate("dtinvadjreq"));
                req.setTransactionNum(rs.getInt("nuinvadjreq"));
                req.setStatus(rs.getString("cdinvreqstatm"));

                requests.add(req);
            }
            return requests;
        }
    }
}
