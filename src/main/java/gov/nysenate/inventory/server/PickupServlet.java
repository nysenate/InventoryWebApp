package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Pickup;

import java.io.BufferedInputStream;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.activation.DataHandler;
import javax.activation.DataSource;
import javax.mail.Message;
import javax.mail.MessagingException;
import javax.mail.Session;
import javax.mail.Transport;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;
import javax.mail.internet.MimeBodyPart;
import javax.mail.internet.MimeMessage;
import javax.mail.internet.MimeMultipart;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import javax.servlet.http.HttpSession;

import org.apache.commons.io.IOUtils;

/**
 *
 * @author Patil
 */
@WebServlet(name = "Pickup", urlPatterns = {"/Pickup"})
public class PickupServlet extends HttpServlet
{
  DbConnect db  = null;
  String userFallback = null;
  Pickup pickup = new Pickup();
  Employee currentEmployee = null;
  String testingModeParam =  null;
  
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
    Logger log = Logger.getLogger(PickupServlet.class.getName());
    db = checkHttpSession(request, out);
    db.ipAddr = request.getRemoteAddr();
    log.info(db.ipAddr + "|" + "Servlet Pickup : start");

    pickup.getOrigin().setCdLoc(request.getParameter("originLocation"));
    pickup.getOrigin().setCdLocType(request.getParameter("cdloctypefrm"));
    pickup.getDestination().setCdLoc(request.getParameter("destinationLocation"));
    pickup.getDestination().setCdLocType(request.getParameter("cdloctypeto"));
    if (request.getParameterValues("barcode[]") != null) {
      pickup.setPickupItems(request.getParameterValues("barcode[]"));
    }
    pickup.setNaPickupBy(request.getParameter("NAPICKUPBY"));
    pickup.setNuxrRelSign(request.getParameter("NUXRRELSIGN"));
    pickup.setNaReleaseBy(request.getParameter("NARELEASEBY").replaceAll("'", "''"));
    pickup.setComments(request.getParameter("DECOMMENTS").replaceAll("'", "''"));
    userFallback =  request.getParameter("userFallback");
    
    try {
      testingModeParam = request.getParameter("testingMode");
      if (testingModeParam!=null && testingModeParam.trim().length()>0) {
        Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER testingMode was set from client. Pickup.processRequest ");
      }
    }
    catch (Exception e) {

    }

    log.info("PickupItems = " + pickup.getPickupItems()); // TODO: for testing.
    int dbResponse = db.invTransit(pickup,userFallback);
    pickup.setNuxrpd(dbResponse);

    if (dbResponse > -1) {
      sendEmail(pickup);
      System.out.println("INV TRANSIT UPDATED CORRECTLY");
      out.println("Database updated sucessfully");
    } else {
      System.out.println("INV TRANSIT FAILED TO UPDATE");
      out.println("Database not updated");
    }
    log.info(db.ipAddr + "|" + "Servlet Pickup : end");
    out.close();
  }

  public void sendEmail(Pickup pickup)
  {
    sendEmail(pickup.getNaPickupBy(), pickup.getOrigin().getCdLoc(), pickup.getDestination().getCdLoc(), pickup.getNuxrpd());
  }

  @SuppressWarnings("empty-statement")
  public void sendEmail(String NAPICKUPBY, String originLocation, String destinationLocation, final int nuxrpd)
  {
    String naemployeeTo = "";
    String msgBody = "";
    byte[] attachment = null;
    Properties properties = new Properties();
    InputStream in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
    try {
      properties.load(in);
    } catch (IOException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    String smtpServer = properties.getProperty("smtpServer");
    final String pickupReceiptURL = properties.getProperty("pickupReceiptURL");

    Properties props = new Properties();
    props.setProperty("mail.smtp.host", smtpServer);
    Session session = Session.getDefaultInstance(props, null);
    StringBuilder sb = new StringBuilder();
    String naemailTo1 = null;
    String naemailNameTo1 = null;
    String naemailTo2 = null; 
    String naemailNameTo2 = null; 
    properties.getProperty("pickupEmailTo2");
    String naemailFrom = null; 
    naemailFrom = properties.getProperty("pickupEmailFrom");
    String naemailNameFrom = null; 
    naemailNameFrom = properties.getProperty("pickupEmailNameFrom");
    
    try {
        naemailTo1 = properties.getProperty("pickupEmailTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
        naemailNameTo1 = properties.getProperty("pickupEmailNameTo1");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo1 COULD NOT BE PROCESSED Pickup.processRequest ");
    }

    try {
        naemailTo2 = properties.getProperty("pickupEmailTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    try {
        naemailNameTo1 = properties.getProperty("pickupEmailNameTo2");   
    }
    catch (NullPointerException e) {
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 NOT FOUND Pickup.processRequest ");
    }
    catch (Exception e) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****PARAMETER pickupEmailNameTo2 COULD NOT BE PROCESSED Pickup.processRequest ");
    }
    
    // Get the employee who signed the Release 
    currentEmployee = db.getEmployeeWhoSigned(pickup.getNuxrRelSign(), false, userFallback);
    currentEmployee.setEmployeeNameOrder(currentEmployee.FIRST_MI_LAST_SUFFIX);
    
    boolean testingMode = false;
    
    /*
     *  If either E-mail to field is filled, then the server is meant to e-mail that specific user
     * instead of the user that should be e-mailed. This would mean that the server is in testing mode.
     */
    
    if (testingModeParam!=null && testingModeParam.trim().length()>0) {
      if (testingModeParam.toUpperCase().indexOf("T")>-1) {
        testingMode = true;
        Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****testingModeParam has a T, so Testing Mode is set to TRUE Pickup.processRequest ");
      }
      else {
        testingMode = false;
      }
    }
    else if ((naemailTo1 != null && naemailTo1.trim().length()>0) || (naemailTo2 != null && naemailTo2.trim().length()>0)) {
      testingMode = true;
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****At least one of the E-mail To Parameters in Server Properties was set, so Testing Mode is set to TRUE Pickup.processRequest ");
    }
    
    if (testingMode) {
      sb.append("<b>TESTINGMODE</b>: E-mail under normal circumstances would have been sent to:");
      sb.append(currentEmployee.getNaemail());
      sb.append("<br /><br />");
    }
    
    sb.append("Dear ");
    sb.append(currentEmployee.getEmployeeName());
    sb.append(",");
    sb.append("<br/><br/> Equipment was picked up by <b>" + NAPICKUPBY + "</b> from <b>" + originLocation + "</b> with the destination of <b>" + destinationLocation + "</b>");
    sb.append("<br /><br />To view Equipment Pickup Receipt, please open the PDF attachment included in this e-mail.");
  
    try {
      attachment = bytesFromUrlWithJavaIO(pickupReceiptURL + nuxrpd); // +"&destype=CACHE&desformat=PDF
      //saveFileFromUrlWithJavaIO(this.nuxrpd+".pdf", );
      System.out.println("ATTACHMENT SIZE:" + attachment.length + " " + ((attachment.length) / 1024.0) + "KB");
    } catch (MalformedURLException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    } catch (IOException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    if (attachment == null) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was null Pickup.processRequest ");
    }
    else if (attachment.length==0) {
      Logger.getLogger(PickupServlet.class.getName()).warning(db.ipAddr + "|" + "****ATTACHMENT was a ZERO LENGTH Pickup.processRequest ");
    }
    
    MimeMultipart mimeMultipart = new MimeMultipart();
    MimeBodyPart attachmentPart = new MimeBodyPart();
    try {
      attachmentPart.setDataHandler(
              new DataHandler(
              new DataSource()
      {
        @Override
        public String getContentType()
        {
          return "application/pdf";
        }

        @Override
        public InputStream getInputStream() throws IOException
        {
          return new ByteArrayInputStream(bytesFromUrlWithJavaIO(pickupReceiptURL + nuxrpd));
        }

        @Override
        public String getName()
        {
          return "pickup_receipt.pdf";
        }

        @Override
        public OutputStream getOutputStream() throws IOException
        {
          return null;
        }
      }));
    } catch (MessagingException ex) {
      Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
    }

    try {
      in = this.getClass().getClassLoader().getResourceAsStream("config.properties");
      properties.load(in);
 
      msgBody = sb.toString();
      MimeMessage msg = new MimeMessage(session);
      System.out.println("EMAILING FROM:" + naemailFrom + ":" + naemailNameFrom);
      msg.setFrom(new InternetAddress(naemailFrom, naemailNameFrom));
      if (testingMode) {
          System.out.println("TESTINGMODE Would have sent (BUT DID NOT) TO:" + currentEmployee.getNaemail() + " (" + currentEmployee.getEmployeeName()+")");
        if (naemailTo1!=null && naemailTo1.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + naemailTo1 + ":" + naemailNameTo1);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo1, naemailNameTo1));  //naemailTo, naemployeeTo
        }
        if (naemailTo2!=null && naemailTo2.trim().length()>0){
          System.out.println("TESTINGMODE EMAILING TO:" + naemailTo2 + ":" + naemailNameTo2);
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(naemailTo2, naemailNameTo2));  //naemailTo, naemployeeTo
        }
      }
      else {
          msg.addRecipient(Message.RecipientType.TO,
            new InternetAddress(currentEmployee.getNaemail(), currentEmployee.getEmployeeName()));  //naemailTo, naemployeeTo
        
      }
      
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

  public byte[] getAsByteArray(URL url) throws IOException
  {
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
          throws MalformedURLException, IOException
  {
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

  public String convertTime(long time)
  {
    long secDiv = 1000;
    long minDiv = 1000 * 60;
    long hourDiv = 1000 * 60 * 60;
    long minutes = time % hourDiv;
    long seconds = minutes % minDiv;
    int hoursConverted = (int) (time / hourDiv);
    int minutesConverted = (int) (minutes / minDiv);
    int secondsConverted = (int) (seconds / secDiv);

    StringBuffer returnTime = new StringBuffer();
    if (hoursConverted > 0) {
      returnTime.append("Hours:");
      returnTime.append(hoursConverted);
      returnTime.append(" ");
    }
    if (hoursConverted > 0 || minutesConverted > 0) {
      returnTime.append("Minutes:");
      returnTime.append(minutesConverted);
      returnTime.append(" ");
    }
    returnTime.append("Seconds:");
    returnTime.append(secondsConverted);
    returnTime.append(" ");

    return returnTime.toString();
  }

  private byte[] getDoc(String p_url) throws IOException
  {
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
          throws MalformedURLException, IOException
  {
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

  public DbConnect checkHttpSession(HttpServletRequest request, PrintWriter out)
  {
    HttpSession httpSession = request.getSession(false);
    DbConnect db;
    String userFallback = "";
    if (httpSession == null) {
      System.out.println("****SESSION NOT FOUND");
      db = new DbConnect();
      Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****SESSION NOT FOUND Pickup.processRequest ");
      userFallback = request.getParameter("userFallback");
      out.println("Session timed out");
    } else {
      long lastAccess = (System.currentTimeMillis() - httpSession.getLastAccessedTime());
      System.out.println("SESSION FOUND!!!! LAST ACCESSED:" + this.convertTime(lastAccess));
      String user = (String) httpSession.getAttribute("user");
      String pwd = (String) httpSession.getAttribute("pwd");
      System.out.println("--------USER:" + user);
      db = new DbConnect(user, pwd);

    }
    return db;
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
          throws ServletException, IOException
  {
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
          throws ServletException, IOException
  {
    processRequest(request, response);
  }

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
