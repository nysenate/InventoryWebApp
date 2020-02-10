package gov.nysenate.inventory.dao.location;

import gov.nysenate.inventory.model.Location;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class LocationDAO
{

    private final int NO_EXTRA_COLUMNS = 100;
    private final int ADD_ENTRYDATE = 101;

    private String SELECT_ALL_LOCATIONS_SQL =
            "SELECT cdlocat, cdloctype, adstreet1, adstreet2, " +
            "adcity, adzipcode, adstate, delocat, cdrespctrhd \n" +
            "FROM sl16location \n" +
            "WHERE cdstatus = 'A'";

    public List<Location> getLocations(Connection conn, int nuxrefsn) throws SQLException, ClassNotFoundException {
        List<Location> locations;
        QueryRunner run = new QueryRunner();
        locations = run.query(conn, SELECT_ALL_LOCATIONS_SQL, new LocationHandler(), nuxrefsn);
        return locations;
    }

    private String SELECT_LOCATION_SQL =
            "SELECT cdlocat, cdloctype, adstreet1, adstreet2, " +
            "adcity, adzipcode, adstate, delocat, cdrespctrhd \n" +
            "FROM sl16location \n" +
            "WHERE cdstatus = 'A' \n" +
            "AND cdlocat = ? AND cdloctype = ?";

    protected Location getLocation(Connection conn, String code, String type) throws SQLException, ClassNotFoundException {
        List<Location> locations;
        QueryRunner run = new QueryRunner();
        locations = run.query(conn, SELECT_LOCATION_SQL, new LocationHandler(), code, type);
        return locations.get(0);
    }

    private String SELECT_LOCATION_OF_ITEM_SQL =
            "SELECT cdlocat, cdloctype, adstreet1, adstreet2,\n" +
            "adcity, adzipcode, adstate, delocat, cdrespctrhd \n" +
            "FROM sl16location INNER JOIN fd12issue ON\n" +
            "(cdlocat = cdlocatto AND cdloctype = cdloctypeto)\n" +
            "WHERE nuxrefsn = ?";

    private String SELECT_LOCATIONS_BY_ITEM_SQL =
            "SELECT MIN(a.dttxnorigin) dttxnorigin, a.cdlocatto cdlocat, a.cdloctypeto cdloctype, b.adstreet1, b.adstreet2, b.adcity, b.adstate, b.adzipcode, b.delocat, b.cdrespctrhd\n" +
            "FROM fd12issaudit a \n" +
            "LEFT OUTER JOIN sl16location b ON (a.cdlocatto = b.cdlocat   AND a.cdloctypeto = b.cdloctype AND b.cdstatus = 'A')\n" +
            "WHERE a.nuxrefsn = ? \n" +
            "  AND a.cdstatus = 'A'\n" +
            "  AND a.cdlocatto IS NOT NULL\n" +
            "GROUP BY a.cdlocatto, a.cdloctypeto,  b.adstreet1, b.adstreet2, b.adcity, b.adstate, b.adzipcode, b.delocat, b.cdrespctrhd \n" +
            "ORDER BY 1 DESC";

    protected Location getLocationOfItem(Connection conn, int itemId) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Location> locs =  run.query(conn, SELECT_LOCATION_OF_ITEM_SQL, new LocationHandler(), itemId);
        return locs.get(0);
    }

    public List<Location> getLocationsOfItem(Connection conn, int itemId) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Location> locs =  run.query(conn, SELECT_LOCATIONS_BY_ITEM_SQL, new LocationHandler(ADD_ENTRYDATE), itemId);
        return locs;
    }

    private class LocationHandler implements ResultSetHandler<List<Location>> {

        int extraColumns = LocationDAO.this.NO_EXTRA_COLUMNS;

        public LocationHandler() {

        }

        public LocationHandler(int extraColumns) {
            this.extraColumns = extraColumns;
        }

        @Override
        public List<Location> handle(ResultSet rs) throws SQLException {
            List<Location> locations = new ArrayList<Location>();
            while (rs.next()) {
                String code = rs.getString("cdlocat");
                String type = rs.getString("cdloctype");
                String street1 = rs.getString("adstreet1");
                String city = rs.getString("adcity");
                String zip = rs.getString("adzipcode");
                String state = rs.getString("adstate");
                String description = rs.getString("delocat");
                String department = rs.getString("cdrespctrhd");

                Location loc = new Location(code, type, street1, city, zip, state, description, department);

                loc.setStreet2(rs.getString("adstreet2"));

                if (this.extraColumns== ADD_ENTRYDATE) {
                    loc.setEntryDate(rs.getDate("dttxnorigin"));
                }

                locations.add(loc);
            }
            return locations;
        }
    }
}
