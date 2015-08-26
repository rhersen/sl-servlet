package user;

import java.util.Deque;
import java.util.Map;

public class JsonData {
    static Object getStopAreaName(Map<String, Object> map) {
        return getTrains(map).getFirst().get("StopAreaName");
    }

    static boolean hasTrains(Map<String, Object> map) {
        return !(getTrains(map)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    static Deque<Map<String, Object>> getTrains(Map<String, Object> map) {
        return (Deque<Map<String, Object>>) map.get("Trains");
    }
}