package user;

import org.junit.Test;

import static org.junit.Assert.*;
import static user.Stations.*;

public class StationsTest {
    @Test
    public void containsAlvsjo() throws Exception {
        assertTrue(getStations().contains("9529"));
    }

    @Test
    public void south() throws Exception {
        assertEquals("9528", Stations.south("9529"));
    }

    @Test
    public void north() throws Exception {
        assertEquals("9529", Stations.north("9528"));
    }
}