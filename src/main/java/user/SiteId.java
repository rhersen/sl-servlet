package user;

import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

class SiteId {
    private static final Pattern pattern = Pattern.compile(".*/([^/]+)");

    static String get(CharSequence requestURI) {
        Matcher matcher = pattern.matcher(requestURI);
        if (matcher.matches())
            try {
                return URLDecoder.decode(matcher.group(1), "UTF-8");
            } catch (UnsupportedEncodingException e) {
                return null;
            }
        return null;
    }
}
