package user;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.time.Duration;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import static org.junit.Assert.*;
import static user.Utils.*;

public class UtilsTest {

    @Test
    public void testGetByteList() throws Exception {
        byte[] bytes = new byte[]{'a', 'r', 'r', 'a', 'y'};

        List<Byte> result = getByteList(new ByteArrayInputStream(bytes));

        assertEquals(5, result.size());
        for (int i = 0; i < bytes.length; i++)
            assertEquals((Byte) bytes[i], result.get(i));
    }

    @Test
    public void testGetAge() throws Exception {
        Map<String, Object> responseData = new LinkedHashMap<>();
        responseData.put("LatestUpdate", "2015-08-19T08:57:27");

        Duration result = getAge(responseData);

        assertFalse(result.isNegative());
    }
}