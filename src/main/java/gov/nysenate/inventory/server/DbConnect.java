package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Delivery;
import gov.nysenate.inventory.model.Pickup;

import gov.nysenate.inventory.model.Commodity;
import gov.nysenate.inventory.model.SimpleListItem;

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

import oracle.sql.BLOB;

import org.apache.log4j.Level;
import org.apache.log4j.Logger;

import com.google.gson.reflect.TypeToken;
import gov.nysenate.inventory.model.SimpleListItem;
import java.awt.Graphics2D;
import java.util.Arrays;

/**
 *
 * @author Patil
 */
public class DbConnect {

    String ipAddr = "";
    static Logger log = Logger.getLogger(DbConnect.class.getName());
    static Properties properties = new Properties();
    static InputStream in;
    static private String userName,  password;
    final int RELEASESIGNATURE = 3001, ACCEPTBYSIGNATURE = 3002;
   
    DbConnect() {
        properties = new Properties();
        in = getClass().getClassLoader().getResourceAsStream("config.properties");
        try {
            properties.load(in);
            userName = properties.getProperty("user");
            password = properties.getProperty("password");

        } catch (IOException ex) {
            java.util.logging.Logger.getLogger(DbConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
        }

    }

    DbConnect(String user, String pwd) {
        userName = user;
        password = pwd;
        System.out.println("NEW DBCONNECT userName:"+userName);
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Main function for testing other functions
     *----------------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {
        log.info("main function ");
        /*    String barcode_num = "77030";
         //   int barcode = Integer.valueOf(barcode_num);
         DbConnect db = new DbConnect();
         String cdlocat = "abcd";
         String barcodes[] = {"077896", "078567", "0268955"};
  
         String barcode="071030";
         //   int result=db.setBarcodesInDatabase(cdlocat, barcodes);
         // int result = db.invTransit("A42FB", "A411A", barcodes, "vikram", "10", "Brian", "11");
         //  int result = db.createNewDelivery("267", barcodes);
         //   System.out.println(result);
         //db.execQuery("hey");
         // String res=db.getDetails(barcode);
         //  System.out.println(new File("").getAbsolutePath());  
         //  ArrayList<String> a = new ArrayList<String>();//= new ArrayList<String>();
         //  int   b= db.confirmDelivery("83", "1234", "vvv", "accpt", a, a);
         //  int   b= db.confirmDelivery("83", "1234", "vvv", "accpt", a, a);
         //    getDbConnection();
         // System.out.println(b);
       
         // prop.load(DbConnect.class.getClassLoader().getResourceAsStream("config.properties");)); 


         log.trace("This is main function");
         log.error(" testing for error");
         log.fatal("testing for fatal");
         log.debug("testing 123456");
         log.info("main function ");
         */
        //   System.out.println("Execution is continued "+res);
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to establish and return database connection 
     *----------------------------------------------------------------------------------------------------*/

    public static Connection getDbConnection() {
        log.info("getDbConnection() begin ");
        Connection conn = null;
        try {
            // Get the connection string, user name and password from the properties file
            String connectionString = properties.getProperty("connectionString");

            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(connectionString, userName, password);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        }
        log.info("getDbConnection() end");
        return conn;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to check if user name and password matches
     *----------------------------------------------------------------------------------------------------*/

    public String validateUser(String user, String pwd) {
        log.info(this.ipAddr + "|" + "validateUser() begin : user= " + user + " & pwd= " + pwd);
        String loginStatus = "NOT VALID";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Properties properties = new Properties();
            DbConnect db = new DbConnect();
            InputStream in = db.getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);

            String connectionString = properties.getProperty("connectionString");
            Connection conn = DriverManager.getConnection(connectionString, user, pwd);
            loginStatus = "VALID";
            //------------for validating the user name and password----//    

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.ERROR, "Handled Error " + ex.getMessage());

            System.out.println(ex.getMessage());
            log.info(this.ipAddr + "|" + "validateUser() loginStatus= " + loginStatus);
            log.info(this.ipAddr + "|" + "validateUser() end ");
            int sqlErr = ex.getErrorCode();
            if (sqlErr == 1017) {  // Invalid Username/Password
                return loginStatus;
            } else {
                return "!!ERROR: " + ex.getMessage() + ". PLEASE CONTACT STS/BAC.";
            }

        } catch (IOException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        }
        log.info(this.ipAddr + "|" + "validateUser() loginStatus= " + loginStatus);
        log.info(this.ipAddr + "|" + "validateUser() end ");
        return loginStatus;
    }
    
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to check user access
     *----------------------------------------------------------------------------------------------------*/

    public String securityAccess(String user, String defrmint) {
        log.info(this.ipAddr + "|" + "securityAccess() begin : user= " + user + " & defrmint= " + defrmint);
        String loginStatus = "!!ERROR: No security clearance has been given to "+user+" for this process. Please contact STSBAC.";
        if (user==null||user.trim().length()==0) {
          return "!!ERROR: Sever needs username parameter to be passed correctly.("+user+") is not a valid value. Please contact STSBAC.";
        }
        if (defrmint==null||defrmint.trim().length()==0) {
          return "!!ERROR: Sever needs screen name parameter to be passed correctly.("+defrmint+") is not a valid value. Please contact STSBAC.";
        }
        String commodityCode = ""; //TODO
        String query = " SELECT 1"
                + " FROM im86modmenu "
                + " WHERE nauser = ?"
                + "   AND defrmint = ?";
        
     /*   String query = "SELECT 1 "
                + "FROM im86modmenu "
                + "WHERE nauser = '"+user.trim().toUpperCase()+"' "
                + "  AND defrmint = '"+defrmint.trim().toUpperCase()+"'";*/
        Connection conn = getDbConnection();
//        Statement pstmt = null;
/*      try {
        pstmt = conn.createStatement();
      } catch (SQLException ex) {
        java.util.logging.Logger.getLogger(DbConnect.class.getName()).log(java.util.logging.Level.SEVERE, null, ex);
      }*/
        
        PreparedStatement pstmt = null;
       try {
             pstmt = conn.prepareStatement(query);
            pstmt.setString(1, user.trim().toUpperCase());
            pstmt.setString(2, defrmint.trim().toUpperCase());
            ResultSet result = pstmt.executeQuery();
         /*   System.out.println("SECURTY QUERY: "+query);
            ResultSet result = pstmt.executeQuery(query);*/

            while (result.next()) {
                System.out.println (user.trim().toUpperCase()+" HAS CLEARANCE");
                loginStatus = "VALID";
            }
        }
        catch (SQLException e) {
            log.error("SQL Exception in securityAccess(): ", e);
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
        }
        System.out.println ("SECURITY RETURNS "+loginStatus+" FOR DEFRMINT "+defrmint);
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
        try {
            Connection conn = getDbConnection();
            CallableStatement cs = conn.prepareCall("{?=call INV_APP.GET_INV_DETAILS(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, barcodeNum);
            cs.executeUpdate();
            details = cs.getString(1);
            System.out.println(details);
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
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
        Connection conn = getDbConnection();
        PreparedStatement pstmt = null;
        try {
            pstmt = conn.prepareStatement(query);
            pstmt.setString(1, barcode);
            ResultSet result = pstmt.executeQuery();

            while (result.next()) {
                commodityCode = result.getString(1);
            }
        }
        catch (SQLException e) {
            log.error("SQL Exception in getItemCommodityCode(): ", e);
        }
        finally {
            try {
                pstmt.close();
            }
            catch (SQLException e) {
                e.printStackTrace();
            }
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
        try {
            Connection conn = getDbConnection();
            CallableStatement cs = conn.prepareCall("{?=call INV_APP.GET_INV_LOC_CODE(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, locCode);
            cs.executeUpdate();
            details = cs.getString(1);
            System.out.println(details);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
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
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF, B.CDLOCATTO, DECODE(b.cdstatus, 'I', b.cdstatus, c.cdstatus) cdstatus "
                    + " FROM FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C"
                    + " WHERE A.CDSTATUS='A'"
                    + " AND b.cdstatus = 'A'"
                    + " AND c.cdstatus = 'A'"
                    + " AND A.NUXREFSN=B.NUXREFSN"
                    + " AND B.NUXREFCO=C.NUXREFCO"
                    + " AND b.cdlocatto = '" + locCode + "'";

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {

                VerList vl = new VerList();
                vl.NUSENATE = result.getString(1);
                vl.CDCATEGORY = result.getString(2);
                vl.DECOMMODITYF = result.getString(3);
                vl.CDLOCATTO = result.getString(4);
                vl.CDSTATUS = result.getString(5);
                itemList.add(vl);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getLocationItemList() : " + e.getMessage());
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
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
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
            ResultSet result = stmt.executeQuery(qry);
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
        }
        log.info(this.ipAddr + "|" + "getCommodityList() end");
        return commodityList;
    }    
    
    public ArrayList getLocCodes(String userFallback) {
        log.info("getLocCodes() begin  ");
        return getLocCodes("ALL", userFallback);
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocCodes(String natype, String userFallback) {
        log.info(this.ipAddr + "|" + "getLocCodes(String natype) begin : natype= " + natype);
        if (natype.isEmpty() || natype == null) {
            throw new IllegalArgumentException("Invalid location Code");
        }
        ArrayList<String> locCodes = new ArrayList<String>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();

            String qry = "select distinct cdlocat,adstreet1, cdloctype from sl16location a where a.cdstatus='A' ORDER BY cdlocat, cdloctype";
            if (natype.equalsIgnoreCase("DELIVERY")) {
                qry = "select distinct cdlocat,adstreet1, cdloctype from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatto FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A')) ORDER BY cdlocat, cdloctype";
            }

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {

                String locCode = result.getString(1);
                String adstreet1 = result.getString(2);
                String cdloctype = result.getString(3);
                String locCodeListElement = locCode + "-" + cdloctype + ": " + adstreet1;
                locCodes.add(locCodeListElement);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        log.info(this.ipAddr + "|" + "getLocCodes() end");
        return locCodes;
    }

    
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getPickupSearchByList(String userFallback) {
        ArrayList<SimpleListItem> pickupList = new ArrayList<SimpleListItem>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();

            String qry;
            qry = "select distinct 'CDLOCATTO' natype ,cdlocat||'-'||adstreet1||': '||cdloctype navalue from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatto FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A'))"
                + "UNION ALL "
                + "select distinct 'CDLOCATFROM' natype ,cdlocat||'-'||adstreet1||': '||cdloctype navalue from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatfrom FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A'))"
                + "UNION ALL "
                + "select distinct 'DTTXNORIGIN' natype, TO_CHAR(dttxnorigin, 'MM/DD/RRRR') navalue  FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A')"
                + "UNION ALL "
                + "select distinct 'NAPICKUPBY' natype, napickupby navalue  FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A')"
                + " ORDER BY  1, 2";

            ResultSet result = stmt.executeQuery(qry);
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
        }
        return pickupList;
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
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();

            // delete old data for given location code from SASS15018
            String qry = "delete from SASS15018 where CDLOCAT='" + cdlocat + "'";

            ResultSet result2 = stmt.executeQuery(qry);

            for (int i = 0; i < invItems.size(); i++) {
               InvItem curInvItem =  invItems.get(i);
               
               // left padding 0 to string 
               String barcodeStr = String.format("%6s", curInvItem.getNusenate()).replace(' ', '0');
               if (curInvItem.getType().equalsIgnoreCase("NEW")||curInvItem.getType().equalsIgnoreCase("INACTIVE")) {
                  CallableStatement cs = conn.prepareCall("{?=call inv_app.store_new_inv_item(?,?, ?, ?, ?, ?)}");                  
                  cs.registerOutParameter(1, Types.VARCHAR);
                  cs.setString(2, cdlocat);
                  cs.setString(3, curInvItem.getNusenate());
                  cs.setString(4, cdloctype);
                  cs.setString(5, curInvItem.getCdcommodity());
                  cs.setString(6, curInvItem.getDecomments());
                  cs.setString(7, "VERIFICATION");
                  cs.executeUpdate();
                  r = cs.getString(1);
               }
               else {
                  CallableStatement cs = conn.prepareCall("{?=call INV_APP.copy_data(?,?, ?)}");
                  cs.registerOutParameter(1, Types.VARCHAR);
                  cs.setString(2, cdlocat);
                  cs.setString(3, barcodeStr);
                  cs.setString(4, cdloctype);
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
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, this.ipAddr + "|" + ex.getMessage());
        }
        log.info(this.ipAddr + "|" + "setBarcodesInDatabase() end");
        
        
        return result;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to start a new pickup-delivery
     *----------------------------------------------------------------------------------------------------*/
    public int invTransit(Pickup pickup, String userFallback) {
        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String qry = "SELECT FM12INVINTRANS_SEQN.nextval FROM  dual ";
            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                pickup.setNuxrpd(result.getInt(1));
            }
            String updQry = "INSERT INTO FM12INVINTRANS (NUXRPD,CDLOCATTO, cdloctypeto, CDLOCATFROM, cdloctypefrm, CDINTRANSIT,"
                    + "NAPICKUPBY, NARELEASEBY,NUXRRELSIGN,NADELIVERBY,NAACCEPTBY,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,"
                    + "NATXNUPDUSER,DEPUCOMMENTS, DTPICKUP) "
                    + "VALUES(" + pickup.getNuxrpd() + ",'" + pickup.getDestination().getCdLoc() + "','" + pickup.getDestination().getCdLocType()
                    + "','" + pickup.getOrigin().getCdLoc() + "','" + pickup.getOrigin().getCdLocType() + "','" + "Y" + "','"
                    + pickup.getNaPickupBy() + "','" + pickup.getNaReleaseBy() + "'," + pickup.getNuxrRelSign() + ",'" + "" + "','" + ""
                    + "','" + "A" + "',SYSDATE,SYSDATE,'" + pickup.getNaPickupBy() + "','" + pickup.getNaPickupBy() + "','"
                    + pickup.getComments() + "',SYSDATE)";
            stmt.executeQuery(updQry);
            log.info("** updQry *** : " + updQry);
            log.info("****PICKUPITEMS: " + pickup.getPickupItems());

            for (String nusenate : pickup.getPickupItems()) {
                String insertQry = "INSERT INTO FD12INVINTRANS (NUXRPD,NUSENATE,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,NATXNUPDUSER) "
                        + "VALUES(" + pickup.getNuxrpd() + ",'" + nusenate + "','" + "A" + "',SYSDATE,SYSDATE,'" + pickup.getNaPickupBy()
                        + "','" + pickup.getNaPickupBy() + "')";
                stmt.executeQuery(insertQry);
            }
            conn.close();
        }
        catch (SQLException ex) {
            log.fatal("SQL error in invTransit ", ex);
            return -1;
        }
        return pickup.getNuxrpd();
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the in transit deliveries to the given location
     *----------------------------------------------------------------------------------------------------*/

    public List<PickupGroup> getDeliveryList(String locCode, String userFallback) {
        log.info(this.ipAddr + "|" + "getDeliveryList() begin : locCode= " + locCode);
        if (locCode.isEmpty()) {
            throw new IllegalArgumentException("Invalid locCode");
        }
        java.lang.reflect.Type listOfTestObject = new TypeToken<List<PickupGroup>>() {
        }.getType();
        List<PickupGroup> pickupList = Collections.synchronizedList(new ArrayList<PickupGroup>());
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT a.nuxrpd, TO_CHAR(a.dtpickup, 'MM/DD/RR HH:MI:SSAM') dtpickup, a.cdlocatfrom, a.napickupby, a.nareleaseby, c.adstreet1, c.adcity, c.adstate, c.adzipcode, COUNT(b.nuxrpd) nucount "
                    + " FROM FM12INVINTRANS a, FD12INVINTRANS b, sl16location c"
                    + " WHERE a.CDSTATUS='A'"
                    + " AND a.CDINTRANSIT='Y'"
                    + " AND a.CDLOCATTO='" + locCode + "'"
                    + " AND b.nuxrpd = a.nuxrpd"
                    + " AND b.cdstatus = 'A'"
                    + " AND c.cdlocat = a.cdlocatfrom"
                    + " GROUP BY a.nuxrpd, a.dtpickup, a.cdlocatfrom, a.napickupby, a.nareleaseby, c.adstreet1, c.adcity, c.adstate, c.adzipcode"
                    + " ORDER BY a.dtpickup NULLS LAST";
            System.out.println(qry);
            ResultSet result = stmt.executeQuery(qry);
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
                pickupList.add(new PickupGroup(nuxrpd, dtpickup, napickupby, nareleaseby, cdlocatfrom, adstreet1, adcity, adstate, adzipcode, nucount));
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getDeliveryList() : " + e.getMessage());
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
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF,e.nuxrpd,b.cdlocatto, e.cdlocatto, e.cdintransit FROM "
                    + " FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C,fd12invintrans d,fm12invintrans e "
                    + " WHERE A.CDSTATUS='A' "
                    + " AND A.NUXREFSN=B.NUXREFSN "
                    + " AND B.NUXREFCO=C.NUXREFCO "
                    + " and a.nusenate=d.nusenate "
                    + " AND d.nuxrpd =e.nuxrpd "
                    + " and e.nuxrpd=" + nuxrpd;
            ResultSet result = stmt.executeQuery(qry);
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

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getDeliveryDetails() : " + e.getMessage());
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
        Connection con = getDbConnection();
        if (con==null) {
            log.fatal(this.ipAddr + "|" + "Null Connection in insertSignature() after getDbConnection().");
        }
        System.out.println("DbConnect insertSignature byte Image Length:" + imageInArray.length);

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

        PreparedStatement ps;
        try {
            Statement stmtSequence = con.createStatement();
            if (con==null) {
                System.out.println("insertSignature Connection was NULL when creating statement from it");
            }
            else if (stmtSequence==null) {
                System.out.println("insertSignature could not createStatement from Connection");
                
            }
            ResultSet rsSequence = stmtSequence.executeQuery("select FP12SIGNREF_SQNC.NEXTVAL FROM DUAL");

            while (rsSequence.next()) {
                nuxrsign = rsSequence.getInt(1);
            }

            con.setAutoCommit(false);
            //blobValue = new SerialBlob(imageInArray);
            System.out.println ("insert into FD12INVSIGNS nuxrsign:"+nuxrsign+", nuxrefem:"+nuxrefem+", nauser:"+nauser);
            ps = con.prepareStatement("insert into FD12INVSIGNS (nuxrsign, blsign, nuxrefem, cdstatus, natxnorguser, natxnupduser, dttxnorigin, dttxnupdate ) values(?, empty_blob(), ?, 'A', ?,  ?, SYSDATE, SYSDATE )");
            ps.setInt(1, nuxrsign);
            ps.setInt(2, nuxrefem);
            ps.setString(3, nauser);
            ps.setString(4, nauser);
            // size must be converted to int otherwise it results in error
//            ps.setBlob(2, blobValue);
            ps.executeUpdate();
            con.commit();
            System.out.println(imageInArray.length + " bytes should have been saved to PCIMAGE");

            Statement stmt = con.createStatement();
            ResultSet rs = stmt.executeQuery("select blsign from FD12INVSIGNS where nuxrsign=" + nuxrsign + " for update");
            BLOB writeBlob = null;

            if (rs.next()) {
                System.out.println("RECORD TO WRITE BLOB");
                writeBlob = (BLOB) rs.getBlob(1);
            } else {
                System.out.println("handelSaveAskTree(): BLOB object could not be found...");
            }
            OutputStream outStream = writeBlob.getBinaryOutputStream();
            outStream.write(imageInArray);
            outStream.close();
            outStream = null;
            con.commit();

        } catch (SQLException ex) {
            System.out.println("!!!!!!!!!!SQL EXCEPTION OCCURED:"+ ex.getMessage());
            System.out.println(ex.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in insertSignature() : " + ex.getMessage());
        } catch (IOException ex) {
            System.out.println("!!!!!!!!!!IO EXCEPTION OCCURED:"+ ex.getMessage());
            ex.printStackTrace();
            log.fatal(this.ipAddr + "|" + "IOException in insertSignature() : " + ex.getMessage());
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
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            if (nalast == null) {
                nalast = "";
            }
            //  String loc_code;
            String qry = "SELECT a.nuxrefem, a.nafirst, a.nalast, a.namidinit, a.nasuffix"
                    + " FROM pm21personn a "
                    + " WHERE a.cdempstatus LIKE '" + cdempstatus + "'"
                    + "  AND a.nalast LIKE'" + nalast + "%'"
                    + " ORDER BY  a.nalast||DECODE(a.nasuffix, NULL, NULL, ' '||a.nasuffix)||', '||a.nafirst||DECODE(a.namidinit, NULL, NULL, ' '||a.namidinit)";


            System.out.println("QRY:" + qry);
            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {

                Employee employee = new Employee();
                employee.setEmployeeData(result.getInt(1), result.getString(2), result.getString(3), result.getString(4), result.getString(5));
                employeeList.add(employee);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in getEmployeeList() : " + e.getMessage());
        }
        log.info(this.ipAddr + "|" + "getEmployeeList() end");
        return employeeList;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to confirm delivery i.e. updates the FD12Issue table and changes location-----
     *------------------------------------------------------------------------------------------------------*/
    public int confirmDelivery(Delivery delivery, String userFallback) {
        log.info(this.ipAddr + "|" + "confirmDelivery() begin.");

        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "SELECT CDLOCATTO,CDLOCTYPETO,CDLOCATFROM,CDLOCTYPEFRM "
                    + " FROM fm12invintrans  "
                    + " WHERE CDSTATUS='A' "
                    + " AND nuxrpd=" + delivery.getNuxrpd();

            ResultSet res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                delivery.getDestination().setCdLoc(res1.getString(1));
                delivery.getDestination().setCdLocType(res1.getString(2));
                delivery.getOrigin().setCdLoc(res1.getString(3));
                delivery.getOrigin().setCdLocType(res1.getString(4));
            }

            // Update its entry in FM12InvInTrans to show it is delivered.
            String query = "UPDATE fm12invintrans "
                    + "SET CDINTRANSIT='N' "
                    + " ,DTTXNUPDATE=SYSDATE "
                    + " ,NATXNUPDUSER=USER "
                    + " ,NUXRACCPTSIGN=" + delivery.getNuxrAccptSign()
                    + " ,NADELIVERBY='" + delivery.getNaDeliverBy()
                    + "' ,NAACCEPTBY='" + delivery.getNaAcceptBy()
                    + "' ,DTDELIVERY=SYSDATE "
                    + "  ,DEDELCOMMENTS='" + delivery.getComments()
                    + "' WHERE NUXRPD=" + delivery.getNuxrpd();
            stmt.executeUpdate(query);
            conn.commit();

            // Move delivered Items to their new location.
            for (String item : delivery.getCheckedItems()) {
                String nusenate = item;
                CallableStatement cs = conn.prepareCall("{?=call move_inventory_item(?,?,?,?,?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, nusenate);
                cs.setString(3, delivery.getOrigin().getCdLoc());
                cs.setString(4, delivery.getOrigin().getCdLocType());
                cs.setString(5, delivery.getDestination().getCdLoc());
                cs.setString(6, delivery.getDestination().getCdLocType());
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
            conn.close();
        }
        catch (SQLException ex) {
            log.fatal("SQL error in confirmDelivery(). ", ex);
        }
        return 0;
    }

   public Employee getEmployeeWhoSigned(String nuxrsign, String userFallback) {
     return getEmployeeWhoSigned(nuxrsign, true, userFallback);
   }
    
   public Employee getEmployeeWhoSigned(String nuxrsign, boolean upperCase, String userFallback) {
     String nuxrefem = "";
     Connection conn = getDbConnection();
     Statement stmt;
     try {
          stmt = conn.createStatement();
          // Get location info for this transaction.
          String qry1 = "SELECT nuxrefem "
                    + " FROM fd12invsigns  "
                    + " WHERE cdstatus = 'A' "
                    + " AND nuxrsign = " + nuxrsign;

          ResultSet res1 = stmt.executeQuery(qry1);
           while (res1.next()) {
              nuxrefem = res1.getString(1);
            }
           stmt.close();
           conn.close();
        }
     catch (SQLException ex) {
        log.fatal("SQL error in getEmployeeWhoSigned(). ", ex);
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
        
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
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
                    
            ResultSet result = stmt.executeQuery(qry);
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
        }
        log.info(this.ipAddr + "|" + "getEmployee() end");
        return currentEmployee;
    }    
    

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to create new delivery i.e. inserts new records into FM12InvInTrans-----
     *----------------------------------------------------------------------------------------------------*/
    public int createNewPickup(Delivery delivery, String userFallback) {
        log.info(this.ipAddr + "|" + "createNewDelivery() begin :");

        Pickup pickup = new Pickup();
        pickup.setPickupItems(delivery.getNotCheckedItems());

        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String qry = "SELECT NUXRPD,CDLOCATFROM, CDLOCTYPEFRM, CDLOCATTO,CDLOCTYPETO, NAPICKUPBY, NARELEASEBY, NUXRRELSIGN FROM   "
                    + "  FM12INVINTRANS"
                    + " WHERE CDSTATUS='A'"
                    + " and nuxrpd=" + delivery.getNuxrpd();

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                String nuxrpd = result.getString(1);
                pickup.getOrigin().setCdLoc(result.getString(2));
                pickup.getOrigin().setCdLocType(result.getString(3));
                pickup.getDestination().setCdLoc(result.getString(4));
                pickup.getDestination().setCdLocType(result.getString(5));
                pickup.setNaPickupBy(result.getString(6));
                pickup.setNaReleaseBy(result.getString(7));
                pickup.setNuxrRelSign(result.getString(8));
            }
            conn.close();
        }
        catch (SQLException ex) {
            log.fatal(this.ipAddr + "|" + "Error getting pickup info in createNewDelivery(). ", ex);
        }

        DbConnect db = new DbConnect();
        db.invTransit(pickup, userFallback);
        log.info(this.ipAddr + "|" + "createNewDelivery() end ");
        return 0;
    }

    public void setUsernamePwd(String user, String pwd) {
        userName = user;
        password = pwd;
    }

}
