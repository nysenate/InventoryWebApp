package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Delivery extends Transaction {

    private ArrayList<String> allItems;
    private ArrayList<String> checkedItems;
    private ArrayList<String> notCheckedItems;
    private String comments;
    private String nadeliverby;
    private String naacceptby;
    private String nuxraccptsign;

    public Delivery() {
        comments = "";
        nadeliverby = "";
        naacceptby = "";
        nuxraccptsign = "";
        origin = new Location();
        destination = new Location();
        allItems = new ArrayList<String>();
        checkedItems = new ArrayList<String>();
        notCheckedItems = new ArrayList<String>();
    }

    public void generateNotCheckedItems() {
        notCheckedItems = (ArrayList<String>) allItems.clone();
        for (String item : checkedItems) {
            notCheckedItems.remove(item);
        }
    }

    public ArrayList<String> getAllItems() {
        return allItems;
    }

    public void setAllItems(ArrayList<String> allItems) {
        this.allItems = allItems;
    }

    public void setAllItems(String[] allItems) {
        this.allItems = new ArrayList<String>(Arrays.asList(allItems));
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

    public String getNadeliverby() {
        return nadeliverby;
    }

    public void setNadeliverby(String nadeliverby) {
        this.nadeliverby = nadeliverby.toUpperCase();
    }

    public String getNaacceptby() {
        return naacceptby;
    }

    public void setNaacceptby(String naacceptby) {
        this.naacceptby = naacceptby.toUpperCase();
    }

    public String getNuxraccptsign() {
        return nuxraccptsign;
    }

    public void setNuxrsccptsign(String nuxraccptsign) {
        this.nuxraccptsign = nuxraccptsign;
    }

}
