package user;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Map;

public class Hello extends javax.servlet.http.HttpServlet {
    protected void doPost(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {

    }

    protected void doGet(javax.servlet.http.HttpServletRequest request, javax.servlet.http
            .HttpServletResponse response) throws javax.servlet.ServletException, IOException {
        HttpURLConnection conn = getConn();

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        response.setContentType("text/html");
        PrintWriter writer = response.getWriter();

        writer.print("<table>");
        for (Map<String, Object> train : Parser.trains(conn.getInputStream())) {
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

    private HttpURLConnection getConn() throws IOException {
        URL url = new URL("http://api.sl.se/api2/realtimedepartures.json" +
                "?key=" + Key.get() +
                "&SiteId=9525" +
                "&TimeWindow=60");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("GET");
        conn.setRequestProperty("Accept", "application/json");
        return conn;
    }

}
