package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.base.DbManager;
import gov.nysenate.inventory.model.AdjustCode;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdjustCodeDAO extends DbManager
{
    private static final Logger log = Logger.getLogger(AdjustCodeDAO.class.getName());

    private String SELECT_ALL_ADJUST_CODES_SQL = "SELECT cdadjust, deadjust" +
            " FROM fl12adjustcd WHERE cdadjusttype = 'S' and cdstatus = 'A'";

    protected List<AdjustCode> getAdjustCodes(Connection conn) throws SQLException, ClassNotFoundException {
        List<AdjustCode> adjustcodes;
        QueryRunner run = new QueryRunner();
        adjustcodes = run.query(conn, SELECT_ALL_ADJUST_CODES_SQL, new AdjustCodeListHandler());
        return adjustcodes;
    }

    private String SELECT_ADJUST_CODE_OF_REMOVAL_REQUEST =
            "SELECT cdadjust, deadjust\n" +
            "FROM fl12adjustcd INNER JOIN fm12invadjreq\n" +
            "ON cdadjusted = cdadjust\n" +
            "WHERE nuinvadjreq = ?";

    protected AdjustCode getRemovalRequestAdjustCode(Connection conn, int removalRequestNum) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<AdjustCode> codes = run.query(conn, SELECT_ADJUST_CODE_OF_REMOVAL_REQUEST, new AdjustCodeListHandler(), removalRequestNum);
        return codes.get(0);
    }

    private String SELECT_ADJUST_CODE_BY_ITEM_ID =
            "SELECT adj.cdadjust, adj.deadjust\n" +
            "FROM fl12adjustcd adj INNER JOIN fd12issue issue\n" +
            "ON adj.cdadjust = issue.cdadjust\n" +
            "WHERE issue.nuxrefsn = ?";

    public AdjustCode getItemAdjustCode(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        return run.query(conn, SELECT_ADJUST_CODE_BY_ITEM_ID, new AdjustCodeHandler(), id);
    }

    private class AdjustCodeListHandler implements ResultSetHandler<List<AdjustCode>> {

        @Override
        public List<AdjustCode> handle(ResultSet rs) throws SQLException {
            List<AdjustCode> adjustCodes = new ArrayList<AdjustCode>();
            while (rs.next()) {
                String code = rs.getString("cdadjust");
                String description = rs.getString("deadjust");
                adjustCodes.add(new AdjustCode(code, description));
            }
            return adjustCodes;
        }
    }

    private class AdjustCodeHandler implements ResultSetHandler<AdjustCode> {

        @Override
        public AdjustCode handle(ResultSet rs) throws SQLException {
            String adjustCode = "";
            String description = "";
            if (rs.next()) {
                // Set database null values to empty strings.
                adjustCode = rs.getString("cdadjust") != null ? rs.getString("cdadjust") : "";
                description = rs.getString("deadjust") != null ? rs.getString("deadjust") : "";
            }
            return new AdjustCode(adjustCode, description);
        }
    }
}
