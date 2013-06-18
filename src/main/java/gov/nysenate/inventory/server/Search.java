package gov.nysenate.inventory.server;

import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLEncoder;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Search", urlPatterns = {"/Search"})
public class Search extends HttpServlet
{
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
                   throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        try {
            /* TODO output your page here. You may use following sample code. 
            out.println("<html>");
            out.println("<head>");
            out.println("<title>Servlet Search</title>");            
            out.println("</head>");
            out.println("<body>");
            out.println("<h1>Servlet Search at " + request.getContextPath() + "</h1>");
            out.println("</body>");
            out.println("</html>");  */
            String barcode_num = request.getParameter("barcode_num");
            System.out.println("Search Servlet  barcode_num "+barcode_num);
            //  out.println("Barcode # "+barcode_num);
            
            //int barcode=Integer.valueOf(barcode_num);
            System.out.println("Search Servlet  barcode "+barcode_num);
            DbConnect db = new DbConnect();
            String details = db.getDetails(barcode_num);
            
            if (details.equals("no")) {
              
                out.println("Does not exist in system");
            }
            else {
                String model[] = details.split("\\|");
                System.out.println("DETAILS:"+details+" MODEL LENGTH:"+model.length);
                // out.println(" Model   :  "+model[0]+"\n Location :  "+model[1]+"\n Manufacturer : "+model[2]+"\n Signed By  :    "+model[3]);
                //V_NUSENATE,V_NUXREFSN,V_NUSERIAL,V_DTISSUE,V_CDLOCATTO,V_CDLOCTYPETO,V_CDCATEGORY,V_DECOMMODITYF
                //out.println(" Barcode   :  "+model[0]+"\n NUXREFSN :  "+model[1]+"\n NUSERIAL : "+model[2]+"\n DTISSUE  :    "+model[3]+"\n CDLOCATTO  :    "+model[4]+"\n CDLOCTYPETO :    "+model[5]+"\n CDCATEGORY  :    "+model[6]+"\n DECOMMODITYF  :    "+model[7]);

                //Psuedo JSON for now
                out.println("{\"nusenate\":\""+model[0]+"\",\"nuxrefsn\":\""+model[1]+"\",\"dtissue\":\""+model[3]+"\",\"cdlocatto\":\""+model[4]+"\",\"cdloctypeto\":\""+model[5]+"\",\"cdcategory\":\""+model[6]+"\",\"adstreet1to\":\""+model[7].replaceAll("\"", "&#34;") +"\",\"decommodityf\":\""+model[8].replaceAll("\"", "&#34;")+"\"}");
            }
        }
        finally {            
            out.close();
        }
    } // processRequest()



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
                   throws ServletException, IOException
    {
        processRequest(request, response);
    } // doGet()



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
                   throws ServletException, IOException
    {
        processRequest(request, response);
    } // doPost()



    /**
     * Returns a short description of the servlet.
     *
     * @return a String containing servlet description
     */
    @Override
    public String getServletInfo()
    {
        return "Short description";
    }// </editor-fold>
}
