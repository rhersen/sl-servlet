package user;

import org.junit.Test;

import java.util.*;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class CommonFieldsTest {

    @Test
    public void get() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        Map<String, Object> map = new HashMap<>();
        map.put("SiteId", "9530");
        map.put("StopAreaName", "Stockholms södra");
        map.put("StopAreaNumber", "5131");
        map.put("TransportMode", "TRAIN");
        trains.add(map);
        Collection<Object> result = new ArrayList<>();

        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("Trains", trains);
        responseData.put("LatestUpdate", "2015-08-19T22:44:05");
        responseData.put("DataAge", 22);
        result.addAll(CommonFields.get(responseData).values());

        assertEquals(asList("2015-08-19T22:44:05", 22, "9530", "Stockholms södra"), result);
    }

    @Test
    public void empty() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        Map<String, Object> result = CommonFields.extract(trains);
        assertTrue(result.isEmpty());
    }

    @Test
    public void same() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap("k1", "v1", "k2", "v2"));
        trains.add(getMap("k1", "v1", "k2", "v2"));

        Map<String, Object> result = CommonFields.extract(trains);

        assertEquals(2, result.size());
        assertTrue(result.keySet().containsAll(asList("k1", "k2")));
    }

    @Test
    public void different() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap("k1", "v1", "k2", "v2"));
        trains.add(getMap("k1", "v2", "k2", "v1"));
        Map<String, Object> result = CommonFields.extract(trains);
        assertTrue(result.isEmpty());
    }

    @Test
    public void oneSameOneDifferent() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap("k1", "v1", "k2", "v2"));
        trains.add(getMap("k1", "v1", "k2", "v1"));

        Map<String, Object> result = CommonFields.extract(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    @Test
    public void twoSameOneDifferent() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap("k1", "v1", "k2", "v2"));
        trains.add(getMap("k1", "v1", "k2", "v2"));
        trains.add(getMap("k1", "v1", "k2", "v1"));

        Map<String, Object> result = CommonFields.extract(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    @Test
    public void oneNull() throws Exception {
        Deque<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap("k1", "v1", "k2", "v1"));
        trains.add(getMap("k1", "v1", "k2", null));

        Map<String, Object> result = CommonFields.extract(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    private Map<String, Object> getMap(String k1, String v1, String k2, String v2) {
        Map<String, Object> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}