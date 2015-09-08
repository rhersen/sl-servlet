package user;

import java.time.LocalDateTime;
import java.time.temporal.Temporal;
import java.util.Collection;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.time.Duration.between;
import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;
import static java.util.stream.Collectors.joining;
import static java.util.stream.Collectors.toList;

public class TrainFormatter {

    private static final Pattern wholeMinutes = Pattern.compile(".+T(.+):00");
    private static final Pattern dateTime = Pattern.compile(".+T(.+)");
    private static final Pattern hhmm = Pattern.compile("\\d\\d:\\d\\d");

    public static String get(Map<String, Object> train, String key) {
        if (key.equals("expecteddatetime"))
            return getExpected(train);
        if (key.equals("timetableddatetime"))
            return getTimeTabled(train);
        if (key.equals("displaytime"))
            return getDisplay(train);
        if (key.equals("remaining"))
            return getRemaining(train, LocalDateTime.now());
        return getString(train, key);
    }

    public static String getRemaining(Map<String, Object> train, Temporal now) {
        LocalDateTime expectedDateTime = parse(getString(train, "ExpectedDateTime"));
        return Long.toString(between(now, expectedDateTime).getSeconds());
    }

    private static String getTimeTabled(Map<String, Object> train) {
        Matcher m;
        String raw = getString(train, "TimeTabledDateTime");
        if (raw.equals(getString(train, "ExpectedDateTime")))
            return "";
        return (m = wholeMinutes.matcher(raw)).matches() ? m.group(1) : raw;
    }

    private static String getExpected(Map<String, Object> train) {
        @SuppressWarnings("unchecked") Collection<Map<String, Object>>
                deviations = (Collection<Map<String, Object>>) train.get("Deviations");
        if (deviations != null && !deviations.isEmpty()) {
            Collection<Map<String, Object>> important = deviations
                    .stream()
                    .filter(deviation -> (Integer) deviation.get("ImportanceLevel") < 5)
                    .collect(toList());
            if (!important.isEmpty())
                return important
                        .stream()
                        .map(deviation -> deviation.get("Text"))
                        .map(Object::toString)
                        .collect(joining());
        }

        Matcher m;
        String raw = getString(train, "ExpectedDateTime");
        for (Pattern pattern : asList(wholeMinutes, dateTime))
            if ((m = pattern.matcher(raw)).matches())
                return m.group(1);
        return raw;
    }

    private static String getDisplay(Map<String, Object> train) {
        String raw = getString(train, "DisplayTime");
        return hhmm.matcher(raw).matches() ? "" : raw;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }
}
