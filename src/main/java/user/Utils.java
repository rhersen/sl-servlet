package user;

import java.io.IOException;
import java.io.InputStream;
import java.time.Duration;
import java.time.LocalDateTime;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import static java.time.Duration.between;
import static java.time.LocalDateTime.*;
import static java.time.LocalTime.NOON;

class Utils {
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

    static String getAgeClass(Map<String, Object> cached) {
        Duration age = getAge(cached);
        long seconds = age.getSeconds();
        if (seconds < 120)
            return "fresh";
        if (seconds > 1800)
            return "dead";
        if (seconds > 500)
            return "stale";
        return "recent";
    }

    static boolean isExpired(Map<String, Object> responseData) {
        return responseData == null || getAge(responseData).compareTo(Duration.ofSeconds(60)) > 0;
    }

    static String getDirectionRegex(LocalTime now) {
        return now.isBefore(NOON) ? "[02468]$" : "[13579]$";
    }
}
