package gov.nysenate.inventory;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.location.LocationService;
import gov.nysenate.inventory.model.Location;
import org.junit.Before;
import org.junit.Test;

import java.util.List;

import static org.hamcrest.core.Is.is;
import static org.junit.Assert.assertThat;
import static org.junit.matchers.JUnitMatchers.containsString;

public class LocationTests extends BaseTest {

    private LocationService locationService;
    private DbConnect dbConnect;

    @Before
    public void setup() {
        locationService = new LocationService();
        dbConnect = new DbConnect();
    }

    @Test
    public void canListAllLocations() throws Exception {
        int expectedNumberOfLocations = 5;
        List<Location> actualLocations = locationService.getLocationsDbConnect(dbConnect, getConnection());
        assertThat(actualLocations.size(), is(expectedNumberOfLocations));
    }

    @Test
    public void canGetLocationDetails() throws Exception {
        String actualLocationDescription = locationService.getLocationDbConnect(dbConnect, getConnection(), "A42FB", "W");
        assertThat(actualLocationDescription, containsString("A42FB"));

        // Test location with multiple cdloctype's
        actualLocationDescription = locationService.getLocationDbConnect(dbConnect, getConnection(), "L711B", "W");
        assertThat(actualLocationDescription, containsString("L711B"));
        assertThat(actualLocationDescription, containsString("W"));

        actualLocationDescription = locationService.getLocationDbConnect(dbConnect, getConnection(), "L711B", "S");
        assertThat(actualLocationDescription, containsString("L711B"));
        assertThat(actualLocationDescription, containsString("S"));
    }

}
