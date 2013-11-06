package gov.nysenate.inventory.model;


public abstract class Transaction {

    protected int nuxrpd;
    protected Location origin;
    protected Location destination;

    public String getOriginCdLoc() {
        return origin.getCdlocat();
    }

    public String getOriginCdLocType() {
        return origin.getCdloctype();
    }

    public String getOriginAddressLine1() {
        return origin.getAdstreet1();
    }

    public String getDestinationCdLoc() {
        return destination.getCdlocat();
    }

    public String getDestinationCdLocType() {
        return destination.getCdloctype();
    }

    public String getDestinationAddressLine1() {
        return destination.getAdstreet1();
    }

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

    public String getOriginSummaryString() {
        return origin.getLocationSummaryString();
    }

    public String getDestinationSummaryString() {
        return destination.getLocationSummaryString();
    }
}
