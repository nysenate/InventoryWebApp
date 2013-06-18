package gov.nysenate.inventory.server;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.util.Properties;

import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeMessage;
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.nio.ByteBuffer;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.commons.io.IOUtils;
//import sun.misc.IOUtils;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Pickup", urlPatterns = {"/Pickup"})
public class Pickup extends HttpServlet {

    String msgBody = "";
    String naemailTo = "";
    String naemployeeTo = "MR. SO AND SO";
    String barcodeStr = "";
    String originLocation = "";
    String destinationLocation = "";
    String NAPICKUPBY = "";
    String NUXRRELSIGN = "";
    String NARELEASEBY = "";
    String NADELIVERBY = "";
    String NAACCEPTBY = "";
    String NUXRACCPTSIGN = "";
    String DECOMMENTS = "";
    int nuxrpd = -1;
    byte[] attachment = null;

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
        try {
            DbConnect db = new DbConnect();
             db.ipAddr=request.getRemoteAddr();
            Logger.getLogger(Pickup.class.getName()).info(db.ipAddr+"|"+"Servlet Pickup : start");
            barcodeStr = request.getParameter("barcodes");
            originLocation = request.getParameter("originLocation");
            destinationLocation = request.getParameter("destinationLocation");
            NAPICKUPBY = request.getParameter("NAPICKUPBY");
            NUXRRELSIGN = request.getParameter("NUXRRELSIGN");
            NARELEASEBY = request.getParameter("NARELEASEBY").replaceAll("'", "''");;
            NADELIVERBY = request.getParameter("NADELIVERBY");
            NAACCEPTBY = "";
            try {
                NAACCEPTBY = request.getParameter("NAACCEPTBY").replaceAll("'", "''");
            } catch (NullPointerException e) {
                NAACCEPTBY = "";
            }
            NUXRACCPTSIGN = request.getParameter("NUXRACCPTSIGN");
            DECOMMENTS = request.getParameter("DECOMMENTS").replaceAll("'", "''");
            String barcodes[] = barcodeStr.split(",");
            System.out.println("point 1 ");
           
            //String barcodes[] = {"077896", "078567","0268955"};
            System.out.println("Pickup Servlet NUXRRELSIGN:" + NUXRRELSIGN);
            nuxrpd = db.invTransit(originLocation, destinationLocation, barcodes, NAPICKUPBY, NARELEASEBY, NUXRRELSIGN, NADELIVERBY, NAACCEPTBY, NUXRACCPTSIGN, DECOMMENTS);
            //int result = db.invTransit("A42FB", "A411A", barcodes, "vikram", 10, "Brian", 11);
            if (nuxrpd > -1) {
                sendEmail();
                out.println("Database updated sucessfully");
            } else {
                out.println("Database not updated");
            }
            Logger.getLogger(Pickup.class.getName()).info(db.ipAddr+"|"+"Servlet Pickup : end");
        } finally {
            out.close();
        }
    }

    @SuppressWarnings("empty-statement")
    public void sendEmail() {
        Properties properties = new Properties();
        InputStream in = this.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
        try {
            properties.load(in);
        } catch (IOException ex) {
            Logger.getLogger(Pickup.class.getName()).log(Level.SEVERE, null, ex);
        }

        String smtpServer = properties.getProperty("smtpServer");
        String pickupReceiptURL = properties.getProperty("pickupReceiptURL");

        Properties props = new Properties();
        props.setProperty("mail.smtp.host", smtpServer);
        Session session = Session.getDefaultInstance(props, null);
        StringBuilder sb = new StringBuilder();

        System.out.println("Trying to send an email");
        sb.append("Dear ");
        sb.append(this.naemployeeTo);
        sb.append("<br/><br/> Equipment was picked up by <b>" + this.NAPICKUPBY + "</b> from <b>" + this.originLocation + "</b> with the destination of <b>" + this.destinationLocation + "</b>");
        sb.append("<br /><br />To view Equipment Pickup Receipt, please open the PDF attachment included in this e-mail.");
        sb.append(this.nuxrpd);
        sb.append("'>HERE</a>.");// &destype=CACHE&desformat=PDF

        try {
            this.attachment = bytesFromUrlWithJavaIO(pickupReceiptURL + this.nuxrpd); // +"&destype=CACHE&desformat=PDF
            //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
            System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((double) (attachment.length) / 1024.0) + "KB");
        } catch (MalformedURLException ex) {
            Logger.getLogger(Pickup.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(Pickup.class.getName()).log(Level.SEVERE, null, ex);
        }

        if (attachment == null) {
        }
        MimeMultipart mimeMultipart = new MimeMultipart();
        MimeBodyPart attachmentPart = new MimeBodyPart();
        try {
            attachmentPart.setDataHandler(
                    new DataHandler(
                    new DataSource() {
                public String getContentType() {
                    return "application/pdf";
                }

                public InputStream getInputStream() throws IOException {
                    return new ByteArrayInputStream(attachment);
                }

                public String getName() {
                    return "pickup_receipt.pdf";
                }

                public OutputStream getOutputStream() throws IOException {
                    return null;
                }
            }));
        } catch (MessagingException ex) {
            Logger.getLogger(Pickup.class.getName()).log(Level.SEVERE, null, ex);
        }



        try {
            /*
             *            Properties properties = new Properties();
             DbConnect db= new DbConnect();
             InputStream in =  db.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
             properties.load(in);
     
             String connectionString = properties.getProperty("connectionString");
             String userName = properties.getProperty("user");
             String password = properties.getProperty("password");

             */

            in = this.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
            properties.load(in);

            String naemailTo1 = properties.getProperty("pickupEmailTo1");
            String naemailNameTo1 = properties.getProperty("pickupEmailTo1");
            String naemailTo2 = properties.getProperty("pickupEmailTo2");
            String naemailNameTo2 = properties.getProperty("pickupEmailTo2");
            String naemailFrom = properties.getProperty("pickupEmailFrom");
            String naemailNameFrom = properties.getProperty("pickupEmailNameFrom");


            msgBody = sb.toString();
            MimeMessage msg = new MimeMessage(session);
            msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
            msg.addRecipient(Message.RecipientType.TO,
                    new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
            msg.setSubject("Equipment Pickup Receipt");
            //msg.setText(msgBody, "utf-8", "html");
            MimeBodyPart mbp1 = new MimeBodyPart();
            mbp1.setText(msgBody);
            mbp1.setContent(msgBody, "text/html");
            mimeMultipart.addBodyPart(mbp1);
            mimeMultipart.addBodyPart(attachmentPart);
            msg.setContent(mimeMultipart);
            Transport.send(msg);
            System.out.println("E-mail sent with no errors.");

        } catch (AddressException e) {
            e.printStackTrace();
        } catch (MessagingException e) {
            e.printStackTrace();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    public byte[] getAsByteArray(URL url) throws IOException {
        URLConnection connection = url.openConnection();
        // Since you get a URLConnection, use it to get the InputStream
        InputStream in = connection.getInputStream();
        // Now that the InputStream is open, get the content length
        int contentLength = connection.getContentLength();

        // To avoid having to resize the array over and over and over as
        // bytes are written to the array, provide an accurate estimate of
        // the ultimate size of the byte array
        ByteArrayOutputStream tmpOut;
        if (contentLength != -1) {
            tmpOut = new ByteArrayOutputStream(contentLength);
        } else {
            tmpOut = new ByteArrayOutputStream(); // Pick some appropriate size
        }
        int offset = 0;
        int numRead = 0;
        byte[] buf = new byte[512];
        while (true) {
            int len = in.read(buf, offset, buf.length - offset); // added ", offset, buf.length-offset"
            if (len == -1) {
                break;
            }
            tmpOut.write(buf, offset, len);  // offset was 0
        }
        in.close();
        tmpOut.flush();
        tmpOut.close(); // No effect, but good to do anyway to keep the metaphor alive

        byte[] array = tmpOut.toByteArray();

        //Lines below used to test if file is corrupt
        FileOutputStream fos = new FileOutputStream("C:\\abc.pdf");
        fos.write(array);
        fos.close();

        return array;
    }

// Using Java IO
    public static void saveFileFromUrlWithJavaIO(String fileName, String fileUrl)
            throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        FileOutputStream fout = null;
        try {
            in = new BufferedInputStream(new URL(fileUrl).openStream());
            fout = new FileOutputStream(fileName);

            byte data[] = new byte[1024];
            int count;
            while ((count = in.read(data, 0, 1024)) != -1) {
                fout.write(data, 0, count);
            }
        } finally {
            if (in != null) {
                in.close();
            }
            if (fout != null) {
                fout.close();
            }
        }
    }

    private byte[] getDoc(String p_url) throws IOException {
        ByteArrayOutputStream baos = null;
        byte[] bytes = null;
        try {

            URL url = new URL(p_url);

            URLConnection urlc = url.openConnection();

            int length = urlc.getContentLength();

            InputStream in = urlc.getInputStream();

//    bytes = IOUtils.toByteArray(in);
//bytes = IOUtils.readFully(in, -1, false);

        } catch (Exception e) {
        }
        return bytes;
    }

// Using Java IO
    public static byte[] bytesFromUrlWithJavaIO(String fileUrl)
            throws MalformedURLException, IOException {
        BufferedInputStream in = null;
        ByteArrayOutputStream bout;
        byte[] returnBytes = null;
        bout = new ByteArrayOutputStream();
        try {
            in = new BufferedInputStream(new URL(fileUrl).openStream());
            /* byte data[] = new byte[1024*200];
             int count;
             System.out.println("Get byte array");
             while ((count = in.read(data, 0, 1024*200)) != -1) {
             bout.write(data, 0, count);
             }
             System.out.println("Perform flush");
             bout.flush(); */
            returnBytes = IOUtils.toByteArray(in);
        } finally {

            if (in != null) {
                in.close();
            }
            /* if (bout != null) {
             returnBytes = bout.toByteArray();
             bout.close(); }*/
        }
        FileOutputStream fos = new FileOutputStream("C:\\abc.pdf");
        fos.write(returnBytes);
        fos.flush();
        fos.close();


        return returnBytes;
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