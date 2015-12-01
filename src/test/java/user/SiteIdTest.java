package user;

import org.junit.Test;

import static org.junit.Assert.*;

public class SiteIdTest {

    @Test
    public void extractsLastPartOfUri() {
        assertEquals("9525", SiteId.get("/9525"));
        assertEquals("Sta", SiteId.get("/Sta"));
        assertEquals("Äs", SiteId.get("/%C3%84s"));
        assertEquals("9525", SiteId.get("/sl/9525"));
        assertNull(SiteId.get("/"));
        assertNull(SiteId.get(""));
    }
}
