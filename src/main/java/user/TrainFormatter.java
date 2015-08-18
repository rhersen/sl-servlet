package user;

import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import static java.util.Arrays.asList;

public class TrainFormatter {

    private static final Pattern wholeMinutes = Pattern.compile(".+T(.+):00");
    private static final Pattern dateTime = Pattern.compile(".+T(.+)");

    public static String get(Map<String, Object> train, String key) {
        Matcher m;
        if (key.equals("expecteddatetime")) {
            String expectedDateTime = getString(train, "ExpectedDateTime");
            for (Pattern pattern : asList(wholeMinutes, dateTime))
                if ((m = pattern.matcher(expectedDateTime)).matches())
                    return m.group(1);
            return expectedDateTime;
        }
        return getString(train, key);
    }

    private static String getString(Map<String, Object> map, String key) {
        Object value = map.get(key);
        return value == null ? "" : value.toString();
    }
}
