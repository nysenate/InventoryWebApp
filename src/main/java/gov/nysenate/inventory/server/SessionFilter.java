/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import gov.nysenate.inventory.util.HttpUtils;
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
import org.apache.log4j.Logger;
import org.apache.log4j.MDC;

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

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {

        log.debug("(SessionFilter start) testing");
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String url = request.getServletPath();
        boolean allowedRequest = false;
        log.debug("(SessionFilter)");

        String path = request.getRequestURI();
        log.debug("(SessionFilter) Path:" + path);

        /*
         * Servlets/URLs where there should be no Sessions will set "allowRequest = true" so this filter will
         * not attempt to circumvent the request if no Session is found.
         * 
         * In the future, a property may be useful in determining the paths that should be allowed through
         * without checking for a session but for the near future, the following four servlets should be the only
         * ones.
         * 
         */

        if (path.equals("/InventoryWebApp/") || path.startsWith("/InventoryWebApp/Login") || path.equals("/InventoryWebApp/CheckAppVersion") || path.equals("/InventoryWebApp/DownloadServlet")) {
            allowedRequest = true;
        }

        if (!allowedRequest) {
            HttpSession session = request.getSession(false);
            if (null == session) {
                log.debug("(SessionFilter) Right before opening output stream for Session time out. "+path);
                PrintWriter out = response.getWriter();
                log.debug("(SessionFilter)Session timed out");
                out.println("Session timed out");
                try {
                    out.flush();
                    out.close();
                } catch (Exception e) {
                    log.error(null, e);
                }
                return;
            }
        }
        
        chain.doFilter(req, res);
    }

    public void destroy() {        
        /*
         * called before the Filter instance is removed from service by the web
         * container
         */
    }

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {
    }
}