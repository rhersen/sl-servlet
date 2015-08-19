package user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class DefaultServlet extends HttpServlet {

    private final List<String> specific = asList("JourneyDirection", "Destination",
            "DisplayTime", "expecteddatetime", "timetableddatetime");

    protected void doPost(HttpServletRequest q, HttpServletResponse p)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.endsWith("css")) {
            response.setContentType("text/css");
            PrintWriter w = response.getWriter();
            w.print("body {font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', " +
                    "'Helvetica Neue', Arial, Helvetica, sans-serif;}");
            return;
        }

        HttpURLConnection conn = getConn(Key.get(), SiteId.get(uri));

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
        PrintWriter w = response.getWriter();

        Deque<Map<String, Object>> trains = Parser.trains(conn.getInputStream());

        w.print("<!doctype html>");
        w.print("<meta charset=utf-8>");

        tag("title", trains.getFirst().get("StopAreaName"), w);

        w.print("<link rel='stylesheet' type='text/css' href='css'/>");

        for (Object value : CommonFields.get(trains).values())
            tag("span", value, w);

        if (!trains.isEmpty()) {
            w.print("<table>");
            for (Map<String, Object> train : trains) {
                w.print("<tr>");
                for (String key : specific)
                    tag("td", TrainFormatter.get(train, key), w);
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
