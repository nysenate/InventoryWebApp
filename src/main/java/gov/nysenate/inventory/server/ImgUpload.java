package gov.nysenate.inventory.server;

import java.io.DataInputStream;
import java.io.IOException;
import java.io.PrintWriter;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.Part;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

/**
 *
 * @author jonhoffman
 */
@WebServlet(name = "ImgUpload", urlPatterns = {"/ImgUpload"})
public class ImgUpload extends HttpServlet
{

    private static final Logger log = Logger.getLogger(ImgUpload.class.getName());

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();

        try {
            DbConnect db = HttpUtils.getHttpSession(request, response, out);
            String nauser = request.getParameter("nauser");
            if (nauser != null) {
                nauser = nauser.toUpperCase();
            }
            String nuxrefemString = request.getParameter("nuxrefem");
            log.info("Saving signature info for nauser = " + nauser + " and nuxrefem = " + nuxrefemString);

            int nuxrefem = -1;
            int nuxrsign = -1;
            if (nauser == null || nauser.length() < 1) {
                out.println("Failure: No Username given");
                log.info("ImgUpload Failure: No Username given");
            } else if (nuxrefemString == null || nuxrefemString.length() < 1) {
                out.println("Failure: No Employee Xref given");
                log.info("ImgUpload Failure: No Employee Xref given");
            } else {
                boolean nuxrefemIsNumber = false;

                try {
                    nuxrefem = Integer.parseInt(nuxrefemString);
                    nuxrefemIsNumber = true;
                } catch (Exception e) {
                    log.error("Exception at Servlet ImgUpload : " + e.getMessage());
                    nuxrefemIsNumber = false;
                }

                if (nuxrefemIsNumber) {
                    //Create an input stream from our request.
                    //This input stream contains the image itself.
                    Part signature = request.getPart("Signature");
                    DataInputStream din = new DataInputStream(signature.getInputStream());
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

                    nuxrsign = db.insertSignature(data, nuxrefem, nauser);
                    //define the path to save the file using the file name from the URL.
                    //String path = "c:\\Datafiles\\"+name+".png";

                    /*InputStream in = new ByteArrayInputStream(data);
                     BufferedImage bImageFromConvert = ImageIO.read(in);
 
                     ImageIO.write(bImageFromConvert, "png", new File(path));*/
                    //out.println("Success");
                    if (nuxrsign < 0) {
                        out.println("Failure: NUXRSIGN:" + nuxrsign);
                        log.info("ImgUpload Return: Failure: NUXRSIGN:" + nuxrsign);
                    } else {
                        out.println("NUXRSIGN:" + nuxrsign);
                        log.info("ImgUpload success: nuxrsign = " + nuxrsign);
                    }
                } else {
                    out.println("Failure: Employee Xref must be a number. RECEIVED:" + nuxrefemString);
                }
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
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws ServletException, IOException
    {
        processRequest(request, response);
    }

}
