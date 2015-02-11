package gov.nysenate.inventory.util;

import gov.nysenate.inventory.model.AdjustCode;
import org.junit.Test;

import java.util.List;

import static org.junit.Assert.*;

public class AdjustCodeParserTest
{

    @Test
    public void testParseSingle() {
        String testJson = "{\"code\":\"O\",\"description\":\"SURPLUS/SOLD\"}";
        List<AdjustCode> actual = Serializer.deserialize(testJson, AdjustCode.class);
        assertEquals("O", actual.get(0).getCode());
        assertEquals("SURPLUS/SOLD", actual.get(0).getDescription());
    }

    @Test
    public void testParseMultiple() {
        String testJson = "[{\"code\":\"O\",\"description\":\"SURPLUS/SOLD\"}," +
                "{\"code\":\"L\",\"description\":\"LOST/CONVERSION TO SFMS\"}," +
                "{\"code\":\"J\",\"description\":\"JUNKED BEYOND REPAIR\"}," +
                "{\"code\":\"S\",\"description\":\"STOLEN\"}," +
                "{\"code\":\"E\",\"description\":\"ERROR\"}," +
                "{\"code\":\"T\",\"description\":\"TRANSFERRED\"}]";

        List<AdjustCode> actual = Serializer.deserialize(testJson, AdjustCode.class);
        assertEquals(6, actual.size());
    }

}