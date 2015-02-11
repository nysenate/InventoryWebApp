package gov.nysenate.inventory.model;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Date;

/**
 * Maps to the Database table FM12INVINTRANS.
 * <p>
 * Adds Domain Logic methods.
 */
public class Transaction {

    private int nuxrpd;
    private Location origin;
    private Location destination;
    // Pickup Info
    private ArrayList<InvItem> pickupItems;
    private String pickupComments;
    private String napickupby;
    private String nareleaseby;
    private String nuxrrelsign;
    private Date pickupDate;
    private int count;
    // Delivery Info
    private ArrayList<String> checkedItems;
    private String deliveryComments;
    private String nadeliverby;
    private String naacceptby;
    private String nuxraccptsign;
    private Date deliveryDate;
    // Remote Info
    private int shipId;
    private String shipType;
    private String shipTypeDesc;
    private String shipComments;
    private int verificationId;
    private String verificationMethod;
    private String verificationComments;
    private int employeeId;
    private String helpReferenceNum;

    public Transaction() {
        origin = new Location();
        destination = new Location();
        pickupItems = new ArrayList<InvItem>();
        checkedItems = new ArrayList<String>();
        pickupComments = "";
        napickupby = "";
        nareleaseby = "";
        nuxrrelsign = "";
        pickupDate = new Date();
        deliveryComments = "";
        nadeliverby = "";
        naacceptby = "";
        nuxraccptsign = "";
        deliveryDate = new Date();
        shipType = "";
        shipComments = "";
        verificationMethod = "";
        verificationComments = "";
        helpReferenceNum = "";
    }

    public Transaction shallowCopy() {
        Transaction copy = new Transaction();
        copy.origin = this.origin;
        copy.destination = this.destination;
        copy.pickupItems = this.pickupItems;
        copy.checkedItems = this.checkedItems;
        copy.pickupComments = this.pickupComments;
        copy.napickupby = this.napickupby;
        copy.nareleaseby = this.nareleaseby;
        copy.nuxrrelsign = this.nuxrrelsign;
        copy.pickupDate = this.pickupDate;
        copy.deliveryComments = this.deliveryComments;
        copy.nadeliverby = this.nadeliverby;
        copy.naacceptby = this.naacceptby;
        copy.nuxraccptsign = this.nuxraccptsign;
        copy.deliveryDate = this.deliveryDate;
        copy.shipType = this.shipType;
        copy.shipComments = this.shipComments;
        copy.verificationMethod = this.verificationMethod;
        copy.verificationComments = this.verificationComments;
        copy.helpReferenceNum = this.helpReferenceNum;

        return copy;
    }

    // shipType must exists for all remote transactions.
    public boolean isRemote() {
        if (shipType == null) {
            return false;
        } else {
            return !shipType.equals("");
        }
    }

    public boolean isRemoteDelivery() {

        if (this.isRemote()) {

            try {
                return origin.getAdcity().equalsIgnoreCase("Albany");
            } catch (Exception e) {
                e.printStackTrace();
                return false;
            }
        }
        return false;
    }

    public boolean isRemotePickup() {
        if (this.isRemote()) {
            return destination.getAdcity().equalsIgnoreCase("Albany");
        }
        return false;
    }

    public String getRemoteType() {
        if (this.isRemote()) {
            if (origin.getAdcity().equalsIgnoreCase("Albany")) {
                return "RDL";
            }

            return "RPK";
        }
        return "";
    }

    public ArrayList<String> getNotCheckedItems() {
        ArrayList<String> notCheckedItems = new ArrayList<String>();
        for (InvItem item : pickupItems) {
            if (!checkedItems.contains(item.getNusenate())) {
                notCheckedItems.add(item.getNusenate());
            }
        }
        return notCheckedItems;
    }

    public String getOriginCdLoc() {
        return origin.getCdlocat();
    }

    public String getOriginCdLocType() {
        return origin.getCdloctype();
    }

    public String getOriginAddressLine1() {
        return origin.getAdstreet1();
    }

    public String getDestinationCdLoc() {
        return destination.getCdlocat();
    }

    public String getDestinationCdLocType() {
        return destination.getCdloctype();
    }

    public String getDestinationAddressLine1() {
        return destination.getAdstreet1();
    }

    public int getNuxrpd() {
        return nuxrpd;
    }

    public void setNuxrpd(int nuxrpd) {
        this.nuxrpd = nuxrpd;
    }

    public void setOrigin(Location origin) {
        this.origin = origin;
    }

    public Location getOrigin() {
        return origin;
    }

    public void setDestination(Location destination) {
        this.destination = destination;
    }

    public Location getDestination() {
        return destination;
    }

    public String getOriginSummaryString() {
        return origin.getLocationSummaryString();
    }

    public String getDestinationSummaryString() {
        return destination.getLocationSummaryString();
    }

    public int getShipId() {
        return shipId;
    }

    public void setShipId(int shipId) {
        this.shipId = shipId;
    }

    public String getShipType() {
        return shipType;
    }

    public void setShipType(String shipType) {
        this.shipType = shipType;
    }

    public String getShipTypeDesc() {
        return shipTypeDesc;
    }

    public void setShipTypeDesc(String shipTypeDesc) {
        this.shipTypeDesc = shipTypeDesc;
    }

    public String getShipComments() {
        return shipComments;
    }

    public void setShipComments(String shipComments) {
        this.shipComments = shipComments;
    }

    public int getVerificationId() {
        return verificationId;
    }

    public void setVerificationId(int verificationId) {
        this.verificationId = verificationId;
    }

    public String getVerificationMethod() {
        return verificationMethod;
    }

    public void setVerificationMethod(String verificationMethod) {
        this.verificationMethod = verificationMethod;
    }

    public String getVerificationComments() {
        return verificationComments;
    }

    public void setVerificationComments(String verificationComments) {
        this.verificationComments = verificationComments;
    }

    public int getEmployeeId() {
        return employeeId;
    }

    public void setEmployeeId(int employeeId) {
        this.employeeId = employeeId;
    }

    public String getHelpReferenceNum() {
        return helpReferenceNum;
    }

    public void setHelpReferenceNum(String helpReferenceNum) {
        this.helpReferenceNum = helpReferenceNum;
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

    public String getPickupComments() {
        return pickupComments;
    }

    public void setPickupComments(String comments) {
        this.pickupComments = comments;
    }

    public String getNapickupby() {
        return napickupby;
    }

    public void setNapickupby(String napickupby) {
        if (napickupby != null) {
            this.napickupby = napickupby.toUpperCase();
        } else {
            this.napickupby = "";
        }
    }

    public String getNareleaseby() {
        return nareleaseby;
    }

    public void setNareleaseby(String nareleaseby) {
        if (nareleaseby != null) {
            this.nareleaseby = nareleaseby.toUpperCase();
        } else {
            this.nareleaseby = "";
        }
    }

    public String getNuxrrelsign() {
        return nuxrrelsign;
    }

    public void setNuxrrelsign(String nuxrrelsign) {
        this.nuxrrelsign = nuxrrelsign;
    }

    public Date getPickupDate() {
        return pickupDate;
    }

    public void setPickupDate(Date date) {
        this.pickupDate = date;
    }

    public Date getDeliveryDate() {
        return deliveryDate;
    }

    public void setDeliveryDate(Date date) {
        this.deliveryDate = date;
    }

    public void setCount(int count) {
        this.count = count;
    }

    public int getCount() {
        return count;
    }

    public ArrayList<String> getCheckedItems() {
        return checkedItems;
    }

    public void setCheckedItems(String[] checkedItems) {
        this.checkedItems = new ArrayList<String>(Arrays.asList(checkedItems));
    }

    public void setCheckedItems(ArrayList<InvItem> checkedItems) {
        this.checkedItems.clear();
        for (InvItem item : checkedItems) {
            this.checkedItems.add(item.getNusenate());
        }
    }

    public String getDeliveryComments() {
        return deliveryComments;
    }

    public void setDeliveryComments(String comments) {
        this.deliveryComments = comments;
    }

    public String getNadeliverby() {
        return nadeliverby;
    }

    public void setNadeliverby(String nadeliverby) {
        if (nadeliverby != null) {
            this.nadeliverby = nadeliverby.toUpperCase();
        } else {
            this.nadeliverby = "";
        }
    }

    public String getNaacceptby() {
        return naacceptby;
    }

    public void setNaacceptby(String naacceptby) {
        if (naacceptby != null) {
            this.naacceptby = naacceptby.toUpperCase();
        } else {
            this.naacceptby = "";
        }
    }

    public String getNuxraccptsign() {
        return nuxraccptsign;
    }

    public void setNuxrsccptsign(String nuxraccptsign) {
        this.nuxraccptsign = nuxraccptsign;
    }

    public String getOriginFullAddress() {
        return origin.getFullAddress();
    }

    public String getDestinationFullAddress() {
        return destination.getFullAddress();
    }
}
