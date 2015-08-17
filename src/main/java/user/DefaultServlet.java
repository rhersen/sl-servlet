package user;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

import static java.util.Arrays.asList;

public class DefaultServlet extends javax.servlet.http.HttpServlet {

    private final List<String> specific = asList("LineNumber", "JourneyDirection", "Destination",
            "SecondaryDestinationName", "DisplayTime", "TimeTabledDateTime",
            "ExpectedDateTime", "StopPointNumber", "StopPointDesignation", "Deviations");

    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        HttpURLConnection conn = getConn(Key.get(), SiteId.get(request.getRequestURI()));

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();

        Deque<Map<String, Object>> trains = Parser.trains(conn.getInputStream());

        w.print("<!doctype html>");
        w.print("<meta charset=utf-8>");

        tag("title", trains.getFirst().get("StopAreaName"), w);

        for (Object value : CommonFields.get(trains).values())
            tag("span", value, w);

        if (!trains.isEmpty()) {
            w.print("<table>");
            for (Map<String, Object> train : trains) {
                w.print("<tr>");
                for (String key : specific)
                    tag("td", train.get(key), w);
                w.println("</tr>");
            }
            w.println("</table>");
        }

        conn.disconnect();

    }

    private HttpURLConnection getConn(String key, String siteId) throws IOException {
        URL url = new URL("http://api.sl.se/api2/realtimedepartures.json" +
                "?key=" + key +
                "&SiteId=" + siteId +
                "&TimeWindow=60");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

    private void tag(String tag, Object text, PrintWriter writer) {
        writer.print("<" + tag + ">");
        writer.print(text);
        writer.println("</" + tag + ">");
    }

}
