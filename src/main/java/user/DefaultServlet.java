package user;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static java.util.Arrays.asList;
import static user.JsonData.*;

public class DefaultServlet extends HttpServlet {

    private final List<String> specific = asList("displaytime", "Destination",
            "expecteddatetime", "timetableddatetime");
    private Logger logger;
    private ExecutorService executor;

    public void init() throws ServletException {
        super.init();
        logger = Logger.getAnonymousLogger();
        executor = Executors.newSingleThreadExecutor();
    }

    @Override
    public void destroy() {
        super.destroy();
        executor.shutdown();
    }

    protected void doPost(HttpServletRequest q, HttpServletResponse p)
            throws ServletException, IOException {
    }

    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        String uri = request.getRequestURI();

        logger.info(uri);

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

        ServletContext cache = request.getSession().getServletContext();

        setHeaders(response);
        PrintWriter w = response.getWriter();
        writeHeaders(w);

        String siteId = SiteId.get(uri);
        if (siteId == null) {
            writeCssHeader(w);
            w.print("<title>");
            w.print("s1");
            w.print("</title>");
            w.print("<table>");
            for (Integer id : asList(9325, 9510, 9000, 9530, 9531, 9529, 9528, 9527, 9526, 9525, 9524)) {
                Map<String, Object> cached = readFrom(cache, id.toString());
                w.print("<tr>");
                w.print("<td>");
                w.print("<a href='" + id + "'>");
                w.print(id);
                w.print("</a>");
                w.print("</td>");
                w.print("<td>");
                w.print("<a href='" + id + "'>");
                w.print(cached == null ? "-" : getStopAreaName(cached));
                w.print("</a>");
                w.print("</td>");
                w.print("</tr>");
            }
            w.print("</table>");
            return;
        }

        Map<String, Object> found = readFrom(cache, siteId);

        if (found == null || isExpired(found)) {
            executor.submit(() -> {
                logger.info("in thread");
                Map<String, Object> r = DefaultServlet.this.getDataFromServer(siteId);
                logger.info("got data");
                cache.setAttribute(siteId, r);
                logger.info("wrote cache");
                return r;
            });
        }

        if (found == null) {
            w.print("<a href=" + siteId + ">Uppdatera</a>");
            return;
        }

        logger.info("cached");

        if (!hasTrains(found))
            w.print("<div>no trains for SiteId " + siteId + "</div>");
        else {
            writeHeaders(CommonFields.get(found).values(), w, getStopAreaName(found));
            w.print("<div><a href='/'>Tillbaka</a></div>");
            w.print("<a href=" + siteId + ">" + getStopAreaName(found) + "</a>");
            if (hasTrains(found))
                writeTrains(getTrains(found), w);
        }
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readFrom(ServletContext cache, String siteId) {
        return (Map<String, Object>) cache.getAttribute(siteId);
    }

    private boolean isExpired(Map<String, Object> found) {
        LocalDateTime latestUpdate = parse(found.get("LatestUpdate").toString());
        LocalDateTime now = now();
        Duration age = Duration.between(latestUpdate, now);
        return age.compareTo(Duration.ofMinutes(1)) > 0;
    }

    private Map<String, Object> getDataFromServer(String siteId) throws IOException {
        HttpURLConnection conn = getConn(Key.get(), siteId);

        if (conn.getResponseCode() != 200)
            throw new RuntimeException("Failed : HTTP error code : " + conn.getResponseCode());

        Map<String, Object> responseData = Parser.parse(conn.getInputStream());
        conn.disconnect();
        return responseData;
    }

    private void setHeaders(ServletResponse response) {
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

    private void writeHeaders(Iterable<Object> commonFields, PrintWriter w, Object stopAreaName) {
        tag("title", stopAreaName, w);

        writeCssHeader(w);

        for (Object value : commonFields)
            tag("span", value, w);
    }

    private void writeTrains(Iterable<Map<String, Object>> trains, PrintWriter w) {
        w.print("<table>");
        for (Map<String, Object> train : trains) {
            w.print("<tr>");
            for (String key : specific)
                tag("td", TrainFormatter.get(train, key), w);
            w.println("</tr>");
        }
        w.println("</table>");
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
