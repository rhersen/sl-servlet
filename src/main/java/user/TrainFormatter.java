package user;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Deque;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.between;
import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;

class TrainFormatter {

    private static final Pattern wholeMinutes = Pattern.compile(".+T(.+):00");
    private static final Pattern dateTime = Pattern.compile(".+T(.+)");

    static String get(Map<String, Object> train, String key) {
        if (key.equals("advertisedtimeatlocation"))
            return getWholeMinutes(train, "AdvertisedTimeAtLocation");
        if (key.equals("tolocation"))
            return getDestination(train);
        if (key.equals("remaining"))
            return getRemaining(train, LocalDateTime.now());
        if (key.equals("time"))
            return time(train);
        return getString(train, key);
    }

    static String getRemaining(Map<String, Object> train, Temporal now) {
        String t = getString(train, "EstimatedTimeAtLocation");
        if (t.isEmpty())
            t = getString(train, "AdvertisedTimeAtLocation");
        LocalDateTime expectedDateTime = parse(t);
        long seconds = between(now, expectedDateTime).getSeconds();
        if (seconds >= 600)
            return String.format("%dm", seconds / 60);
        else if (seconds >= 0)
            return String.format("%d:%02d", seconds / 60, seconds % 60);
        else if (seconds >= -100)
            return String.format("%d", seconds);
        else
            return "";
    }

    private static String getWholeMinutes(Map<String, Object> train, String field) {
        Matcher m;
        String raw = getString(train, field);
        for (Pattern pattern : asList(wholeMinutes, dateTime))
            if ((m = pattern.matcher(raw)).matches())
                return m.group(1);
        return raw;
    }

    private static String getDestination(Map<String, Object> train) {
        Deque raw = (Deque) train.get("ToLocation");
        if (raw != null && !raw.isEmpty())
            return raw.getLast().toString();
        else
            return "" + raw;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }

    static boolean isEstimated(Map<String, Object> train) {
        return train.get("EstimatedTimeAtLocation") != null && train.get("TimeAtLocation") == null;
    }

    static boolean isActual(Map<String, Object> train) {
        return train.get("TimeAtLocation") != null;
    }

    static String time(Map<String, Object> train) {
        if (isActual(train))
            return getWholeMinutes(train, "TimeAtLocation");
        if (isEstimated(train))
            return getWholeMinutes(train, "EstimatedTimeAtLocation");
        return getWholeMinutes(train, "AdvertisedTimeAtLocation");
    }
}
