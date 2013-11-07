package gov.nysenate.inventory.model;


public abstract class Transaction {

    protected int nuxrpd;
    protected Location origin;
    protected Location destination;
    private int shipId;
    private String shipType;
    private String shipComments;
    private int verificationId;
    private String verificationMethod;
    private String verificationComments;
    private int employeeId;
    private String helpReferenceNum;

    // shipType must exists for all remote transactions.
    public boolean isRemote() {
        if (shipType != null) {
            return true;
        }
        return false;
    }

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

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public String getShipComments() {
        return shipComments;
    }

    public void setShipComments(String shipComments) {
        this.shipComments = shipComments;
    }

    public int getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(int verificationId) {
        this.verificationId = verificationId;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationComments() {
        return verificationComments;
    }

    public void setVerificationComments(String verificationComments) {
        this.verificationComments = verificationComments;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getHelpReferenceNum() {
        return helpReferenceNum;
    }

    public void setHelpReferenceNum(String helpReferenceNum) {
        this.helpReferenceNum = helpReferenceNum;
    }
}
