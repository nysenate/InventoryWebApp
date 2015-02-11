package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.InvItem;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.assertEquals;

public class InvItemParserTest
{
    @Test
    public void correctlyParsesJson() {
        InvItem expected = new InvItem();
        expected.setNusenate("123456");
        expected.setCdlocat("Albany");

        String json = Serializer.serialize(expected);
        List<InvItem> actual = Serializer.deserialize(json, InvItem.class);
        assertEquals(actual.get(0).getNusenate(), "123456");
        assertEquals(actual.get(0).getCdlocat(), "Albany");
    }

    // Manually made json representing and InvItem in ItemDetails servlet needs to be parsable.
    @Test
    public void correctlyParsesManuallyMadeJson() {
        String manuallyMadeJson = "{\"nusenate\":\"070963\",\"nuxrefsn\":\"63015\",\"dtissue\":\"04-MAY-01\",\"cdlocatto\":\"L905\",\"cdloctypeto\":\"W\",\"cdcategory\":\"TELEPHONE\",\"adstreet1to\":\"RM. 905 LOB\",\"decommodityf\":\"TELEPHONE- #8410D- LUCENT DIGITAL BLACK - DESK TYPE- W/ BUILT IN DISPLAY - PART # 323505BK..\",\"cdlocatfrom\":\"L215\",\"cdstatus\":\"I\",\"cdintransit\":\"N\"}";

        List<InvItem> item = Serializer.deserialize(manuallyMadeJson, InvItem.class);
        assertEquals(item.get(0).getNusenate(), "070963");
        assertEquals(item.get(0).getCdlocatto(), "L905");
    }
}
