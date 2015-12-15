package user;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;
import static org.junit.Assert.*;
import static user.TrainFormatter.*;

public class TrainFormatterTest {

    @Test
    public void nullReturnsEmptyString() throws Exception {
        assertEquals("", get(getTrain("ExpectedDateTime", ""), "Destination"));
    }

    @Test
    public void getsValueFromTrain() throws Exception {
        Map<String, Object> train = getTrain("ExpectedDateTime", "2015-08-18T17:29:00");
        assertEquals("2015-08-18T17:29:00", get(train, "ExpectedDateTime"));
    }

    @Test
    public void removesDay() throws Exception {
        assertEquals("17:01:25", get(getTrain("AdvertisedTimeAtLocation", "2015-08-18T17:01:25"),
                "advertisedtimeatlocation"));
    }

    @Test
    public void removesSecondsIfZero() throws Exception {
        assertEquals("17:01", get(getTrain("AdvertisedTimeAtLocation", "2015-08-18T17:01:00"),
                "advertisedtimeatlocation"));
    }

    @Test
    public void actual() throws Exception {
        assertFalse(isActual(getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:25")));
        assertTrue(isActual(getTrain("TimeAtLocation", "2015-08-18T17:01:25")));
    }

    @Test
    public void estimated() throws Exception {
        assertFalse(isEstimated(getTrain("AdvertisedTimeAtLocation", "2015-08-18T17:01:25")));
        assertTrue(isEstimated(getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:25")));
        assertFalse(isEstimated(getTrain("TimeAtLocation", "2015-08-18T17:01:25")));
        assertFalse(isEstimated(getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:25",
                "TimeAtLocation", "2015-08-18T17:01:25")));
    }

    @Test
    public void estimatedTime() throws Exception {
        assertEquals(time(getTrain("AdvertisedTimeAtLocation", "2015-08-18T17:01:00")), "17:01");
        assertEquals(time(getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:00")), "17:01");
        assertEquals(time(getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:00", "TimeAtLocation", "2015-08-18T17:01:25")), "17:01:25");
    }

    @Test
    public void doesntCrashIfNoExpectedDateTime() throws Exception {
        assertEquals("", get(getTrain("TimeTabledDateTime", "17:01:25"), "expecteddatetime"));
    }

    @Test
    public void showsTimeTabledAsEmptyIfSameAsExpected() throws Exception {
        Map<String, Object> train = getTrain(
                "TimeTabledDateTime", "2015-08-18T17:00:00",
                "ExpectedDateTime", "2015-08-18T17:00:00");
        assertEquals("", get(train, "timetableddatetime"));
    }

    @Test
    public void showsEmptyDisplayTimeIfAbsolute() throws Exception {
        assertEquals("", get(getTrain("DisplayTime", "17:00"), "displaytime"));
    }

    @Test
    public void toLocation() throws Exception {
        List<String> strings = asList("Äs", "Söc");
        assertEquals("Söc", get(getTrain("ToLocation", new ArrayDeque<>(strings)), "tolocation"));
    }

    @Test
    public void remainingShowsSeconds() throws Exception {
        Map<String, Object> train = getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:30");
        assertEquals("-90", getRemaining(train, parse("2015-08-18T17:03")));
        assertEquals("-1", getRemaining(train, parse("2015-08-18T17:01:31")));
        assertEquals("0:00", getRemaining(train, parse("2015-08-18T17:01:30")));
        assertEquals("0:30", getRemaining(train, parse("2015-08-18T17:01")));
        assertEquals("9:30", getRemaining(train, parse("2015-08-18T16:52")));
        assertEquals("9:59", getRemaining(train, parse("2015-08-18T16:51:31")));
        assertEquals("10m", getRemaining(train, parse("2015-08-18T16:51:30")));
    }

    @Test
    public void dontShowSecondsLessThanMinus99() throws Exception {
        Map<String, Object> train = getTrain("EstimatedTimeAtLocation", "2015-08-18T17:01:30");
        assertEquals("", getRemaining(train, parse("2015-08-18T17:04")));
    }

    @Test
    public void remainingDoesntCrash() throws Exception {
        Map<String, Object> train = getTrain("AdvertisedTimeAtLocation", "2015-08-18T17:01:30");
        assertEquals("0:30", getRemaining(train, parse("2015-08-18T17:01")));
    }

    private Map<String, Object> getTrain(String key, Object value) {
        Map<String, Object> train = new HashMap<>();
        train.put(key, value);
        return train;
    }

    private Map<String, Object> getTrain(String key, String value, String key1, String value1) {
        Map<String, Object> train = new HashMap<>();
        train.put(key, value);
        train.put(key1, value1);
        return train;
    }
}
