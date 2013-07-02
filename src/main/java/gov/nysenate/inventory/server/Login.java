package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.io.PrintWriter;
import java.io.Reader;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.regex.Matcher;
import java.util.regex.Pattern;
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
            /* TODO output your page here. You may use following sample code. */
            DbConnect db = new DbConnect();
          
            db.ipAddr=request.getRemoteAddr();         //Logger.getLogger(Login.class.getName()).info("Servlet Login : start");
            db.log.info(db.ipAddr+"|"+"Servlet Login : start");
            String name = request.getMethod().toString();
            String user = request.getParameter("user");
            String pwd = request.getParameter("pwd");
            HttpSession httpSession = request.getSession(true);
            String status = "N";

            // create an object of the db class and pass user name and password to it   
            //  Use this code if we decide to create a new table for user name and password and 
            // validate it from database function
          
            status = db.validateUser(user, pwd);


            if (status.equalsIgnoreCase("VALID")) {
                httpSession.setAttribute("user", user);
                httpSession.setAttribute("pwd", pwd);
            }
            else {
                httpSession.setAttribute("user", null);
                httpSession.setAttribute("pwd", null);
            }


            //---------- Call the MyWorkplace web server and validate the user name and password
          /*
             int userNumber = -1000;

             Properties properties = new Properties();
             InputStream ins =  db.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
             properties.load(ins);
     
             String connectionString = properties.getProperty("myWpAPI");
            
             // validating from myWorkPlace API 
             URL url = new URL(connectionString + user + "+PVCXVNXCU=" + pwd);
             URLConnection con = url.openConnection();
             BufferedReader in = new BufferedReader(new InputStreamReader(con.getInputStream()));
             String inputLine;
             StringBuilder builder = new StringBuilder();
             while ((inputLine = in.readLine()) != null) {
             builder.append(inputLine.trim());
             }
             in.close();
             String serverResponse = builder.toString(); // this string is the response we get from server

             int start = serverResponse.indexOf("<body>");
             int end = serverResponse.indexOf("</body>");

             String bodyText = serverResponse.substring(start + 6, end).trim(); // the server reponse is HTML page, we just need 
             // content of BODY tag
             // server checks if the user name and password combination is correct, if yes it returns us 
             // the user number or it will return us -2 (invalid user)
             try {
             userNumber = Integer.parseInt(bodyText);
             } catch (Exception e) {
             out.println(e.getMessage() + " " + e.getStackTrace()[0].toString());
             }


             if (userNumber >= 0) {
             status = "VALID";
             }
             */
            // ------------MyWorkPlace Validation end


            // pass the status to the app
            out.println(status);
            Logger.getLogger(Login.class.getName()).info(db.ipAddr+"|"+"Servlet Login : end");
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
