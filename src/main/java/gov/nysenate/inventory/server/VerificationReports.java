package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.JsonArray;
import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.InvItem;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

/**
 *
 * @author Patil
 */
@WebServlet(name = "VerificationReports", urlPatterns = {"/VerificationReports"})
public class VerificationReports extends HttpServlet {

    private static final Logger log = Logger.getLogger(VerificationReports.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(request, HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
            String cdlocat = request.getParameter("cdlocat");
            String scannedItems = request.getParameter("scannedItems");

            JsonParser parser = new JsonParser();
            JsonArray jsonArray = (JsonArray)parser.parse(scannedItems);
            
            ArrayList<InvItem> invItems = new ArrayList<InvItem>();
            
            for (int x=0;x<jsonArray.size();x++) {
              //JsonObject o = (JsonObject) jsonArray.get(x);

              JsonElement json2 = jsonArray.get(x);
              Gson gson = new Gson();
              InvItem invItem = gson.fromJson(json2, InvItem.class); 
              invItems.add(invItem);
            }
            
/*            for (int x=0;x<invItems.size();x++) {
              InvItem invItem = invItems.get(x);
              System.out.println(x+": CDLOCAT:"+invItem.getCdlocat()+", NUSENATE:"+invItem.getNusenate()+", CDCOMMODITY:"+invItem.getCdcommodity());
            }*/
            
            String cdloctype = null;
            
            try {
              cdloctype = request.getParameter("cdloctype");
              /*
               * If the cdloctype that was passess is larger than one character, the value must
               * be wrong, so clear it out so the database procedure will simply look at location code
               * which is one to one in most cases..  Future enhancement might be to pass back an informational 
               * message to the client.
               */
              if (cdloctype!=null && cdloctype.trim().length()>1) {
                log.warn("****Parameter cdloctype was passed by the Client with a value of "+cdloctype+" which is larger than 1 character. IGNORING. to VerificationReports.processRequest ");
                cdloctype = null;
              }              
            }
            catch (Exception e) {
              /*
               * If Parameter cdloctype was not passed by the client, then simply pass null for the
               * value, the database procedure which is eventually called will handle nulls.
              */
              log.info("****Parameter cdloctype was not passed by the Client to VerificationReports.processRequest ");
              cdloctype = null;
            }
            
            int result = db.setBarcodesInDatabase(cdlocat, cdloctype, invItems);
  
            if (result == 0) {
                out.println("Database updated successfully");
            } else {
                out.println("Database not updated");
            }
        } finally {
            out.close();
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
