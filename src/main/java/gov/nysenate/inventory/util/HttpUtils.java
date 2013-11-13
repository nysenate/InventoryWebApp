package gov.nysenate.inventory.util;

import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.server.PickupServlet;

import java.io.PrintWriter;
import java.util.logging.Logger;

import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

public class HttpUtils {

    public static final int SC_SESSION_TIMEOUT = 599;

    // TODO: Temporary refactoring to consolidate session checking code.
    public static DbConnect getHttpSession(HttpServletRequest request, HttpServletResponse response, PrintWriter out) {
        HttpSession httpSession = request.getSession(false);
        DbConnect db;
        String userFallback = "";
        if (httpSession == null) {
            System.out.println("****SESSION NOT FOUND");
            db = new DbConnect();
            Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****SESSION NOT FOUND Pickup.processRequest ");
            userFallback = request.getParameter("userFallback");
            out.println("Session timed out");
            response.setStatus(HttpUtils.SC_SESSION_TIMEOUT);
        } else {
            long lastAccess = (System.currentTimeMillis() - httpSession.getLastAccessedTime());
            System.out.println("SESSION FOUND!!!! LAST ACCESSED:" + convertTime(lastAccess));
            String user = (String) httpSession.getAttribute("user");
            String pwd = (String) httpSession.getAttribute("pwd");
            db = new DbConnect(user, pwd);
        }
        return db;
    }

    public static String convertTime(long time) {
        long secDiv = 1000;
        long minDiv = 1000 * 60;
        long hourDiv = 1000 * 60 * 60;
        long minutes = time % hourDiv;
        long seconds = minutes % minDiv;
        int hoursConverted = (int) (time / hourDiv);
        int minutesConverted = (int) (minutes / minDiv);
        int secondsConverted = (int) (seconds / secDiv);

        StringBuilder returnTime = new StringBuilder();
        if (hoursConverted > 0) {
            returnTime.append("Hours:");
            returnTime.append(hoursConverted);
            returnTime.append(" ");
        }
        if (hoursConverted > 0 || minutesConverted > 0) {
            returnTime.append("Minutes:");
            returnTime.append(minutesConverted);
            returnTime.append(" ");
        }
        returnTime.append("Seconds:");
        returnTime.append(secondsConverted);
        returnTime.append(" ");

        return returnTime.toString();
    }
}
