package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;
import org.apache.log4j.Logger;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
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

    if (dbResponse > -1) {
      int emailReceiptStatus = 0;
      try {
        System.out.println("Before E-mail Receipt");
        HttpSession httpSession = request.getSession(false);
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");        

        EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(user, pwd, "pickup" ,pickup);
        user = null;
        pwd = null;

        System.out.println("RIGHT Before E-mail Receipt");
        //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
        Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
        threadEmailMoveReceipt.start();
        //System.out.println("emailReceiptStatus:" + emailReceiptStatus);



//        if (emailReceiptStatus == 0) {
          //System.out.println("Database updated successfully");
          out.println("Database updated successfully");
/*        } else {
          System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + ").");
          out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + ").");
        }*/
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).["+e.getMessage()+":"+e.getStackTrace()[0].toString()+"]");
          out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
      }
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
