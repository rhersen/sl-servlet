package user;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.net.HttpURLConnection;
import java.net.URL;
import java.time.Duration;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;
import static user.JsonData.*;
import static user.Stations.*;
import static user.Utils.*;

public class DefaultServlet extends HttpServlet {

    private Logger logger;
    private ExecutorService executor;
    private byte[] favicon;
    private String css;

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
            response.setContentType("image/png");
            if (favicon == null)
                favicon = getByteArray(cache.getResourceAsStream("/WEB-INF/web.ico"));
            response.getOutputStream().write(favicon);
            return;
        }

        if (css == null)
            css = getCssString(cache.getResourceAsStream("/WEB-INF/web.css"));

        if (uri.endsWith("css")) {
            response.setContentType("text/css");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(css);
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

        Map<String, Object> found = getFrom(cache, siteId);

        if (found == null) {
            writeNotInCache(w, "Inget data", format("<a href=%s>Uppdatera</a>", siteId));
            return;
        }

        logger.info("cached");

        if (!hasTrains(found))
            writeNoTrains(siteId, w);
        else
            writeStation(siteId, found, cache, w);
    }

    private byte[] getByteArray(InputStream stream) throws IOException {
        List<Byte> list = getByteList(stream);
        byte[] array = new byte[list.size()];
        for (int i = 0; i < array.length; i++)
            array[i] = list.get(i);
        return array;
    }

    private String getCssString(InputStream stream) throws IOException {
        InputStreamReader reader = new InputStreamReader(stream, "UTF-8");
        StringBuilder list = new StringBuilder();
        int read;
        while ((read = reader.read()) != -1)
            list.append((char) read);
        return list.toString();
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

    private void writeIndex(ServletContext cache, PrintWriter w) {
        writeHeader(w, "s1");
        w.print("<table>");
        for (String id : getStations()) {
            Map<String, Object> cached = readFrom(cache, id);
            w.print("<tr>");
            w.print("<td>");
            w.print(format("<a href='%s'>", id));
            w.print(id);
            w.print("</a>");
            w.print("<td>");
            w.print(format("<a href='%s'>", id));
            if (cached != null)
                w.print(getStopAreaName(cached));
            w.print("</a>");
            w.print("<td>");
            if (cached != null)
                w.print(getAge(cached).getSeconds());
        }
        w.print("</table>");
    }

    private Map<String, Object> getFrom(ServletContext cache, String siteId) {
        Map<String, Object> found = readFrom(cache, siteId);
        refreshIfNecessary(cache, siteId, found);
        return found;
    }

    private void writeNotInCache(PrintWriter w, String stopAreaName, String s) {
        writeHeader(w, stopAreaName);
        w.print(s);
    }

    private void writeNoTrains(String siteId, PrintWriter w) {
        w.print(format("<div>no trains for SiteId %s</div>", siteId));
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
        w.print(format("<a href=%s class=%s>", siteId, getAgeClass(site)));
        w.print(getStopAreaName(site));
        w.print("</a> ");
        writeLinkTo(north(siteId), cache, w);
        w.print("</div>");
        writeTrains(getTrains(site), isExpired(site), w);
    }

    private void writeHeader(PrintWriter w, Object stopAreaName) {
        tag("title", "", stopAreaName, w);
        w.print(format("<style>%s</style>", css));
        w.print("<div><a href='/'>Hem</a></div>");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readFrom(ServletContext cache, String siteId) {
        return (Map<String, Object>) cache.getAttribute(siteId);
    }

    private void refreshIfNecessary(ServletContext cache, String id, Map<String, Object> found) {
        if (isExpired(found))
            executor.submit(() -> {
                try {
                    Map<String, Object> checkAgain = readFrom(cache, id);
                    boolean expired = checkAgain == null || isExpired(checkAgain);
                    if (expired) {
                        URL url = new URL(format(
                                "http://api.sl.se/api2" +
                                        "/realtimedepartures.json" +
                                        "?key=%s&SiteId=%s&TimeWindow=60",
                                Key.get(), id));
                        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
                        conn.setRequestMethod("GET");
                        conn.setRequestProperty("Accept", "application/json");

                        if (conn.getResponseCode() != 200)
                            throw new RuntimeException(
                                    format("Failed: HTTP error code: %d", conn.getResponseCode()));

                        Map<String, Object> responseData = Parser.parse(conn.getInputStream());
                        conn.disconnect();
                        logger.info("caching " + getStopAreaName(responseData));
                        cache.setAttribute(id, responseData);
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                }
            });
    }

    private void writeLinkTo(String southId, ServletContext cache, PrintWriter w) {
        Map<String, Object> found = getFrom(cache, southId);
        Object name = found != null ? getStopAreaName(found) : southId;
        w.print(format("<a href=%s>%s</a> ", southId, name));
    }

    private void tag(String tag, String classes, Object text, PrintWriter writer) {
        writer.print(format("<%s class='%s'>", tag, classes));
        writer.print(text);
        writer.println(format("</%s>", tag));
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

    private boolean isExpired(Map<String, Object> responseData) {
        return responseData == null || getAge(responseData).compareTo(Duration.ofSeconds(60)) > 0;
    }
}
