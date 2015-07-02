package gov.nysenate.inventory.server.filter;

import gov.nysenate.inventory.dao.DbConnect;
import org.apache.log4j.MDC;

import javax.servlet.*;
import javax.servlet.annotation.WebFilter;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;
import java.io.IOException;

/**
 * Adds username, server, and ipaddress info to all log
 * statements in this thread.
 */
@WebFilter(urlPatterns = { "/*" })
public class LogFilter implements Filter {

    @Override
    public void init(FilterConfig filterConfig) throws ServletException {

    }

    @Override
    public void doFilter(ServletRequest request, ServletResponse response, FilterChain chain) throws IOException, ServletException {
        HttpServletRequest req = (HttpServletRequest) request;

        putUserName(req);
        putServerName();
        putClientIpAddress(req);

        try {
            chain.doFilter(request, response);
        } finally {
            MDC.remove("userName");
            MDC.remove("server");
            MDC.remove("ipAddress");
        }
    }

    private void putUserName(HttpServletRequest req) {
        HttpSession session = req.getSession(false);
        String username = null;
        if (session != null) {
            username = (String) session.getAttribute("user");
        }
        if (username != null) {
            MDC.put("userName", username);
        }
    }

    private void putServerName() {
        String server = new DbConnect().getDatabaseName();
        String[] array = server.split(":");
        MDC.put("server", array[array.length - 1]);
    }

    private void putClientIpAddress(HttpServletRequest req) {
        String ipAddress = req.getRemoteAddr();
        MDC.put("ipAddress", ipAddress);
    }

    @Override
    public void destroy() {

    }
}
