/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package gov.nysenate.inventory.server;

import static gov.nysenate.inventory.server.DbConnect.log;

import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.IOException;
import java.io.InputStream;
import java.io.PrintWriter;
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.zip.ZipEntry;
import java.util.zip.ZipFile;

import javax.servlet.ServletException;
import javax.servlet.annotation.WebServlet;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import com.google.gson.JsonObject;

/**
 *
 * @author senateuser
 */
@WebServlet(name = "CheckAppVersion", urlPatterns = {"/CheckAppVersion"})
public class CheckAppVersion extends HttpServlet {
    
    String APKManifest;
    String downloadPath;
    String webFileUrl;
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
            throws ServletException, IOException {
        serverOS = System.getProperty("os.name");
        if (serverOS.toUpperCase().indexOf("WINDOWS")==-1) {
            pathDelimeter = "/";
        }
 
        PrintWriter out = response.getWriter();
        response.setContentType("text/html");
        try {
            Properties properties = new Properties();
            InputStream in =  this.getClass().getClassLoader().getResourceAsStream("config.properties");
            try {            
                 properties.load(in);
             } catch (IOException ex) {
                Logger.getLogger(PickupServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
            
            try {
               downloadPath = properties.getProperty("downloadPath").trim();           
            }
            catch (Exception e) {
              downloadPath = "";           
              e.printStackTrace();
            }
            
            try {
               webFileUrl = properties.getProperty("webFileUrl");
            }
            catch (Exception e) {
              webFileUrl = "";
              e.printStackTrace();
            }
            
            String appName = request.getParameter("appName");
            if (!appName.toLowerCase().endsWith(".zip") && !appName.toLowerCase().endsWith(".apk")) {
                // Strings are immutable but won't cause any major performance issues (at the moment)
                  appName = appName+".apk";
            }
            StringBuilder localFile = new StringBuilder();
            localFile.append(downloadPath);
            if (!downloadPath.trim().endsWith(pathDelimeter)) {
              localFile.append(pathDelimeter);
            }
            localFile.append(appName);

            StringBuilder webFile = new StringBuilder();
            webFile.append(webFileUrl);
            webFile.append(appName);
            
            APKManifest = parseAPK(localFile.toString(), "AndroidManifest.xml");
               
            //send a JSON response with the app Version and file URI
            int APKVersion = -1;
            String APKVersionName = "";
            if (APKManifest!=null && APKManifest.trim().length()>0) {
                APKVersion = getAPKVersionNumber();
                APKVersionName = this.getAPKVersionName();
            }
            
            JsonObject myObj = new JsonObject();
            if (APKVersion<0) {
                myObj.addProperty("success", false);
            }
            else {
                myObj.addProperty("success", true);
            }
            myObj.addProperty("latestVersion", APKVersion);
            myObj.addProperty("latestVersionName", APKVersionName);
            myObj.addProperty("appURI", webFile.toString());

            out.println(myObj.toString());
        }
        finally  {
                out.close();
         }
    }  
    
    private String parseAPK(String fileName, String zipEntry) {
       InputStream is = null;
       ZipFile zip = null;
       File f = new File(fileName);
       int fileSize = (int)f.length();
       f = null;
       System.gc();
       DbConnect db = new DbConnect();
               
       if (fileName.endsWith(".apk") || fileName.endsWith(".zip")) {
            try {
                zip = new ZipFile(fileName);
            } catch (IOException ex) {
                  log.info(db.ipAddr + "|" + "***WARNING: "+fileName+" not found on Server. The Server cannot check for the latest version of the App or allow the App to be downloaded without the file on the server.");                
//                Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                return "";
            }
            ZipEntry mft = zip.getEntry(zipEntry);
            try {
                is = zip.getInputStream(mft);
            } catch (IOException ex) {
                Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                return "";
            }
                       
            } else {
            try {
                is = new FileInputStream(fileName);
            } catch (FileNotFoundException ex) {
                Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                return "";
            }
                }
               
                byte[] buf = new byte[fileSize];
            try {
                 int bytesRead = is.read(buf);
                } catch (IOException ex) {
                    Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                    return "";
            }
            try {
                 is.close();
                } catch (IOException ex) {
                    Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                    return "";
               }
                if (zip != null) {
                    try {
                        zip.close();
                    } catch (IOException ex) {
                        Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, ex.getMessage(), ex);
                        return "";
                }
                }
                String xml = AndroidXMLDecompress.decompressXML(buf);
                return xml;
    }
    
    private int getAPKVersionNumber() {
        try {
            String lookFor = "<manifest versionCode=\"resourceID ";
            int start = APKManifest.indexOf(lookFor)+lookFor.length();
            int end = APKManifest.indexOf("\"", start+1);
            String rawVersion = APKManifest.substring(start, end).trim();
            int xPos = rawVersion.indexOf("x");
            String hexVersion = rawVersion.substring(xPos+1);
            int version = Integer.parseInt(hexVersion, 16);
            return version;       
        }
        catch (Exception e) {
            Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, e.getMessage(), e);
            return -1;
        }
    }
   
     private String getAPKVersionName() {
         try {
            String lookFor = "versionName=\"";
            int start = APKManifest.indexOf(lookFor)+lookFor.length();
            int end = APKManifest.indexOf("\"", start+1);
            if (start==-1) {
                return "";
            }
            String versionName = APKManifest.substring(start, end).trim();
            return versionName;     
         }
        catch (Exception e) {
            Logger.getLogger(TestGson.class.getName()).log(Level.WARNING, e.getMessage(), e);
            return "";
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
