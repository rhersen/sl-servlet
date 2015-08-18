package user;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class TrainFormatter {

    private static final Pattern wholeMinutes = Pattern.compile(".+T(.+):00");
    private static final Pattern dateTime = Pattern.compile(".+T(.+)");

    public static String get(Map<String, Object> train, String key) {
        if (key.equals("expecteddatetime"))
            return getExpected(train);
        if (key.equals("timetableddatetime"))
            return getTimeTabled(train);
        return getString(train, key);
    }

    private static String getTimeTabled(Map<String, Object> train) {
        Matcher m;
        String timeTabled = getString(train, "TimeTabledDateTime");
        if (timeTabled.equals(getString(train, "ExpectedDateTime"))) {
            return "";
        }
        for (Pattern pattern : asList(wholeMinutes, dateTime))
            if ((m = pattern.matcher(timeTabled)).matches())
                return m.group(1);
        return timeTabled;
    }

    private static String getExpected(Map<String, Object> train) {
        Matcher m;
        String expected = getString(train, "ExpectedDateTime");
        for (Pattern pattern : asList(wholeMinutes, dateTime))
            if ((m = pattern.matcher(expected)).matches())
                return m.group(1);
        return expected;
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }
}
