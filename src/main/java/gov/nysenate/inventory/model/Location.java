package gov.nysenate.inventory.model;

public class Location {


    private String cdLocType;
    private String cdLoc;
    private String locName;
    /*
     * private char cdLocTypeFrom;
     * private char cdLocTypeTo;
     * private String cdLocatFrom;
     * private String cdLocatTo;
     * private String originLocation;
     * private String destinationLocation;
     */

    public String getCdLocType() {
        return cdLocType;
    }

    public void setCdLocType(String cdLocType) {
        this.cdLocType = cdLocType;
    }

    public String getCdLoc() {
        return cdLoc;
    }

    public void setCdLoc(String cdLoc) {
        this.cdLoc = cdLoc;
    }

    public String getLocName() {
        return locName;
    }

    public void setLocName(String locName) {
        this.locName = locName;
    }
}
