package gov.nysenate.inventory.model;

import gov.nysenate.inventory.server.InvItem;

import java.util.ArrayList;
import java.util.Arrays;

public class Pickup extends Transaction {

    private ArrayList<InvItem> pickupItems;
    private String comments;
    private String napickupby;
    private String nareleaseby;
    private String nuxrrelsign;
    private String date;
    private int count;

    public Pickup() {
        comments = "";
        napickupby = "";
        nareleaseby = "";
        nuxrrelsign = "";
        pickupItems = new ArrayList<InvItem>();
        origin = new Location();
        destination = new Location();
    }

    public ArrayList<InvItem> getPickupItems() {
        return pickupItems;
    }

    public void setPickupItems(InvItem[] pickupItems) {
        this.pickupItems = new ArrayList<InvItem>(Arrays.asList(pickupItems));
    }

    public void setPickupItems(String[] items) {
        ArrayList<InvItem> pickupItems = new ArrayList<InvItem>();
        for (String item : items) {
            InvItem invItem = new InvItem();
            invItem.setNusenate(item);
            pickupItems.add(invItem);
        }
        this.pickupItems = pickupItems;
    }

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

    public String getNapickupby() {
        return napickupby;
    }

    public void setNapickupby(String napickupby) {
        this.napickupby = napickupby.toUpperCase();
    }

    public String getNareleaseby() {
        return nareleaseby;
    }

    public void setNareleaseby(String nareleaseby) {
        this.nareleaseby = nareleaseby.toUpperCase();
    }

    public String getNuxrrelsign() {
        return nuxrrelsign;
    }

    public void setNuxrrelsign(String nuxrrelsign) {
        this.nuxrrelsign = nuxrrelsign;
    }
    
    public String getDate() {
        return date;
    }

    public void setDate(String date) {
        this.date = date;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }
}
