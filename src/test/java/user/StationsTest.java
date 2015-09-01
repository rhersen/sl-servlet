package user;

import org.junit.Test;

import static org.junit.Assert.*;
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
        assertEquals("9531", north("9529"));
    }

    @Test
    public void notEvenForSouth() throws Exception {
        assertEquals("9529", south("9531"));
    }
}