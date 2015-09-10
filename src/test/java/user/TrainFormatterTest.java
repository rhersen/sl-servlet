package user;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.Map;

import static java.time.LocalDateTime.parse;
import static java.util.Collections.singletonList;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotEquals;
import static user.TrainFormatter.*;

public class TrainFormatterTest {

    @Test
    public void nullReturnsEmptyString() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "");
        assertEquals("", get(train, "Destination"));
    }

    @Test
    public void getsValueFromTrain() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:29:00");
        assertEquals("2015-08-18T17:29:00", get(train, "ExpectedDateTime"));
    }

    @Test
    public void removesDay() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:01:25");
        assertEquals("17:01:25", get(train, "expecteddatetime"));
    }

    @Test
    public void removesSecondsIfZero() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:01:00");
        assertEquals("17:01", get(train, "expecteddatetime"));
    }

    @Test
    public void showsDeviation() throws Exception {
        Map<String, Object> deviation = new HashMap<>();
        deviation.put("Text", "Inställd");
        deviation.put("Consequence", "CANCELLED");
        deviation.put("ImportanceLevel", 0);
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:00:00");
        train.put("Deviations", new ArrayDeque<>(singletonList(deviation)));
        assertEquals("Inställd", get(train, "expecteddatetime"));
    }

    @Test
    public void butNotForImportanceLevel5() throws Exception {
        Map<String, Object> deviation = new HashMap<>();
        deviation.put("Text", "Resa förbi Arlanda C kräver både UL- och SL- biljett.");
        deviation.put("Consequence", "INFORMATION");
        deviation.put("ImportanceLevel", 5);
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:00:00");
        train.put("Deviations", new ArrayDeque<>(singletonList(deviation)));
        assertEquals("17:00", get(train, "expecteddatetime"));
    }

    @Test
    public void doesntCrashIfNoDateTimeDelimiter() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "17:01:25");
        assertEquals("17:01:25", get(train, "expecteddatetime"));
    }

    @Test
    public void doesntCrashIfNoExpectedDateTime() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "17:01:25");
        assertEquals("", get(train, "expecteddatetime"));
    }

    @Test
    public void showsTimeTabledIfDifferentFromExpected() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:10:00");
        train.put("ExpectedDateTime", "2015-08-18T17:11:25");
        assertEquals("10", get(train, "timetableddatetime"));
    }

    @Test
    public void showsTimeTabledAsEmptyIfSameAsExpected() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:00:00");
        assertEquals("", get(train, "timetableddatetime"));
    }

    @Test
    public void showsDisplayTimeIfRelative() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("DisplayTime", "7 min");
        assertEquals("7 min", get(train, "displaytime"));
    }

    @Test
    public void showsEmptyDisplayTimeIfAbsolute() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("DisplayTime", "17:00");
        assertEquals("", get(train, "displaytime"));
    }

    @Test
    public void remaining() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:01:25");
        assertNotEquals("", get(train, "remaining"));
    }

    @Test
    public void remainingShowsSeconds() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:01:30");
        assertEquals("-30", getRemaining(train, parse("2015-08-18T17:02")));
        assertEquals("-1", getRemaining(train, parse("2015-08-18T17:01:31")));
        assertEquals("0:00", getRemaining(train, parse("2015-08-18T17:01:30")));
        assertEquals("0:30", getRemaining(train, parse("2015-08-18T17:01")));
        assertEquals("9:30", getRemaining(train, parse("2015-08-18T16:52")));
        assertEquals("9:59", getRemaining(train, parse("2015-08-18T16:51:31")));
        assertEquals("10m", getRemaining(train, parse("2015-08-18T16:51:30")));
    }
}