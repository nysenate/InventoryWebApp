package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Employee;
import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;

import gov.nysenate.inventory.model.Commodity;

import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.sql.Blob;
import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Properties;

import javax.imageio.ImageIO;
import javax.mail.internet.AddressException;
import javax.mail.internet.InternetAddress;

import oracle.sql.BLOB;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import gov.nysenate.inventory.model.InvSerialNumber;
import gov.nysenate.inventory.model.LoginStatus;
import gov.nysenate.inventory.model.SimpleListItem;
import gov.nysenate.inventory.util.DbManager;

import java.awt.Graphics2D;
import java.math.BigDecimal;
import java.util.Arrays;
import java.util.Calendar;
import java.util.Date;

/**
 *
 * @author Patil
 */
public class DbConnect extends DbManager {

    public String ipAddr = "";
    static Logger log = Logger.getLogger(DbConnect.class.getName());
    static private Properties properties;
    private String userName,  password;
    final int RELEASESIGNATURE = 3001, ACCEPTBYSIGNATURE = 3002;
    private String dbaName = "";
   
    public DbConnect() {
        loadProperties();
        userName = properties.getProperty("user");
        password = properties.getProperty("password");
    }

    public DbConnect(String user, String pwd) {
        loadProperties();
        userName = user;
        password = pwd;
    }

    private void loadProperties() {
        if (properties == null) {
            properties = new Properties();
            InputStream in = getClass().getClassLoader().getResourceAsStream("config.properties");
            try {
                properties.load(in);
            } catch (IOException e) {
                log.error("Error loading properties: ", e);
            }
        }
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Main function for testing other functions
     *----------------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
        log.info("main function ");
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to establish and return database connection 
     *----------------------------------------------------------------------------------------------------*/

    public Connection getDbConnection() throws ClassNotFoundException, SQLException {
        log.info("getDbConnection() begin ");
        Connection conn = null;
        // Get the connection string, user name and password from the properties file
        String connectionString = properties.getProperty("connectionString");
        
        if (connectionString!=null && connectionString.contains(":")) {
            String[] connectStrings = connectionString.split(":");
            this.dbaName = connectStrings[connectStrings.length-1];
        }

        Class.forName("oracle.jdbc.driver.OracleDriver");
        conn = DriverManager.getConnection(connectionString, userName, password);

        log.info("getDbConnection() end");
        return conn;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to check if user name and password matches
     *----------------------------------------------------------------------------------------------------*/

    public LoginStatus validateUser() {
        log.info(this.ipAddr + "|" + "validateUser() begin : user= " + userName + " & pwd= " + password);
        LoginStatus loginStatus = new LoginStatus();
        loginStatus.setNauser(userName);
        Connection conn = null;
        try {
            conn = getDbConnection();
            loginStatus.setNustatus(loginStatus.VALID);
            loginStatus.setSQLErrorCode(-1);
        } catch (ClassNotFoundException ex) {
            loginStatus.setNustatus(loginStatus.INVALID);
            loginStatus.setDestatus("!!ERROR: ClassNotFoundException in validUser when trying to validate username/password. Please contact STSBAC.");
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.ERROR, "Handled Error " + ex.getMessage());

            System.out.println(ex.getMessage());
            log.info(this.ipAddr + "|" + "validateUser() loginStatus= " + loginStatus);
            log.info(this.ipAddr + "|" + "validateUser() end ");
            int sqlErr = ex.getErrorCode();
            loginStatus.setSQLErrorCode(sqlErr);
            if (sqlErr == 1017) {  // Invalid Username/Password
                loginStatus.setNustatus(loginStatus.INVALID_USERNAME_OR_PASSWORD);
                loginStatus.setDestatus("!!ERROR: Invalid Username and/or password.");
            } else {
                loginStatus.setNustatus(loginStatus.INVALID);
                loginStatus.setDestatus("!!ERROR: " + ex.getMessage() + ". PLEASE CONTACT STS/BAC.");
            }
           return loginStatus;
        } finally {
            closeConnection(conn);
        }
             
        return loginStatus;
    }
    
  private Date getOnlyDate(Date fecha) {
    Date res = fecha;
    Calendar calendar = Calendar.getInstance();

    calendar.setTime( fecha );
    calendar.set(Calendar.HOUR_OF_DAY, 0);
    calendar.set(Calendar.MINUTE, 0);
    calendar.set(Calendar.SECOND, 0);
    calendar.set(Calendar.MILLISECOND, 0);

    res = calendar.getTime();

    return res;
  }    
       
 /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to check user access
     *----------------------------------------------------------------------------------------------------*/

    public LoginStatus securityAccess(String user, String defrmint, LoginStatus loginStatus) {
        log.info(this.ipAddr + "|" + "securityAccess() begin : user= " + user + " & defrmint= " + defrmint);
        System.out.println(this.ipAddr + "|" + "securityAccess() begin : user= " + user + " & defrmint= " + defrmint);
        loginStatus.setNustatus(loginStatus.NO_ACCESS);
        loginStatus.setDestatus("!!ERROR: No security clearance has been given to "+user+" for this process. Please contact STSBAC.");
        if (user==null||user.trim().length()==0) {
          loginStatus.setDestatus( "!!ERROR: Server needs username parameter to be passed correctly.("+user+") is not a valid value. Please contact STSBAC.");
          return loginStatus;
        }
        if (defrmint==null||defrmint.trim().length()==0) {
          loginStatus.setDestatus( "!!ERROR: Server needs screen name parameter to be passed correctly.("+defrmint+") is not a valid value. Please contact STSBAC.");
          return loginStatus;
        }
        Date dtpasswdexp = null;
        String commodityCode = ""; //TODO
        String query = " SELECT CDSECLEVEL"
                + " FROM im86modmenu "
                + " WHERE nauser = ?"
                + "   AND defrmint = ?"
                + "   AND cdstatus = 'A'";
               
        PreparedStatement pstmt = null;
        ResultSet result = null;
        Connection conn = null;
       try {
            conn = getDbConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.trim().toUpperCase());
            pstmt.setString(2, defrmint.trim().toUpperCase());
            result = pstmt.executeQuery();
            Date dtToday = this.getOnlyDate(new Date());
            int passwordExpireWarning = 7;

            while (result.next()) {
                System.out.println (user.trim().toUpperCase()+" HAS CLEARANCE");
                loginStatus.setNustatus(loginStatus.VALID);
                loginStatus.setCdseclevel(result.getString(1));
            }
            query = "Select dtpasswdexp From Im86orgid where nauser = ?";
            log.info("Select dtpasswdexp From Im86orgid where nauser = ?");
            System.out.println("Select dtpasswdexp From Im86orgid where nauser = ?");
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.trim().toUpperCase());
            result = pstmt.executeQuery();
            while (result.next()) {
               dtpasswdexp = this.getOnlyDate(result.getDate(1));
               loginStatus.setDtpasswdexp(dtpasswdexp);
               log.info("dtpasswdexp:"+dtpasswdexp);
               System.out.println("dtpasswdexp:"+dtpasswdexp);
            }
            
            if (dtpasswdexp.before(dtToday)) {
              loginStatus.setNustatus(loginStatus.PASSWORD_EXPIRED);
              loginStatus.setDestatus("!!ERROR: the password has expired in SFMS.");
            }
            else if (dtpasswdexp.before(new Date(dtToday.getTime() + (passwordExpireWarning * 24 * 3600 * 1000))) ) {
                int daysLeft = (int)((dtpasswdexp.getTime() - dtToday.getTime())/ (24 * 3600 * 1000));
              loginStatus.setNustatus(loginStatus.PASSWORD_EXPIRES_SOON);
              loginStatus.setDestatus("***WARNING: Your password will expire within " +daysLeft+ " days. Do you want to change it?");
            }
        }
        catch (SQLException e) {
            log.error("SQL Exception in securityAccess(): ", e);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
        finally {
            closeResultSet(result);
            closeStatement(pstmt);
            closeConnection(conn);
        }

        //System.out.println ("SECURITY RETURNS "+loginStatus+" FOR DEFRMINT "+defrmint);
        return loginStatus;
    }        
    
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return details of given barcode (item details)
     *----------------------------------------------------------------------------------------------------*/

    public String getDetails(String barcodeNum, String userFallback) {
        log.info(this.ipAddr + "|" + "getDetails() begin : barcodeNum= " + barcodeNum);
        if ((Integer.parseInt(barcodeNum) < 0)) {
            System.out.println("Error in DbConnect.getDetails() - Barcode Number Not Valid");
            log.error(this.ipAddr + "|" + "Error in DbConnect.getDetails() - Barcode Number Not Valid");
            throw new IllegalArgumentException("Invalid Barcode Number");
        }
        String details = null;
        Connection conn = null;
        CallableStatement cs = null;
        try {
            conn = getDbConnection();
            cs = conn.prepareCall("{?=call INV_APP.GET_INV_DETAILS(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, barcodeNum);
            cs.executeUpdate();
            details = cs.getString(1);
            //System.out.println(details);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeStatement(cs);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getDetails() details = " + details);
        log.info(this.ipAddr + "|" + "getDetails() end ");
        return details;
    }


    public String getItemCommodityCode(String barcode, String userFallback) {
        log.info(this.ipAddr + "|" + "getItemCommodityCode() begin : barcodeNum= " + barcode);

        String commodityCode = ""; //TODO
        String query = "SELECT fm12comxref.cdcommodity "
                + "FROM fm12comxref, fd12issue, fm12senxref "
                + "WHERE fm12comxref.nuxrefco = fd12issue.nuxrefco "
                + "AND fd12issue.nuxrefsn = fm12senxref.nuxrefsn "
                + "AND fm12senxref.nusenate like ? ";
        PreparedStatement pstmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, barcode);
            result = pstmt.executeQuery();

            while (result.next()) {
                commodityCode = result.getString(1);
            }
        }
        catch (SQLException e) {
            log.error("SQL Exception in getItemCommodityCode(): ", e);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        }
        finally {
            closeResultSet(result);
            closeStatement(pstmt);
            closeConnection(conn);
        }
        return commodityCode;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return details related to given location code( Address, type etc) 
     *----------------------------------------------------------------------------------------------------*/
    public String getInvLocDetails(String locCode, String userFallback) {
        log.info(this.ipAddr + "|" + "getInvLocDetails() begin : locCode= " + locCode);
        if (locCode.isEmpty() || locCode == null) {
            log.info(this.ipAddr + "|" + "Invalid location Code " + locCode);
            throw new IllegalArgumentException("Invalid location Code");
        }
        String details = null;
        CallableStatement cs = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            cs = conn.prepareCall("{?=call INV_APP.GET_INV_LOC_CODE(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, locCode);
            cs.executeUpdate();
            details = cs.getString(1);
            //System.out.println(details);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeStatement(cs);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getInvLocDetails() end ");
        return details;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the items at a given location codes 
     *----------------------------------------------------------------------------------------------------*/
   
    public ArrayList getLocationItemList(String locCode, String userFallback) {
        log.info(this.ipAddr + "|" + "getLocationItemList() begin : locCode= " + locCode);
        if (locCode.isEmpty() || locCode == null) {
            throw new IllegalArgumentException("Invalid location Code");
        }

        ArrayList<VerList> itemList = new ArrayList<VerList>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF, B.CDLOCATTO, DECODE(b.cdstatus, 'I', b.cdstatus, c.cdstatus) cdstatus, b.cdloctypeto "
                    + " FROM FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C"
                    + " WHERE A.CDSTATUS='A'"
                    + " AND b.cdstatus = 'A'"
                    + " AND c.cdstatus = 'A'"
                    + " AND A.NUXREFSN=B.NUXREFSN"
                    + " AND B.NUXREFCO=C.NUXREFCO"
                    + " AND b.cdlocatto = '" + locCode + "'";

            result = stmt.executeQuery(qry);
            while (result.next()) {

                VerList vl = new VerList();
                vl.NUSENATE = result.getString(1);
                vl.CDCATEGORY = result.getString(2);
                vl.DECOMMODITYF = result.getString(3);
                vl.CDLOCATTO = result.getString(4);
                vl.CDSTATUS = result.getString(5);
                vl.CDLOCTYPETO = result.getString(6);
                itemList.add(vl);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getLocationItemList() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getLocationItemList() end");
        return itemList;
    }
                
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the commodity codes based on the keywords
     *----------------------------------------------------------------------------------------------------*/  
   
    public ArrayList getCommodityList(String keywords, String userFallback) {
        log.info(this.ipAddr + "|" + "getLocationItemList() begin : locCode= " + keywords);
        if (keywords.isEmpty() || keywords == null) {
            throw new IllegalArgumentException("No Keywords Found");
        }

        ArrayList<Commodity> commodityList = new ArrayList<Commodity>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            //  String loc_code;
            String qry =  " WITH results AS " +
                          " (SELECT a.nuxrefco, a.cdcommodity, b.cdissunit, b.cdcategory, b.cdtype, b.decommodityf, c.keyword" +
                          " FROM fm12comxref a, fm12commodty b, (select column_value keyword" +
                          " FROM TABLE(split(UPPER('"+keywords+"')))) c  " +
                          " WHERE a.nuxrefco = b.nuxrefco " +
                          " AND a.cdstatus = 'A'" +
                          " AND b.cdstatus = 'A'" +
                          " AND b.decommodityf LIKE '%'||c.keyword||'%')" +
                          " SELECT count(*) nucnt, a.decommodityf, a.nuxrefco, a.cdcommodity, a.cdissunit, a.cdcategory, a.cdissunit, a.cdtype " +
                          " FROM results a" +
                          " GROUP BY  a.nuxrefco, a.cdcommodity, a.cdissunit, a.cdcategory, a.cdissunit, a.cdtype, a.decommodityf " +
                          " ORDER BY 1 DESC, 2";
            result = stmt.executeQuery(qry);
            while (result.next()) {
                Commodity commodity = new Commodity();
                commodity.setNucnt(result.getString(1));
                commodity.setDecommodityf(result.getString(2));
                commodity.setNuxrefco(result.getString(3));
                commodity.setCdcommodity(result.getString(4));
                commodity.setCdcategory(result.getString(6));
                commodity.setCdtype(result.getString(8));
                commodityList.add(commodity);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getCommodityList() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getCommodityList() end");
        return commodityList;
    }    

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/
    public ArrayList<Location> getLocCodes() {
        log.info(this.ipAddr + "|" + "getLocCodes(String natype) begin)");

        String qry = "SELECT DISTINCT cdloctype, cdlocat, adstreet1, adcity, adzipcode, adstate " +
                "FROM sl16location a where a.cdstatus='A' ORDER BY cdlocat, cdloctype";

        ArrayList<Location> locations = new ArrayList<Location>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            result = stmt.executeQuery(qry);
            while (result.next()) {
                Location loc = new Location();
                loc.setCdloctype(result.getString(1));
                loc.setCdlocat(result.getString(2));
                loc.setAdstreet1(result.getString(3));
                loc.setAdcity(result.getString(4));
                loc.setAdzipcode(result.getString(5));
                loc.setAdstate(result.getString(6));
                locations.add(loc);
            }
        } catch (SQLException e) {
            log.error("Error in getLocCodes: ", e);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getLocCodes() end");
        return locations;
    }

    
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all pickups
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getPickupSearchByList(String userFallback) {
        ArrayList<SimpleListItem> pickupList = new ArrayList<SimpleListItem>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();

            String qry;
            qry = "select distinct 'CDLOCATTO' natype ,cdlocat||'-'||cdloctype||': '||adstreet1 navalue from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatto FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A'))"
                + "UNION ALL "
                + "select distinct 'CDLOCATFROM' natype ,cdlocat||'-'||cdloctype||': '||adstreet1 navalue from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatfrom FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A'))"
                + "UNION ALL "
                + "select distinct 'DTTXNORIGIN' natype, TO_CHAR(dttxnorigin, 'MM/DD/RRRR- Day') navalue  FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A')"
                + "UNION ALL "
                + "select distinct 'NAPICKUPBY' natype, napickupby navalue  FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A')"
                + " ORDER BY  1, 2";

            result = stmt.executeQuery(qry);
            while (result.next()) {

                String natype = result.getString(1);
                String navalue = result.getString(2);
                SimpleListItem simpleListItem = new SimpleListItem();
                simpleListItem.setNatype(natype);
                simpleListItem.setNavalue(navalue);
                pickupList.add(simpleListItem);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return pickupList;
    }

   /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all Serial#s
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getNuSerialList(String nuserialFilter, int numaxResults, String userFallback) {
        ArrayList<InvSerialNumber> invSerialList = new ArrayList<InvSerialNumber>();
        Statement stmt = null;
        ResultSet resultCnt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            int nucnt = 0;
            
            String gryCnt ="SELECT COUNT(DISTINCT b.nuserial)\n" +
                "FROM fm12senxref a, fd12issue b, fm12commodty c, fm12comxref e\n" +
                "WHERE a.nuxrefsn = b.nuxrefsn\n" +
                "AND b.nuserial LIKE '"+nuserialFilter.toUpperCase()+"%'\n" +
                "AND b.nuxrefco = c.nuxrefco\n" +
                "AND e.nuxrefco = c.nuxrefco\n" +
                "AND a.cdstatus = 'A' \n" +
                "AND c.cdstatus = 'A'\n" +
                "AND e.cdstatus = 'A'\n" +
                "AND b.nuserial IN (SELECT a2.nuserial \n" +
                "FROM fd12issue a2 \n" +
                "GROUP BY a2.nuserial HAVING COUNT(*)=1)\n" +
                "ORDER BY b.nuserial";

            resultCnt = stmt.executeQuery(gryCnt);
            
            while (resultCnt.next()) {
                nucnt = resultCnt.getInt(1);
            }
             log.info(this.ipAddr + "|" + "getNuSerialList() gryCnt ("+nucnt+") :"+gryCnt);
            System.out.println ( "getNuSerialList() gryCnt ("+nucnt+") :"+gryCnt);
            String qry;
            
            qry ="SELECT a.nuxrefsn, b.nuserial, a.nusenate, e.cdcommodity, c.decommodityf\n" +
                "FROM fm12senxref a, fd12issue b, fm12commodty c, fm12comxref e\n" +
                "WHERE a.nuxrefsn = b.nuxrefsn\n" +
                "AND b.nuserial LIKE '"+nuserialFilter.toUpperCase()+"%'\n" +                    
                "AND b.nuxrefco = c.nuxrefco\n" +
                "AND e.nuxrefco = c.nuxrefco\n" +
                "AND a.cdstatus = 'A' \n" +
                "AND c.cdstatus = 'A'\n" +
                "AND e.cdstatus = 'A'\n" +
                "AND b.nuserial IN (SELECT a2.nuserial \n" +
                "FROM fd12issue a2 \n" +
                "GROUP BY a2.nuserial HAVING COUNT(*)=1)\n" +
                "ORDER BY b.nuserial";


             log.info(this.ipAddr + "|" + "getNuSerialList() qry:"+qry);
                     System.out.println ( "getNuSerialList() qry:"+qry);
            
            result = stmt.executeQuery(qry);
            
            while (result.next()) {
              
                String nuxrefsn = result.getString(1);
                String nuserial = result.getString(2);
                String nusenate = result.getString(3);
                String cdcommodity = result.getString(4);
                String decommodityf = result.getString(5);
                
                 log.info(this.ipAddr + "|" + "getNuSerialList() qry loop:"+nuserial);   
                   System.out.println ( "getNuSerialList() qry loop:"+nuserial);
                InvSerialNumber invSerialNumber = new InvSerialNumber();
                invSerialNumber.setNuxrefsn(nuxrefsn);
                invSerialNumber.setNuserial(nuserial);
                invSerialNumber.setNusenate(nusenate);
                invSerialNumber.setCdcommodity(cdcommodity);
                invSerialNumber.setDecommodityf(decommodityf);
                if (numaxResults<nucnt) {
                  invSerialNumber.setStatusNum(Integer.toString(nucnt));
                  invSerialList.add(invSerialNumber);
                  break;
                }
                else {
                  invSerialNumber.setStatusNum("0");
                }
                invSerialList.add(invSerialNumber);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(resultCnt);
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return invSerialList;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to insert items found at given location(barcodes) for verification
     *----------------------------------------------------------------------------------------------------*/
    public int setBarcodesInDatabase(String cdlocat, ArrayList<InvItem> invItems, String userFallback) {
      return setBarcodesInDatabase(cdlocat, null, invItems, userFallback);
    }

    
    public int setBarcodesInDatabase(String cdlocat, String cdloctype, ArrayList<InvItem> invItems, String userFallback) {
        log.info(this.ipAddr + "|" + "setBarcodesInDatabase() begin : cdlocat= " + cdlocat + " Number of Inf Items= " + invItems.size());
        if (cdlocat.isEmpty() || invItems == null) {
            throw new IllegalArgumentException("Invalid location Code");
        }
        int result = 0;
        String r = "";
        Statement stmt = null;
        ResultSet result2 = null;
        Connection conn = null;
        BigDecimal nuxrmoappver = new BigDecimal(0);
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();

            // delete old data for given location code from SASS15018
            String qry = "delete from SASS15018 where CDLOCAT='" + cdlocat + "'";

            result2 = stmt.executeQuery(qry);
            //log.info("-=-=-=-=-=-=-=-=-=-=-=-verification_xref:"+cdlocat+"-"+cdloctype);
            //System.out.println ("-=-=-=-=-=-=-=-=-=-=-=-verification_xref:"+cdlocat+"-"+cdloctype);
            CallableStatement csXref = conn.prepareCall("{?=call inv_app.verification_xref(?,?)}");                  
            csXref.registerOutParameter(1, Types.NUMERIC);
            csXref.setString(2, cdlocat);
            csXref.setString(3, cdloctype);
            csXref.executeUpdate();
            nuxrmoappver = csXref.getBigDecimal(1);
            //log.info("-=-=-=-=-=-=-=-=-=-=-=-verification_xref:"+cdlocat+"-"+cdloctype+"="+nuxrmoappver);
            //System.out.println ("-=-=-=-=-=-=-=-=-=-=-=-verification_xref:"+cdlocat+"-"+cdloctype+"="+nuxrmoappver);

            for (int i = 0; i < invItems.size(); i++) {
               InvItem curInvItem =  invItems.get(i);
               
               // left padding 0 to string 
               String barcodeStr = String.format("%6s", curInvItem.getNusenate()).replace(' ', '0');
               if (curInvItem.getType().equalsIgnoreCase("NEW")||curInvItem.getType().equalsIgnoreCase("INACTIVE")) {
                   //System.out.println ("-=-=-=-=-=-=-=-=-=-=-=-store_new_inv_item:"+curInvItem.getNusenate()+"="+nuxrmoappver);
                  CallableStatement cs = conn.prepareCall("{?=call inv_app.store_new_inv_item(?,?, ?, ?, ?, ?, ?)}");                  
                  cs.registerOutParameter(1, Types.VARCHAR);
                  cs.setString(2, cdlocat);
                  cs.setString(3, curInvItem.getNusenate());
                  cs.setString(4, cdloctype);
                  cs.setString(5, curInvItem.getCdcommodity());
                  cs.setString(6, curInvItem.getDecomments());
                  cs.setString(7, "VERIFICATION");
                  cs.setBigDecimal(8, nuxrmoappver);
                  cs.executeUpdate();
                  r = cs.getString(1);
               }
               else {
                  //log.info("-=-=-=-=-=-=-=-=-=-=-=-copy_data:"+barcodeStr+"="+nuxrmoappver);                 
                  //System.out.println ("-=-=-=-=-=-=-=-=-=-=-=-copy_data:"+barcodeStr+"="+nuxrmoappver);
                  CallableStatement cs = conn.prepareCall("{?=call INV_APP.copy_data(?,?,?,?)}");
                  cs.registerOutParameter(1, Types.VARCHAR);
                  cs.setString(2, cdlocat);
                  cs.setString(3, barcodeStr);
                  cs.setString(4, cdloctype);
                  cs.setBigDecimal(5, nuxrmoappver);
                  cs.executeUpdate();
                  r = cs.getString(1);
               }
                
                /*
                 * Result was not being set properly
                 */
                
                if (r.trim().equalsIgnoreCase("SUCCESS")) {
                    result = 0;
                  }
                else {
                  result = 1;
                }
                System.out.println(r);
            }
        }
        catch (SQLException ex) {
            result = 2;
            ex.printStackTrace();
            log.info(this.ipAddr + "|" + "setBarcodesInDatabase() end");
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, this.ipAddr + "|" + ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result2);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "setBarcodesInDatabase() end");
        
        
        return result;
    }

    public int invTransit(Transaction pickup, String userFallback) {
        return invTransit(pickup, userFallback, 0);
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to start a new pickup-delivery
     *----------------------------------------------------------------------------------------------------*/
    public int invTransit(Transaction pickup, String userFallback, int oldnuxrpd) {
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            String qry = "SELECT FM12INVINTRANS_SEQN.nextval FROM  dual ";
            result = stmt.executeQuery(qry);
            while (result.next()) {
                pickup.setNuxrpd(result.getInt(1));
            }
            String updQry = "INSERT INTO FM12INVINTRANS (NUXRPD,CDLOCATTO, cdloctypeto, CDLOCATFROM, cdloctypefrm, CDINTRANSIT,"
                    + "NAPICKUPBY, NARELEASEBY,NUXRRELSIGN,NADELIVERBY,NAACCEPTBY,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,"
                    + "NATXNUPDUSER,DEPUCOMMENTS, DTPICKUP, NUXRPDORIG) "
                    + "VALUES(" + pickup.getNuxrpd() + ",'" + pickup.getDestination().getCdlocat() + "','" + pickup.getDestination().getCdloctype()
                    + "','" + pickup.getOrigin().getCdlocat() + "','" + pickup.getOrigin().getCdloctype() + "','" + "Y" + "','"
                    + pickup.getNapickupby() + "','" + pickup.getNareleaseby() + "'," + pickup.getNuxrrelsign() + ",'" + "" + "','" + ""
                    + "','" + "A" + "',SYSDATE,SYSDATE,'" + pickup.getNapickupby() + "','" + pickup.getNapickupby() + "','"
                    + pickup.getPickupComments() + "',SYSDATE," + getOldNuxrpdValue(oldnuxrpd) + ")";
            stmt.executeQuery(updQry);
            log.info("** updQry *** : " + updQry);
            log.info("****PICKUPITEMS: " + pickup.getPickupItems());

            for (String nusenate : pickup.getPickupItemsNusenate()) {
                String insertQry = "INSERT INTO FD12INVINTRANS (NUXRPD,NUSENATE,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,NATXNUPDUSER) "
                        + "VALUES(" + pickup.getNuxrpd() + ",'" + nusenate + "','" + "A" + "',SYSDATE,SYSDATE,'" + pickup.getNapickupby()
                        + "','" + pickup.getNapickupby() + "')";
                stmt.executeQuery(insertQry);
            }
        }
        catch (SQLException ex) {
            log.fatal("SQL error in invTransit ", ex);
            return -1;
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return pickup.getNuxrpd();
    }
    
    private String getOldNuxrpdValue(int num) {
        if (num == 0) {
            return "''";
        }
        return Integer.toString(num);
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the in transit pickups for the given values
     *----------------------------------------------------------------------------------------------------*/

    public List<PickupGroup> getPickupList(ArrayList<SimpleListItem> searchByList, String userFallback) {
        //log.info(this.ipAddr + "|" + "getDeliveryList() begin : locCode= " + locCode);
        if (searchByList==null||searchByList.size()==0) {
            throw new IllegalArgumentException("No Search By Parameters for DbConnect.getPickupList");
        }
        StringBuilder supplementatWhereClause = new StringBuilder();
        
        for (int x=0;x<searchByList.size();x++) {
          SimpleListItem simpleListItem = searchByList.get(x);
          if (simpleListItem.getNatype()==null||simpleListItem.getNatype().trim().length()==0||simpleListItem.getNavalue()==null||simpleListItem.getNavalue().trim().length()==0) {
            throw new IllegalArgumentException("Search By Parameter Name and Value cannot be null.  Parameter Name:("+simpleListItem.getNatype()+")  Parameter Value:("+simpleListItem.getNavalue()+") for DbConnect.getPickupList");
          }
          // Below just simply looking at the name of the column to see if it starts with dt
          // if it does then assume it is a date column.  NOTE: This will not work with dt...year since
          // that is a VARCHAR2 column. Just used for code simplicity instead of having to check the database
          // column type. If it a date, we will assume that it came in as a MM/DD/YY format for simplicity.
          if (simpleListItem.getNatype().toLowerCase().startsWith("dt")) {
              supplementatWhereClause.append("  AND TRUNC(a.");
              supplementatWhereClause.append(simpleListItem.getNatype());
              supplementatWhereClause.append(")  = TO_DATE('");
              supplementatWhereClause.append(simpleListItem.getNavalue());
              supplementatWhereClause.append("' , 'mm/dd/rr')");
              
          }
          else {
              supplementatWhereClause.append("  AND a.");
              supplementatWhereClause.append(simpleListItem.getNatype());
              supplementatWhereClause.append(" = '");
              supplementatWhereClause.append(simpleListItem.getNavalue());
              supplementatWhereClause.append("'");
          }
        }
        java.lang.reflect.Type listOfTestObject = new TypeToken<List<PickupGroup>>() {
        }.getType();
        List<PickupGroup> pickupList = Collections.synchronizedList(new ArrayList<PickupGroup>());
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT a.nuxrpd, TO_CHAR(a.dtpickup, 'MM/DD/RR HH:MI:SSAM') dtpickup, a.cdlocatfrom, a.napickupby, a.nareleaseby, c.adstreet1, c.adcity, c.adstate, c.adzipcode, COUNT(b.nuxrpd) nucount "
                    + " FROM FM12INVINTRANS a, FD12INVINTRANS b, sl16location c"
                    + " WHERE a.CDSTATUS='A'"
                    + " AND a.CDINTRANSIT='Y'"
                    + supplementatWhereClause.toString()
                    + " AND b.nuxrpd = a.nuxrpd"
                    + " AND b.cdstatus = 'A'"
                    + " AND c.cdlocat = a.cdlocatfrom"
                    + " GROUP BY a.nuxrpd, a.dtpickup, a.cdlocatfrom, a.napickupby, a.nareleaseby, c.adstreet1, c.adcity, c.adstate, c.adzipcode"
                    + " ORDER BY a.dtpickup NULLS LAST";
            //System.out.println(qry);
            result = stmt.executeQuery(qry);
            while (result.next()) {
                int nuxrpd = result.getInt(1);
                String dtpickup = result.getString(2);
                String cdlocatfrom = result.getString(3);
                String napickupby = result.getString(4);
                String nareleaseby = result.getString(5);
                String adstreet1 = result.getString(6);
                String adcity = result.getString(7);
                String adstate = result.getString(8);
                String adzipcode = result.getString(9);
                int nucount = result.getInt(10);
                //String pickupDetails = NUXRPD + " : From " + CDLOCATFROM + "\n To " + CDLOCATTO + "\n Pickup by : " + NAPICKUPBY;
                pickupList.add(new PickupGroup(nuxrpd, dtpickup, napickupby, nareleaseby, cdlocatfrom, adstreet1, adcity, adstate, adzipcode, nucount, 0));
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getDeliveryList() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getDeliveryList() end");
        return pickupList;

    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the items related to a perticular delivery nuxrpd
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList<InvItem> getDeliveryDetails(String nuxrpd, String userFallback) {
        log.info(this.ipAddr + "|" + "getDeliveryDetails() begin : nuxrpd= " + nuxrpd);
        if (nuxrpd.isEmpty()) {
            throw new IllegalArgumentException("Invalid locCode");
        }
        ArrayList<InvItem> deliveryDetails = new ArrayList<InvItem>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF,e.nuxrpd,b.cdlocatto, e.cdlocatto, e.cdintransit FROM "
                    + " FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C,fd12invintrans d,fm12invintrans e "
                    + " WHERE A.CDSTATUS='A' "
                    + " AND A.NUXREFSN=B.NUXREFSN "
                    + " AND B.NUXREFCO=C.NUXREFCO "
                    + " and a.nusenate=d.nusenate "
                    + " AND d.nuxrpd =e.nuxrpd "
                    + " AND d.cdstatus = 'A'"
                    + " and e.nuxrpd=" + nuxrpd;
            result = stmt.executeQuery(qry);
            while (result.next()) {
                String nusenate = result.getString(1);
                String cdcategory = result.getString(2);
                String decommodityf = result.getString(3);
                String cdlocat = result.getString(5);
                String cdlocatto = result.getString(6);
                String cdintransit = result.getString(7);
                InvItem curInvItem = new InvItem(nusenate, cdcategory, "EXISTING", decommodityf);
                curInvItem.setCdlocat(cdlocat);
                curInvItem.setCdlocatto(cdlocatto);
                curInvItem.setCdintransit(cdintransit);
                deliveryDetails.add(curInvItem);
            }

        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getDeliveryDetails() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getDeliveryDetails() end");
        return deliveryDetails;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to 
     *----------------------------------------------------------------------------------------------------*/

    int invPickup(String originLocation, String destinationLocation, String[] barcodes, String NAPICKUPBY, String NARELEASEBY, String NUXRRELSIGN, String NADELIVERBY, String NAACCEPTBY, String NUXRACCPTSIGN, String userFallback) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to insert signature into database
     *----------------------------------------------------------------------------------------------------*/

    public int insertSignature(byte[] imageInArray, int nuxrefem, String nauser, String userFallback) {
        log.info(this.ipAddr + "|" + "insertSignature() begin : nuxrefem= " + nuxrefem + " &nauser=" + nauser);
        if (imageInArray == null || nuxrefem < 0 || nauser == null) {
            throw new IllegalArgumentException("Invalid imageInArray or nuxrefem or nauser");
        }

        //System.out.println("DbConnect insertSignature byte Image Length:" + imageInArray.length);

        Blob blobValue;
        int nuxrsign = -1;
        
        //System.out.println("Not converting the Image on the Server Side for testing");

        // If the Image was a PNG with a  transparent Background, below will convert it to a white background
        // jpg.
        // Commented ou 7/26/13 BH for testing purponses
        try {
            log.info(this.ipAddr + "|IMAGE FORMATS AVAILABLE:"+Arrays.toString(ImageIO.getReaderFormatNames()));
            System.out.println("IMAGE FORMATS AVAILABLE:"+Arrays.toString(ImageIO.getReaderFormatNames()));
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageInArray));
            
            if (bufferedImage==null) {
               log.warn(this.ipAddr + "|" + "***WARNING: bufferedImage for a Signature Image was null!! (DBCONNECT.insertSignature)");
            }
            Graphics2D newGraphic = bufferedImage.createGraphics();
            if (newGraphic==null) {
               log.warn(this.ipAddr + "|" + "***WARNING: An attempt to create a new Graphic for a Signature Image has failed!!! Resulting in a NULL Result. (DBCONNECT.insertSignature)");
            }
            
            newGraphic.drawImage(bufferedImage, 0, 0, Color.WHITE, null);
            ByteArrayOutputStream baos = new ByteArrayOutputStream();
            ImageIO.write(bufferedImage, "jpg", baos);
            baos.flush();
            imageInArray = baos.toByteArray();
            baos.close();
            System.out.println("Image should have been converted to a white background jpg.");
        } catch (Exception e) {
            e.printStackTrace();
            log.fatal(this.ipAddr + "|" + "Exception in insertSignature() : " + e.getMessage());
        }

        PreparedStatement ps = null;
        Statement stmtSequence = null;
        Statement stmt = null;
        ResultSet rsSequence = null;
        ResultSet rs = null;
        Connection con = null;
        OutputStream outStream = null;
        try {
            con = getDbConnection();
            stmtSequence = con.createStatement();
            if (con==null) {
                System.out.println("insertSignature Connection was NULL when creating statement from it");
            }
            else if (stmtSequence==null) {
                System.out.println("insertSignature could not createStatement from Connection");
                
            }
            rsSequence = stmtSequence.executeQuery("select FP12SIGNREF_SQNC.NEXTVAL FROM DUAL");

            while (rsSequence.next()) {
                nuxrsign = rsSequence.getInt(1);
            }

            con.setAutoCommit(false);
            //blobValue = new SerialBlob(imageInArray);
            //System.out.println ("insert into FD12INVSIGNS nuxrsign:"+nuxrsign+", nuxrefem:"+nuxrefem+", nauser:"+nauser);
            ps = con.prepareStatement("insert into FD12INVSIGNS (nuxrsign, blsign, nuxrefem, cdstatus, natxnorguser, natxnupduser, dttxnorigin, dttxnupdate ) values(?, empty_blob(), ?, 'A', ?,  ?, SYSDATE, SYSDATE )");
            ps.setInt(1, nuxrsign);
            ps.setInt(2, nuxrefem);
            ps.setString(3, nauser);
            ps.setString(4, nauser);
            // size must be converted to int otherwise it results in error
//            ps.setBlob(2, blobValue);
            ps.executeUpdate();
            con.commit();
            //System.out.println(imageInArray.length + " bytes should have been saved to PCIMAGE");

            stmt = con.createStatement();
            rs = stmt.executeQuery("select blsign from FD12INVSIGNS where nuxrsign=" + nuxrsign + " for update");
            BLOB writeBlob = null;

            if (rs.next()) {
                //System.out.println("RECORD TO WRITE BLOB");
                writeBlob = (BLOB) rs.getBlob(1);
            } else {
                System.out.println("handelSaveAskTree(): BLOB object could not be found...");
            }
            outStream = writeBlob.setBinaryStream(0);
            outStream.write(imageInArray);
            outStream.flush();
            outStream.close(); // OutputStream Must be closed before committing when writing Blob.
            con.commit();

        } catch (SQLException ex) {
            System.out.println("!!!!!!!!!!SQL EXCEPTION OCCURED:"+ ex.getMessage());
            System.out.println(ex.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in insertSignature() : " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("!!!!!!!!!!IO EXCEPTION OCCURED:"+ ex.getMessage());
            ex.printStackTrace();
            log.fatal(this.ipAddr + "|" + "IOException in insertSignature() : " + ex.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(rs);
            closeResultSet(rsSequence);
            closeStatement(stmt);
            closeStatement(stmtSequence);
            closeStatement(ps);
            closeConnection(con);
            if (outStream != null) {
                try {
                    outStream.close();
                } catch (IOException e) {
                    e.printStackTrace();
                }
            }
        }
        log.info(this.ipAddr + "|" + "insertSignature() end");
        return nuxrsign;

    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to 
     *----------------------------------------------------------------------------------------------------*/
    public ArrayList<Employee> getEmployeeList(String nalast, String userFallback) {
        //   if(nalast==null){
        //       throw new IllegalArgumentException("Invalid nalast");
        //   }
        log.info(this.ipAddr + "|" + "getEmployeeList(String nalast) begin : nalast= " + nalast);
        return getEmployeeList(nalast, "A");
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return the list of employee names
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList<Employee> getEmployeeList(String nalast, String cdempstatus, String userFallback) {
        log.info(this.ipAddr + "|" + "getEmployeeList(String nalast, String cdempstatus) begin : nalast= " + nalast + " &cdempstatus=" + cdempstatus);
        // if(nalast.isEmpty()||cdempstatus.isEmpty()){
        // throw new IllegalArgumentException("Invalid nalst or cdempstatus");    
        //  }
        ArrayList<Employee> employeeList = new ArrayList<Employee>();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            if (nalast == null) {
                nalast = "";
            }
            //  String loc_code;
            String qry = "SELECT a.nuxrefem, a.nafirst, a.nalast, a.namidinit, a.nasuffix, a.naemail"
                    + " FROM pm21personn a "
                    + " WHERE a.cdempstatus LIKE '" + cdempstatus + "'"
                    + "  AND a.nalast LIKE'" + nalast + "%'"
                    + " AND a.naemail IS NOT null"
                    + " ORDER BY  a.nalast||DECODE(a.nasuffix, NULL, NULL, ' '||a.nasuffix)||', '||a.nafirst||DECODE(a.namidinit, NULL, NULL, ' '||a.namidinit)";

            //System.out.println("QRY:" + qry);
            result = stmt.executeQuery(qry);
            while (result.next()) {

                Employee employee = new Employee();
                employee.setEmployeeData(result.getInt(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5));
                if (emailIsValid(result.getString(6))) {
                    employeeList.add(employee);
                }
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getEmployeeList() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getEmployeeList() end");
        return employeeList;
    }

    public boolean emailIsValid(String email) {
        boolean isValid = true;
        try {
            InternetAddress address = new InternetAddress(email);
            address.validate();
        } catch (AddressException ex) {
            isValid = false;
        }
        return isValid;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to confirm delivery i.e. updates the FD12Issue table and changes location-----
     *------------------------------------------------------------------------------------------------------*/
    public int confirmDelivery(Transaction delivery, String userFallback) {
        //log.info(this.ipAddr + "|" + "confirmDelivery() begin.");

        Statement stmt = null;
        CallableStatement cs = null;
        ResultSet res1 = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "SELECT CDLOCATTO,CDLOCTYPETO,CDLOCATFROM,CDLOCTYPEFRM "
                    + " FROM fm12invintrans  "
                    + " WHERE CDSTATUS='A' "
                    + " AND nuxrpd=" + delivery.getNuxrpd();

            res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                delivery.getDestination().setCdlocat(res1.getString(1));
                delivery.getDestination().setCdloctype(res1.getString(2));
                delivery.getOrigin().setCdlocat(res1.getString(3));
                delivery.getOrigin().setCdloctype(res1.getString(4));
            }

            // Update its entry in FM12InvInTrans to show it is delivered.
            String query = "UPDATE fm12invintrans "
                    + "SET CDINTRANSIT='N' "
                    + " ,DTTXNUPDATE=SYSDATE "
                    + " ,NATXNUPDUSER=USER "
                    + " ,NUXRACCPTSIGN=" + delivery.getNuxraccptsign()
                    + " ,NADELIVERBY='" + delivery.getNadeliverby()
                    + "' ,NAACCEPTBY='" + delivery.getNaacceptby()
                    + "' ,DTDELIVERY=SYSDATE "
                    + "  ,DEDELCOMMENTS='" + delivery.getDeliveryComments()
                    + "' WHERE NUXRPD=" + delivery.getNuxrpd();
            stmt.executeUpdate(query);
            conn.commit();

            // Move delivered Items to their new location.
            for (String item : delivery.getCheckedItems()) {
                String nusenate = item;
                cs = conn.prepareCall("{?=call inv_app.move_inventory_item(?,?,?,?,?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, nusenate);
                cs.setString(3, delivery.getOrigin().getCdlocat());
                cs.setString(4, delivery.getOrigin().getCdloctype());
                cs.setString(5, delivery.getDestination().getCdlocat());
                cs.setString(6, delivery.getDestination().getCdloctype());
                cs.setString(7, String.valueOf(delivery.getNuxrpd()));
                cs.executeUpdate();
            }

            // Delete items in this transaction that were not delivered, they will be put in a new transaction.
            if (delivery.getNotCheckedItems().size() > 0) {
                for (String nusenate : delivery.getNotCheckedItems()) {
                    String del = "DELETE FROM FD12INVINTRANS WHERE nuxrpd=" + delivery.getNuxrpd() + "AND nusenate = '" + nusenate + "'";
                    stmt.executeUpdate(del);
                }
            }
        }
        catch (SQLException ex) {
            log.fatal("SQL error in confirmDelivery(). ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(res1);
            closeStatement(stmt);
            closeStatement(cs);
            closeConnection(conn);
        }
        return 0;
    }

   public Employee getEmployeeWhoSigned(String nuxrsign, String userFallback) {
     return getEmployeeWhoSigned(nuxrsign, true, userFallback);
   }
    
   public Employee getEmployeeWhoSigned(String nuxrsign, boolean upperCase, String userFallback) {
     String nuxrefem = "";
     Statement stmt = null;
     ResultSet res1 = null;
     Connection conn = null;
     try {
         conn = getDbConnection();
          stmt = conn.createStatement();
          // Get location info for this transaction.
          String qry1 = "SELECT nuxrefem "
                    + " FROM fd12invsigns  "
                    + " WHERE cdstatus = 'A' "
                    + " AND nuxrsign = " + nuxrsign;

          res1 = stmt.executeQuery(qry1);
           while (res1.next()) {
              nuxrefem = res1.getString(1);
            }
        }
     catch (SQLException ex) {
        log.fatal("SQL error in getEmployeeWhoSigned(). ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(res1);
            closeStatement(stmt);
            closeConnection(conn);
        }
     return getEmployee(nuxrefem, upperCase, userFallback);
   }
    
   public Employee getEmployee(String nuxrefem,  String userFallback) {
     return getEmployee(nuxrefem, true, userFallback);
   }
   
   public Employee getEmployee(String nuxrefem, boolean upperCase, String userFallback) {
        log.info(this.ipAddr + "|" + "getEmployee() begin : nuxrefem= " + nuxrefem);
        if (nuxrefem.isEmpty() || nuxrefem == null) {
            throw new IllegalArgumentException("Invalid nuxrefem");
        }

        Employee currentEmployee = new Employee();
        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            //  String loc_code;
            String qry = null;
            if (upperCase) {
              qry = "SELECT a.nafirst, a.namidinit, a.nalast, a.nasuffix, a.naemail"
                    + " FROM pm21personn a"
                    + " WHERE a.nuxrefem = "+nuxrefem;
            }
            else {
              qry = "SELECT a.ffnafirst, a.ffnamidinit, a.ffnalast, a.ffnasuffix, a.naemail"
                    + " FROM pm21personn a"
                    + " WHERE a.nuxrefem = "+nuxrefem;
            }
                    
            result = stmt.executeQuery(qry);
            while (result.next()) {
              currentEmployee.setNafirst(result.getString(1), false);
              currentEmployee.setNamidinit(result.getString(2), false);
              currentEmployee.setNalast(result.getString(3), false);
              currentEmployee.setNasuffix(result.getString(4), false);
              currentEmployee.setNaemail(result.getString(5));
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getEmployee() : " + e.getMessage());
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        log.info(this.ipAddr + "|" + "getEmployee() end");
        return currentEmployee;
    }    

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to create new delivery i.e. inserts new records into FM12InvInTrans-----
     *----------------------------------------------------------------------------------------------------*/
    public int createNewPickup(Transaction delivery, String userFallback) {
        log.info(this.ipAddr + "|" + "createNewDelivery() begin :");

        Transaction pickup = new Transaction();
        pickup.setPickupItemsList(delivery.getNotCheckedItems());

        Statement stmt = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            String qry = "SELECT NUXRPD,CDLOCATFROM, CDLOCTYPEFRM, CDLOCATTO,CDLOCTYPETO, NAPICKUPBY, NARELEASEBY, NUXRRELSIGN, DEPUCOMMENTS FROM   "
                    + "  FM12INVINTRANS"
                    + " WHERE CDSTATUS='A'"
                    + " and nuxrpd=" + delivery.getNuxrpd();

            result = stmt.executeQuery(qry);
            while (result.next()) {
                String nuxrpd = result.getString(1);
                pickup.getOrigin().setCdlocat(result.getString(2));
                pickup.getOrigin().setCdloctype(result.getString(3));
                pickup.getDestination().setCdlocat(result.getString(4));
                pickup.getDestination().setCdloctype(result.getString(5));
                pickup.setNapickupby(result.getString(6));
                pickup.setNareleaseby(result.getString(7));
                pickup.setNuxrrelsign(result.getString(8));
                pickup.setPickupComments(result.getString(9));
            }
        }
        catch (SQLException ex) {
            log.fatal(this.ipAddr + "|" + "Error getting pickup info in createNewDelivery(). ", ex);
        } catch (ClassNotFoundException e) {
            log.error("Error getting oracle jdbc driver: ", e);
        } finally {
            closeResultSet(result);
            closeStatement(stmt);
            closeConnection(conn);
        }
        DbConnect db = new DbConnect();
        db.invTransit(pickup, userFallback, delivery.getNuxrpd());
        log.info(this.ipAddr + "|" + "createNewDelivery() end ");
        return 0;
    }

    public void setUsernamePwd(String user, String pwd) {
        userName = user;
        password = pwd;
    }

    public void cancelPickup(int nuxrpd) throws SQLException, ClassNotFoundException {
        String query = "UPDATE FM12INVINTRANS "
                + "SET CDINTRANSIT = 'C', "
                + "DTTXNUPDATE = SYSDATE, "
                + "NATXNUPDUSER = USER "
                + "WHERE NUXRPD = ?";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, nuxrpd);
            ps.executeUpdate();
        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public void changePickupLocation(int nuxrpd, String cdLoc) throws SQLException, ClassNotFoundException {
        String query = "UPDATE FM12INVINTRANS "
                + "SET CDLOCATFROM = ?, "
                + "DTTXNUPDATE = SYSDATE, "
                + "NATXNUPDUSER = USER "
                + "WHERE NUXRPD = ?";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, cdLoc);
            ps.setInt(2, nuxrpd);
            ps.executeUpdate();
        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public void changeDeliveryLocation(int nuxrpd, String cdLoc) throws SQLException, ClassNotFoundException {
        String query = "UPDATE FM12INVINTRANS "
                + "SET CDLOCATTO = ?, "
                + "DTTXNUPDATE = SYSDATE, "
                + "NATXNUPDUSER = USER "
                + "WHERE NUXRPD = ?";
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, cdLoc);
            ps.setInt(2, nuxrpd);
            ps.executeUpdate();
        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }
    
    public void setLocationInfo(Location location) throws SQLException, ClassNotFoundException {
      //System.out.println ("DBCONNECT.setLocationInfo cdlocat:"+location.getCdlocat()+", cdloctype:"+location.getCdloctype());
      if  (location.getCdlocat()==null||location.getCdlocat().trim().length()==0) {
         log.info("Location has no cdlocat, setLocationInfo could not obtain location information."); 
         return;         
      }

      Statement stmt = null;
      ResultSet res0 = null;
      ResultSet res1 = null;
      Connection conn = null;
      try {
          conn = getDbConnection();
          stmt = conn.createStatement();      

          if  (location.getCdloctype()==null||location.getCdloctype().trim().length()==0) {
              log.info("Location has no cdloctype, looking up cdloctype.. Recommending to always pass cdloctype."); 

              // Get location info for this transaction.
              String qry0 = "SELECT cdloctype "
                      + " FROM sl16location a "
                      + " WHERE cdlocat = '"+location.getCdlocat()+"' "
                      + "   AND cdstatus = 'A' "
                      + "   AND NOT EXISTS (SELECT 1 "
                      + "                   FROM sl16location a2 "
                      + "                   WHERE a2.cdlocat = a.cdlocat "
                      + "                     AND a2.cdloctype != a.cdloctype "
                      + "                     AND a2.cdstatus = 'A')";

              res0 = stmt.executeQuery(qry0);
              while (res0.next()) {
                  location.setCdloctype(res0.getString(1));
              }               
          }
          if  (location.getCdloctype()==null||location.getCdloctype().trim().length()==0) {
              log.info("Location has no cdloctype, setLocationInfo could not obtain location information.");
              return;
          }
          stmt = conn.createStatement();
          // Get location info for this transaction.
          String qry1 = "SELECT adstreet1,adcity,adstate,adzipcode "
                  + " FROM sl16location  "
                  + " WHERE cdlocat = '"+location.getCdlocat()+"' "
                  + "   AND cdloctype = '"+location.getCdloctype()+"' "
                  + "   AND cdstatus = 'A'";

          //System.out.println ("DBCONNECT Location "+location.getCdlocat()+" QRY: "+qry1);
          res1 = stmt.executeQuery(qry1);
          while (res1.next()) {
              location.setAdstreet1(res1.getString(1));
              location.setAdcity(res1.getString(2));
              location.setAdstate(res1.getString(3));
              location.setAdzipcode(res1.getString(4));
          }
      } finally {
          closeResultSet(res1);
          closeResultSet(res0);
          closeStatement(stmt);
          closeConnection(conn);
      }
           //System.out.println ("DBCONNECT Location "+location.getCdlocat()+" SET: "+location.getAdstreet1());
    }

    public Employee getEmployee(String nauser) throws SQLException, ClassNotFoundException {
        Employee employee = new Employee();

        Statement stmt = null;
        ResultSet res1 = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "SELECT b.nuxrefem, b.nafirst, b.nalast, b.namidinit, b.nasuffix, b.naemail, SUBSTR(UPPER(b.naemail), 1, DECODE(INSTR(b.naemail, '@'), -1, LENGTH(b.naemail), INSTR(b.naemail, '@')-1)) nauser\n"
                    + " FROM  pm21personn b  "
                    + " WHERE SUBSTR(UPPER(b.naemail), 1, DECODE(INSTR(b.naemail, '@'), -1, LENGTH(b.naemail), INSTR(b.naemail, '@')-1)) = '"+nauser.toUpperCase()+"' "
                    + "   AND b.cdempstatus = 'A'"
                    + " ORDER BY DECODE( INSTR(b.naemail, '@'), -1, 1, 0)";

            //System.out.println ("getEmployee qry1:"+qry1);

            res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                employee.setEmployeeXref(res1.getInt(1));
                employee.setNafirst(res1.getString(2));
                employee.setNalast(res1.getString(3));
                employee.setNamidinit(res1.getString(4));
                employee.setNasuffix(res1.getString(5));
                employee.setNaemail(res1.getString(6));
            }
        } finally {
            closeResultSet(res1);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return employee;
    }

    public void removeDeliveryItems(int nuxrpd, String[] items) throws SQLException, ClassNotFoundException {
        String query = "UPDATE FD12INVINTRANS "
                + "SET CDSTATUS = 'I', "
                + "DTTXNUPDATE = SYSDATE, "
                + "NATXNUPDUSER = USER "
                + "WHERE NUSENATE = ? "
                + "AND NUXRPD = ?";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            ps = conn.prepareStatement(query);

            for (String item : items) {
                ps.setString(1, item);
                ps.setInt(2, nuxrpd);
                ps.addBatch();
            }
            ps.executeBatch();
        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public String[] getEmployeeInfo(String nalast) throws SQLException, ClassNotFoundException {
        String[] empInfo = new String[3];
        String query = "SELECT nafirst, nalast, cdrespctrhd"
                + " FROM PM21PERSONN"
                + " WHERE nalast = ?"
                + " AND SUBSTR(naemail, 0, REGEXP_INSTR(naemail, '@') - 1) = lower(nalast)";

        PreparedStatement ps = null;
        ResultSet rs = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, nalast);
            rs = ps.executeQuery();
            while (rs.next()) {
                empInfo[0] = rs.getString(1);
                empInfo[1] = rs.getString(2);
                empInfo[2] = rs.getString(3);
            }
        } finally {
            closeResultSet(rs);
            closeStatement(ps);
            closeConnection(conn);
        }
        return empInfo;
    }

    public ArrayList<Employee> getEmailSupervisors(String nauser) throws SQLException, ClassNotFoundException {
        ArrayList<Employee> emailSupervisors = new ArrayList<Employee>();

        Statement stmt = null;
        ResultSet res1 = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "Select C.Nuxrefem, C.Nafirst, C.Nalast, C.Namidinit, C.Nasuffix, C.Naemail, Substr(Upper(C.Naemail), 1, Decode(Instr(C.Naemail, '@'), -1, Length(C.Naemail), Instr(C.Naemail, '@')-1)) Nauser\n"
                    + " FROM  fm12emlsup a, pm21personn b, pm21personn c  "
                    + " WHERE SUBSTR(UPPER(b.naemail), 1, DECODE(INSTR(b.naemail, '@'), -1, LENGTH(b.naemail), INSTR(b.naemail, '@')-1)) = '"+nauser.toUpperCase()+"' "
                    + "   AND b.cdempstatus = 'A'"
                    + "   AND b.nuxrefem = a.nuxrefem"
                    + "   AND c.cdempstatus = 'A'"
                    + "   AND a.nuxrefemlsup = c.nuxrefem"
                    + "   AND a.cdstatus = 'A' "
                    + " ORDER BY DECODE( INSTR(c.naemail, '@'), -1, 1, 0)";

            //System.out.println ("getEmployee qry1:"+qry1);

            res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                Employee employee = new Employee();
                employee.setEmployeeXref(res1.getInt(1));
                employee.setNafirst(res1.getString(2));
                employee.setNalast(res1.getString(3));
                employee.setNamidinit(res1.getString(4));
                employee.setNasuffix(res1.getString(5));
                employee.setNaemail(res1.getString(6));
                emailSupervisors.add(employee);
            }
        } finally {
            closeResultSet(res1);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return emailSupervisors;
    }

    public ArrayList<Employee> getEmailSupervisors(int nuxrefem) throws SQLException, ClassNotFoundException {
        ArrayList<Employee> emailSupervisors = new ArrayList<Employee>();

        Statement stmt = null;
        ResultSet res1 = null;
        Connection conn = null;
        try {
            conn = getDbConnection();
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "SELECT c.nuxrefem, c.nafirst, c.nalast, c.namidinit, c.nasuffix, c.naemail, SUBSTR(UPPER(c.naemail), 1, DECODE(INSTR(c.naemail, '@'), -1, LENGTH(c.naemail), INSTR(c.naemail, '@')-1)) nauser\n"
                    + " FROM  fm12emlsup a, pm21personn c  "
                    + " WHERE a.nuxrefem = "+nuxrefem+" "
                    + "   AND c.cdempstatus = 'A'"
                    + "   AND a.nuxrefemlsup = c.nuxrefem"
                    + "   AND a.cdstatus = 'A' "
                    + " ORDER BY DECODE( INSTR(c.naemail, '@'), -1, 1, 0)";

            //System.out.println ("getEmployee qry1:"+qry1);

            res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                Employee employee = new Employee();
                employee.setEmployeeXref(res1.getInt(1));
                employee.setNafirst(res1.getString(2));
                employee.setNalast(res1.getString(3));
                employee.setNamidinit(res1.getString(4));
                employee.setNasuffix(res1.getString(5));
                employee.setNaemail(res1.getString(6));
                emailSupervisors.add(employee);
            }
        } finally {
            closeResultSet(res1);
            closeStatement(stmt);
            closeConnection(conn);
        }
        return emailSupervisors;
    }
    
    public String getDatabaseName() {
        String connectionString = properties.getProperty("connectionString");
        
        if (connectionString!=null && connectionString.contains(":")) {
            String[] connectStrings = connectionString.split(":");
            this.dbaName = connectStrings[connectStrings.length-1];
        }
      return connectionString;

    }
    
    public String changePassword(String password) throws SQLException, ClassNotFoundException {
      return changePassword(this.userName, password);
    }
    
    public String changePassword(String user, String password) throws SQLException, ClassNotFoundException {
        String results = null;
        
        System.out.println("changePassword loadProperties()");
        
        loadProperties();
        System.out.println("changePassword loadProperties() DONE dbaUrl:"+properties.getProperty("dbaURL"));
        
        String[] dbaUrl = properties.getProperty("dbaUrl").replaceAll("http://", "").split(":");
        System.out.println("changePassword dbaURL[0]:"+dbaUrl[0]);
        
        String serverName = dbaUrl[0];
        String ldapUserbase = "dc=senate,dc=state,dc=ny,dc=us";

        /*String query = "SELECT nafirst, nalast, cdrespctrhd"
                + " FROM PM21PERSONN"
                + " WHERE nalast = ?"
                + " AND SUBSTR(naemail, 0, REGEXP_INSTR(naemail, '@') - 1) = lower(nalast)";*/
                
        PreparedStatement ps = null;
        CallableStatement cs = null;
        ResultSet rs = null;
        Connection conn = null;
        int passwordValidity = 90;
        
        try {
            conn = getDbConnection();
            cs = conn.prepareCall("{?=call change_password(?,?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, user);
            cs.setString(3, password);
            cs.executeUpdate();
            results  = cs.getString(1);
            System.out.println("{?=call change_password("+user+","+password+")}");
            log.info("{?=call change_password("+user+","+password+")}");
           
            if (results !=null && !results.isEmpty()) {
              return results;
            }
            
            ps = conn.prepareStatement("SELECT To_Number(paramval) FROM sass_parameters WHERE cdparameter = 'PASSWD_VALIDITY'");
            rs = ps.executeQuery();

            while (rs.next()) {
                passwordValidity = rs.getInt(1);
                System.out.println("passwordValidity:"+passwordValidity);
                log.info("passwordValidity:"+passwordValidity);
            }
            
            System.out.println("UPDATE im86orgid SET dtpasswdset = SYSDATE, dtpasswdexp = SYSDATE + "+passwordValidity+" WHERE nauser = '"+user+"'");
            log.info("UPDATE im86orgid SET dtpasswdset = SYSDATE, dtpasswdexp = SYSDATE + "+passwordValidity+" WHERE nauser = '"+user+"'");
            ps = conn.prepareStatement("UPDATE im86orgid SET dtpasswdset = SYSDATE, dtpasswdexp = SYSDATE + ? WHERE nauser = ?");
            ps.setInt(1, passwordValidity);
            ps.setString(2, user.trim().toUpperCase());
            ps.executeUpdate();
            
            System.out.println("call changeSSOPassword ('389', '"+serverName+"', '"+ldapUserbase+"', '"+user+"', '"+password+"'}");
            log.info("UPDATE im86orgid SET dtpasswdset = SYSDATE, dtpasswdexp = SYSDATE + "+passwordValidity+" WHERE nauser = '"+user+"'");
            cs = conn.prepareCall("{call changeSSOPassword ('389', ?, ?, ?, ?)}");
            cs.setString(1, serverName);
            cs.setString(2, ldapUserbase);
            cs.setString(3, user.trim().toLowerCase());
            cs.setString(4, password.trim().toLowerCase());
            cs.executeUpdate();
 
            System.out.println("call updateSSOUserResource ('"+user+"', '"+password+"', '"+this.dbaName+"||con', 'OracleDB', '389', '"+serverName+"', '"+ldapUserbase+"', , 'cn=Extended Properties,cn=OracleContext,"+ldapUserbase+"')}");
            cs = conn.prepareCall("{call updateSSOUserResource (?, ?, ?, 'OracleDB', '389', ?, ?, ?)}");
            cs.setString(1, user);
            cs.setString(2, password);
            cs.setString(3, this.dbaName+"con");
            cs.setString(4, serverName);
            cs.setString(5, ldapUserbase);
            cs.setString(6, "cn=Extended Properties,cn=OracleContext,"+ldapUserbase);
            cs.executeUpdate();
            
        } finally {
            closeResultSet(rs);
            closeStatement(cs);
            closeStatement(ps);
            closeConnection(conn);
        }        
        return results;
    }
}
