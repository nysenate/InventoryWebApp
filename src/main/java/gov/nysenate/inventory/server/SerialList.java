package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.InvSerialNumber;
import gov.nysenate.inventory.util.HttpUtils;
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
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "SerialList", urlPatterns = {"/SerialList"})
public class SerialList extends HttpServlet
{

  int numaxResults = 500;
  private static final Logger log = Logger.getLogger(SerialList.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
            String nuserial = request.getParameter("nuserial");
            String maxResults = request.getParameter("maxResults");
            log.info("SerialList nuserial = " + nuserial);

            if (maxResults!=null) {
               try {
                this.numaxResults = Integer.valueOf(maxResults);
               }
               catch (Exception e) {
                 log.warn(e.getMessage(), e);
               }
            }

            List<InvSerialNumber> serialList = Collections.synchronizedList(new ArrayList<InvSerialNumber>());
            
            serialList = db.getNuSerialList(nuserial, numaxResults);

            if (serialList.size() == 0) {
                System.out.println("NO SERIAL#s FOUND");
            }

            String json = Serializer.serialize(serialList);
            log.info("Serial list results = " + json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");

            out.print(json);
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
