package gov.nysenate.inventory.server;

import gov.nysenate.inventory.model.Transaction;

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
     * ---------------Function to return details of given barcode (item details)
     *----------------------------------------------------------------------------------------------------*/

    public String getDetails(String barcodeNum, String userFallback) {
        log.info(this.ipAddr + "|" + "getDetails() begin : barcodeNum= " + barcodeNum);
        if ((Integer.parseInt(barcodeNum) <= 0)) {
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
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

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
     * ---------------Function to insert items found at given location(barcodes) for verification
     *----------------------------------------------------------------------------------------------------*/
    public int setBarcodesInDatabase(String cdlocat, String barcodes[], String userFallback) {
        log.info(this.ipAddr + "|" + "setBarcodesInDatabase() begin : cdlocat= " + cdlocat + " &barcodes= " + barcodes);
        if (cdlocat.isEmpty() || barcodes == null) {
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

            for (int i = 0; i < barcodes.length; i++) {
                // left padding 0 to string 
                String barcodeStr = String.format("%6s", barcodes[i]).replace(' ', '0');
                CallableStatement cs = conn.prepareCall("{?=call INV_APP.copy_data(?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, cdlocat);
                cs.setString(3, barcodeStr);
                cs.executeUpdate();
                r = cs.getString(1);
                System.out.println(r);
            }
        }
        catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, this.ipAddr + "|" + ex.getMessage());
        }
        log.info(this.ipAddr + "|" + "setBarcodesInDatabase() end");
        return result;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to start a new pickup-delivery
     *----------------------------------------------------------------------------------------------------*/
    public int invTransit(Transaction trans, String userFallback) {
        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String qry = "SELECT FM12INVINTRANS_SEQN.nextval FROM  dual ";
            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                trans.setNuxrpd(result.getInt(1));
            } 
            String updQry = "INSERT INTO FM12INVINTRANS (NUXRPD,CDLOCATTO, cdloctypeto, CDLOCATFROM, cdloctypefrm, CDINTRANSIT,"
                    + "NAPICKUPBY, NARELEASEBY,NUXRRELSIGN,NADELIVERBY,NAACCEPTBY,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,"
                    + "NATXNUPDUSER,DEPUCOMMENTS, DTPICKUP) "
                    + "VALUES(" + trans.getNuxrpd() + ",'" + trans.getDestination().getCdLoc() + "','" + trans.getDestination().getCdLocType()
                    + "','" + trans.getOrigin().getCdLoc() + "','" + trans.getOrigin().getCdLocType() + "','" + "Y" + "','"
                    + trans.getPickup().getNaPickupBy() + "','" + trans.getPickup().getNaReleaseBy() + "'," + trans.getPickup().getNuxrRelSign()
                    + ",'" + trans.getDelivery().getNaDeliverBy() + "','" + trans.getDelivery().getNaAcceptBy() + "','" + "A"
                    + "',SYSDATE,SYSDATE,'" + trans.getPickup().getNaPickupBy() + "','" + trans.getPickup().getNaPickupBy() + "','"
                    + trans.getPickup().getComments() + "',SYSDATE)";
            stmt.executeQuery(updQry);
            log.info("** updQry *** : " + updQry);


            for (String nusenate : trans.getPickup().getPickupItems()) {
                String insertQry = "INSERT INTO FD12INVINTRANS (NUXRPD,NUSENATE,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,NATXNUPDUSER) "
                        + "VALUES(" + trans.getNuxrpd() + ",'" + nusenate + "','" + "A" + "',SYSDATE,SYSDATE,'" + trans.getPickup().getNaPickupBy()
                        + "','" + trans.getPickup().getNaPickupBy() + "')";
                stmt.executeQuery(insertQry);
            }
            conn.close();
        }
        catch (SQLException ex) {
            log.fatal("SQL error in invTransit ", ex);
            return -1;
        }
        return trans.getNuxrpd();
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


        // If the Image was a PNG with a  transparent Background, below will convert it to a white background
        // jpg.
        try {
            BufferedImage bufferedImage = ImageIO.read(new ByteArrayInputStream(imageInArray));
            bufferedImage.createGraphics().drawImage(bufferedImage, 0, 0, Color.WHITE, null);
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
    public int confirmDelivery(Transaction trans, String userFallback) {
        log.info(this.ipAddr + "|" + "confirmDelivery() begin.");

        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            // Get location info for this transaction.
            String qry1 = "SELECT CDLOCATTO,CDLOCTYPETO,CDLOCATFROM,CDLOCTYPEFRM "
                    + " FROM fm12invintrans  "
                    + " WHERE CDSTATUS='A' "
                    + " AND nuxrpd=" + trans.getNuxrpd();

            ResultSet res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                trans.getDestination().setCdLoc(res1.getString(1));
                trans.getDestination().setCdLocType(res1.getString(2));
                trans.getOrigin().setCdLoc(res1.getString(3));
                trans.getOrigin().setCdLocType(res1.getString(4));
            }

            // Update its entry in FM12InvInTrans to show it is delivered.
            String query = "UPDATE fm12invintrans "
                    + "SET CDINTRANSIT='N' "
                    + " ,DTTXNUPDATE=SYSDATE "
                    + " ,NATXNUPDUSER=USER "
                    + " ,NUXRACCPTSIGN=" + trans.getDelivery().getNuxrAccptSign()
                    + " ,NADELIVERBY='" + trans.getDelivery().getNaDeliverBy()
                    + "' ,NAACCEPTBY='" + trans.getDelivery().getNaAcceptBy()
                    + "' ,DTDELIVERY=SYSDATE "
                    + "  ,DEDELCOMMENTS='" + trans.getDelivery().getComments()
                    + "' WHERE NUXRPD=" + trans.getNuxrpd();
            stmt.executeUpdate(query);
            conn.commit();

            // Move delivered Items to their new location.
            for (String item : trans.getDelivery().getCheckedItems()) {
                String nusenate = item;
                CallableStatement cs = conn.prepareCall("{?=call move_inventory_item(?,?,?,?,?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, nusenate);
                cs.setString(3, trans.getOrigin().getCdLoc());
                cs.setString(4, trans.getOrigin().getCdLocType());
                cs.setString(5, trans.getDestination().getCdLoc());
                cs.setString(6, trans.getDestination().getCdLocType());
                cs.setString(7, String.valueOf(trans.getNuxrpd()));
                cs.executeUpdate();
            }

            // Delete items in this transaction that were not delivered, they will be put in a new transaction.
            if (trans.getDelivery().getNotCheckedItems().length > 0) {
                for (String nusenate : trans.getDelivery().getNotCheckedItems()) {
                    String del = "DELETE FROM FD12INVINTRANS WHERE nuxrpd=" + trans.getNuxrpd() + "AND nusenate = '" + nusenate + "'";
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

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to create new delivery i.e. inserts new records into FM12InvInTrans-----
     *----------------------------------------------------------------------------------------------------*/
    public int createNewDelivery(Transaction trans, String userFallback) {
        log.info(this.ipAddr + "|" + "createNewDelivery() begin :");
        Connection conn = getDbConnection();
        Statement stmt;
        try {
            stmt = conn.createStatement();
            String qry = "SELECT NUXRPD,CDLOCATFROM, CDLOCTYPEFRM, CDLOCATTO,CDLOCTYPETO, NAPICKUPBY, NARELEASEBY, NUXRRELSIGN FROM   "
                    + "  FM12INVINTRANS"
                    + " WHERE CDSTATUS='A'"
                    + " and nuxrpd=" + trans.getNuxrpd();

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                String nuxrpd = result.getString(1);
                trans.getOrigin().setCdLoc(result.getString(2));
                trans.getOrigin().setCdLocType(result.getString(3));
                trans.getDestination().setCdLoc(result.getString(4));
                trans.getDestination().setCdLocType(result.getString(5));
                trans.getPickup().setNaPickupBy(result.getString(6));
                trans.getPickup().setNaReleaseBy(result.getString(7));
                trans.getPickup().setNuxrRelSign(result.getString(8));
            }
            conn.close();
        }
        catch (SQLException ex) {
            log.fatal(this.ipAddr + "|" + "Error getting pickup info in createNewDelivery(). ", ex);
        }

        DbConnect db = new DbConnect();
        db.invTransit(trans, userFallback);
        log.info(this.ipAddr + "|" + "createNewDelivery() end ");
        return 0;
    }

    public void setUsernamePwd(String user, String pwd) {
        userName = user;
        password = pwd;
    }

}
