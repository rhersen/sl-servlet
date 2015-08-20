package user;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.Collection;
import java.util.Deque;
import java.util.List;
import java.util.Map;

import static java.util.Arrays.asList;

public class DefaultServlet extends HttpServlet {

    private final List<String> specific = asList("displaytime", "Destination",
            "expecteddatetime", "timetableddatetime");

    protected void doPost(HttpServletRequest q, HttpServletResponse p)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        if (uri.endsWith("ico")) {
            return;
        }

        if (uri.endsWith("css")) {
            response.setContentType("text/css");
            PrintWriter w = response.getWriter();
            w.print("body {font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', " +
                    "'Helvetica Neue', Arial, Helvetica, sans-serif;}");
            return;
        }

        PrintWriter w = response.getWriter();

        setHeaders(response);
        writeHeaders(w);

        String siteId = SiteId.get(uri);
        if (siteId == null) {
            writeCssHeader(w);
            for (Integer id : asList(9325, 9510, 9000, 9530, 9531, 9529, 9528, 9527, 9526, 9525, 9524))
                w.print("<div><a href='" + id + "'>" + id + "</a></div>");
            return;
        }

        HttpURLConnection conn = getConn(Key.get(), siteId);

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        Map<String, Object> responseData = Parser.parse(conn.getInputStream());
        conn.disconnect();

        @SuppressWarnings("unchecked") Deque<Map<String, Object>>
                trains = (Deque<Map<String, Object>>) responseData.get("Trains");

        if (trains.isEmpty())
            w.print("<div>no trains for SiteId " + siteId + "</div>");
        else
            writeTrains(trains, CommonFields.get(responseData).values(), w);
    }

    private void setHeaders(HttpServletResponse response) {
        response.setContentType("text/html");
        response.setCharacterEncoding("UTF-8");
    }

    private void writeHeaders(PrintWriter w) {
        w.print("<!doctype html>");
        w.print("<meta content=\"true\" name=\"HandheldFriendly\">");
        w.print("<meta");
        w.print(" content=\"width=device-width, height=device-height, user-scalable=no\"");
        w.print(" name=\"viewport\"");
        w.print(">");
        w.print("<meta charset=utf-8>");
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

    private void writeTrains(Deque<Map<String, Object>> trains, Collection<Object> commonFields,
                             PrintWriter w) {
        tag("title", trains.getFirst().get("StopAreaName"), w);

        writeCssHeader(w);

        for (Object value : commonFields)
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
    }

    private void writeCssHeader(PrintWriter w) {
        w.print("<link rel='stylesheet' type='text/css' href='css'/>");
    }

    private void tag(String tag, Object text, PrintWriter writer) {
        writer.print("<" + tag + ">");
        writer.print(text);
        writer.println("</" + tag + ">");
    }

}
