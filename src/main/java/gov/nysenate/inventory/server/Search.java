package gov.nysenate.inventory.server;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.io.PrintWriter;
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
@WebServlet(name = "Search", urlPatterns = {"/Search"})
public class Search extends HttpServlet
{

    private static final Logger log = Logger.getLogger(Search.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));

        try {
            String barcode_num = request.getParameter("barcode_num");
            log.info("Searching for info on barcode: " + barcode_num);

            String details = db.getDetails(barcode_num);
            String commodityCode = db.getItemCommodityCode(barcode_num);

            if (details.equals("no")) {
                out.println("Does not exist in system");
                log.info("barcode: " + barcode_num + " does not exist.");
            }
            else {
                details += "|" + commodityCode;
                log.info("Info for barcode: " + barcode_num + " = " + details);
                String model[] = details.split("\\|");
                String deadjust = "";
                if (model.length>11) {
                    deadjust = model[11].replaceAll("\"", "&#34;");
                }
                                // out.println(" Model   :  "+model[0]+"\n Location :  "+model[1]+"\n Manufacturer : "+model[2]+"\n Signed By  :    "+model[3]);
                //V_NUSENATE,V_NUXREFSN,V_NUSERIAL,V_DTISSUE,V_CDLOCATTO,V_CDLOCTYPETO,V_CDCATEGORY,V_DECOMMODITYF
                //out.println(" Barcode   :  "+model[0]+"\n NUXREFSN :  "+model[1]+"\n NUSERIAL : "+model[2]+"\n DTISSUE  :    "+model[3]+"\n CDLOCATTO  :    "+model[4]+"\n CDLOCTYPETO :    "+model[5]+"\n CDCATEGORY  :    "+model[6]+"\n DECOMMODITYF  :    "+model[7]);

                //Psuedo JSON for now               

                out.println("{\"nusenate\":\"" + model[0] + "\",\"nuxrefsn\":\"" + model[1] + "\",\"dtissue\":\"" + model[3] + "\",\"cdlocatto\":\"" + model[4] + "\",\"cdloctypeto\":\"" + model[5]
                        + "\",\"cdcategory\":\"" + model[6] + "\",\"adstreet1to\":\"" + model[7].replaceAll("\"", "&#34;") + "\",\"decommodityf\":\"" + model[8].replaceAll("\"", "&#34;")
                        + "\",\"cdstatus\":\"" + model[10] + "\",\"deadjust\":\"" + deadjust + "\",\"dtlstinvntry\":\"" + model[13] + "\",\"commodityCd\":\"" + model[14] +  "\",\"nuserial\":\"" + model[2] + "\"}");
             }

        } finally {
            out.close();
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException
    {
        processRequest(request, response);
    } // doGet()

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
                   throws ServletException, IOException
    {
        processRequest(request, response);
    }

}
