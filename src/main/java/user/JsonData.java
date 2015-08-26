package user;

import java.util.Deque;
import java.util.Map;

public class JsonData {
    static Object getStopAreaName(Map<String, Object> found) {
        return getTrains(found).getFirst().get("StopAreaName");
    }

    static boolean hasTrains(Map<String, Object> found) {
        return !(getTrains(found)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    static Deque<Map<String, Object>> getTrains(Map<String, Object> found) {
        return (Deque<Map<String, Object>>) found.get("Trains");
    }
}
