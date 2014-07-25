package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.AdjustCode;
import org.apache.commons.dbutils.DbUtils;
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
        adjustcodes = run.query(conn, SELECT_ALL_ADJUST_CODES_SQL, new AdjustCodeHandler());
        return adjustcodes;
    }

    private String SELECT_ADJUST_CODE_OF_REMOVAL_REQUEST =
            "SELECT cdadjust, deadjust\n" +
            "FROM fl12adjustcd INNER JOIN fm12invadjreq\n" +
            "ON cdadjusted = cdadjust\n" +
            "WHERE nuinvadjreq = ?";

    protected AdjustCode getRemovalRequestAdjustCode(Connection conn, int removalRequestNum) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<AdjustCode> codes = run.query(conn, SELECT_ADJUST_CODE_OF_REMOVAL_REQUEST, new AdjustCodeHandler(), removalRequestNum);
        return codes.get(0);
    }

    private class AdjustCodeHandler implements ResultSetHandler<List<AdjustCode>> {

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
}
