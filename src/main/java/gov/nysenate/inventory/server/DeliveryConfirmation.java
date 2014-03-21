package gov.nysenate.inventory.server;

import com.google.gson.JsonSyntaxException;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.ArrayList;

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

        Transaction delivery = new Transaction();
        DbConnect db = null;
        String userFallback = null;

        PrintWriter out = response.getWriter();
        db = HttpUtils.getHttpSession(request, response, out);

        try {
            db.clientIpAddr = request.getRemoteAddr();
            log.info(db.clientIpAddr + "|" + "Servlet DeliveryConfirmation : Start");

            String deliveryJson = URLDecoder.decode(request.getParameter("Delivery"), "UTF-8");

            TransactionMapper mapper = new TransactionMapper();
            try {
                delivery = TransactionParser.parseTransaction(deliveryJson);
                mapper.completeDelivery(db, delivery);
            } catch (SQLException e) {
                out.println("Database not updated");
                log.info(db.clientIpAddr + "|" + "Database not updated");
                log.error("SQL Exception ", e);
                return;
            } catch (ClassNotFoundException e) {
                log.error("Error getting oracle jdbc driver: ", e);
            } catch (JsonSyntaxException e) {
                log.error("DeliveryConfirmation Json Syntax Exception: ", e);
                response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            }

            emailDeliveryReceipt(out, "Database updated successfully", delivery, request);
            log.info(db.clientIpAddr + "|" + "Database updated successfully");

            log.info(db.clientIpAddr + "|" + "Servlet DeliveryConfirmation : end");
        } finally {
            out.close();
        }
    }

    public void emailDeliveryReceipt(PrintWriter out, String msg, Transaction delivery, HttpServletRequest request) {
      int emailReceiptStatus  = 0;
      try {
        HttpSession httpSession = request.getSession(false);        
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");

        EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(user, pwd, "delivery", delivery);
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
