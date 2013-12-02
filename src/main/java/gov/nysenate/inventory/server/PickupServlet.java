package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.util.HttpUtils;
import gov.nysenate.inventory.util.TransactionMapper;
import gov.nysenate.inventory.util.TransactionParser;

import java.io.BufferedInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.net.URLDecoder;
import java.sql.SQLException;
import java.util.logging.Logger;

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
@WebServlet(name = "Pickup", urlPatterns = {"/Pickup"})
public class PickupServlet extends HttpServlet
{

  String nafileext = ".pdf";

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
    Logger log = Logger.getLogger(PickupServlet.class.getName());

    String userFallback = null;
    Transaction pickup = new Transaction();
    String testingModeParam = null;
    DbConnect db = null;

    PrintWriter out = response.getWriter();
    db = HttpUtils.getHttpSession(request, response, out);

    db.ipAddr = request.getRemoteAddr();
    log.info(db.ipAddr + "|" + "Servlet Pickup : start");

    pickup = TransactionParser.parseTransaction(URLDecoder.decode(request.getParameter("pickup"), "UTF-8"));

    // TODO: what are these for?
    try {
      db.setLocationInfo(pickup.getOrigin());
    } catch (SQLException ex) {
      //Logger.getLogger(PickupServlet.class.getName()).log(Level.WARNING, null, ex);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }
    try {
      db.setLocationInfo(pickup.getDestination());
    } catch (SQLException ex) {
      //Logger.getLogger(PickupServlet.class.getName()).log(Level.WARNING, null, ex);
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    userFallback = request.getParameter("userFallback");
    System.out.println("After Parameters");

    try {
      testingModeParam = request.getParameter("testingMode");
      if (testingModeParam != null && testingModeParam.trim().length() > 0) {
        Logger.getLogger(PickupServlet.class.getName()).info(db.ipAddr + "|" + "****PARAMETER testingMode was set from client. Pickup.processRequest ");
      }
    } catch (Exception e) {
    }
    System.out.println("A)PickupItems = " + pickup.getPickupItems());

    TransactionMapper mapper = new TransactionMapper();
    int dbResponse = -1;
    try {
        dbResponse = mapper.insertPickup(db, pickup);
    } catch (SQLException e1) {
        e1.printStackTrace();
    } catch (ClassNotFoundException e) {
        // TODO Auto-generated catch block
        e.printStackTrace();
    }

    pickup.setNuxrpd(dbResponse);

    if (dbResponse > -1) {
      int emailReceiptStatus = 0;
      try {
        System.out.println("Before E-mail Receipt");
        HttpSession httpSession = request.getSession(false);        
        String user = (String) httpSession.getAttribute("user");
        String pwd = (String) httpSession.getAttribute("pwd");        

        EmailMoveReceipt emailMoveReceipt = new EmailMoveReceipt(user, pwd, "pickup" ,pickup);
        user = null;
        pwd = null;

        System.out.println("RIGHT Before E-mail Receipt");
        //emailReceiptStatus = emailMoveReceipt.sendEmailReceipt(pickup);
        Thread threadEmailMoveReceipt = new Thread(emailMoveReceipt);
        threadEmailMoveReceipt.start();
        //System.out.println("emailReceiptStatus:" + emailReceiptStatus);



//        if (emailReceiptStatus == 0) {
          //System.out.println("Database updated successfully");
          out.println("Database updated successfully");
/*        } else {
          System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + ").");
          out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + ").");
        }*/
      } catch (Exception e) {
          e.printStackTrace();
          System.out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).["+e.getMessage()+":"+e.getStackTrace()[0].toString()+"]");
          out.println("Database updated successfully but could not generate receipt (E-MAIL ERROR#:" + emailReceiptStatus + "-2).");
      }
    } else {
      out.println("Database not updated");
    }
    //System.out.println("(C) Servlet Pickup : end");
    log.info(db.ipAddr + "|" + "Servlet Pickup : end");
    out.close();
  }

  public byte[] getAsByteArray(URL url, String filename) throws IOException
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
    FileOutputStream fos = new FileOutputStream("C:\\" + filename);
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

    StringBuilder returnTime = new StringBuilder();
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
  
  public String insertTextInto(String allText, String whereText, String insertText)
  {
    if (allText == null || allText.length() == 0) {
      return allText;
    }

    String searchText = whereText.replaceAll("^", "");
    String replaceText = whereText.replaceAll("^", insertText);

    allText = allText.replaceAll(searchText, replaceText);

    return allText;
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
