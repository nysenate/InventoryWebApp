package gov.nysenate.inventory.dao;

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

    private String SELECT_ALL_LOCATIONS_SQL =
            "SELECT cdlocat, cdloctype, adstreet1, adstreet2, " +
            "adcity, adzipcode, adstate, delocat, cdrespctrhd \n" +
            "FROM sl16location \n" +
            "WHERE cdstatus = 'A'";

    public List<Location> getAllLocations(DbConnect db) throws SQLException, ClassNotFoundException {
        List<Location> locations;
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            locations = run.query(conn, SELECT_ALL_LOCATIONS_SQL, new LocationHandler());
        } finally {
            DbUtils.close(conn);
        }
        return locations;
    }

    private String SELECT_LOCATION_BY_CODE_SQL =
            "SELECT cdlocat, cdloctype, adstreet1, adstreet2, " +
            "adcity, adzipcode, adstate, delocat, cdrespctrhd \n" +
            "FROM sl16location \n" +
            "WHERE cdstatus = 'A' \n" +
            "AND cdlocat = ?";

    public Location getLocation(DbConnect db, String code) throws SQLException, ClassNotFoundException {
        List<Location> locations;
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            locations = run.query(conn, SELECT_LOCATION_BY_CODE_SQL, new LocationHandler(), code);
        } finally {
            DbUtils.close(conn);
        }
        return locations.get(0);
    }

    private class LocationHandler implements ResultSetHandler<List<Location>> {

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

                locations.add(loc);
            }
            return locations;
        }
    }
}
