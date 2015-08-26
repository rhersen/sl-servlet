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
        assertTrue(JsonData.getTrains(empty()).isEmpty());
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
}