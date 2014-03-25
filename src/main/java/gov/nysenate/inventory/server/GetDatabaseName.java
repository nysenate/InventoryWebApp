package gov.nysenate.inventory.server;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "GetDatabaseName", urlPatterns = {"/GetDatabaseName"})
public class GetDatabaseName extends HttpServlet
{

  private static final Logger log = Logger.getLogger(GetDatabaseName.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    PrintWriter out = response.getWriter();
        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);
            log.info("Getting database name.");
            String result = db.getDatabaseName();
            out.println(result);
            
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
