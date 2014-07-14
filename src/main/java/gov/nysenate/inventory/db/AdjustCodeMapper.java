package gov.nysenate.inventory.db;

import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.util.DbManager;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class AdjustCodeMapper extends DbManager
{
    private static final Logger log = Logger.getLogger(AdjustCodeMapper.class.getName());

    private String SELECT_ALL_ADJUST_CODES = "SELECT cdadjust, deadjust" +
            " FROM fl12adjustcd WHERE cdadjusttype = 'S' and cdstatus = 'A'";

    public List<AdjustCode> getAllAdjustCodes(DbConnect db) throws SQLException, ClassNotFoundException {
        List<AdjustCode> adjustcodes;
        ResultSet rs = null;
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(SELECT_ALL_ADJUST_CODES);
            rs = ps.executeQuery();
            adjustcodes = handleResults(rs);
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(conn);
        }

        return adjustcodes;
    }

    private List<AdjustCode> handleResults(ResultSet rs) throws SQLException {
        List<AdjustCode> adjustCodes = new ArrayList<AdjustCode>();
        while (rs.next()) {
            AdjustCode ac = handleResult(rs);
            adjustCodes.add(ac);
        }
        return adjustCodes;
    }

    private AdjustCode handleResult(ResultSet rs) throws SQLException {
        String code = rs.getString("cdadjust");
        String description = rs.getString("deadjust");
        return new AdjustCode(code, description);
    }
}
