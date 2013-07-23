package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Delivery {

    private ArrayList<String> checkedItems;
    private ArrayList<String> notCheckedItems;
    private String comments;
    private String naDeliverBy;
    private String naAcceptBy;
    private String nuxrAccptSign;

    public Delivery() {
        comments = "";
        naDeliverBy = "";
        naAcceptBy = "";
        nuxrAccptSign = "";
        checkedItems = new ArrayList<String>();
        notCheckedItems = new ArrayList<String>();
    }


    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(String[] checkedItems) {
        this.checkedItems = new ArrayList<String>(Arrays.asList(checkedItems));
    }

    public ArrayList<String> getNotCheckedItems() {
        return notCheckedItems;
    }

    public void setNotCheckedItems(String[] notCheckedItems) {
        this.notCheckedItems = new ArrayList<String>(Arrays.asList(notCheckedItems));
    }

    public void setNotCheckedItems(ArrayList<String> notCheckedItems) {
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
