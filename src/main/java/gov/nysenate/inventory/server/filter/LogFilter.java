package gov.nysenate.inventory.server.filter;

import gov.nysenate.inventory.dao.DbConnect;

import jakarta.servlet.Filter;
import jakarta.servlet.FilterChain;
import jakarta.servlet.FilterConfig;
import jakarta.servlet.ServletException;
import jakarta.servlet.ServletRequest;
import jakarta.servlet.ServletResponse;
import jakarta.servlet.annotation.WebFilter;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpSession;
import java.io.IOException;

import org.apache.log4j.MDC;

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
