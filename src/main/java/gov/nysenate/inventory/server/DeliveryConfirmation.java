package gov.nysenate.inventory.server;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        int newDeliveryResult = 0;
        int orgDeliveryResult = 0;
        try {
            // 1. Get the data from app request
            String nuxrpd = request.getParameter("NUXRPD");
            String deliveryItemsStr = request.getParameter("deliveryItemsStr");
            String checkedStr = request.getParameter("checkedStr");
            String NUXRACCPTSIGN = request.getParameter("NUXRACCPTSIGN");
            String NADELIVERBY = request.getParameter("NADELIVERBY");
            String NAACCEPTBY = request.getParameter("NAACCEPTBY");
            String DEDELCOMMENTS = request.getParameter("DECOMMENTS");
           /* System.out.println ("nuxrpd:"+nuxrpd);
            System.out.println ("deliveryItemsStr:"+deliveryItemsStr);
            System.out.println ("checkedStr:"+checkedStr);
            System.out.println ("NUXRACCPTSIGN:"+NUXRACCPTSIGN);
            System.out.println ("NADELIVERBY:"+NADELIVERBY);
            System.out.println ("NAACCEPTBY:"+NAACCEPTBY);
            System.out.println ("DEDELCOMMENTS:"+DEDELCOMMENTS);*/
            
            //2. create list of items which are not delivered, delivered and comapte them

            String deliveryItems[] = deliveryItemsStr.split(",");
            ArrayList<String> deliveryList = new ArrayList<String>();
            ArrayList<String> notDeliveredList = new ArrayList<String>();
            for (int i = 0; i < deliveryItems.length; i++) {
                deliveryList.add(deliveryItems[i]);
                notDeliveredList.add(deliveryItems[i]);// we will add all items first and remove non checked items
            }

            // remove the checked items from the total list 
            String checked[] = checkedStr.split(",");
            for (int i = 0; i < checked.length; i++) {
                int pos = Integer.parseInt(checked[i].trim());
                String item = deliveryList.get(pos).trim();
                notDeliveredList.remove(item);
            }


            //3.  if there are items which are not delivered then create a new nuxrpd using other servlet    
            DbConnect db = new DbConnect();
            if (notDeliveredList.size() > 0) {

                String barcodes[] = notDeliveredList.toArray(new String[notDeliveredList.size()]);
                System.out.println("Not Deliveredd Items found");

          /*-------Following code is copied from pickup servlet and we will be using it to create a new nuxrpickup-------------------*/

                //String barcodes[] = {"077896", "078567","0268955"};
                newDeliveryResult = db.createNewDelivery(nuxrpd, barcodes);
                System.out.println("Not Delivered Items assigned to "+nuxrpd+" newDeliveryResult:"+newDeliveryResult);
               //int result = db.invTransit("A42FB", "A411A", barcodes, "vikram", 10, "Brian", 11);
            }

            // 4. update the items in the details table and the master table that the nuxrpd is delivered and the items will not show up in the queries later

            orgDeliveryResult = db.confirmDelivery(nuxrpd, NUXRACCPTSIGN, NADELIVERBY, NAACCEPTBY, deliveryList, notDeliveredList, DEDELCOMMENTS);
       /*     System.out.println ("db.confirmDelivery("+nuxrpd+", "+NUXRACCPTSIGN+", \""+NADELIVERBY+"\", \""+NAACCEPTBY+"\", \""+deliveryList+"\", \""+notDeliveredList+"\", \""+DEDELCOMMENTS+"\")");
            System.out.println("Original Delivery result "+orgDeliveryResult);*/

            if (orgDeliveryResult == 0 && newDeliveryResult == 0) {
                out.println("Database updated sucessfully");
            } else if (orgDeliveryResult != 0 && newDeliveryResult != 0) {
                out.println("Database not updated");
            } else if (orgDeliveryResult != 0 && newDeliveryResult == 0) {
                out.println("Database partially updated, delivered items were not updated correctly. Please contact STSBAC.");
            } else if (orgDeliveryResult == 0 && newDeliveryResult != 0) {
                out.println("Database partially updated, items left over were not updated correctly. Please contact STSBAC.");
            }
        } finally {
            out.close();
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
