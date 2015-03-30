package gov.nysenate.inventory.dao.history;

import gov.nysenate.inventory.dao.base.DbManager;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.text.ParseException;
import java.util.Date;

public class InventoryHistoryDao extends DbManager {

    private static final Logger log = Logger.getLogger(InventoryHistoryDao.class);

    private String SELECT_DATE_ITEM_LAST_INVENTORIED =
            "SELECT to_char(dtlstinvntry, " + ORACLE_DATE_QUERY + ") dtlstinvntry \n" +
            "FROM fd12issue \n" +
            "WHERE nuxrefsn = ?";

    protected Date getDateItemLastInventoried(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        return run.query(conn, SELECT_DATE_ITEM_LAST_INVENTORIED, new DateItemLastInventoriedMapper(), id);
    }

    private class DateItemLastInventoriedMapper implements ResultSetHandler<Date> {

        @Override
        public Date handle(ResultSet rs) throws SQLException {
            Date lastInventoried = null;
            rs.next();
            try {
                lastInventoried = ORACLE_DATE_FORMAT.parse(rs.getString("dtlstinvntry"));
            } catch (ParseException e) {
                log.error("Error formating last inventoried date. ", e);
            }
            return lastInventoried;
        }
    }

}
