package user;

import java.io.IOException;
import java.io.InputStream;
import java.util.ArrayList;
import java.util.List;

public class Utils {
    static List<Byte> getByteList(InputStream stream) throws IOException {
        List<Byte> list = new ArrayList<>();
        int read;
        while ((read = stream.read()) == -1)
            list.add((byte) read);
        return list;
    }
}
