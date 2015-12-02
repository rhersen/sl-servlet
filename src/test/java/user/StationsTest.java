package user;

import org.junit.Test;

import java.util.ArrayDeque;
import java.util.Deque;

import static org.junit.Assert.assertEquals;
import static user.Stations.*;

public class StationsTest {
    @Test
    public void northIsNextId() throws Exception {
        assertEquals("9523", north("9522"));
    }

    @Test
    public void southIsPreviousId() throws Exception {
        assertEquals("9522", south("9523"));
    }

    @Test
    public void afterLast() throws Exception {
        assertEquals("9523", south("9524"));
    }

    @Test
    public void butSomeStationsAreNotInSequence() throws Exception {
        assertEquals("Spå", north("Sub"));
    }

    @Test
    public void notEvenForSouth() throws Exception {
        assertEquals("Äs", south("Åbe"));
    }

    @Test
    public void getSouthwestStationsStartsInStuvstaAndEndsInTumba() throws Exception {
        Deque<String> result = new ArrayDeque<>(getSouthwestStations());
        assertEquals("Sta", result.getFirst());
        assertEquals("Tu", result.getLast());
    }
}
