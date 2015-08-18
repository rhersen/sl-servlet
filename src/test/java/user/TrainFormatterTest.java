package user;

import org.junit.Test;

import java.util.HashMap;
import java.util.Map;

import static org.junit.Assert.*;

public class TrainFormatterTest {

    @Test
    public void getsValueFromTrain() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("Destination", "Tumba");
        assertEquals("Tumba", TrainFormatter.get(train, "Destination"));
    }

    @Test
    public void nullReturnsEmptyString() throws Exception {
        Map<String, Object> train = new HashMap<>();
        train.put("ExpectedDateTime", "Tumba");
        assertEquals("", TrainFormatter.get(train, "Destination"));
    }
}