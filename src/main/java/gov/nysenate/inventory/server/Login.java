package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.model.LoginStatus;
import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import org.apache.log4j.Logger;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Login", urlPatterns = {"/Login"})
public class Login extends HttpServlet {
 //static Logger log = Logger.getLogger(DbConnect.class.getName());
    /**
     * Processes requests for both HTTP
     * <code>GET</code> and
     * <code>POST</code> methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            String name = request.getMethod().toString();
            String user = request.getParameter("user");
            String pwd = request.getParameter("pwd");

            DbConnect db = new DbConnect(user, pwd);
            db.log.info("Servlet Login : start");

            String defrmint = "";

            try {
               defrmint = request.getParameter("defrmint");
            }
            catch (Exception e) {
                // Do nothing if we fail to get defrmint;
            }
                    
            HttpSession httpSession = request.getSession(true);
            LoginStatus loginStatus = new LoginStatus();

            // create an object of the db class and pass user name and password to it   
            // Use this code if we decide to create a new table for user name and password and 
            // validate it from database function
          
            loginStatus = db.validateUser();
            Logger.getLogger(Login.class.getName()).info("Servlet Login : defrmint:"+defrmint+", status:"+loginStatus.getDestatus());
            if (loginStatus.getNustatus() == loginStatus.VALID) {
                System.out.println("VALID LOGIN:"+loginStatus.getDestatus());
                httpSession.setAttribute("user", user);
                httpSession.setAttribute("pwd", pwd);
                loginStatus = db.securityAccess(user, defrmint, loginStatus);
            }
            else {
                httpSession.setAttribute("user", null);
                httpSession.setAttribute("pwd", null);
            }
            Gson gson = new GsonBuilder().excludeFieldsWithoutExposeAnnotation().create();

            String json = gson.toJson(loginStatus);
            System.out.println("loginStatus:"+json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);
            out.print(json);
            Logger.getLogger(Login.class.getName()).info("Servlet Login : end");
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
