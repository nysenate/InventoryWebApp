package gov.nysenate.inventory.model;

import gov.nysenate.inventory.server.InvItem;

import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {
    // TODO: Divide transaction into a pickup and delivery?
    private int nuxrpd; // TODO: int or string?
    private Location origin;
    private Location destination;
    private ArrayList<InvItem> items;// //// not used yet..
    private String[] itemsToDeliver;
    private String[] checkedItems;
    private String[] notCheckedItems;
    private String pickupComments;
    private String deliveryComments;

    private String naPickupBy = "";
    private String naReleaseBy = "";
    private String naDeliverBy = "";
    private String naAcceptBy = ""; // not used in pickup..
    private String nuxrRelSign = "";
    private String nuxrAccptSign = "";
    // SignatureView signature;
    // byte[] sigBytes;

    public Transaction() {
        this.origin = new Location();
        this.destination = new Location();
        this.items = new ArrayList<InvItem>();
    }

    public void generateNotCheckedItems() {
        ArrayList<String> notChecked = new ArrayList<String>(Arrays.asList(this.itemsToDeliver));
        for (String chkItem : this.checkedItems) {
            notChecked.remove(chkItem);
        }
        this.notCheckedItems = notChecked.toArray(new String[notChecked.size()]);
    }

    public Location getOrigin() {
        return origin;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getDestination() {
        return destination;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public ArrayList<InvItem> getItems() {
        return items;
    }

    public void setItems(ArrayList<InvItem> items) {
        this.items = items;
    }

    public String getPickupComments() {
        return pickupComments;
    }

    public void setPickupComments(String comments) {
        this.pickupComments = comments;
    }

    public String getDeliveryComments() {
        return deliveryComments;
    }

    public void setDeliveryComments(String comments) {
        this.deliveryComments = comments;
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

    public String getNuxrRelSign() {
        return nuxrRelSign;
    }

    public void setNuxrRelSign(String nuxrRelSign) {
        this.nuxrRelSign = nuxrRelSign;
    }

    public String getNuxrAccptSign() {
        return nuxrAccptSign;
    }

    public void setNuxrAccptSign(String nuxrAccptSign) {
        this.nuxrAccptSign = nuxrAccptSign;
    }

    public String[] getItemsToDeliver() {
        return itemsToDeliver;
    }

    public void setItemsToDeliver(String[] items) {
        this.itemsToDeliver = items;
    }

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
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

}
