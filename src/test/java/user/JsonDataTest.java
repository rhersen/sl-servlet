package user;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class JsonDataTest {

    @Test
    public void testGetStopAreaName() throws Exception {
        assertEquals("Jakobsberg", JsonData.getStopAreaName(oneTrain()));
    }

    @Test
    public void testHasTrains() throws Exception {
        assertTrue(JsonData.hasTrains(oneTrain()));
        assertFalse(JsonData.hasTrains(empty()));
    }

    @Test
    public void testGetTrains() throws Exception {
        assertTrue(JsonData.indexedTrains(empty()).isEmpty());
    }

    @Test
    public void id() throws Exception {
        assertEquals(1225, JsonData.getId(getTrain("20:16", "1", "9528")));
        assertEquals(1240, JsonData.getId(getTrain("20:31", "1", "9528")));
        assertEquals(1240, JsonData.getId(getTrain("20:28", "1", "9527")));
        assertEquals(1240, JsonData.getId(getTrain("20:25", "1", "9526")));
        assertEquals(1240, JsonData.getId(getTrain("20:22", "1", "9525")));
        assertEquals(1240, JsonData.getId(getTrain("20:18", "1", "9524")));
        assertEquals(1270, JsonData.getId(getTrain("20:48", "1", "9524")));
        assertEquals(1218, JsonData.getId(getTrain("20:18", "1", "666")));
        assertEquals(-1, JsonData.getId(getTrain("ZO:lB", "1", "666")));
    }

    private Map<String, Object> empty() {
        Map<String, Object> map = new HashMap<>();
        map.put("Trains", new ArrayDeque<Map<String, Object>>());
        return map;
    }

    private Map<String, Object> oneTrain() {
        Map<String, Object> map = new HashMap<>();
        Collection<Map<String, Object>> value = new ArrayDeque<>();
        value.add(train());
        map.put("Trains", value);
        return map;
    }

    private Map<String, Object> train() {
        Map<String, Object> jakobsberg = new HashMap<>();
        jakobsberg.put("StopAreaName", "Jakobsberg");
        return jakobsberg;
    }

    private Map<String, Object> getTrain(String time, String dir, String siteId) {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T" + time + ":00");
        train.put("JourneyDirection", dir);
        train.put("SiteId", siteId);
        return train;
    }
}
