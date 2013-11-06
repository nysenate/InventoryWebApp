package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Delivery;

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
@WebServlet(name = "DeliveryConfirmation", urlPatterns = {"/DeliveryConfirmation"})
public class DeliveryConfirmation extends HttpServlet {
 
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

        Logger log = Logger.getLogger(DeliveryConfirmation.class.getName());
        response.setContentType("text/html;charset=UTF-8");

        Delivery delivery = new Delivery();
        DbConnect db = null;
        String userFallback = null;

        PrintWriter out = response.getWriter();
        int newDeliveryResult = 0;
        int orgDeliveryResult = 0;

        try {
            HttpSession httpSession = request.getSession(false);
      
            userFallback = null;
            if (httpSession==null) {
                System.out.println ("****SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.ipAddr + "|" + "****SESSION NOT FOUND DeliveryConfirmation.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.ipAddr + "|" + "****SESSION NOT FOUND DeliveryConfirmation.processRequest could not process Fallback Username. Generic Username will be used instead.");                
                }
                out.println("Session timed out");
                return;
                
            }
            else {
                System.out.println ("SESSION FOUND!!!!");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
            }

            db.ipAddr = request.getRemoteAddr();
            log.info(db.ipAddr + "|" + "Servlet DeliveryConfirmation : Start");
           
            delivery.setNuxrpd(Integer.parseInt(request.getParameter("NUXRPD")));
            if (request.getParameterValues("deliveryItemsStr[]") != null) {
                delivery.setAllItems(request.getParameterValues("deliveryItemsStr[]"));
            }
            if (request.getParameterValues("checkedStr[]") != null) {
                delivery.setCheckedItems(request.getParameterValues("checkedStr[]"));
            }
            delivery.setNuxrsccptsign(request.getParameter("NUXRACCPTSIGN"));
            delivery.setNadeliverby(request.getParameter("NADELIVERBY"));
            delivery.setNaacceptby(request.getParameter("NAACCEPTBY"));
            delivery.setComments(request.getParameter("DECOMMENTS"));
            delivery.generateNotCheckedItems();

            orgDeliveryResult = db.confirmDelivery(delivery, userFallback);
            log.info(db.ipAddr + "|" + "Delivered Items: " + delivery.getCheckedItems());

            if (delivery.getNotCheckedItems().size() > 0) {
                newDeliveryResult = db.createNewPickup(delivery, userFallback);
                log.info(db.ipAddr + "|" + "Not Delivered Items: " + delivery.getNotCheckedItems());
            }
            else {
                log.info(db.ipAddr + "|" + "All items delivered.");
            }

            if (orgDeliveryResult == 0 && newDeliveryResult == 0) {
                emailDeliveryReceipt(out, "Database updated successfully", delivery, request);
              //out.println("Database updated successfully");
              log.info(db.ipAddr + "|" + "Database updated successfully");
            } else if (orgDeliveryResult != 0 && newDeliveryResult != 0) {
                out.println("Database not updated");
                log.info(db.ipAddr + "|" + "Database not updated");
            } else if (orgDeliveryResult != 0 && newDeliveryResult == 0) {
                out.println("Database partially updated, delivered items were not updated correctly. Please contact STSBAC.");
                log.info(db.ipAddr + "|" + "Database partially updated, delivered items were not updated correctly. Please contact STSBAC.");
            } else if (orgDeliveryResult == 0 && newDeliveryResult != 0) {
                out.println("Database partially updated, items left over were not updated correctly. Please contact STSBAC.");
                log.info(db.ipAddr + "|" + "Database partially updated, items left over were not updated correctly. Please contact STSBAC.");
            }
        
            log.info(db.ipAddr + "|" + "Servlet DeliveryConfirmation : end");
        } finally {
            out.close();
        }
    }
    
    public void emailDeliveryReceipt(PrintWriter out, String msg, Delivery delivery, HttpServletRequest request) {
      int emailReceiptStatus  = 0;
      try {
        HttpSession httpSession = request.getSession(false);        
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");        
        EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(user, pwd, delivery);        
        Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
        threadEmailMoveReceipt.start();          
        
        //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(delivery);
        //if (emailReceiptStatus==0) {
          out.println("Database updated successfully");         
        /*}
        else {
          out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:"+emailReceiptStatus+").");
        }*/
      }
      catch (Exception e) {
        e.printStackTrace();
        System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).["+e.getMessage()+":"+e.getStackTrace()[0].toString()+"]");
        out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:"+emailReceiptStatus+"-2).");
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
