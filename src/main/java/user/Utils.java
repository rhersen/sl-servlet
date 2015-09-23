package user;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.Duration.between;
import static java.time.LocalDateTime.*;

public class Utils {
    static List<Byte> getByteList(InputStream stream) throws IOException {
        List<Byte> list = new ArrayList<>();
        int read;
        while ((read = stream.read()) != -1)
            list.add((byte) read);
        return list;
    }

    static Duration getAge(Map<String, Object> responseData) {
        LocalDateTime latestUpdate = parse(responseData.get("LatestUpdate").toString());
        return between(latestUpdate, now());
    }
}
