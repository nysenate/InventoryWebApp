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
import java.util.logging.Level;

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
      /* HandleEmails handleEmails = new HandleEmails(pickup, "pickup", request, testingModeParam, db);
       Thread threadHandleEmails = new Thread(handleEmails);
       threadHandleEmails.start();*/
      int emailReceiptStatus = 0;
      try {
        System.out.println("Before E-mail Receipt");
        HttpSession httpSession = request.getSession(false);
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");        

        EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(request, user, pwd, "pickup" ,pickup);

//        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: comment code below back in");
        System.out.println("RIGHT Before E-mail Receipt");
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Generating Email for Pickup Part");
        //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
        Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
        threadEmailMoveReceipt.start();
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: PickupPart Email Started");
        
        /*
         * If user is doing a pickup of a remote delivery, we need to also send the paperwork
         * for the remote delivery at the time if pickup. The remote delivery paperwork will be
         * printed and sent to the remote location for signature.
         * 
         */
        
        //log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: *******pickup remote delivery commented out for now");        
       
        log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: *******pickup remote:"+pickup.isRemote()+", shiptype:"+pickup.getShipType()+", RemoteType:"+pickup.getRemoteType()+", Origin Remote:"+pickup.getOrigin().isRemote()+", Destination Remote:"+pickup.getDestination().isRemote()+", Dest City:"+pickup.getDestination().getAdcity());
        if (pickup.getRemoteType().equalsIgnoreCase("RDL")) {
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Generating Email for Remote Delivery Part");
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Generating Email for Remote Delivery Part");
            
            if (db==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email db is NULL!!");              
            }
            
            Transaction remoteDelivery = null;
            TransactionMapper transactionMapper = new TransactionMapper();
            try {
                remoteDelivery = transactionMapper.queryTransaction(db, pickup.getNuxrpd());
                if (remoteDelivery==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(a): Remote Delivery Part Email remoteDelivery==NULL!!");              
                }
            }
            catch (Exception e) {
                e.printStackTrace();
            } 
                if (remoteDelivery==null) {
                log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(b): Remote Delivery Part Email remoteDelivery==NULL!!");              
                }
            //remoteDelivery = db.getDelivery(pickup.getNuxrpd()); 
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet(b): Remote Delivery Part user:"+user+", pwd:"+pwd);                          
            EmailMoveReceipt emailRemoteDeliveryReceipt = new EmailMoveReceipt(request, user, pwd, "delivery" ,remoteDelivery);
            Thread threadEmailRemoteDeliveryReceipt = new Thread(emailRemoteDeliveryReceipt);
            threadEmailRemoteDeliveryReceipt.start();
            log.info("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email Started");
            System.out.println("-=-=-=-=-=-=-=-=-=-=-=-=-=TRACE PickupServlet: Remote Delivery Part Email Started");
        }
        user = null;
        pwd = null;       
        //System.out.println("emailReceiptStatus:" + emailReceiptStatus);

//        if (emailReceiptStatus == 0) {
          //System.out.println("Database updated successfully");
          out.println("Database updated successfully");
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
