package gov.nysenate.inventory.server;

import com.google.gson.reflect.TypeToken;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.FileNotFoundException;
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
/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

/**
 *
 * @author Patil
 */
public class DbConnect {
    String ipAddr="";
static Logger log = Logger.getLogger(DbConnect.class.getName());
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

            Properties properties = new Properties();
            DbConnect db = new DbConnect();
            InputStream in = db.getClass().getClassLoader().getResourceAsStream("config.properties");
            properties.load(in);
            String connectionString = properties.getProperty("connectionString");
            String userName = properties.getProperty("user");
            String password = properties.getProperty("password");

            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(connectionString, userName, password);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        } catch (IOException ex) {
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

    public String getDetails(String barcodeNum) {
        log.info(this.ipAddr+"|"+"getDetails() begin : barcodeNum= " + barcodeNum);
        if ((Integer.parseInt(barcodeNum) <= 0)) {
           System.out.println("Error in DbConnect.getDetails() - Barcode Number Not Valid");
            log.error(this.ipAddr+"|"+"Error in DbConnect.getDetails() - Barcode Number Not Valid");
           throw new IllegalArgumentException("Invalid Barcode Number");
       }
       String details = null;
        try {
            Connection conn = getDbConnection();
            CallableStatement cs = conn.prepareCall("{?=call PATIL.INV_APP.GET_INV_DETAILS(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, barcodeNum);
            cs.executeUpdate();
            details = cs.getString(1);
            System.out.println(details);
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        }
        log.info(this.ipAddr+"|"+"getDetails() details = " + details);
        log.info(this.ipAddr+"|"+"getDetails() end ");
        return details;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return details related to given location code( Address, type etc) 
     *----------------------------------------------------------------------------------------------------*/

    public String getInvLocDetails(String locCode) {
       log.info(this.ipAddr+"|"+"getInvLocDetails() begin : locCode= " + locCode);
        if (locCode.isEmpty() || locCode == null) {
            log.info(this.ipAddr+"|"+"Invalid location Code " + locCode);
            throw new IllegalArgumentException("Invalid location Code");
        }   
        String details = null;
        try {
            Connection conn = getDbConnection();
            CallableStatement cs = conn.prepareCall("{?=call PATIL.INV_APP.GET_INV_LOC_CODE(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setString(2, locCode);
            cs.executeUpdate();
            details = cs.getString(1);
            System.out.println(details);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
        }
        log.info(this.ipAddr+"|"+"getInvLocDetails() end ");
        return details;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the items at a given location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocationItemList(String locCode) {
        log.info(this.ipAddr+"|"+"getLocationItemList() begin : locCode= " + locCode);
        if (locCode.isEmpty() || locCode == null) {
             throw new IllegalArgumentException("Invalid location Code");
        } 
         
        ArrayList<VerList> itemList = new ArrayList<VerList>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF, B.CDLOCATTO FROM   "
                    + "  FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C"
                    + " WHERE A.CDSTATUS='A'"
                    + " AND A.NUXREFSN=B.NUXREFSN"
                    + " AND B.NUXREFCO=C.NUXREFCO"
                    + " and b.cdlocatto = '" + locCode + "'";

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {

                VerList vl = new VerList();
                vl.NUSENATE = result.getString(1);
                vl.CDCATEGORY = result.getString(2);
                vl.DECOMMODITYF = result.getString(3);
                vl.CDLOCATTO = result.getString(4);
                itemList.add(vl);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr+"|"+"SQLException in getLocationItemList() : " + e.getMessage());
        }
        log.info(this.ipAddr+"|"+"getLocationItemList() end");
        return itemList;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocCodes() {
        log.info("getLocCodes() begin  ");
        return getLocCodes("ALL");
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocCodes(String natype) {
        log.info(this.ipAddr+"|"+"getLocCodes(String natype) begin : natype= " + natype);
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
        log.info(this.ipAddr+"|"+"getLocCodes() end");
        return locCodes;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to insert items found at given location(barcodes) for verification
     *----------------------------------------------------------------------------------------------------*/
    public int setBarcodesInDatabase(String cdlocat, String barcodes[]) {
        log.info(this.ipAddr+"|"+"setBarcodesInDatabase() begin : cdlocat= " + cdlocat + " &barcodes= " + barcodes);
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
                CallableStatement cs = conn.prepareCall("{?=call PATIL.INV_APP.copy_data(?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, cdlocat);
                cs.setString(3, barcodeStr);
                cs.executeUpdate();
                r = cs.getString(1);
                System.out.println(r);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, this.ipAddr+"|"+ex.getMessage());
        }
        log.info(this.ipAddr+"|"+"setBarcodesInDatabase() end");
        return result;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to start a new pickup-delivery
     *----------------------------------------------------------------------------------------------------*/

    public int invTransit(String CDLOCATFROM, String CDLOCATTO, String[] barcode, String NAPICKUPBY, String NARELEASEBY, String NUXRRELSIGN, String NADELIVERBY, String NAACCEPTBY, String NUXRACCPTSIGN, String DEPUCOMMENTS) {
        log.info(this.ipAddr+"|"+"invTransit() begin : CDLOCATFROM = " + CDLOCATFROM + " &CDLOCATTO= " + CDLOCATTO + " &barcode= " + barcode + " &NAPICKUPBY= " + NAPICKUPBY + " &NARELEASEBY= " + NARELEASEBY + " &NUXRRELSIGN= " + NUXRRELSIGN + " &NADELIVERBY= " + NADELIVERBY + " &NAACCEPTBY= " + NAACCEPTBY + " &NUXRACCPTSIGN= " + NUXRACCPTSIGN + " &DEPUCOMMENTS= " + DEPUCOMMENTS);
        if (CDLOCATFROM.isEmpty() || CDLOCATTO == null || barcode == null) {
             throw new IllegalArgumentException("Invalid CDLOCATFROM or CDLOCATTO or barcode");
        } 
        int nuxrpd = 0;

        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();

            System.out.println("!!!!!!NUXRRELSIGN:(" + NUXRRELSIGN + ")");

            // 1. get nuxrpickup -- using sequences 
            if (NUXRRELSIGN == null || NUXRRELSIGN.trim().length() == 0) {
                try {
                    NUXRRELSIGN = "null";
                    boolean foundNuxrelsign = false;
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }
            if (NUXRACCPTSIGN == null || NUXRACCPTSIGN.trim().length() == 0) {
                try {
                    NUXRACCPTSIGN = "null";
                } catch (Exception e) {
                    e.printStackTrace();
                }
            }

            String qry = "SELECT FM12INVINTRANS_SEQN.nextval FROM  dual ";
            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                nuxrpd = result.getInt(1);
            }

            //2. insert into FM12INVinTRANS    

            String updQry = "INSERT INTO FM12INVINTRANS (NUXRPD,CDLOCATTO,CDLOCATFROM,CDINTRANSIT, NAPICKUPBY, NARELEASEBY,NUXRRELSIGN,NADELIVERBY,NAACCEPTBY,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,NATXNUPDUSER,DEPUCOMMENTS, DTPICKUP) "
                    + "VALUES(" + nuxrpd + ",'" + CDLOCATTO + "','" + CDLOCATFROM + "','" + "Y" + "','" + NAPICKUPBY + "','" + NARELEASEBY + "'," + NUXRRELSIGN + ",'" + NAACCEPTBY + "'," + NUXRACCPTSIGN + ",'" + "A" + "',SYSDATE,SYSDATE,'" + NAPICKUPBY + "','" + NAPICKUPBY + "','" + DEPUCOMMENTS + "',SYSDATE)";
            System.out.println("inside 3 query : " + updQry);
            ResultSet result2 = stmt.executeQuery(updQry);


            // 3. insert barcodes into FD12INVINTRANS      

            for (int i = 0; i < barcode.length; i++) {
                String insertQry = "INSERT INTO FD12INVINTRANS (NUXRPD,NUSENATE,CDSTATUS,DTTXNORIGIN,DTTXNUPDATE,NATXNORGUSER,NATXNUPDUSER) "
                        + "VALUES(" + nuxrpd + ",'" + barcode[i] + "','" + "A" + "',SYSDATE,SYSDATE,'" + NAPICKUPBY + "','" + NAPICKUPBY + "')";

                ResultSet result3 = stmt.executeQuery(insertQry);
            }
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.FATAL, null, ex);
            return -1;
        }
        log.info(this.ipAddr+"|"+"invTransit() end");
        return nuxrpd;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the in transit deliveries to the given location
     *----------------------------------------------------------------------------------------------------*/

    public List<PickupGroup> getDeliveryList(String locCode) {
        log.info(this.ipAddr+"|"+"getDeliveryList() begin : locCode= " + locCode);
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
                pickupList.add(new PickupGroup(nuxrpd, dtpickup, napickupby, nareleaseby, cdlocatfrom,  adstreet1, adcity, adstate, adzipcode, nucount));
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr+"|"+"SQLException in getDeliveryList() : " + e.getMessage());
        }
        log.info(this.ipAddr+"|"+"getDeliveryList() end");
        return pickupList;

    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the items related to a perticular delivery nuxrpd
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList<InvItem> getDeliveryDetails(String nuxrpd) {
        log.info(this.ipAddr+"|"+"getDeliveryDetails() begin : nuxrpd= " + nuxrpd);
        if (nuxrpd.isEmpty()) {
             throw new IllegalArgumentException("Invalid locCode");
        } 
        ArrayList<InvItem> deliveryDetails = new ArrayList<InvItem>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF,e.nuxrpd,b.cdlocatto, e.cdlocatto FROM "
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
                InvItem curInvItem = new InvItem(nusenate,  cdcategory, "EXISTING", decommodityf);
                curInvItem.setCdlocat(cdlocat);
                curInvItem.setCdlocatto(cdlocatto);
                deliveryDetails.add(curInvItem);
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr+"|"+"SQLException in getDeliveryDetails() : " + e.getMessage());
        }
        log.info(this.ipAddr+"|"+"getDeliveryDetails() end");
        return deliveryDetails;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to 
     *----------------------------------------------------------------------------------------------------*/

    int invPickup(String originLocation, String destinationLocation, String[] barcodes, String NAPICKUPBY, String NARELEASEBY, String NUXRRELSIGN, String NADELIVERBY, String NAACCEPTBY, String NUXRACCPTSIGN) {
        throw new UnsupportedOperationException("Not supported yet."); //To change body of generated methods, choose Tools | Templates.
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to insert signature into database
     *----------------------------------------------------------------------------------------------------*/

    public int insertSignature(byte[] imageInArray, int nuxrefem, String nauser) {
        log.info(this.ipAddr+"|"+"insertSignature() begin : nuxrefem= " + nuxrefem + " &nauser=" + nauser);
        if (imageInArray == null || nuxrefem < 0 || nauser == null) {
           throw new IllegalArgumentException("Invalid imageInArray or nuxrefem or nauser");
       }
        Connection con = getDbConnection();
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
            log.fatal(this.ipAddr+"|"+"Exception in insertSignature() : " + e.getMessage());
        }

        PreparedStatement ps;
        try {
            Statement stmtSequence = con.createStatement();
            ResultSet rsSequence = stmtSequence.executeQuery("select FP12SIGNREF_SQNC.NEXTVAL FROM DUAL");

            while (rsSequence.next()) {
                nuxrsign = rsSequence.getInt(1);
            }

            con.setAutoCommit(false);
            //blobValue = new SerialBlob(imageInArray);
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
            System.out.println("!!!!!!!!!!SQL EXCEPTION OCCURED");
            System.out.println(ex.getMessage());
            log.fatal(this.ipAddr+"|"+"SQLException in insertSignature() : " + ex.getMessage());
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        log.info(this.ipAddr+"|"+"insertSignature() end");
        return nuxrsign;

    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to 
     *----------------------------------------------------------------------------------------------------*/
    public ArrayList<Employee> getEmployeeList(String nalast) {
     //   if(nalast==null){
     //       throw new IllegalArgumentException("Invalid nalast");
     //   }
        log.info(this.ipAddr+"|"+"getEmployeeList(String nalast) begin : nalast= " + nalast);
        return getEmployeeList(nalast, "A");
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return the list of employee names
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList<Employee> getEmployeeList(String nalast, String cdempstatus) {
        log.info(this.ipAddr+"|"+"getEmployeeList(String nalast, String cdempstatus) begin : nalast= " + nalast + " &cdempstatus=" + cdempstatus);
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
            String qry = "SELECT a.nuxrefem, a.nalast, a.nafirst, a.namidinit, a.nasuffix"
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
            log.fatal(this.ipAddr+"|"+"SQLException in getEmployeeList() : " + e.getMessage());
        }
        log.info(this.ipAddr+"|"+"getEmployeeList() end");
        return employeeList;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to confirm delivery i.e. updates the FD12Issue table and changes location-----
     *------------------------------------------------------------------------------------------------------*/
    public int confirmDelivery(String nuxrpd, String NUXRACCPTSIGN, String NADELIVERBY, String NAACCEPTBY, ArrayList deliveryList, ArrayList notDeliveredList, String DEDELCOMMENTS) {
        log.info(this.ipAddr + "|" + "getEmployeeList() begin : nuxrpd= " + nuxrpd + " &NUXRACCPTSIGN=" + NUXRACCPTSIGN + " &NADELIVERBY=" + NADELIVERBY + " &NAACCEPTBY=" + NAACCEPTBY + " &deliveryList=" + deliveryList);
        System.out.println("confirmDelivery nuxrpd " + nuxrpd);
        int result = -1;
   try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
    
            //1. update the master table 
            // Get data from the fm12invintrans table for calling function
        
            String cdlocatfrom = "";
            String CDLOCTYPEFRM = "";
            String cdlocatto = "";
            String CDLOCTYPETO = "";
        
  
            String qry1 = "SELECT CDLOCATTO,CDLOCTYPETO,CDLOCATFROM,CDLOCTYPEFRM FROM "
                    + "fm12invintrans  "
                    + " WHERE CDSTATUS='A' "
                    + " and nuxrpd=" + nuxrpd;
            
         
            ResultSet res1 = stmt.executeQuery(qry1);
            while (res1.next()) {
                cdlocatto = res1.getString(1);
                CDLOCTYPETO = res1.getString(2);
                cdlocatfrom = res1.getString(3);
                CDLOCTYPEFRM = res1.getString(4);
            }
            

            //System.out.println ("(confirmDelivery) updating current delivery nuxrpd:"+nuxrpd);
            
            String query = "update FM12invintrans "
                    + "set CDINTRANSIT='N' "
                    + " ,DTTXNUPDATE=SYSDATE "
                    + " ,NATXNUPDUSER=USER "
                    + " ,NUXRACCPTSIGN=" + NUXRACCPTSIGN
                    + " ,NADELIVERBY='" + NADELIVERBY
                    + "' ,NAACCEPTBY='" + NAACCEPTBY
                    + "' ,DTDELIVERY=SYSDATE "
                    + "  ,DEDELCOMMENTS='" + DEDELCOMMENTS
                    + "' where NUXRPD=" + nuxrpd;
           result = stmt.executeUpdate(query);
            conn.commit();
            System.out.println("(confirmDelivery):" + query);
 
           conn.commit();
    
            //2. update the details table 
                   // we dont need to update the details table since we are marking the record in master as N   
            //3. call the function to move the items in database
           
            // work on it and call the function multiple times for each item in the list
           
           
            for (int i = 0; i < deliveryList.size(); i++) {
                String nusenate = deliveryList.get(i).toString();
                CallableStatement cs = conn.prepareCall("{?=call PATIL.move_inventory_item(?,?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, nusenate);
                cs.setString(3, cdlocatfrom);
                cs.setString(4, cdlocatto);
                cs.executeUpdate();
                String r = cs.getString(1);

            }

           //3. return result
          result = 0;
          conn.close();
    
   } catch (SQLException ex) {
                 System.out.println(ex.getMessage());
            log.fatal(this.ipAddr + "|" + "SQLException in confirmDelivery() : " + ex.getMessage());
        } 
        log.info(this.ipAddr + "|" + "confirmDelivery() end ");
   return result;
}


    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to create new delivery i.e. inserts new records into FM12InvInTrans-----
     *----------------------------------------------------------------------------------------------------*/
    public int createNewDelivery(String nuxrpd, String[] barcode) {
        log.info(this.ipAddr + "|" + "createNewDelivery() begin : nuxrpd= " + nuxrpd + " &barcode= " + barcode);
    /*  if(nuxrpd==null||barcode==null){
          throw new IllegalArgumentException("Invalid nuxrpd or barcode");
      }*/
        try {
            String CDLOCATFROM = "";
            String CDLOCATTO = "";
            String NAPICKUPBY = "";
            String NUXRPUSIGN = "1234";
            String NARELEASEBY = "";
            String NUXRRELSIGN = "";
            String NADELIVERBY = "";
            String NAACCEPTBY = "";
            String NUXRACCPTSIGN = "";
            String DECOMMENT = "";


            System.out.println("(createNewDelivery) from nuxrpd:" + nuxrpd);
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            // Get the details from the master table
            String qry = "SELECT NUXRPD,CDLOCATFROM ,CDLOCATTO ,NAPICKUPBY, NARELEASEBY, NUXRRELSIGN FROM   "
                    + "  FM12INVINTRANS"
                    + " WHERE CDSTATUS='A'"
                    + " AND CDINTRANSIT='Y'"
                    + " and nuxrpd='" + nuxrpd + "'";

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                String NUXRPD = result.getString(1);
                CDLOCATFROM = result.getString(2);
                CDLOCATTO = result.getString(3);
                NAPICKUPBY = result.getString(4);
                //              NUXRPUSIGN = result.getString(5);
                NARELEASEBY = result.getString(5);
                NUXRRELSIGN = result.getString(6);
            }
            System.out.println("createNewDelivery");
            System.out.println("CDLOCATFROM  " + CDLOCATFROM + "CDLOCATTO  " + CDLOCATTO + "NAPICKUPBY  " + NAPICKUPBY);

            // Call invTransit() function 
            DbConnect db = new DbConnect();
            db.invTransit(CDLOCATFROM, CDLOCATTO, barcode, NAPICKUPBY, NARELEASEBY, NUXRRELSIGN, NADELIVERBY, NAACCEPTBY, NUXRACCPTSIGN, DECOMMENT);
            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            log.fatal(this.ipAddr + "|" + "createNewDelivery() " + e.getMessage());
        }
        log.info(this.ipAddr + "|" + "createNewDelivery() end ");
        return 0;
    }
}
