package user;

import org.junit.Test;

import java.io.ByteArrayInputStream;
import java.util.List;

import static org.junit.Assert.assertEquals;

public class UtilsTest {

    @Test
    public void testGetByteList() throws Exception {
        byte[] bytes = new byte[]{'a', 'r', 'r', 'a', 'y'};

        List<Byte> result = Utils.getByteList(new ByteArrayInputStream(bytes));

        assertEquals(5, result.size());
        for (int i = 0; i < bytes.length; i++)
            assertEquals((Byte) bytes[i], result.get(0));
    }
}