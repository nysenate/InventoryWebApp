package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import gov.nysenate.inventory.model.AdjustCode;
import gov.nysenate.inventory.model.Item;
import gov.nysenate.inventory.model.ItemStatus;
import gov.nysenate.inventory.model.RemovalRequest;
import org.junit.Test;

import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import static org.junit.Assert.*;

public class RemovalRequestParserTest
{

    @Test
    public void parsesCorrectly() {
        Date expectedDate = new Date();
        RemovalRequest rr = new RemovalRequest("TestUser", expectedDate);

        String json = new Gson().toJson(rr);
        rr = RemovalRequestParser.parseRemovalRequest(json);

        assertEquals("TestUser", rr.getEmployee());
        assertEquals(expectedDate.toString(), rr.getDate().toString());

        List<Item> expectedItems = new ArrayList<Item>();
        expectedItems.add(new Item(1, "123456"));
        expectedItems.add(new Item(2, "654321"));

        AdjustCode expectedAdjustCode = new AdjustCode("L", "Lost/Missing");
        String expectedStatus = "PE";
        int expectedTransactionNum = 5;

        rr.setItems(expectedItems);
        rr.setAdjustCode(expectedAdjustCode);
        rr.setStatus(expectedStatus);
        rr.setTransactionNum(expectedTransactionNum);

        json = new Gson().toJson(rr);
        rr = RemovalRequestParser.parseRemovalRequest(json);

        assertEquals(expectedItems.get(0).getId(), rr.getItems().get(0).getId());
        assertEquals(expectedItems.get(0).getBarcode(), rr.getItems().get(0).getBarcode());
        assertEquals(expectedAdjustCode.getCode(), rr.getAdjustCode().getCode());
        assertEquals(expectedAdjustCode.getDescription(), rr.getAdjustCode().getDescription());
        assertEquals(expectedStatus, rr.getStatus());
        assertEquals(expectedDate.toString(), rr.getDate().toString());
        assertEquals(expectedTransactionNum, rr.getTransactionNum());
    }
}