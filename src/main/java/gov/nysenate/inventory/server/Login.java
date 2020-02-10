package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.LoginStatus;
import gov.nysenate.inventory.util.Serializer;
import org.apache.log4j.Logger;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {

    private static Logger log = Logger.getLogger(DbConnect.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String name = request.getMethod().toString();
            String user = request.getParameter("user");
            String pwd = request.getParameter("pwd");

            log.info("User " + user + "is attempting to log in");

            DbConnect db = new DbConnect(user, pwd);
            String defrmint = request.getParameter("defrmint");

            HttpSession httpSession = request.getSession(true);
            LoginStatus loginStatus = new LoginStatus();

            loginStatus = db.validateUser();

            /*
                For testing purposes. Set timeout for 30 seconds...
             */

            if (loginStatus.getNustatus() == loginStatus.VALID) {
                System.out.println("VALID LOGIN:"+loginStatus.getDestatus());
                log.info(user + " has access to use this app");
                httpSession.setAttribute("user", user);
                httpSession.setAttribute("pwd", pwd);
                loginStatus = db.securityAccess(user, defrmint, loginStatus);
            }
            else {
                log.info("Unable to validate access for user: " + user);
                httpSession.setAttribute("user", null);
                httpSession.setAttribute("pwd", null);
            }

            String json = Serializer.serialize(loginStatus);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            out.print(json);
        } finally {
            out.close();
        }
    }

    // <editor-fold defaultstate="collapsed" desc="HttpServlet methods. Click on the + sign on the left to edit the code.">
    /**
     * Handles the HTTP
     * <code>GET</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Handles the HTTP
     * <code>POST</code> method.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        processRequest(request, response);
    }

    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo() {
        return "Short description";
    }// </editor-fold>
}
