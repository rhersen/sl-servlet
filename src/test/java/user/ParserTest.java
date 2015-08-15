package user;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

public class ParserTest {
    @Test
    public void parsesArray() throws Exception {
        Collection<Map<String, Object>> trains = Parser.trains(new ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [" +
                "      { \"SiteId\": 1 }," +
                "      { \"SiteId\": 2 }," +
                "      { \"SiteId\": 3 }" +
                "    ]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8)));

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals(1, i.next().get("SiteId"));
        assertEquals(2, i.next().get("SiteId"));
        assertEquals(3, i.next().get("SiteId"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesInt() throws Exception {
        Collection<Map<String, Object>> trains = Parser.trains(new ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [" +
                "      { \"SiteId\": 9525 }" +
                "    ]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8)));

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals(9525, i.next().get("SiteId"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesString() throws Exception {
        Collection<Map<String, Object>> trains = Parser.trains(new ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [      {\n" +
                "      \"Destination\": \"Märsta\"\n" +
                "    }]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8)));

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals("Märsta", i.next().get("Destination"));
        assertFalse(i.hasNext());
    }
}