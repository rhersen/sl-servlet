package user;

import java.util.*;

public class CommonFields {
    static Map<String, Object> get(Map<String, Object> responseData) {
        Map<String, Object> r = new LinkedHashMap<>();

        responseData
                .entrySet()
                .stream()
                .filter(e -> e.getValue() instanceof String || e.getValue() instanceof Integer)
                .forEach(e -> r.put(e.getKey(), e.getValue()));

        @SuppressWarnings("unchecked") Deque<Map<String, Object>>
                trains = (Deque<Map<String, Object>>) responseData.get("TrainAnnouncement");

        if (trains.isEmpty())
            return r;

        Map<String, Object> first = trains.getFirst();
        String key = "SiteId";
        r.put(key, first.get(key));
        return r;
    }

    static Map<String, Object> extract(Deque<Map<String, Object>> trains) {
        Map<String, Object> r = new HashMap<>();

        if (trains.isEmpty())
            return r;

        Map<String, Object> first = trains.getFirst();
        r.putAll(first);
        Set<Map.Entry<String, Object>> firstEntries = first.entrySet();

        for (Map<String, Object> train : trains)
            firstEntries.stream()
                    .filter(entry -> !isEqual(train.get(entry.getKey()), entry.getValue()))
                    .forEach(entry -> r.remove(entry.getKey()));

        return r;
    }

    private static boolean isEqual(Object v1, Object v2) {
        if (v1 == null)
            return v2 == null;
        return v1.equals(v2);
    }
}
