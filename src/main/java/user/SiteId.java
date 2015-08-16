package user;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

public class SiteId {

    private static final Pattern pattern = Pattern.compile(".*/([^/]*)");

    static String get(String requestURI) {
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.matches())
            return matcher.group(1);
        return requestURI;
    }
}
