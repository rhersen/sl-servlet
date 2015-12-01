package user;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.nio.charset.StandardCharsets;
import java.util.Iterator;
import java.util.Map;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;

@SuppressWarnings("unchecked")
public class ParserTest {
    @Test
    public void parsesArray() throws Exception {
        Iterable<Map<String, Object>> trains = (Iterable<Map<String, Object>>) Parser.parse(new
                ByteArrayInputStream(("{\n" +
                "  \"RESPONSE\": {\n" +
                "    \"RESULT\": [\n" +
                "      {\n" +
                "        \"TrainAnnouncement\": [\n" +
                "          {\"AdvertisedTimeAtLocation\": \"2015-11-30T20:59:00\", \"ToLocation\": [\"Söc\"]},\n" +
                "          {\"AdvertisedTimeAtLocation\": \"2015-11-30T21:14:00\", \"ToLocation\": [\"Söc\"]},\n" +
                "          {\"AdvertisedTimeAtLocation\": \"2015-11-30T21:18:00\", \"ToLocation\": [\"Cst\", \"Mr\"]}\n" +
                "        ]\n" +
                "      }\n" +
                "    ]\n" +
                "  }\n" +
                "}").getBytes(StandardCharsets.UTF_8))).get("TrainAnnouncement");

        Iterator<Map<String, Object>> i = trains.iterator();
        assertEquals("2015-11-30T20:59:00", i.next().get("AdvertisedTimeAtLocation"));
        assertEquals("2015-11-30T21:14:00", i.next().get("AdvertisedTimeAtLocation"));
        assertEquals("2015-11-30T21:18:00", i.next().get("AdvertisedTimeAtLocation"));
        assertFalse(i.hasNext());
    }

}
