package gov.nysenate.inventory.server;

import com.sun.org.apache.bcel.internal.generic.Type;
import java.awt.Color;
import java.awt.image.BufferedImage;
import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.URL;
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
import java.util.Properties;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.ImageIO;
import oracle.jdbc.OracleConnection;
import oracle.jdbc.OracleDriver;
import oracle.sql.BLOB;

/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
/**
 *
 * @author Patil
 */
public class DbConnect {

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Main function for testing other functions
     *----------------------------------------------------------------------------------------------------*/
    public static void main(String args[]) {

     /*   String barcode_num = "77030";
        int barcode = Integer.valueOf(barcode_num);
        DbConnect db = new DbConnect();
        String cdlocat = "abcd";
        String barcodes[] = {"077896", "078567", "0268955"};*/
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


        
        
        System.out.println("Execution is continued");
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to establish and return database connection 
     *----------------------------------------------------------------------------------------------------*/
    public static Connection getDbConnection() {

        Connection conn = null;
        try {
            // Get the connection string, user name and password from the properties file
 
           Properties properties = new Properties();
           DbConnect db= new DbConnect();
           InputStream in =  db.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
           properties.load(in);
     
            String connectionString = properties.getProperty("connectionString");
            String userName = properties.getProperty("user");
            String password = properties.getProperty("password");
    
            Class.forName("oracle.jdbc.driver.OracleDriver");
            conn = DriverManager.getConnection(connectionString, userName, password);

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (FileNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (IOException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return conn;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to check if user name and password matches
     *----------------------------------------------------------------------------------------------------*/

    public String validateUser(String user, String pwd) {
        String loginStatus = "Not Valid";
        try {
            Class.forName("oracle.jdbc.driver.OracleDriver");
            Properties properties = new Properties();
            DbConnect db = new DbConnect();
            InputStream in = db.getClass().getClassLoader().getResourceAsStream("gov/nysenate/inventory/server/config.properties");
            properties.load(in);

            String connectionString = properties.getProperty("connectionString");
            Connection conn = DriverManager.getConnection(connectionString, user, pwd);
            loginStatus = "VALID";
            //------------for validating the user name and password----//    

        } catch (ClassNotFoundException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
            System.out.println("incorrect user");
            return loginStatus;

        } catch (IOException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return loginStatus;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return details of given barcode (item details)
     *----------------------------------------------------------------------------------------------------*/

    public String getDetails(int barcodeNum) {
       if((barcodeNum<=0) ){
           System.out.println("Error in DbConnect.getDetails() - Barcode Number Not Valid");
           throw new IllegalArgumentException("Invalid Barcode Number");
       }
       String details = null;
        try {
            Connection conn = getDbConnection();
            CallableStatement cs = conn.prepareCall("{?=call PATIL.INV_APP.GET_INV_DETAILS(?)}");
            cs.registerOutParameter(1, Types.VARCHAR);
            cs.setInt(2, barcodeNum);
            cs.executeUpdate();
            details = cs.getString(1);
            System.out.println(details);
            conn.close();
        } catch (SQLException ex) {
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return details;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return details related to given location code( Address, type etc) 
     *----------------------------------------------------------------------------------------------------*/

    public String getInvLocDetails(String locCode) {
        if(locCode.isEmpty()||locCode==null){
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
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        }

        return details;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the items at a given location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocationItemList(String locCode) {
         if(locCode.isEmpty()||locCode==null){
             throw new IllegalArgumentException("Invalid location Code");
        } 
         
        ArrayList<VerList> itemList = new ArrayList<VerList>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF FROM   "
                    + "  FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C"
                    + " WHERE A.CDSTATUS='A'"
                    + " AND A.NUXREFSN=B.NUXREFSN"
                    + " AND B.NUXREFCO=C.NUXREFCO"
                    + " and b.cdlocatto='" + locCode + "'";

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {

                VerList vl = new VerList();
                vl.NUSENATE = result.getInt(1);
                vl.CDCATEGORY = result.getString(2);
                vl.DECOMMODITYF = result.getString(3);
                itemList.add(vl);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return itemList;
    }

    public ArrayList getLocCodes() {
        return getLocCodes("ALL");
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return arraylist of all the location codes 
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getLocCodes(String natype) {
          if(natype.isEmpty()||natype==null){
             throw new IllegalArgumentException("Invalid location Code");
        }  
        ArrayList<String> locCodes = new ArrayList<String>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
           
            String qry = "select distinct cdlocat,adstreet1 from sl16location a where a.cdstatus='A'";
            if (natype.equalsIgnoreCase("DELIVERY")) {
                qry = "select distinct cdlocat,adstreet1 from sl16location a where a.cdstatus='A' AND cdlocat IN (SELECT a2.cdlocatto FROM fm12invintrans a2 WHERE a2.cdstatus = 'A' AND a2.cdintransit = 'Y' AND EXISTS (SELECT 1 FROM fd12invintrans b2 WHERE b2.nuxrpd = a2.nuxrpd AND b2.cdstatus = 'A'))";
            }

           ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
           
                String locCode = result.getString(1);
                String adstreet1 = result.getString(2);
                String locCodeListElement = locCode + "-" + adstreet1;
                locCodes.add(locCodeListElement);
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return locCodes;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to insert items found at given location(barcodes) for verification
     *----------------------------------------------------------------------------------------------------*/
    public int setBarcodesInDatabase(String cdlocat, String barcodes[]) {
        if(cdlocat.isEmpty()||barcodes==null){
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
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
        }
        return result;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to start a new pickup-delivery
     *----------------------------------------------------------------------------------------------------*/

    public int invTransit(String CDLOCATFROM, String CDLOCATTO, String[] barcode, String NAPICKUPBY, String NARELEASEBY, String NUXRRELSIGN, String NADELIVERBY, String NAACCEPTBY, String NUXRACCPTSIGN, String DEPUCOMMENTS) {
       if(CDLOCATFROM.isEmpty()||CDLOCATTO==null||barcode==null){
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
            Logger.getLogger(DbConnect.class.getName()).log(Level.SEVERE, null, ex);
            return -1;
        }

        return nuxrpd;
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the in transit deliveries to the given location
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getDeliveryList(String locCode) {
        if(locCode.isEmpty()){
             throw new IllegalArgumentException("Invalid locCode");
        } 
        ArrayList<String> pickupList = new ArrayList<String>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            //  String loc_code;
            String qry = "SELECT NUXRPD,CDLOCATFROM ,CDLOCATTO ,NAPICKUPBY FROM   "
                    + "  FM12INVINTRANS"
                    + " WHERE CDSTATUS='A'"
                    + " AND CDINTRANSIT='Y'"
                    + " and cdlocatto='" + locCode + "'";

            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                String NUXRPD = result.getString(1);
                String CDLOCATFROM = result.getString(2);
                String CDLOCATTO = result.getString(3);
                String NAPICKUPBY = result.getString(4);
                String pickupDetails = NUXRPD + " : From " + CDLOCATFROM + "\n To " + CDLOCATTO + "\n Pickup by : " + NAPICKUPBY;
                pickupList.add(pickupDetails);
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
        return pickupList;

    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return all the items related to a perticular delivery nuxrpd
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList getDeliveryDetails(String nuxrpd) {
        if(nuxrpd.isEmpty()){
             throw new IllegalArgumentException("Invalid locCode");
        } 
        ArrayList<String> deliveryDetails = new ArrayList<String>();
        try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
            String qry = "SELECT A.NUSENATE,C.CDCATEGORY,C.DECOMMODITYF,e.nuxrpd FROM "
                    + " FM12SENXREF A,FD12ISSUE B, FM12COMMODTY C,fd12invintrans d,fm12invintrans e "
                    + " WHERE A.CDSTATUS='A' "
                    + " AND A.NUXREFSN=B.NUXREFSN "
                    + " AND B.NUXREFCO=C.NUXREFCO "
                    + " and a.nusenate=d.nusenate "
                    + " AND d.nuxrpd =e.nuxrpd "
                    + " and e.nuxrpd=" + nuxrpd;
            ResultSet result = stmt.executeQuery(qry);
            while (result.next()) {
                String NUSENATE = result.getString(1);
                String CDCATEGORY = result.getString(2);
                String DECOMMODITYF = result.getString(3);
                String details = NUSENATE + ":  " + CDCATEGORY + " " + DECOMMODITYF;
                deliveryDetails.add(details);
            }

            // Close the connection
            conn.close();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
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
       if(imageInArray==null||nuxrefem<0||nauser==null){
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
        } catch (IOException ex) {
            ex.printStackTrace();
        }
        return nuxrsign;

    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to 
     *----------------------------------------------------------------------------------------------------*/
    public ArrayList<Employee> getEmployeeList(String nalast) {
     //   if(nalast==null){
     //       throw new IllegalArgumentException("Invalid nalast");
     //   }
        return getEmployeeList(nalast, "A");
    }
    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to return the list of employee names
     *----------------------------------------------------------------------------------------------------*/

    public ArrayList<Employee> getEmployeeList(String nalast, String cdempstatus) {
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
        }
        return employeeList;
    }

    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to confirm delivery i.e. updates the FD12Issue table and changes location-----
     *------------------------------------------------------------------------------------------------------*/
 public int confirmDelivery(String nuxrpd,String NUXRACCPTSIGN,String NADELIVERBY,String NAACCEPTBY,ArrayList barcodes,ArrayList a, String DEDELCOMMENTS){
    System.out.println("confirmDelivery nuxrpd "+nuxrpd);
      int result=-1;
   try {
            Connection conn = getDbConnection();
            Statement stmt = conn.createStatement();
    
            //1. update the master table 
            System.out.println ("(confirmDelivery) updating current delivery nuxrpd:"+nuxrpd);
            
            String query="update FM12invintrans "+
                 "set CDINTRANSIT='N' "+
                 " ,DTTXNUPDATE=SYSDATE "+
                 " ,NATXNUPDUSER=USER "+
                 " ,NUXRACCPTSIGN="+NUXRACCPTSIGN+
                 " ,NADELIVERBY='"+NADELIVERBY+
                 "' ,NAACCEPTBY='"+NAACCEPTBY+
                 "' ,DTDELIVERY=SYSDATE "+
                 "  ,DEDELCOMMENTS='" +DEDELCOMMENTS+
                 "' where NUXRPD="+nuxrpd;
           result = stmt.executeUpdate(query);
           
           System.out.println ("(confirmDelivery):"+query);
           
           conn.commit();
    
            //2. update the details table 
                   // we dont need to update the details table since we are marking the record in master as N   
            //3. return result
          result = 0;
          conn.close();
    
   } catch (SQLException ex) {
                 System.out.println(ex.getMessage());
        } 
   return result;
}


    /*-------------------------------------------------------------------------------------------------------
     * ---------------Function to create new delivery i.e. inserts new records into FM12InvInTrans-----
     *----------------------------------------------------------------------------------------------------*/
    public int createNewDelivery(String nuxrpd, String[] barcode) {
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
        }
        return 0;
    }
}
