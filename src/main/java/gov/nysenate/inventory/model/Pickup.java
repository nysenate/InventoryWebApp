package gov.nysenate.inventory.model;

import gov.nysenate.inventory.server.InvItem;

import java.util.ArrayList;
import java.util.Arrays;

public class Pickup extends Transaction {

    private ArrayList<InvItem> pickupItems;
    private String comments;
    private String naPickupBy;
    private String naReleaseBy;
    private String nuxrRelSign;
    private String date;

    public Pickup() {
        super();
        comments = "";
        naPickupBy = "";
        naReleaseBy = "";
        nuxrRelSign = "";
        pickupItems = new ArrayList<InvItem>();
    }

    public ArrayList<InvItem> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(InvItem[] pickupItems) {
        this.pickupItems = new ArrayList<InvItem>(Arrays.asList(pickupItems));
    }

    // TODO: should we send the picked up items to servlet as a InvItem array instead of a String array? If so wont need this..
    public void setPickupItems(String[] items) {
        ArrayList<InvItem> pickupItems = new ArrayList<InvItem>();
        for (String item : items) {
            InvItem invItem = new InvItem();
            invItem.setNusenate(item);
            pickupItems.add(invItem);
        }
        this.pickupItems = pickupItems;
    }

    // TODO: temp fix
    public void setPickupItemsList(ArrayList<String> items) {
        ArrayList<InvItem> pickupItems = new ArrayList<InvItem>();
        for (String item : items) {
            InvItem invItem = new InvItem();
            invItem.setNusenate(item);
            pickupItems.add(invItem);
        }
        this.pickupItems = pickupItems;
    }

    public String[] getPickupItemsNusenate() {
        String[] nusenates = new String[pickupItems.size()];
        for (int i = 0; i < pickupItems.size(); i++) {
            nusenates[i] = pickupItems.get(i).getNusenate();
        }
        return nusenates;
    }

    public void setPickupItems(ArrayList<InvItem> pickupItems) {
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

    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }
}
