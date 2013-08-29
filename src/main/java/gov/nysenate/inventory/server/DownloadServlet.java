/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import java.io.BufferedInputStream;
import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.net.URLEncoder;
import java.util.Properties;
import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.apache.log4j.Logger;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "DownloadServlet", urlPatterns = {"/DownloadServlet"})
public class DownloadServlet extends HttpServlet
{
  Logger log = Logger.getLogger(DownloadServlet.class.getName());
  Properties properties = new Properties();
  InputStream in;  
  String serverOS = "Windows"; // Default to Windows OS
  String pathDelimeter = "\\"; 

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
      /* TODO output your page here. You may use following sample code. */
      out.println("<!DOCTYPE html>");
      out.println("<html>");
      out.println("<head>");
      out.println("<title>Servlet DownloadServlet</title>");      
      out.println("</head>");
      out.println("<body>");
      out.println("<h1>Servlet DownloadServlet at " + request.getContextPath() + "</h1>");
      out.println("</body>");
      out.println("</html>");
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
          throws ServletException, IOException
  {
        serverOS = System.getProperty("os.name");
        if (serverOS.toUpperCase().indexOf("WINDOWS")==-1) {
            pathDelimeter = "/";
        }
        
        properties = new Properties();
        in = getClass().getClassLoader().getResourceAsStream("config.properties");
        String filename = "";
        String downloadPath = "";
        try {
            properties.load(in);
            downloadPath = properties.getProperty("downloadPath");
  
        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DbConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }
    
    BufferedInputStream fin = null;
    BufferedOutputStream outs = null;

    try{
        filename = request.getParameter("filename");
        String filePath;
        if (downloadPath==null||downloadPath.trim().length()==0) {
          filePath = filename;
        }
        else {
          filename = filename.replace("/", "").replace("\\", "");
          
          /*
           * There should not be any Leading or Trailing Spaces in the FilePath.
           */ 
          downloadPath = downloadPath.trim();
          if (downloadPath.endsWith(pathDelimeter)){
            filePath = downloadPath.trim()+filename;
          }
          else {
            filePath = downloadPath+pathDelimeter+filename;
          }
        }
        System.out.println("File Path:"+filePath);
        File file = new File(filePath);
        
        String browser = request.getHeader("User-Agent");
        String mimetype = request.getSession().getServletContext().getMimeType(file.getName());

        if (browser != null && browser.indexOf("MSIE") > -1){
            filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        }else if (browser != null && browser.indexOf("Chrome") > -1){
            filename = URLEncoder.encode(filename, "UTF-8").replaceAll("\\+", "%20");
        }else if (browser != null && browser.indexOf("Opera") > -1) {
            filename = new String(filename.getBytes("UTF-8"), "8859_1");
        }else{
            filename = new String(filename.getBytes("UTF-8"), "8859_1");
        }

        response.setHeader("Content-Length", "" + file.length());
        response.setHeader("Content-Transfer-Encoding", "binary");
        response.setHeader("Content-Disposition", "attachment; filename=\"" + filename + "\";");
        response.setHeader("Content-Type", "application/vnd.android.package-archive");

        if (file.isFile() && file.length() > 0){
            int read = 0;
            byte b[] = new byte[1024];
            fin = new BufferedInputStream(new FileInputStream(file));
            outs = new BufferedOutputStream(response.getOutputStream());

            while ((read = fin.read(b)) != -1) {
                outs.write(b,0,read);
            }
        }
    }catch (Exception e) {
        e.printStackTrace();

    }finally {
        try{
            if(outs!=null) outs.close();
        }catch(Exception e){
            e.printStackTrace();
        }

        try{
            if(fin!=null) fin.close();
        }catch(Exception e){
            e.printStackTrace();
        }
    }  
    //processRequest(request, response);
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
