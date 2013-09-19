package gov.nysenate.inventory.model;

public class Location {

    private String cdLocType;
    private String cdLoc;
    private String addressStreet1;
    private String city;
    private String zip;

    public String getAddressStreet1() {
        return addressStreet1;
    }

    public void setAddressStreet1(String addressStreet1) {
        this.addressStreet1 = addressStreet1;
    }

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

    public String getCity() {
        return city;
    }

    public void setCity(String city) {
        this.city = city;
    }

    public String getZip() {
        return zip;
    }

    public void setZip(String zip) {
        this.zip = zip;
    }
}
