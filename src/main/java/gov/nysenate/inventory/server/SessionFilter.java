package gov.nysenate.inventory.server;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

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

    public void doFilter(ServletRequest req, ServletResponse res, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;

        boolean sessionRequired = doesPathRequireSession(request.getRequestURI());

        if (sessionRequired) {
            HttpSession session = request.getSession(false);
            if (session == null) {
                PrintWriter out = response.getWriter();
                response.setStatus(HttpUtils.SC_SESSION_TIMEOUT); // TODO: convert to this instead of "Session timed out" string.
                log.info("Invalid or expired session.");
                return;
            }
        }

        chain.doFilter(req, res);
    }

    private boolean doesPathRequireSession(String path) {
        return path.equals("/InventoryWebApp/") || path.startsWith("/InventoryWebApp/Login") ||
               path.equals("/InventoryWebApp/CheckAppVersion") || path.equals("/InventoryWebApp/DownloadServlet") ?
            false : true;
    }

    public void destroy() {
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}