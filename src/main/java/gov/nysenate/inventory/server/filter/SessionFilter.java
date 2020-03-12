package gov.nysenate.inventory.server.filter;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author Brian Heitner
 * 
 * SessionFilter looks for a Session and returns "Session timed out" if one is not found before
 * any request to any servlet other than the Servlets that are marked with allowRequest = true.
 * 
 */
@WebFilter(urlPatterns = {"/*"})
public class SessionFilter implements Filter {

    private static final Logger log = Logger.getLogger(SessionFilter.class.getName());
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        boolean sessionRequired = doesPathRequireSession(request.getRequestURI());

        Map serverMessage = new HashMap();

        if (sessionRequired) {
            HttpSession session = request.getSession(false);

            if (session == null ) {
                PrintWriter out = response.getWriter();
                serverMessage.put("Error", "Session timed out");
                out.println(gson.toJson(serverMessage));
                response.setStatus(HttpUtils.SC_SESSION_TIMEOUT);
                log.info("Invalid or expired session.");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean doesPathRequireSession(String path) {
        if (path == null || path.isEmpty()){
            return false;
        }

        // Used for local server testing when InventoryWebApp is not part of the URL

        return ((path.equals("/") || path.startsWith("/Login") ||
                path.equals("/CheckAppVersion") || path.equals("/DownloadServlet"))) ?
                false : true;


        // Real path filter for real server.. Uncomment below when moving

    }

    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}