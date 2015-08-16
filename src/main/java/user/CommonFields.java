package user;

import java.util.*;

public class CommonFields {
    static HashMap<String, Object> get(Collection<Map<String, Object>> trains) {
        HashMap<String, Object> r = new HashMap<>();

        if (trains.isEmpty())
            return r;

        Map<String, Object> first = trains.iterator().next();
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
