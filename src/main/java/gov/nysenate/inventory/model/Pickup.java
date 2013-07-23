package gov.nysenate.inventory.model;

public class Pickup {

    private String[] pickupItems;
    private String comments;
    private String naPickupBy;
    private String naReleaseBy;
    private String nuxrRelSign;

    public Pickup() {
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
    }

    public String[] getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(String[] pickupItems) {
        this.pickupItems = pickupItems;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNaPickupBy() {
        return naPickupBy;
    }

    public void setNaPickupBy(String naPickupBy) {
        this.naPickupBy = naPickupBy.toUpperCase();
    }

    public String getNaReleaseBy() {
        return naReleaseBy;
    }

    public void setNaReleaseBy(String naReleaseBy) {
        this.naReleaseBy = naReleaseBy.toUpperCase();
    }

    public String getNuxrRelSign() {
        return nuxrRelSign;
    }

    public void setNuxrRelSign(String nuxrRelSign) {
        this.nuxrRelSign = nuxrRelSign;
    }

}
