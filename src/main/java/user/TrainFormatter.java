package user;

import java.util.Map;

public class TrainFormatter {
    public static String get(Map<String, Object> train, String key) {
        Object o = train.get(key);
        if (o != null) {
            return o.toString();
        }
        return "";
    }
}
