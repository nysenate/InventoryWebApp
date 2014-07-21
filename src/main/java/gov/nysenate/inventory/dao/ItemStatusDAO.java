package gov.nysenate.inventory.dao;

import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;

public class ItemStatusDAO
{
    private String IS_ITEM_IN_TRANSIT_SQL =
            "SELECT CASE WHEN EXISTS \n" +
            "(SELECT 1 FROM fm12invintrans m \n" +
            "INNER JOIN fd12invintrans d ON m.nuxrpd = d.nuxrpd \n" +
            "WHERE m.cdstatus = 'A' AND m.cdintransit = 'Y' \n" +
            "AND d.cdstatus = 'A' AND d.nusenate = ?) \n" +
            "THEN 'Y' \n" +
            "ELSE 'N' \n" +
            "END AS in_transit \n" +
            "FROM dual";

    public boolean isItemInTransit(Connection conn, String barcode) throws SQLException, ClassNotFoundException {
        boolean inTransit;
        QueryRunner run = new QueryRunner();
        inTransit = run.query(conn, IS_ITEM_IN_TRANSIT_SQL, new ItemTransitHandler(), barcode);
        return inTransit;
    }

    private class ItemTransitHandler implements ResultSetHandler<Boolean> {
        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            boolean inTransit = false;
            while (rs.next()) {
                if (rs.getString("in_transit").equals("Y")) {
                    inTransit = true;
                }
            }
            return inTransit;
        }
    }

    // Item stauts is kept in fd12issue, not fm12senxref
    private String IS_ITEM_INACTIVE_SQL =
            "SELECT cdstatus \n" +
            "FROM fd12issue \n" +
            "WHERE nuxrefsn = ?";

    public boolean isItemInActive(Connection conn, int id) throws SQLException, ClassNotFoundException {
        boolean isInactive;
        QueryRunner run = new QueryRunner();
        isInactive = run.query(conn, IS_ITEM_INACTIVE_SQL, new ItemInactiveHandler(), id);
        return isInactive;
    }

    private class ItemInactiveHandler implements ResultSetHandler<Boolean> {
        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            boolean isInactive = true;
            while (rs.next()) {
                if (rs.getString("cdstatus").equals("A")) {
                    isInactive = false;
                }
            }
            return isInactive;
        }
    }

    private String IS_ITEM_PENDING_REMOVAL_SQL =
            "SELECT CASE WHEN EXISTS \n" +
            "(SELECT 1 FROM fm12invadjreq m \n" +
            "INNER JOIN fd12invadjreq d \n" +
            "ON m.nuinvadjreq = d.nuxriareq \n" +
            "WHERE m.cdstatus = 'A' \n" +
            "AND d.cdstatus = 'A' \n" +
            "AND (m.cdinvreqstatm = 'PE' \n" +
            "OR m.cdinvreqstatm = 'SI' \n" +
            "OR m.cdinvreqstatm = 'SM') \n" +
            "and d.nuxrefsn = ?) \n" +
            "THEN 'Y' \n" +
            "ELSE 'N' \n" +
            "END AS pending_removal \n" +
            "FROM dual";

    public boolean isItemPendingRemoval(Connection conn, int id) throws SQLException, ClassNotFoundException {
        boolean isPendingRemvoal;
        QueryRunner run = new QueryRunner();
        isPendingRemvoal = run.query(conn, IS_ITEM_PENDING_REMOVAL_SQL, new ItemPendingRemovalHandler(), id);
        return isPendingRemvoal;
    }

    private class ItemPendingRemovalHandler implements ResultSetHandler<Boolean> {
        @Override
        public Boolean handle(ResultSet rs) throws SQLException {
            boolean isPendingRemoval = false;
            while (rs.next()) {
                if (rs.getString("pending_removal").equals("Y")) {
                    isPendingRemoval = true;
                }
            }
            return isPendingRemoval;
        }
    }
}
