package user;

import com.fasterxml.jackson.core.JsonFactory;
import com.fasterxml.jackson.core.JsonParser;
import com.fasterxml.jackson.core.JsonToken;

import java.io.IOException;
import java.io.InputStream;
import java.util.*;

import static com.fasterxml.jackson.core.JsonToken.*;

public class Parser {
    public static Map<String, Object> parse(InputStream in) throws IOException {
        JsonParser p = new JsonFactory().createParser(in);
        skipUntilTrains(p);
        //noinspection unchecked
        return (Map<String, Object>) getNextTokenValue(p, p.nextToken());
    }

    private static void skipUntilTrains(JsonParser p) throws IOException {
        JsonToken t = VALUE_NULL;
        while (t != null && !isFieldNameTrains(t, p.getCurrentName()))
            t = p.nextToken();
        p.nextToken();
    }

    private static boolean isFieldNameTrains(JsonToken t, String currentName) throws IOException {
        return t == FIELD_NAME && currentName.equals("RESULT");
    }

    private static Object getNextTokenValue(JsonParser p, JsonToken t) throws IOException {
        if (t == VALUE_NUMBER_INT)
            return p.getIntValue();
        else if (t == START_ARRAY)
            return getArrayValue(p);
        else if (t == START_OBJECT)
            return getObjectValue(p);
        return p.getValueAsString();
    }

    private static Map<String, Object> getObjectValue(JsonParser p) throws IOException {
        JsonToken t;
        Map<String, Object> r = new HashMap<>();
        while ((t = p.nextToken()) != END_OBJECT)
            r.put(p.getCurrentName(), getNextTokenValue(p, t));
        return r;
    }

    private static Deque<Object> getArrayValue(JsonParser p) throws IOException {
        JsonToken t;
        Deque<Object> r = new ArrayDeque<>();
        while ((t = p.nextToken()) != END_ARRAY)
            r.add(getNextTokenValue(p, t));
        return r;
    }

}
