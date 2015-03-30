package gov.nysenate.inventory.dto;

import gov.nysenate.inventory.model.Item;

import java.util.Date;

public class ItemInventoriedDetails {

    private Item item;
    private Date lastInventoried;

    public ItemInventoriedDetails(Item item, Date lastInventoried) {
        this.item = item;
        this.lastInventoried = lastInventoried;
    }

    public Item getItem() {
        return item;
    }

    public Date getLastInventoried() {
        return lastInventoried;
    }
}
