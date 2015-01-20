package gov.nysenate.inventory.util;

import org.apache.log4j.Logger;

import javax.servlet.http.HttpSession;

public class HttpUtils {

    public static final int SC_SESSION_TIMEOUT = 599;
    public static final int SC_SESSION_OK = 200;

    private static final Logger log = Logger.getLogger(HttpUtils.class.getName());

    public static String getUserName(HttpSession session) {
        return (String) session.getAttribute("user");
    }

    public static String getPassword(HttpSession session) {
        return (String) session.getAttribute("pwd");
    }
}
