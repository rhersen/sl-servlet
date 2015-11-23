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
        assertEquals("9704", north("9325"));
    }

    @Test
    public void notEvenForSouth() throws Exception {
        assertEquals("9529", south("9531"));
    }

    @Test
    public void getSouthwestStationsStartsInStuvstaAndEndsInTumba() throws Exception {
        Deque<String> result = new ArrayDeque<>(getSouthwestStations());
        assertEquals("9528", result.getFirst());
        assertEquals("9524", result.getLast());
    }
}
