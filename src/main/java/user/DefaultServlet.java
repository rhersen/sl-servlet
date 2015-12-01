package user;

import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletResponse;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.*;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.logging.Logger;

import static java.lang.String.format;
import static user.JsonData.getStopAreaName;
import static user.Stations.getStations;
import static user.Utils.getAge;
import static user.Utils.getByteList;

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

        URL url = new URL("http://api.trafikinfo.trafikverket.se/v1/data.json");
        HttpURLConnection conn = (HttpURLConnection) url.openConnection();
        conn.setRequestMethod("POST");
        conn.setRequestProperty("Content-Type", "text/xml");
        conn.setDoOutput(true);
        OutputStreamWriter outputStreamWriter = new OutputStreamWriter(conn.getOutputStream());
        outputStreamWriter.write("<REQUEST>\n" +
                " <LOGIN authenticationkey=\"" + Key.get() + "\" />\n" +
                " <QUERY objecttype=\"TrainAnnouncement\" orderby=\"AdvertisedTimeAtLocation\">\n" +
                "  <FILTER>\n" +
                "   <AND>\n" +
                "    <EQ name=\"ActivityType\" value=\"Avgang\" />\n" +
//                "    <EQ name=\"AdvertisedTrainIdent\" value=\"" + 2769 + "\" />\n" +
                "    <EQ name=\"LocationSignature\" value=\"" + siteId + "\" />\n" +
                "    <AND>\n" +
                "     <GT name=\"AdvertisedTimeAtLocation\" value=\"$dateadd(-00:10:00)\" />\n" +
                "     <LT name=\"AdvertisedTimeAtLocation\" value=\"$dateadd(00:50:00)\" />\n" +
                "    </AND>\n" +
                "   </AND>\n" +
                "  </FILTER>\n" +
                "  <INCLUDE>LocationSignature</INCLUDE>\n" +
                "  <INCLUDE>AdvertisedTrainIdent</INCLUDE>\n" +
                "  <INCLUDE>AdvertisedTimeAtLocation</INCLUDE>\n" +
                "  <INCLUDE>EstimatedTimeAtLocation</INCLUDE>\n" +
                "  <INCLUDE>ProductInformation</INCLUDE>\n" +
                "  <INCLUDE>ToLocation</INCLUDE>\n" +
                " </QUERY>\n" +
                "</REQUEST>");
        outputStreamWriter.close();

        if (conn.getResponseCode() != 200)
            throw new RuntimeException(
                    format("Failed: HTTP error code: %d", conn.getResponseCode()));

        InputStream inputStream = conn.getInputStream();
        Map<String, Object> responseData = Parser.parse(inputStream);
        conn.disconnect();
        Iterable<Map> trainAnnouncement = (Iterable<Map>) responseData.get("TrainAnnouncement");
        w.println("<table>");
        for (Map train : trainAnnouncement) {
            w.println("<tr>");
            w.println("<td>");
            w.println(TrainFormatter.get(train, "LocationSignature"));
            w.println("<td>");
            w.println(TrainFormatter.get(train, "advertisedtimeatlocation"));
            w.println("<td>");
            w.println(TrainFormatter.get(train, "estimatedtimeatlocation"));
            w.println("<td>");
            w.println(TrainFormatter.get(train, "ToLocation"));
            w.println("<td>");
            w.println(TrainFormatter.get(train, "AdvertisedTrainIdent"));
            w.println("<td>");
            w.println(TrainFormatter.get(train, "ProductInformation"));
        }
        w.println("</table>");
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

    private void writeHeader(PrintWriter w, Object stopAreaName) {
        tag("title", "", stopAreaName, w);
        w.print(format("<style>%s</style>", css));
        w.print("<div><a href='/'>Hem</a></div>");
    }

    @SuppressWarnings("unchecked")
    private Map<String, Object> readFrom(ServletContext cache, String siteId) {
        return (Map<String, Object>) cache.getAttribute(siteId);
    }

    private void tag(String tag, String classes, Object text, PrintWriter writer) {
        writer.print(format("<%s class='%s'>", tag, classes));
        writer.print(text);
        writer.println(format("</%s>", tag));
    }
}
