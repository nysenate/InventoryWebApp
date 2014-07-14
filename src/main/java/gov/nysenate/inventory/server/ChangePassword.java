package gov.nysenate.inventory.server;

import gov.nysenate.inventory.db.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "ChangePassword", urlPatterns = {"/ChangePassword"})
public class ChangePassword extends HttpServlet
{

  private static final Logger log = Logger.getLogger(ChangePassword.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        System.out.println();
        DbConnect db = HttpUtils.getHttpSession(request, response, out, HttpUtils.SC_SESSION_OK);
        try {
            String name = request.getMethod().toString();
            String user = request.getParameter("user");
            String newPassword = request.getParameter("newPassword");
            log.info("Changing password for user = " + user);
            String status = "OK";
          try {
            status = db.changePassword(user, newPassword);
            if (status==null||status.trim().length()==0) {
                status = "OK";
            }
          } catch (SQLException ex) {
            log.warn(null, ex);
          } catch (ClassNotFoundException ex) {
            log.warn(null, ex);
          }
            out.println(status);
        } finally {
            out.close();
        }
    }

  @Override
  protected void doGet(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

  @Override
  protected void doPost(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

}
