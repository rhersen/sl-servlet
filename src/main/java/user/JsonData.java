package user;

import java.util.Collection;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class JsonData {
    private static final Pattern hoursMinutes = Pattern.compile(".+T(\\d\\d):(\\d\\d):00");

    static Object getStopAreaName(Map<String, Object> map) {
        Collection<Map<String, Object>> trains = getTrains(map);
        if (trains.isEmpty())
            return map.get("SiteId");
        return trains.stream().findFirst().get().get("StopAreaName");
    }

    static boolean hasTrains(Map<String, Object> map) {
        return !(getTrains(map)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static Collection<Map<String, Object>> getTrains(Map<String, Object> map) {
        return (Collection<Map<String, Object>>) map.get("Trains");
    }

    @SuppressWarnings("unchecked")
    static Map<Integer, Map<String, Object>> indexedTrains(Map<String, Object> map) {
        Map<Integer, Map<String, Object>> r = new LinkedHashMap<>();
        for (Map<String, Object> train : getTrains(map)) {
            r.put(getId(train), train);
        }
        return r;
    }

    static int getId(Map<String, Object> train) {
        Map<String, Integer> lookup = new HashMap<>();
        lookup.put("9524", 22);
        lookup.put("9525", 18);
        lookup.put("9526", 15);
        lookup.put("9527", 12);
        lookup.put("9528", 9);

        Matcher m = hoursMinutes.matcher(getString(train, "TimeTabledDateTime"));
        if (!m.matches())
            return -1;

        return parseInt(m.group(1)) * 60 +
                parseInt(m.group(2)) +
                lookup.getOrDefault(getString(train, "SiteId"), 0);
    }

    private static CharSequence getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }
}
