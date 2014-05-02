package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HandleEmails;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
import java.sql.SQLException;

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
@WebServlet(name = "Pickup", urlPatterns = {"/Pickup"})
public class PickupServlet extends HttpServlet
{

  private static final Logger log = Logger.getLogger(PickupServlet.class.getName());

  protected void processRequest(HttpServletRequest request, HttpServletResponse response)
          throws ServletException, IOException
  {
    response.setContentType("text/html;charset=UTF-8");
    
    Transaction pickup = new Transaction();
    String testingModeParam = null;
    DbConnect db = null;
    PrintWriter out = response.getWriter();
    db = HttpUtils.getHttpSession(request, response, out);

    String pickupJson = request.getParameter("pickup");
    log.info("Attempting to complete pickup: " + pickupJson);

    try {
        pickup = TransactionParser.parseTransaction(pickupJson);
        db.setLocationInfo(pickup.getOrigin());
    } catch (SQLException | ClassNotFoundException ex) {
        log.error(ex.getMessage(), ex);
    } catch (JsonSyntaxException e) {
        log.error("PickupServlet Json Syntax Exception: ", e);
        response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
    }

    try {
      db.setLocationInfo(pickup.getDestination());
    } catch (SQLException | ClassNotFoundException ex) {
        log.error(ex.getMessage(), ex);
    }

    System.out.println("After Parameters");

    try {
      testingModeParam = request.getParameter("testingMode");
      if (testingModeParam != null && testingModeParam.trim().length() > 0) {
        log.info("testingMode parameter was set from client and = " + testingModeParam);
      }
    } catch (Exception e) {
    }
    System.out.println("A)PickupItems = " + pickup.getPickupItems());

    TransactionMapper mapper = new TransactionMapper();
    int dbResponse = -1;
    try {
        dbResponse = mapper.insertPickup(db, pickup);
    } catch (SQLException | ClassNotFoundException ex) {
        log.error("Error saving pickup. ", ex);
    }

    pickup.setNuxrpd(dbResponse);
    String cdshiptyp = pickup.getShipType();
    if (   (cdshiptyp!=null && cdshiptyp.trim().length()>0) ||
           (pickup.getShipTypeDesc()==null||pickup.getShipTypeDesc().trim().length()==0)) {
        try {
            pickup.setShipTypeDesc(db.getShipTypeDesc(cdshiptyp));
        } catch (ClassNotFoundException ex) {
            log.warn(null, ex);
        } catch (SQLException ex) {
            log.warn(null, ex);
        }
    }

    if (dbResponse > -1) {
       HandleEmails handleEmails = new HandleEmails(pickup, HandleEmails.PICKUPTRANSACTION, request, response,  db);
       handleEmails.sendEmails();
    } else {
      out.println("Database not updated");
    }
    //System.out.println("(C) Servlet Pickup : end");
    log.info("Servlet Pickup : end");
    out.close();
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
