package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.InvSerialNumber;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

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
        DbConnect db = HttpUtils.getHttpSession(request, response, out);

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
            
            Gson gson = new GsonBuilder()
                  .excludeFieldsWithoutExposeAnnotation()
                  .create();                 
            //Logger.getLogger(SerialList.class.getName()).info("Servlet SerialList : start");

            List<InvSerialNumber> serialList = Collections.synchronizedList(new ArrayList<InvSerialNumber>());
            
            serialList = db.getNuSerialList(nuserial, numaxResults);

            if (serialList.size() == 0) {
                System.out.println("NO SERIAL#s FOUND");
            }

            String json = gson.toJson(serialList);
            System.out.println ("SERIAL LIST RESULTS:"+json);
            log.info("Serial list results = " + json);
            response.setContentType("application/json");
            response.setCharacterEncoding("UTF-8");
            response.getWriter().write(json);

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
