package user;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.ArrayDeque;
import java.util.Collection;
import java.util.Deque;
import java.util.Map;
import java.util.function.BinaryOperator;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Stream;

import static java.time.Duration.between;
import static java.time.LocalDateTime.parse;

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
        if (key.equals("productinformation"))
            return getProductInformation(train);
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
        if (seconds >= 0)
            return String.format("%d:%02d", seconds / 60, seconds % 60);
        if (seconds >= -100)
            return String.format("%d", seconds);
        return "";
    }

    private static String getWholeMinutes(Map<String, Object> train, String field) {
        String value = getString(train, field);
        return Stream.of(wholeMinutes, dateTime)
                .map(p -> p.matcher(value))
                .filter(Matcher::matches)
                .map(m -> m.group(1))
                .findFirst()
                .orElse(value);
    }

    private static String getDestination(Map<String, Object> train) {
        //noinspection unchecked
        Collection<String> raw = (Collection<String>) train.get("ToLocation");

        if (raw == null)
            return "";

        return raw.stream().reduce((a, b) -> b).orElse("");
    }

    private static String getProductInformation(Map<String, Object> train) {
        //noinspection unchecked
        Deque<String> raw = (ArrayDeque<String>) train.get("ProductInformation");

        if (raw == null || raw.isEmpty())
            return "";

        return raw.getFirst();
    }

    private static String getString(Map<String, Object> map, String key) {
        return map.getOrDefault(key, "").toString();
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
        return "";
    }
}
