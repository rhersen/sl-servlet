package user;

import org.junit.Test;

import java.util.*;

import static org.junit.Assert.*;

public class JsonDataTest {

    @Test
    public void testGetFirstTrain() throws Exception {
        Optional<Map<String, Object>> result = JsonData.getFirstTrain(oneTrain());
        assertEquals("Jkb", result.get().get("LocationSignature"));
    }

    @Test
    public void getFirstTrainDoesNotCrash() throws Exception {
        Optional<Map<String, Object>> result = JsonData.getFirstTrain(empty());
        assertFalse(result.isPresent());
    }

    @Test
    public void testHasTrains() throws Exception {
        assertTrue(JsonData.hasTrains(oneTrain()));
        assertFalse(JsonData.hasTrains(empty()));
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
        map.put("TrainAnnouncement", new ArrayDeque<Map<String, Object>>());
        return map;
    }

    private Map<String, Object> oneTrain() {
        Map<String, Object> map = new HashMap<>();
        Collection<Map<String, Object>> value = new ArrayDeque<>();
        value.add(train());
        map.put("TrainAnnouncement", value);
        return map;
    }

    private Map<String, Object> train() {
        Map<String, Object> jakobsberg = new HashMap<>();
        jakobsberg.put("LocationSignature", "Jkb");
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
