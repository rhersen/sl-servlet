package user;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.lang.Integer.parseInt;

public class JsonData {
    private static final Pattern hoursMinutes = Pattern.compile(".+T(\\d\\d):(\\d\\d):00");

    static Optional<Map<String, Object>> getFirstTrain(Map<String, Object> map) {
        return getTrains(map).stream().findFirst();
    }

    static boolean hasTrains(Map<String, Object> map) {
        return !(getTrains(map)).isEmpty();
    }

    @SuppressWarnings("unchecked")
    private static Collection<Map<String, Object>> getTrains(Map<String, Object> map) {
        return (Collection<Map<String, Object>>) map.get("TrainAnnouncement");
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
