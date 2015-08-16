package user;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;

import static java.util.Arrays.asList;
import static org.junit.Assert.*;

public class CommonFieldsTest {

    @Test
    public void empty() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        HashMap<String, Object> result = CommonFields.get(trains);
        assertTrue(result.isEmpty());
    }

    @Test
    public void same() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap2("k1", "v1", "k2", "v2"));
        trains.add(getMap2("k1", "v1", "k2", "v2"));

        HashMap<String, Object> result = CommonFields.get(trains);

        assertEquals(2, result.size());
        assertTrue(result.keySet().containsAll(asList("k1", "k2")));
    }

    @Test
    public void different() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap2("k1", "v1", "k2", "v2"));
        trains.add(getMap2("k1", "v2", "k2", "v1"));
        HashMap<String, Object> result = CommonFields.get(trains);
        assertTrue(result.isEmpty());
    }

    @Test
    public void oneSameOneDifferent() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap2("k1", "v1", "k2", "v2"));
        trains.add(getMap2("k1", "v1", "k2", "v1"));

        HashMap<String, Object> result = CommonFields.get(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    @Test
    public void twoSameOneDifferent() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap2("k1", "v1", "k2", "v2"));
        trains.add(getMap2("k1", "v1", "k2", "v2"));
        trains.add(getMap2("k1", "v1", "k2", "v1"));

        HashMap<String, Object> result = CommonFields.get(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    @Test
    public void oneNull() throws Exception {
        Collection<Map<String, Object>> trains = new ArrayDeque<>();
        trains.add(getMap2("k1", "v1", "k2", "v1"));
        trains.add(getMap2("k1", "v1", "k2", null));

        HashMap<String, Object> result = CommonFields.get(trains);

        assertEquals(result.toString(), 1, result.size());
        assertTrue(result.keySet().contains("k1"));
    }

    private Map<String, Object> getMap1(String k1, String v1) {
        Map<String, Object> map = new HashMap<>();
        map.put(k1, v1);
        return map;
    }

    private Map<String, Object> getMap2(String k1, String v1, String k2, String v2) {
        Map<String, Object> map = new HashMap<>();
        map.put(k1, v1);
        map.put(k2, v2);
        return map;
    }
}