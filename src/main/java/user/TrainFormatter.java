package user;

import java.util.*;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
import java.util.stream.Collectors;

import static java.util.Arrays.asList;

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
        return getString(train, key);
    }

    private static String getTimeTabled(Map<String, Object> train) {
        Matcher m;
        String raw = getString(train, "TimeTabledDateTime");
        if (raw.equals(getString(train, "ExpectedDateTime"))) {
            return "";
        }
        for (Pattern pattern : asList(wholeMinutes, dateTime))
            if ((m = pattern.matcher(raw)).matches())
                return m.group(1);
        return raw;
    }

    private static String getExpected(Map<String, Object> train) {
        @SuppressWarnings("unchecked") Collection<Map<String, Object>>
                deviations = (Collection<Map<String, Object>>) train.get("Deviations");
        if (deviations != null && !deviations.isEmpty()) {
            Collection<Map<String, Object>> filtered = deviations
                    .stream()
                    .filter(deviation -> Integer.valueOf(deviation.get("ImportanceLevel").toString()) < 5)
                    .collect(Collectors.toList());
            if (!filtered.isEmpty()) {
                return filtered.iterator().next().get("Text").toString();
            }
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
