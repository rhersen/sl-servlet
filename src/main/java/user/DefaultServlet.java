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
import static user.Stations.*;

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
            w.print("body {" +
                            "font-family: 'HelveticaNeue-Light', 'Helvetica Neue Light', " +
                            "'Helvetica Neue', Arial, Helvetica, sans-serif;" +
                            "}" +
                            "a {font-size: 24px;}" +
                            "span.fresh {background-color: green}" +
                            "span.recent {background-color: yellow}" +
                            "span.stale {background-color: orange}" +
                            "span.dead {background-color: red}" +
                            ""
            );
            return;
        }

        ServletContext cache = request.getSession().getServletContext();

        setHeaders(response);
        PrintWriter w = response.getWriter();
        writeHeaders(w);

        String siteId = SiteId.get(uri);
        if (siteId == null) {
            writeHeader(w, "s1");
            w.print("<table>");
            for (String id : getStations()) {
                Map<String, Object> cached = readFrom(cache, id);
                w.print("<tr>");
                w.print("<td>");
                w.print("<a href='" + id + "'>");
                w.print(id);
                w.print("</a>");
                if (cached != null) {
                    w.print("<td>");
                    w.print("<a href='" + id + "'>");
                    w.print(getStopAreaName(cached));
                    w.print("</a>");
                    w.print("<td>");
                    w.print(getAge(cached).getSeconds());
                }
            }
            w.print("</table>");
            return;
        }

        Map<String, Object> found = readFrom(cache, siteId);
        refreshIfNecessary(cache, siteId, found);

        if (found == null) {
            writeHeader(w, "Inget data");
            w.print("<a href=" + siteId + ">Uppdatera</a>");
            return;
        }

        logger.info("cached");

        if (!hasTrains(found))
            w.print("<div>no trains for SiteId " + siteId + "</div>");
        else {
            writeHeader(w, getStopAreaName(found));
            for (Object value : CommonFields.get(found).values())
                tag("span", getAgeClass(found), value, w);
            w.print("<div>");
            writeLinkTo(south(siteId), cache, w);
            w.print("<a href=" + siteId + ">" + getStopAreaName(found) + "</a> ");
            writeLinkTo(north(siteId), cache, w);
            w.print("</div>");
            if (hasTrains(found))
                writeTrains(getTrains(found), w);
        }
    }

    private String getAgeClass(Map<String, Object> cached) {
        Duration age = getAge(cached);
        long seconds = age.getSeconds();
        if (seconds < 120) return "fresh";
        if (seconds > 1800) return "dead";
        if (seconds > 500) return "stale";
        return "recent";
    }

    private void refreshIfNecessary(ServletContext cache, String id, Map<String, Object> found) {
        if (found == null || isExpired(found)) {
            executor.submit(() -> {
                logger.info("in thread");
                Map<String, Object> r = DefaultServlet.this.getDataFromServer(id);
                logger.info("got data");
                cache.setAttribute(id, r);
                logger.info("wrote cache");
                return r;
            });
        }
    }

    private void writeLinkTo(String southId, ServletContext cache, PrintWriter w) {
        w.print("<a href=" + southId + ">" + getNameFor(southId, cache) + "</a> ");
    }

    private Object getNameFor(String siteId, ServletContext cache) {
        Map<String, Object> found = readFrom(cache, siteId);
        refreshIfNecessary(cache, siteId, found);
        return found != null ? getStopAreaName(found) : siteId;
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readFrom(ServletContext cache, String siteId) {
        return (Map<String, Object>) cache.getAttribute(siteId);
    }

    private boolean isExpired(Map<String, Object> found) {
        return getAge(found).compareTo(Duration.ofSeconds(60)) > 0;
    }

    private Duration getAge(Map<String, Object> found) {
        LocalDateTime latestUpdate = parse(found.get("LatestUpdate").toString());
        LocalDateTime now = now();
        return Duration.between(latestUpdate, now);
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

    private void writeHeader(PrintWriter w, Object stopAreaName) {
        tag("title", "", stopAreaName, w);
        writeCssHeader(w);
        w.print("<div><a href='/'>Hem</a></div>");
    }

    private void writeTrains(Iterable<Map<String, Object>> trains, PrintWriter w) {
        w.print("<table>");
        for (Map<String, Object> train : trains) {
            w.print("<tr>");
            for (String key : specific)
                tag("td", "", TrainFormatter.get(train, key), w);
        }
        w.println("</table>");
    }

    private void writeCssHeader(PrintWriter w) {
        w.print("<link rel='stylesheet' type='text/css' href='css'/>");
    }

    private void tag(String tag, String classes, Object text, PrintWriter writer) {
        writer.print("<" + tag + " class='" + classes + "'>");
        writer.print(text);
        writer.println("</" + tag + ">");
    }

}
