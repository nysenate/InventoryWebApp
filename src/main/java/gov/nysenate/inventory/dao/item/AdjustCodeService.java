package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.AdjustCode;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class AdjustCodeService
{
    public List<AdjustCode> getAdjustCodes(DbConnect db) throws SQLException, ClassNotFoundException {
        List<AdjustCode> codes;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            codes = new AdjustCodeDAO().getAdjustCodes(conn);
        } finally {
            DbUtils.close(conn);
        }
        return codes;
    }

    public AdjustCode getRemovalRequestAdjustCode(Connection conn, int id) throws SQLException {
        return new AdjustCodeDAO().getRemovalRequestAdjustCode(conn, id);
    }
}
