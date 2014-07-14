package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.AdjustCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AdjustCodeParserTest
{

    @Test
    public void testParseSingle() {
        AdjustCodeParser parser = new AdjustCodeParser();
        String testJson = "{\"code\":\"O\",\"description\":\"SURPLUS/SOLD\"}";
        AdjustCode actual = parser.parseAdjustCode(testJson);
        assertEquals("O", actual.getCode());
        assertEquals("SURPLUS/SOLD", actual.getDescription());
    }

    @Test
    public void testParseMultiple() {
        AdjustCodeParser parser = new AdjustCodeParser();
        String testJson = "[{\"code\":\"O\",\"description\":\"SURPLUS/SOLD\"}," +
                "{\"code\":\"L\",\"description\":\"LOST/CONVERSION TO SFMS\"}," +
                "{\"code\":\"J\",\"description\":\"JUNKED BEYOND REPAIR\"}," +
                "{\"code\":\"S\",\"description\":\"STOLEN\"}," +
                "{\"code\":\"E\",\"description\":\"ERROR\"}," +
                "{\"code\":\"T\",\"description\":\"TRANSFERRED\"}]";

        List<AdjustCode> actual = parser.parseAdjustCodes(testJson);
        assertEquals(6, actual.size());
    }

}