package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.Arrays;

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
        PrintWriter out = response.getWriter();
        int newDeliveryResult = 0;
        int orgDeliveryResult = 0;
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;            
            String userFallback = null;
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
            
            db.ipAddr=request.getRemoteAddr();
            log.info(db.ipAddr + "|" + "Servlet DeliveryConfirmation : Start");

            // 1. Get the data from app request
            String nuxrpd = request.getParameter("NUXRPD");
            String[] itemsInDelivery = request.getParameterValues("deliveryItemsStr[]");
            String[] checkedStr = request.getParameterValues("checkedStr[]");
            String NUXRACCPTSIGN = request.getParameter("NUXRACCPTSIGN");
            String NADELIVERBY = request.getParameter("NADELIVERBY");
            String NAACCEPTBY = request.getParameter("NAACCEPTBY");
            String DEDELCOMMENTS = request.getParameter("DECOMMENTS");
            String[] notChecked = generateNotCheckedItems(itemsInDelivery, checkedStr);

            // Make delivery
            orgDeliveryResult = db.confirmDelivery(nuxrpd, NUXRACCPTSIGN, NADELIVERBY, NAACCEPTBY, checkedStr, DEDELCOMMENTS, userFallback);
            log.info(db.ipAddr + "|" + "Delivered Items: " + Arrays.toString(itemsInDelivery));

            if (notChecked.length > 0) {
                // Make new row in FM12INVINTRANS for items not delivered.
                newDeliveryResult = db.createNewDelivery(nuxrpd, notChecked, userFallback);
                log.info(db.ipAddr + "|" + "Not Delivered Items: " + Arrays.toString(notChecked));
            }
            else {
                log.info(db.ipAddr + "|" + "All items delivered.");
            }
            
            if (orgDeliveryResult == 0 && newDeliveryResult == 0) {
                out.println("Database updated sucessfully");
                log.info(db.ipAddr + "|" + "Database updated sucessfully");
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

    private String[] generateNotCheckedItems(String[] deliveryItemsStr, String[] checkedStr) {
        ArrayList<String> notChecked = new ArrayList<String>(Arrays.asList(deliveryItemsStr));
        for (String chkItem : checkedStr) {
            notChecked.remove(chkItem);
        }
        return notChecked.toArray(new String[notChecked.size()]);
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
