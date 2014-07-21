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
 */
@WebFilter(urlPatterns = { "/*" })
public class SessionFilter implements Filter {

    private static final Logger log = Logger.getLogger(SessionFilter.class.getName());

    public void doFilter(ServletRequest req, ServletResponse res,
            FilterChain chain) throws IOException, ServletException {
 
        HttpServletRequest request = (HttpServletRequest) req;
        HttpServletResponse response = (HttpServletResponse) res;
        String url = request.getServletPath();
        boolean allowedRequest = false;
        System.out.println("(SessionFilter)");
        log.info("(SessionFilter)");
        
        PrintWriter out = response.getWriter();
        String path = request.getRequestURI();
        System.out.println("(SessionFilter) Path:"+path);
        log.info("(SessionFilter) Path:"+path);
        if(path.equals("/InventoryWebApp/")|| path.startsWith("/InventoryWebApp/Login")) {
            allowedRequest = true;
        }
             
        if (!allowedRequest) {
            HttpSession session = request.getSession(false);
            if (null == session) {
                  System.out.println("(SessionFilter)Session timed out");
                  log.info("(SessionFilter)Session timed out");
                  out.println("Session timed out");
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