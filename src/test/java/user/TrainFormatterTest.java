package user;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TrainFormatterTest {

    @Test
    public void nullReturnsEmptyString() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "");
        assertEquals("", TrainFormatter.get(train, "Destination"));
    }

    @Test
    public void getsValueFromTrain() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:29:00");
        assertEquals("2015-08-18T17:29:00", TrainFormatter.get(train, "ExpectedDateTime"));
    }

    @Test
    public void removesDay() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:01:25");
        assertEquals("17:01:25", TrainFormatter.get(train, "expecteddatetime"));
    }

    @Test
    public void removesSecondsIfZero() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "2015-08-18T17:01:00");
        assertEquals("17:01", TrainFormatter.get(train, "expecteddatetime"));
    }

    @Test
    public void doesntCrashIfNoDateTimeDelimiter() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "17:01:25");
        assertEquals("17:01:25", TrainFormatter.get(train, "expecteddatetime"));
    }

    @Test
    public void doesntCrashIfNoExpectedDateTime() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "17:01:25");
        assertEquals("", TrainFormatter.get(train, "expecteddatetime"));
    }

    @Test
    public void showsTimeTabledIfDifferentFromExpected() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:01:25");
        assertEquals("17:00", TrainFormatter.get(train, "timetableddatetime"));
    }

    @Test
    public void showsTimeTabledAsEmptyIfSameAsExpected() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("TimeTabledDateTime", "2015-08-18T17:00:00");
        train.put("ExpectedDateTime", "2015-08-18T17:00:00");
        assertEquals("", TrainFormatter.get(train, "timetableddatetime"));
    }

    @Test
    public void showsDisplayTimeIfRelative() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("DisplayTime", "7 min");
        assertEquals("7 min", TrainFormatter.get(train, "displaytime"));
    }

    @Test
    public void showsEmptyDisplayTimeIfAbsolute() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("DisplayTime", "17:00");
        assertEquals("", TrainFormatter.get(train, "displaytime"));
    }
}