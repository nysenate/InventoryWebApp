package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.Location;
import org.junit.Test;

import java.time.Instant;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class ItemParserTest
{

    @Test
    public void parsesIndividualCorrectly() {
        Item expected = new Item(1, "123456");
        expected.setSerialNumber("1a2b3c4D");
        expected.setCommodity(new Commodity(4, "subtype", "tech", "dj3"));
        expected.setLocation(new Location());
        expected.setStatus(ItemStatus.IN_TRANSIT);

        String json = Serializer.serialize(expected);
        Item actual = Serializer.deserialize(json, Item.class).get(0);


        assertEquals(1, actual.getId());
        assertEquals("123456", actual.getBarcode());
        assertEquals(ItemStatus.IN_TRANSIT, actual.getStatus());
        assertNotNull(actual.getCommodity());
        assertNotNull(actual.getLocation());
    }

    @Test
    public void parsesListCorrectly() {
        List<Item> expectedItems = new ArrayList<Item>();
        expectedItems.add(new Item(1, "111111"));
        expectedItems.add(new Item(2, "123456"));
        expectedItems.add(new Item(2831, "8271"));

        String json = Serializer.serialize(expectedItems);
        List<Item> actualItems = Serializer.deserialize(json, Item.class);

        assertEquals(expectedItems.size(), actualItems.size());
        assertEquals(expectedItems.get(2).getId(), actualItems.get(2).getId());
    }

}