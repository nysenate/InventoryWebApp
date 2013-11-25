package gov.nysenate.inventory.model;

public class Location {

    private String cdloctype;
    private String cdlocat;
    private String adstreet1;
    private String adcity;
    private String adzipcode;
    private String adstate;

    public Location() {

    }

    public Location(String summary) {
        String[] tmp = summary.split("-");
        cdlocat = tmp[0];
        tmp = tmp[1].split(":");
        cdloctype = tmp[0];
        adstreet1 = tmp[1].trim();
    }

    // Location is remote if it is outside of albany.
    public boolean isRemote() {
        return adcity.equalsIgnoreCase("Albany") ? false : true;
    }

    public String getAdstreet1() {
        return adstreet1;
    }

    public void setAdstreet1(String adstreet1) {
        this.adstreet1 = adstreet1;
    }

    public String getCdloctype() {
        return cdloctype;
    }

    public void setCdloctype(String cdloctype) {
        this.cdloctype = cdloctype;
    }

    public String getCdlocat() {
        return cdlocat;
    }

    public void setCdlocat(String cdlocat) {
        this.cdlocat = cdlocat;
    }

    public String getAdcity() {
        return adcity;
    }

    public void setAdcity(String adcity) {
        this.adcity = adcity;
    }
    
    public String getAdstate() {
        return adstate;
    }

    public void setAdstate(String adstate) {
        this.adstate = adstate;
    }

    public String getAdzipcode() {
        return adzipcode;
    }

    public void setAdzipcode(String adzipcode) {
        this.adzipcode = adzipcode;
    }

    public String getLocationSummaryString() {
        return getCdlocat() + "-" + getCdloctype()+ ": " + getAdstreet1();
    }

}
