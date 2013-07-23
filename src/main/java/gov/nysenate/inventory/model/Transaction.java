package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Arrays;

public class Transaction {

    private String nuxrpd;
    private Location origin;
    private Location destination;
    private Pickup pickup;
    private Delivery delivery;

    // SignatureView signature;
    // byte[] sigBytes;

    public Transaction() {
        this.origin = new Location();
        this.destination = new Location();
        this.pickup = new Pickup();
        this.delivery = new Delivery();
    }

    public void generateDeliveryNotCheckedItems() {
        ArrayList<String> notChecked = new ArrayList<String>(Arrays.asList(this.pickup.getPickupItems()));
        for (String chkItem : this.delivery.getCheckedItems()) {
            notChecked.remove(chkItem);
        }
        this.delivery.setNotCheckedItems(notChecked.toArray(new String[notChecked.size()]));
    }

    public String getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(String nuxrpd) {
        this.nuxrpd = nuxrpd;
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

    public Pickup getPickup() {
        return pickup;
    }

    public void setPickup(Pickup pickup) {
        this.pickup = pickup;
    }

    public Delivery getDelivery() {
        return delivery;
    }

    public void setDelivery(Delivery delivery) {
        this.delivery = delivery;
    }
}
