package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.RemovalRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.Locale;

public class RemovalRequestDAO extends DbManager
{
    private static final Logger log = Logger.getLogger(RemovalRequestDAO.class.getName());

    private String SELECT_REMOVAL_REQUEST_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND nuinvadjreq = ?";

    protected RemovalRequest getRemovalRequst(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> rrs = run.query(conn, SELECT_REMOVAL_REQUEST_SQL, new RemovalRequestHandler(), id);
        return rrs.get(0);
    }

    private String SELECT_ALL_PENDING_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + " ) dtinvadjreq, cdinvreqstatm\n" +
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

    private String GET_NEXT_ID_VALUE_SQL = "SELECT NUINVADJREQ_SQNC.nextval id FROM dual";

    protected int getNextIdValue(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        return run.query(conn, GET_NEXT_ID_VALUE_SQL, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt("id");
            }
        });
    }

    private String INSERT_REMOVAL_REQUEST_SQL =
            "INSERT INTO fm12invadjreq (nuinvadjreq, nauser, dtinvadjreq, cdadjusted, cdinvreqstatm,\n" +
            "cdstatus, dttxnorigin, dttxnupdate, natxnorguser, natxnupduser)\n" +
            "VALUES(?, ?, ?, ?, 'PE', 'A', SYSDATE, SYSDATE, USER, USER)";

    protected void insertRemovalRequest(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, INSERT_REMOVAL_REQUEST_SQL, rr.getTransactionNum(), rr.getEmployee(),
                new java.sql.Date(rr.getDate().getTime()), rr.getAdjustCode().getCode());
    }

    private String INSERT_REMOVAL_REQUEST_ITEM_SQL = "INSERT into fd12invadjreq (nuxriareq, nuxrefsn) VALUES (?, ?)";

    protected void insertRemovalRequestItem(Connection conn, RemovalRequest rr, Item item) throws SQLException {
        QueryRunner run  = new QueryRunner();
        run.update(conn, INSERT_REMOVAL_REQUEST_ITEM_SQL, rr.getTransactionNum(), item.getId());
    }

    // TODO: ensure dttxnupdate and natxnupdatuser are being updated by trigger correctly.
    private String UPDATE_REMOVAL_REQUEST_SQL =
            "UPDATE fm12invadjreq SET nauser = ?, dtinvadjreq = ?, cdadjusted = ?, cdinvreqstatm = ? \n" +
            "WHERE nuinvadjreq = ?";

    protected void updateRemovalRequest(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, UPDATE_REMOVAL_REQUEST_SQL, rr.getEmployee(), new java.sql.Date(rr.getDate().getTime()), rr.getAdjustCode().getCode(), rr.getStatus(), rr.getTransactionNum());
    }

    private String DELETE_REMOVAL_REQUEST_ITEM_SQL =
            "UPDATE fd12invadjreq SET cdstatus = 'I' WHERE nuxriareq = ? and nuxrefsn = ? \n";

    protected void deleteRemovalRequestItem(Connection conn, RemovalRequest rr, Item item) throws SQLException {
        QueryRunner run = new QueryRunner();
        run.update(conn, DELETE_REMOVAL_REQUEST_ITEM_SQL, rr.getTransactionNum(), item.getId());
    }

    private String DELETE_REMOVAL_REQUEST_SQL =
            "UPDATE fm12invadjreq SET cdstatus = 'I'\n" +
            "WHERE nuinvadjreq = ?";

    protected void deleteRemovalRequest(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, DELETE_REMOVAL_REQUEST_SQL, rr.getTransactionNum());
    }

    private class RemovalRequestHandler implements ResultSetHandler<List<RemovalRequest>> {

        @Override
        public List<RemovalRequest> handle(ResultSet rs) throws SQLException {
            List<RemovalRequest> requests = new ArrayList<RemovalRequest>();
            while(rs.next()) {
                String user = rs.getString("nauser");
                try {
                    Date date = ORACLE_DATE_FORMAT.parse(rs.getString("dtinvadjreq"));
                    RemovalRequest req = new RemovalRequest(user, date);
                    req.setTransactionNum(rs.getInt("nuinvadjreq"));
                    req.setStatus(rs.getString("cdinvreqstatm"));
                    requests.add(req);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return requests;
        }
    }
}
