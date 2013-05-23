package gov.nysenate.inventory.server;

import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.DataInputStream;
import java.io.File;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import javax.imageio.ImageIO;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

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
            //Get the name of the file from the URL string
            String nauser = (String) request.getParameter("nauser");
            System.out.println("NAUSER:(" + nauser + ")");
            String nuxrefemString = (String) request.getParameter("nuxrefem");
            System.out.println("NUXREFEM:(" + nuxrefemString + ")");
            int nuxrefem = -1;
            int nuxrsign = -1;
            if (nauser == null || nauser.length() < 1) {
                out.println("Failure: No Username given");
            } else if (nuxrefemString == null || nuxrefemString.length() < 1) {
                out.println("Failure: No Employee Xref given");
            } else {
                boolean nuxrefemIsNumber = false;

                try {
                    nuxrefem = Integer.parseInt(nuxrefemString);
                    nuxrefemIsNumber = true;
                } catch (Exception e) {
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
                    DbConnect db = new DbConnect();
                    nuxrsign = db.insertSignature(data, nuxrefem, nauser);
                    //define the path to save the file using the file name from the URL.
                    //String path = "c:\\Datafiles\\"+name+".png";

                    /*InputStream in = new ByteArrayInputStream(data);
                     BufferedImage bImageFromConvert = ImageIO.read(in);
 
                     ImageIO.write(bImageFromConvert, "png", new File(path));*/
                    //out.println("Success");
                    if (nuxrsign < 0) {
                        out.println("Failure: NUXRSIGN:" + nuxrsign);
                    } else {
                        out.println("NUXRSIGN:" + nuxrsign);
                    }
                } else {
                    out.println("Failure: Employee Xref must be a number. RECEIVED:" + nuxrefemString);
                }
            }
        } catch (Exception e) {
            e.printStackTrace();
            out.println("Failure");
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
