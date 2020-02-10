package gov.nysenate.inventory.server;

import com.google.gson.Gson;
import com.google.gson.GsonBuilder;
import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.util.HttpUtils;
import org.apache.log4j.Logger;

import javax.servlet.annotation.MultipartConfig;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;
import java.io.IOException;
import java.io.PrintWriter;
import java.net.URLDecoder;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.Map;

/**
 *
 * @author jonhoffman
 */
@WebServlet(name = "ImgUpload", urlPatterns = {"/ImgUpload"})
@MultipartConfig
public class ImgUpload extends HttpServlet
{

    private static final Logger log = Logger.getLogger(ImgUpload.class.getName());
    private static final Gson gson = new GsonBuilder()
            .setDateFormat("yyyy-MM-dd'T'HH:mm:ssZ").create();

    protected void processRequest(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        response.setContentType("text/html;charset=UTF-8");
        PrintWriter out = response.getWriter();
        HttpSession session = request.getSession(false);
        DbConnect db = new DbConnect(HttpUtils.getUserName(session), HttpUtils.getPassword(session));
        String json = null;
        Map<String, String> values = new HashMap<>();

        String requestString = org.apache.commons.io.IOUtils.toString(request.getInputStream());
        if (requestString != null) {
            requestString = URLDecoder.decode(requestString, "UTF-8");
        }

        try {
            String nauser = request.getParameter("nauser");
            if (nauser != null) {
                nauser = nauser.toUpperCase();
            }
            String nuxrefemString = request.getParameter("nuxrefem");

            int nuxrefem = -1;
            int nuxrsign = -1;

            if (nauser == null || nauser.length() < 1) {
                values.put("Error", "Image Upload  failure: No Username given" );
            } else if (nuxrefemString == null || nuxrefemString.length() < 1) {
                values.put("Error", "Image Upload  failure: No Employee Xref given" );
            } else {
                String signature = request.getParameter("signature");

                if (requestString != null && signature==null) {
                    if (requestString.indexOf("&signature=")< requestString.indexOf("&userFallback=")) {
                        signature = requestString.substring(requestString.indexOf("&signature=") + 11, requestString.indexOf("&userFallback=")-1);
                    }
                    else if (signature==null){
                        signature = requestString.substring(requestString.indexOf("&signature=") + 11);
                    }
                }
                else if (requestString == null) {
                    values.put("Error", "Image Upload failure: Nothing requested" );
                }

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
//                    Part signature = request.getPart("Signature");
                  //  DataInputStream din = new DataInputStream(signature.getInputStream());
                    byte[] data = new byte[0];
                    if (signature!=null) {
                        signature = URLDecoder.decode(signature, "UTF-8");

                        String[] byteValues  = signature.substring(1,  signature.length() - 1).split(",");
                        data = new byte[byteValues.length];
                        for (int i=0, len=data.length; i<len; i++) {
                            data[i] = Byte.parseByte(byteValues[i].trim());
                        }
                    }
                    nuxrsign = db.insertSignature(data, nuxrefem, nauser);

                    values.put("nuxrrelsign", String.valueOf(nuxrsign));


                    if (nuxrsign < 0) {
                        values.put("Error", "Image Upload failure: nuxrsign:" + nuxrsign);
                    } else {
                          values.put("Message", "Image Upload success");
                    }

                } else {
                    values.put("Error", "Image Upload failure: Employee Xref must be a number. Received: " + nuxrefemString);
                }
            }

        } finally {
            json = gson.toJson(values);
            out.println(json);
            out.close();
        }
    }

    public void testParameters(HttpServletRequest request) {
        Enumeration<String> parameterNames = request.getParameterNames();

        while (parameterNames.hasMoreElements()) {
            String paramName = parameterNames.nextElement();
            System.out.println("IMGUPLOAD Parameter: "+paramName);

            String[] paramValues = request.getParameterValues(paramName);
            for (int i = 0; i < paramValues.length; i++) {
                String paramValue = paramValues[i];
                System.out.println("                      "+paramValue);
            }
        }
    }

    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response)
            throws  IOException
    {
        processRequest(request, response);
    }

    @Override
    protected void doPost(HttpServletRequest request, HttpServletResponse response)
            throws IOException
    {
        processRequest(request, response);
    }

}
