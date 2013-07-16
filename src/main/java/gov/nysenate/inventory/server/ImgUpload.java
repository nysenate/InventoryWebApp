package gov.nysenate.inventory.server;

import static gov.nysenate.inventory.server.DbConnect.log;
import java.io.DataInputStream;
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
 * @author jonhoffman
 */
@WebServlet(name = "ImgUpload", urlPatterns = {"/ImgUpload"})
public class ImgUpload extends HttpServlet {

    /**
     * Processes requests for both HTTP GET and POST methods.
     *
     * @param request servlet request
     * @param response servlet response
     * @throws ServletException if a servlet-specific error occurs
     * @throws IOException if an I/O error occurs
     */
    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException {

        //Set Response Type
        response.setContentType("text/html;charset=UTF-8");

        //Use out to send content to the user's browser
        PrintWriter out = response.getWriter();
        try {
            HttpSession httpSession = request.getSession(false);
            DbConnect db;         
            String userFallback = null;
            if (httpSession==null) {
                System.out.println ("**** IMGUPLOAD SESSION NOT FOUND");
                db = new DbConnect();
                log.info(db.ipAddr + "|" + "****IMGUPLOAD SESSION NOT FOUND ImgUpload.processRequest ");                
                try {
                   userFallback  = request.getParameter("userFallback");
                }
                catch (Exception e) {
                    log.info(db.ipAddr + "|" + "****IMGUPLOAD SESSION NOT FOUND ImgUpload.processRequest could not process Fallback Username. Generic Username will be used instead.");                
                } 
                // Seems like ImgUpload can never find a SESSION. Possibly due to Client POSTING instead of using GET???
//                out.println("");  // If sessions is not working, tablet will bomb for now with this
//                return;
            }
            else {
                System.out.println ("IMGUPLOAD SESSION FOUND!!!!");
                String user = (String)httpSession.getAttribute("user");
                String pwd = (String)httpSession.getAttribute("pwd");
                System.out.println ("--------USER:"+user);
                db = new DbConnect(user, pwd);
                
            }
            db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(ImgUpload.class.getName()).info(db.ipAddr+"|"+"Servlet ImgUpload : start");
            //Get the name of the file from the URL string
            String nauser = (String) request.getParameter("nauser");
            System.out.println("NAUSER:(" + nauser + ")");
            String nuxrefemString = (String) request.getParameter("nuxrefem");
            System.out.println("NUXREFEM:(" + nuxrefemString + ")");
            int nuxrefem = -1;
            int nuxrsign = -1;
            if (nauser == null || nauser.length() < 1) {
                out.println("Failure: No Username given");
                System.out.println ("ImgUpload Failure: No Username given");
            } else if (nuxrefemString == null || nuxrefemString.length() < 1) {
                out.println("Failure: No Employee Xref given");
                System.out.println ("ImgUpload Failure: No Employee Xref given");
            } else {
                boolean nuxrefemIsNumber = false;

                try {
                    nuxrefem = Integer.parseInt(nuxrefemString);
                    nuxrefemIsNumber = true;
                } catch (Exception e) {
                    Logger.getLogger(ImgUpload.class.getName()).fatal(db.ipAddr+"|"+"Exception at Servlet ImgUpload : " + e.getMessage());
                    nuxrefemIsNumber = false;
                }

                if (nuxrefemIsNumber) {
                    //Create an input stream from our request.
                    //This input stream contains the image itself.
                    DataInputStream din = new DataInputStream(request.getInputStream());
                    byte[] data = new byte[0];
                    byte[] buffer = new byte[512];
                    int bytesRead;
                    while ((bytesRead = din.read(buffer)) > 0) {
                        // construct an array large enough to hold the data we currently have
                        byte[] newData = new byte[data.length + bytesRead];
                        // copy data that was previously read into newData
                        System.arraycopy(data, 0, newData, 0, data.length);
                        // append new data from buffer into newData
                        System.arraycopy(buffer, 0, newData, data.length, bytesRead);
                        // set data equal to newData in prep for next block of data
                        data = newData;
                    }
                    System.out.println("IMGUPLOAD insertSignature({"+data.length+"},"+nuxrefem+","+nauser+","+userFallback+")");
                            
                 
                    nuxrsign = db.insertSignature(data, nuxrefem, nauser, userFallback);
                    //define the path to save the file using the file name from the URL.
                    //String path = "c:\\Datafiles\\"+name+".png";

                    /*InputStream in = new ByteArrayInputStream(data);
                     BufferedImage bImageFromConvert = ImageIO.read(in);
 
                     ImageIO.write(bImageFromConvert, "png", new File(path));*/
                    //out.println("Success");
                    if (nuxrsign < 0) {
                        out.println("Failure: NUXRSIGN:" + nuxrsign);
                        System.out.println ("ImgUpload Return: Failure: NUXRSIGN:" + nuxrsign);
                    } else {
                        out.println("NUXRSIGN:" + nuxrsign);
                        System.out.println ("ImgUpload Return: NUXRSIGN:" + nuxrsign);
                    }
                } else {
                    out.println("Failure: Employee Xref must be a number. RECEIVED:" + nuxrefemString);
                    System.out.println ("ImgUpload Return: NUXRSIGN:" + nuxrsign);                    
                }
            }
            Logger.getLogger(ImgUpload.class.getName()).info(db.ipAddr+"|"+"Servlet ImgUpload : end");
        } catch (Exception e) {
            e.printStackTrace();
            Logger.getLogger(ImgUpload.class.getName()).fatal(request.getRemoteAddr()+"|"+"Exception at Servlet ImgUpload : " + e.getMessage());
            System.out.println("Failure: "+ e.getMessage()+" "+e.getStackTrace()[0].toString());
            out.println("Failure: "+ e.getMessage()+" "+e.getStackTrace()[0].toString());
        } finally {
            out.close();
        }
    }

    /**
     * Handles the HTTP GET method.
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
     * Handles the HTTP POST method.
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
        return "PNG Image upload servlet";
    }
}
