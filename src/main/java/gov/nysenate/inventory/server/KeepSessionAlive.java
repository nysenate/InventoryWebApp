/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "KeepSessionAlive", urlPatterns = {"/KeepSessionAlive"})
public class KeepSessionAlive extends HttpServlet {

    private static final Logger log = Logger.getLogger(KeepSessionAlive.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        HttpSession httpSession = request.getSession(false);
        if (httpSession == null) {
            System.out.println ("****SESSION NOT FOUND");
            log.info("Session Not found/timed out");
            out.println("Session timed out");
        }
        else {
            String user = (String)httpSession.getAttribute("user");
            System.out.println ("SESSION FOUND!!!");
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

}
