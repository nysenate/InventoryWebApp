package gov.nysenate.inventory.dao.removalrequest;

import gov.nysenate.inventory.dao.base.DbManager;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.RemovalRequest;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

public class RemovalRequestDAO extends DbManager
{
    private static final Logger log = Logger.getLogger(RemovalRequestDAO.class.getName());

    private String SELECT_REMOVAL_REQUEST_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm, deinvctrlcmts \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND nuinvadjreq = ?";

    protected RemovalRequest getRemovalRequst(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> rrs = run.query(conn, SELECT_REMOVAL_REQUEST_SQL, new RemovalRequestHandler(), id);
        return rrs.get(0);
    }

    private String SELECT_ALL_PENDING_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + " ) dtinvadjreq, cdinvreqstatm, deinvctrlcmts \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'PE'";

    protected List<RemovalRequest> getPendingRequests(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_PENDING_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_SUBMITTED_TO_INVENTORY_CONTROL_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm, deinvctrlcmts \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'SI'";

    protected List<RemovalRequest> getSubmittedToInventoryControl(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_SUBMITTED_TO_INVENTORY_CONTROL_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_SUBMITTED_TO_MANAGEMENT_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm, deinvctrlcmts\n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'SM'";

    protected List<RemovalRequest> getSubmittedToManagement(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_SUBMITTED_TO_MANAGEMENT_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_APPROVED_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm, deinvctrlcmts \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'AP'"; 

    protected List<RemovalRequest> getApproved(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_APPROVED_SQL, new RemovalRequestHandler());
        return requests;
    }

    private String SELECT_ALL_REJECTED_SQL =
            "SELECT nuinvadjreq, nauser, to_char(dtinvadjreq, " + ORACLE_DATE_QUERY + ") dtinvadjreq, cdinvreqstatm, deinvctrlcmts \n" +
            "FROM fm12invadjreq\n" +
            "WHERE cdstatus = 'A'\n" +
            "AND cdinvreqstatm = 'RJ'";

    protected List<RemovalRequest> getRejected(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<RemovalRequest> requests = run.query(conn, SELECT_ALL_REJECTED_SQL, new RemovalRequestHandler());
        return requests;
    }

    //private String GET_NEXT_ID_VALUE_SQL = "SELECT NUINVADJREQ_SQNC.nextval id FROM dual";
    private String GET_NEXT_ID_VALUE_SQL = "SELECT NVL(MAX(nuinvadjreq),0) + 1 id FROM fp12invadjreq";
    private String SET_NEXT_ID_VALUE_SQL = "UPDATE fp12invadjreq SET nuinvadjreq = ?, natxnupduser = USER, dttxnupdate = SYSDATE";
    private String INS_NEXT_ID_VALUE_SQL = "INSERT INTO fp12invadjreq (nuinvadjreq, natxnorguser, dttxnorigin, natxnupduser, dttxnupdate) VALUES (?, USER, SYSDATE, USER, SYSDATE)";

    protected int getNextIdValue(Connection conn) throws SQLException {
        QueryRunner run = new QueryRunner();

        int result;
        
        result =  run.query(conn,GET_NEXT_ID_VALUE_SQL, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt("id");
            }
        });
               
        run = new QueryRunner();
        
        if (result > 0) {
            int updates = run.update(conn, SET_NEXT_ID_VALUE_SQL, result);
            if (updates==0) {
                int inserts = run.update(conn, INS_NEXT_ID_VALUE_SQL, result);
            }
            return result;
        }
        else {
            /*
             * If no parameter record was found or if the parameter table xref value is
             * less than 1 then set it to 1 since it should never be less. 
             */
            result = 1;
            int inserts = run.update(conn, INS_NEXT_ID_VALUE_SQL, result);
            return result;
        }
        
     /*   return run.query(conn, GET_NEXT_ID_VALUE_SQL, new ResultSetHandler<Integer>() {
            @Override
            public Integer handle(ResultSet rs) throws SQLException {
                rs.next();
                return rs.getInt("id");
            }
        });*/
    }

    private String INSERT_REMOVAL_REQUEST_SQL =
            "INSERT INTO fm12invadjreq (nuinvadjreq, nauser, dtinvadjreq, cdadjusted, cdinvreqstatm, deinvctrlcmts,\n" +
            "cdstatus, dttxnorigin, dttxnupdate, natxnorguser, natxnupduser)\n" +
            "VALUES(?, ?, ?, ?, 'PE', ?, 'A', SYSDATE, SYSDATE, USER, USER)";

    protected void insertRemovalRequest(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
        run.update(conn, INSERT_REMOVAL_REQUEST_SQL, rr.getTransactionNum(), rr.getEmployee(),
                new Timestamp(rr.getDate().getTime()), rr.getAdjustCode().getCode(), rr.getInventoryControlComments());
    }

    private String INSERT_REMOVAL_REQUEST_ITEM_SQL = "INSERT into fd12invadjreq (nuxriareq, nuxrefsn) VALUES (?, ?)";

    protected void insertRemovalRequestItem(Connection conn, RemovalRequest rr, Item item) throws SQLException {
        QueryRunner run  = new QueryRunner();
        run.update(conn, INSERT_REMOVAL_REQUEST_ITEM_SQL, rr.getTransactionNum(), item.getId());
    }

    private String UPDATE_REMOVAL_REQUEST_SQL =
            "UPDATE fm12invadjreq SET nauser = ?, dtinvadjreq = ?, cdadjusted = ?, cdinvreqstatm = ?, deinvctrlcmts = ? \n" +
                    "WHERE nuinvadjreq = ?";

    protected void updateRemovalRequest(Connection conn, RemovalRequest rr) throws SQLException, ClassNotFoundException {
        QueryRunner run = new QueryRunner();
   /*      String testResult = UPDATE_REMOVAL_REQUEST_SQL;

        //
        //  Even though commented out, keeping for now just in case it it needed.
        //  Temporary fix due to push to get this out by a deadline.
        //  When more than one parameter is set,  ORA-04044: procedure, function, package, or type is not allowed here
        //  with the changes on the Inventory App to use Volley.  This way is vulnerable to SQL INJECTION and is not the proper way, but
        //  is being done for now to get it to work for now. Keeping
        //

       try {
            testResult = testResult.replaceFirst("\\?", "'" + rr.getEmployee() + "'");
            SimpleDateFormat simpleDateFormat = new SimpleDateFormat("MM/dd/yyyy hh:mm:ss a");
            String date = simpleDateFormat.format( rr.getDate() );
            testResult = testResult.replaceFirst("\\?", " TO_DATE('" + date + "','MM/DD/RRRR HH:MI:SS AM') ");
            testResult = testResult.replaceFirst("\\?", "'" + rr.getAdjustCode().getCode() + "'");
            testResult = testResult.replaceFirst("\\?", "'" + rr.getStatus() + "'");
            testResult = testResult.replaceFirst("\\?", "'" + rr.getInventoryControlComments() + "'");
            testResult = testResult.replaceFirst("\\?", "'" + rr.getTransactionNum() + "'");
            System.out.println("updateRemovalRequest SQL: "+testResult);
        }
        catch (Exception e) {
            e.printStackTrace();
        }
        System.out.println("nuinvadjreq: ("+rr.getTransactionNum()+")");
        System.out.println("nauser: ("+rr.getEmployee()+"):"+rr.getEmployee().length());
        System.out.println("dtinvadjreq: ("+new Timestamp(rr.getDate().getTime())+")");
        System.out.println("cdadjusted: ("+rr.getAdjustCode().getCode()+"):"+rr.getAdjustCode().getCode().length());
        System.out.println("cdinvreqstatm: ("+rr.getStatus()+"):"+rr.getStatus().length());
        System.out.println("deinvctrlcmts: ("+rr.getInventoryControlComments()+")");

        int rows = run.update(conn, testResult);*/

    //  Correct update that should be used if the error doesn't occur below...

        int rows = run.update(conn, UPDATE_REMOVAL_REQUEST_SQL, rr.getEmployee(), new Timestamp(rr.getDate().getTime()),
                rr.getAdjustCode().getCode(), rr.getStatus(), rr.getInventoryControlComments(),rr.getTransactionNum());
    }

    private String DELETE_REMOVAL_REQUEST_ITEM_SQL =
            "UPDATE fd12invadjreq SET cdstatus = 'I' WHERE nuxriareq = ? and nuxrefsn = ? \n";

    private String TEST_DELETE_REMOVAL_REQUEST_ITEM_SQL =
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
                    req.setInventoryControlComments(rs.getString("deinvctrlcmts"));
                    requests.add(req);
                } catch (ParseException e) {
                    log.error(e.getMessage(), e);
                }
            }
            return requests;
        }
    }
}