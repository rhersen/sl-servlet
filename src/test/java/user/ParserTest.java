package user;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Collection;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNull;

@SuppressWarnings("unchecked")
public class ParserTest {
    @Test
    public void parsesArray() throws Exception {
        Iterable<Map<String, Object>> trains = (Iterable<Map<String, Object>>) Parser.parse(new
                ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [" +
                "      { \"SiteId\": 1 }," +
                "      { \"SiteId\": 2 }," +
                "      { \"SiteId\": 3 }" +
                "    ]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8))).get("Trains");

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals(1, i.next().get("SiteId"));
        assertEquals(2, i.next().get("SiteId"));
        assertEquals(3, i.next().get("SiteId"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesInt() throws Exception {
        Iterable<Map<String, Object>> trains = (Iterable<Map<String, Object>>) Parser.parse(new
                ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [" +
                "      { \"SiteId\": 9525 }" +
                "    ]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8))).get("Trains");

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals(9525, i.next().get("SiteId"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesString() throws Exception {
        Iterable<Map<String, Object>> trains = (Iterable<Map<String, Object>>) Parser.parse(new
                ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Trains\": [      {\n" +
                "      \"Destination\": \"Märsta\"\n" +
                "    }]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8))).get("Trains");

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals("Märsta", i.next().get("Destination"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesLatestUpdate() throws Exception {
        Map<String, Object> result = Parser.parse(new ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"LatestUpdate\":\"2015-08-19T08:57:27\",\n" +
                "    \"Trains\": [      {\n" +
                "      \"Destination\": \"Märsta\"\n" +
                "    }]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8)));
        Iterable<Map<String, Object>> trains = (Iterable<Map<String, Object>>) result.get("Trains");

        assertEquals("2015-08-19T08:57:27", result.get("LatestUpdate"));
        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals("Märsta", i.next().get("Destination"));
        assertFalse(i.hasNext());
    }

    @Test
    public void parsesInputWithoutTrains() throws Exception {
        Collection<Map<String, Object>> trains = (Collection<Map<String, Object>>) Parser.parse(new
                ByteArrayInputStream(("{\n" +
                "  \"ResponseData\": {\n" +
                "    \"Buses\": [      {\n" +
                "      \"Destination\": \"Märsta\"\n" +
                "    }]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8))).get("Trains");

        assertNull(trains);
    }
}