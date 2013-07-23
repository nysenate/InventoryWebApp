package gov.nysenate.inventory.model;

public class Delivery {

    private String[] checkedItems;
    private String[] notCheckedItems;
    private String comments;
    private String naDeliverBy;
    private String naAcceptBy;
    private String nuxrAccptSign;

    public Delivery() {
        comments = "";
        naDeliverBy = "";
        naAcceptBy = "";
        nuxrAccptSign = "";
    }


    public String[] getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(String[] checkedItems) {
        this.checkedItems = checkedItems;
    }

    public String[] getNotCheckedItems() {
        return notCheckedItems;
    }

    public void setNotCheckedItems(String[] notCheckedItems) {
        this.notCheckedItems = notCheckedItems;
    }

    public String getComments() {
        return comments;
    }

    public void setComments(String comments) {
        this.comments = comments;
    }

    public String getNaDeliverBy() {
        return naDeliverBy;
    }

    public void setNaDeliverBy(String naDeliverBy) {
        this.naDeliverBy = naDeliverBy.toUpperCase();
    }

    public String getNaAcceptBy() {
        return naAcceptBy;
    }

    public void setNaAcceptBy(String naAcceptBy) {
        this.naAcceptBy = naAcceptBy.toUpperCase();
    }

    public String getNuxrAccptSign() {
        return nuxrAccptSign;
    }

    public void setNuxrAccptSign(String nuxrAccptSign) {
        this.nuxrAccptSign = nuxrAccptSign;
    }

}
