package gov.nysenate.inventory.util;

import com.google.gson.Gson;
import gov.nysenate.inventory.model.InvItem;
import org.junit.Test;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;

public class InvItemParserTest
{
    @Test
    public void correctlyParsesJson() {
        Gson gson = new Gson();
        InvItem expected = new InvItem();
        expected.setNusenate("123456");
        expected.setCdlocat("Albany");

        InvItem actual = InvItemParser.parseItem(gson.toJson(expected));
        assertEquals(actual.getNusenate(), "123456");
        assertEquals(actual.getCdlocat(), "Albany");
    }

    // Manually made json representing and InvItem in ItemDetails servlet needs to be parsable.
    @Test
    public void correctlyParsesManuallyMadeJson() {
        String manuallyMadeJson = "{\"nusenate\":\"070963\",\"nuxrefsn\":\"63015\",\"dtissue\":\"04-MAY-01\",\"cdlocatto\":\"L905\",\"cdloctypeto\":\"W\",\"cdcategory\":\"TELEPHONE\",\"adstreet1to\":\"RM. 905 LOB\",\"decommodityf\":\"TELEPHONE- #8410D- LUCENT DIGITAL BLACK - DESK TYPE- W/ BUILT IN DISPLAY - PART # 323505BK..\",\"cdlocatfrom\":\"L215\",\"cdstatus\":\"I\",\"cdintransit\":\"N\"}";

        InvItem item = InvItemParser.parseItem(manuallyMadeJson);
        assertEquals(item.getNusenate(), "070963");
        assertEquals(item.getCdlocatto(), "L905");
    }
}
