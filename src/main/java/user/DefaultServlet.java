package user;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletOutputStream;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.time.LocalDateTime;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.time.LocalDateTime.now;
import static java.time.LocalDateTime.parse;
import static user.JsonData.*;
import static user.Stations.*;

public class DefaultServlet extends HttpServlet {

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

        ServletContext cache = request.getSession().getServletContext();

        if (uri.endsWith("ico")) {
            writeStream("image/png", cache.getResourceAsStream("/WEB-INF/web.ico"), response);
            return;
        }

        if (uri.endsWith("css")) {
            writeStream("text/css", cache.getResourceAsStream("/WEB-INF/web.css"), response);
            return;
        }

        setHeaders(response);
        PrintWriter w = response.getWriter();
        writeHeaders(w);

        String siteId = SiteId.get(uri);
        if (siteId == null) {
            writeIndex(cache, w);
            return;
        }

        Map<String, Object> found = readFrom(cache, siteId);
        refreshIfNecessary(cache, siteId, found);

        if (found == null) {
            writeNotInCache(w, "Inget data", "<a href=" + siteId + ">Uppdatera</a>");
            return;
        }

        logger.info("cached");

        if (!hasTrains(found))
            writeNoTrains(siteId, w);
        else
            writeStation(siteId, found, cache, w);
    }

    private void writeStream(String contentType, InputStream stream, ServletResponse response)
            throws IOException {
        response.setContentType(contentType);
        int len = 1 << 11;
        byte[] b = new byte[len];
        ServletOutputStream w = response.getOutputStream();
        int read;
        while ((read = stream.read(b, 0, len)) != -1) {
            w.write(b, 0, read);
            logger.info(String.format("read %d bytes", read));
        }
        logger.info("done");
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

    private void writeIndex(ServletContext cache, PrintWriter w) {
        writeHeader(w, "s1");
        w.print("<table>");
        for (String id : getStations()) {
            Map<String, Object> cached = readFrom(cache, id);
            w.print("<tr>");
            w.print("<td>");
            w.print("<a href='" + id + "'>");
            w.print(id);
            w.print("</a>");
            w.print("<td>");
            w.print("<a href='" + id + "'>");
            if (cached != null)
                w.print(getStopAreaName(cached));
            w.print("</a>");
            w.print("<td>");
            if (cached != null)
                w.print(getAge(cached).getSeconds());
        }
        w.print("</table>");
    }

    private void writeNotInCache(PrintWriter w, String stopAreaName, String s) {
        writeHeader(w, stopAreaName);
        w.print(s);
    }

    private void writeNoTrains(String siteId, PrintWriter w) {
        w.print("<div>no trains for SiteId " + siteId + "</div>");
    }

    private void writeStation(
            String siteId,
            Map<String, Object> site,
            ServletContext cache,
            PrintWriter w) {
        writeHeader(w, getStopAreaName(site));
        for (Object value : CommonFields.get(site).values())
            tag("span", getAgeClass(site), value, w);
        w.print("<div>");
        writeLinkTo(south(siteId), cache, w);
        w.print("<a href=" + siteId + " class=" + getAgeClass(site) + ">");
        w.print(getStopAreaName(site));
        w.print("</a> ");
        writeLinkTo(north(siteId), cache, w);
        w.print("</div>");
        writeTrains(getTrains(site), isExpired(site), w);
    }

    private String getAgeClass(Map<String, Object> cached) {
        Duration age = getAge(cached);
        long seconds = age.getSeconds();
        if (seconds < 120)
            return "fresh";
        if (seconds > 1800)
            return "dead";
        if (seconds > 500)
            return "stale";
        return "recent";
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

    private void refreshIfNecessary(ServletContext cache, String id, Map<String, Object> found) {
        if (found == null || isExpired(found))
            executor.submit(() -> {
                try {
                    Map<String, Object> checkAgain = readFrom(cache, id);
                    boolean expired = checkAgain == null || isExpired(checkAgain);
                    logger.info("getting " + id + " " + expired);
                    if (expired) {
                        Map<String, Object> r = DefaultServlet.this.getDataFromServer(id);
                        logger.info("caching " + getStopAreaName(r) + r.get("LatestUpdate"));
                        cache.setAttribute(id, r);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private boolean isExpired(Map<String, Object> responseData) {
        return getAge(responseData).compareTo(Duration.ofSeconds(60)) > 0;
    }

    private Duration getAge(Map<String, Object> responseData) {
        LocalDateTime latestUpdate = parse(responseData.get("LatestUpdate").toString());
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

    private void writeTrains(Iterable<Map<String, Object>> trains, boolean expired, PrintWriter w) {
        w.print("<table>");
        for (Map<String, Object> train : trains) {
            w.print("<tr>");
            tag("td", "", TrainFormatter.get(train, "remaining"), w);
            if (!expired)
                tag("td", "", TrainFormatter.get(train, "displaytime"), w);
            tag("td", "", TrainFormatter.get(train, "Destination"), w);
            tag("td", "", TrainFormatter.get(train, "expecteddatetime"), w);
            tag("td", "", TrainFormatter.get(train, "timetableddatetime"), w);
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
