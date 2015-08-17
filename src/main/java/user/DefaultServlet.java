package user;

import java.io.IOException;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class DefaultServlet extends javax.servlet.http.HttpServlet {
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
        PrintWriter writer = response.getWriter();

        Collection<Map<String, Object>> trains = Parser.trains(new InputStreamReader(conn.getInputStream(), "UTF-8"));

        writer.print("<!doctype html>");
        writer.print("<meta charset=utf-8>");
        writer.print("<title>");
        writer.print(trains.iterator().next().get("StopAreaName"));
        writer.println("</title>");

        Map<String, Object> commonFields = CommonFields.get(trains);
        for (Object e : commonFields.values()) {
            writer.print("<span>");
            writer.print(e);
            writer.println("</span>");
        }

        List<String> specificFields = asList("LineNumber", "JourneyDirection", "Destination",
                "SecondaryDestinationName", "DisplayTime", "TimeTabledDateTime",
                "ExpectedDateTime", "StopPointNumber", "StopPointDesignation", "Deviations");

        if (!trains.isEmpty()) {
            specificFields.removeAll(commonFields.keySet());
            writer.print("<table>");

            for (Map<String, Object> train : trains) {
                writer.print("<tr>");

                for (String specificField : specificFields) {
                    writer.print("<td>");
                    writer.print(train.get(specificField));
                    writer.println("</td>");
                }

                writer.println("</tr>");
            }
            writer.println("</table>");
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

}
