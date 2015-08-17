package user;

import java.util.*;

import static java.util.Arrays.asList;

public class CommonFields {
    static Map<String, Object> get(Deque<Map<String, Object>> trains) {
        Map<String, Object> r = new LinkedHashMap<>();

        if (trains.isEmpty())
            return r;

        Map<String, Object> first = trains.getFirst();
        for (String key : asList("TransportMode", "SiteId", "StopAreaName", "StopAreaNumber"))
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

        for (Map<String, Object> train : trains) {
            firstEntries.stream()
                    .filter(entry -> !isEqual(train.get(entry.getKey()), entry.getValue()))
                    .forEach(entry -> r.remove(entry.getKey()));
        }

        return r;
    }

    private static boolean isEqual(Object v1, Object v2) {
        if (v1 == null)
            return v2 == null;
        return v1.equals(v2);
    }
}
