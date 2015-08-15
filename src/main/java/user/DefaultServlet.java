package user;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class DefaultServlet extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        String requestURI = request.getRequestURI();
        int i = requestURI.lastIndexOf('/');
        HttpURLConnection conn = getConn(Key.get(), requestURI.substring(i + 1));

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        Collection<Map<String, Object>> trains = Parser.trains(conn.getInputStream());

        getCommonFields(trains)
                .entrySet()
                .stream()
                .filter(e -> e.getValue() != Boolean.FALSE)
                .forEach(e -> {
                    writer.print("<div>");
                    writer.print(e);
                    writer.println("</div>");
                });

        writer.print("<table>");
        for (Map<String, Object> train : trains) {
            writer.print("<tr>");

            writer.print("<td>");
            writer.print(train.get("ExpectedDateTime"));
            writer.println("</td>");

            writer.print("<td>");
            writer.print(train.get("Destination"));
            writer.println("</td>");

            writer.print("<td>");
            writer.print(train.get("DisplayTime"));
            writer.println("</td>");

            writer.println("</tr>");
        }
        writer.println("</table>");

        conn.disconnect();

    }

    private HashMap<String, Object> getCommonFields(Collection<Map<String, Object>> trains) {
        HashMap<String, Object> r = new HashMap<>();
        for (Map<String, Object> train : trains) {
            Set<Map.Entry<String, Object>> entries = train.entrySet();
            for (Map.Entry<String, Object> entry : entries) {
                String key = entry.getKey();
                Object o = r.get(key);
                if (o == null) {
                    r.put(key, entry.getValue());
                } else if (!o.equals(entry.getValue())) {
                    r.put(key, false);
                }
            }
        }
        return r;
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
