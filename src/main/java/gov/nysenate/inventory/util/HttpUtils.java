package gov.nysenate.inventory.util;

import gov.nysenate.inventory.dao.DbConnect;
import org.apache.log4j.Logger;

import java.io.PrintWriter;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpUtils {

    public static final int SC_SESSION_TIMEOUT = 599;
    public static final int SC_SESSION_OK = 200;

    private static final Logger log = Logger.getLogger(HttpUtils.class.getName());
    
//    public static DbConnect getHttpSession(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
//        return getHttpSession(request, response, out, SC_SESSION_TIMEOUT);
//    }
    
//    public static DbConnect getHttpSession(HttpServletRequest request, HttpServletResponse response, PrintWriter out, int noSessionStatus) {
//        HttpSession httpSession = request.getSession(false);
//        DbConnect db;
//        if (httpSession == null) {
//            System.out.println("****SESSION NOT FOUND");
//            db = new DbConnect(request);
//            log.info("Session not found/timed out.");
//            out.println("Session timed out");
//            response.setStatus(noSessionStatus);
//        } else {
//            String user = (String) httpSession.getAttribute("user");
//            String pwd = (String) httpSession.getAttribute("pwd");
//            db = new DbConnect(request, user, pwd);
//        }
//        return db;
//    }

    public static String getUserName(HttpSession session) {
        return (String) session.getAttribute("user");
    }

    public static String getPassword(HttpSession session) {
        return (String) session.getAttribute("pwd");
    }
}
