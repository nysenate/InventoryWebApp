package gov.nysenate.inventory.dao.location;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Location;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class LocationService
{
    public List<Location> getLocations(DbConnect db) throws SQLException, ClassNotFoundException {
        List<Location> locations;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            locations = new LocationDAO().getLocations(conn);
        } finally {
            DbUtils.close(conn);
        }

        return locations;
    }

    public Location getLocation(DbConnect db, String code, String type) throws SQLException, ClassNotFoundException {
        Location location;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            location = new LocationDAO().getLocation(conn, code, type);
        } finally {
            DbUtils.close(conn);
        }

        return location;
    }

    public Location getLocationOfItem(Connection conn, int itemId) throws SQLException {
        return new LocationDAO().getLocationOfItem(conn, itemId);
    }

    /**
     * Wraps the old method for getting locations in DbConnect.
     */
    public List<Location> getLocationsDbConnect(DbConnect db, Connection conn) {
        return db.getLocCodes(conn);
    }

    /**
     * Wraps the old method for location details in package
     */
    public String getLocationDbConnect(DbConnect db, Connection conn, String locCode) {
        return db.getInvLocDetails(locCode, conn);
    }

}
