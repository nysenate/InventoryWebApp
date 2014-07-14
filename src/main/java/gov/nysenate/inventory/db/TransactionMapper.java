package gov.nysenate.inventory.db;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.*;

import org.apache.log4j.Logger;

import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.model.InvItem;
import java.sql.Timestamp;

public class TransactionMapper extends DbManager {

    private static final String fm12invintransColumns = "NUXRPD, CDLOCATTO, CDLOCATFROM, CDINTRANSIT, NAPICKUPBY, NARELEASEBY, " +
            "NUXRRELSIGN, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER, DEPUCOMMENTS, NUXRACCPTSIGN, NADELIVERBY, " +
            "NAACCEPTBY, NUXRPDORIG, DTPICKUP, DTDELIVERY, DEDELCOMMENTS, CDLOCTYPEFRM, CDLOCTYPETO, DESHIPCOMMENTS, DEVERCOMMENTS, " +
            "NUHELPREF, NUXREFEMPPWRK, NUXRSHIPTYP, NUXRVERMTHD";

    private static final String fd12invintransColumns = "NUXRPD, NUSENATE, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER";

    private static final Logger log = Logger.getLogger(TransactionMapper.class.getName());
    private static final String oracleDateString = "'MM/DD/RR HH:MI:SSAM'";
    private final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ssa", Locale.US);

    public int insertPickup(DbConnect db, Transaction trans) throws SQLException, ClassNotFoundException {
        return insertPickup(db, trans, 0);
    }

    /**
     * @param db
     * @param trans
     * @param oldNuxrpd if this pickup is auto generated from a partial delivery, this is the original deliveries unique id.
     * @return unique id of inserted row.
     * @throws SQLException 
     * @throws ClassNotFoundException 
     */
    public int insertPickup(DbConnect db, Transaction trans, int oldNuxrpd) throws SQLException, ClassNotFoundException {
        ResultSet result = null;
        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            String query = "SELECT FM12INVINTRANS_SEQN.nextval FROM dual ";
            ps = conn.prepareStatement(query);
            result = ps.executeQuery();
            result.next();
            trans.setNuxrpd(result.getInt(1));
            
            query = "INSERT INTO FM12INVINTRANS (NUXRPD, CDLOCATTO, CDLOCATFROM, CDINTRANSIT, NAPICKUPBY, NARELEASEBY, " +
                    "NUXRRELSIGN, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER, DEPUCOMMENTS, " +
                    "NUXRPDORIG, DTPICKUP, CDLOCTYPEFRM, CDLOCTYPETO, NUXRSHIPTYP, CDPPWRKTYP" + ") " +
                    "VALUES(?,?,?,?,?,?,?,?,SYSDATE,SYSDATE,USER,USER,?,?,?,?,?,?,?)";

            ps = conn.prepareStatement(query);

            ps.setInt(1, trans.getNuxrpd());
            ps.setString(2, trans.getDestinationCdLoc());
            ps.setString(3, trans.getOriginCdLoc());
            ps.setString(4, "Y");
            ps.setString(5, trans.getNapickupby());
            ps.setString(6, trans.getNareleaseby());
            if (trans.getNuxrrelsign().equals("")) {
                ps.setNull(7, java.sql.Types.INTEGER);
            } else {
                ps.setInt(7, Integer.valueOf(trans.getNuxrrelsign()));
            }
            ps.setString(8, "A");
            ps.setString(9, trans.getPickupComments());
            if (oldNuxrpd == 0) {
                ps.setNull(10, java.sql.Types.INTEGER);
            } else {
                ps.setInt(10, oldNuxrpd);
            }
            ps.setTimestamp(11, getSqlDate(trans.getPickupDate()));
            ps.setString(12, trans.getOriginCdLocType());
            ps.setString(13, trans.getDestinationCdLocType());
            if (getTransShipId(conn, trans) == 0) {
                ps.setNull(14, java.sql.Types.INTEGER);
            } else {
                ps.setInt(14, getTransShipId(conn, trans));
            }
            ps.setString(15, trans.getRemoteType());
            ps.executeUpdate();

            // Also insert each picked up item.
            for (String nusenate : trans.getPickupItemsNusenate()) {
                String insertQry = "INSERT INTO FD12INVINTRANS ( " + fd12invintransColumns + " ) " +
                        "VALUES( " + trans.getNuxrpd() + ", '" + nusenate + "', '" + "A" + "', " + "SYSDATE" + ", " +
                        "SYSDATE" + ", " + "USER" + ", " + "USER" + " ) ";
                ps = conn.prepareStatement(insertQry);
                ps.executeUpdate();
            }
        } finally {
            closeResultSet(result);
            closeStatement(ps);
            closeConnection(conn);
        }
        return trans.getNuxrpd();
    }

    public void updateTransaction(DbConnect db, Transaction trans) throws SQLException, ClassNotFoundException {
        String query = "UPDATE fm12invintrans SET " +
        "CDLOCATTO = ?, " +
        "CDLOCATFROM = ?, " +
        "DTTXNUPDATE = SYSDATE, " +
        "NATXNUPDUSER = USER, " +
        "DEPUCOMMENTS = ?, " +
        "CDLOCTYPEFRM = ?, " +
        "CDLOCTYPETO = ?, " +
        "NUXRSHIPTYP = ?, " +
        "DESHIPCOMMENTS = ?, " +
        "CDPPWRKTYP = ? " +
        "WHERE nuxrpd = ? ";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, trans.getDestinationCdLoc());
            ps.setString(2, trans.getOriginCdLoc());
            //ps.setTimestamp(3, getCurrentDate());
            ps.setString(3, trans.getPickupComments());
            ps.setString(4, trans.getOriginCdLocType());
            ps.setString(5, trans.getDestinationCdLocType());
            if (getTransShipId(conn, trans) == 0) {
                ps.setNull(6, java.sql.Types.INTEGER);
            } else {
                ps.setInt(6, getTransShipId(conn, trans));
            }
            ps.setString(7, trans.getShipComments());
            ps.setString(8, trans.getRemoteType());
            ps.setInt(9, trans.getNuxrpd());

            ps.executeUpdate();
        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public Transaction queryTransaction(DbConnect db, int nuxrpd) throws SQLException, ClassNotFoundException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, " + oracleDateString + " ) dtpickup, " +
                "invintrans.napickupby, invintrans.nareleaseby, invintrans.nadeliverby, invintrans.naacceptby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adstate fromstate, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adstate tostate, loc2.adzipcode tozip, " +
                "invintrans.NUXREFEMPPWRK, invintrans.nuxrrelsign, " +
                "(SELECT count(nusenate) from fd12invintrans d where d.nuxrpd = invintrans.nuxrpd and d.cdstatus = 'A') cnt, shiptyp.deshiptyp " +
                "FROM fm12invintrans invintrans " +
                "LEFT OUTER JOIN fl12shiptyp shiptyp " +
                "ON invintrans.nuxrshiptyp = shiptyp.nuxrshiptyp " +
                "LEFT OUTER JOIN fl12vermthd vermthd " +
                "ON invintrans.nuxrvermthd = vermthd.nuxrvermthd " +
                "INNER JOIN sl16location loc1 " +
                "ON invintrans.cdlocatfrom = loc1.cdlocat " +
                "LEFT OUTER JOIN sl16location loc2 " +
                "ON invintrans.cdlocatto = loc2.cdlocat " +
                "WHERE invintrans.cdstatus = 'A' " +
                "AND invintrans.nuxrpd = ?";

        Transaction trans = new Transaction();
        PreparedStatement ps = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setInt(1, nuxrpd);
            result = ps.executeQuery();
            result.next();
            trans = parseTransaction(result);
        } finally {
            closeResultSet(result);
            closeStatement(ps);
            closeConnection(conn);
        }

        // Get pickup items
        ArrayList<InvItem> items = db.getDeliveryDetails(Integer.toString(nuxrpd));
        trans.setPickupItems(items);

        return trans;
    }

    public Collection<Transaction> queryAllValidTransactions(DbConnect db) throws SQLException, ClassNotFoundException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, " + oracleDateString + " ) dtpickup, " +
                "invintrans.napickupby, invintrans.nareleaseby, invintrans.nadeliverby, invintrans.naacceptby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adstate fromstate, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adstate tostate, loc2.adzipcode tozip, " +
                "invintrans.NUXREFEMPPWRK, invintrans.nuxrrelsign, " +
                "(SELECT count(nusenate) from fd12invintrans d where d.nuxrpd = invintrans.nuxrpd and d.cdstatus = 'A') cnt, shiptyp.deshiptyp " +
                "FROM fm12invintrans invintrans " +
                "LEFT OUTER JOIN fl12shiptyp shiptyp " +
                "ON invintrans.nuxrshiptyp = shiptyp.nuxrshiptyp " +
                "LEFT OUTER JOIN fl12vermthd vermthd " +
                "ON invintrans.nuxrvermthd = vermthd.nuxrvermthd " +
                "INNER JOIN sl16location loc1 " +
                "ON invintrans.cdlocatfrom = loc1.cdlocat " +
                "LEFT OUTER JOIN sl16location loc2 " +
                "ON invintrans.cdlocatto = loc2.cdlocat " +
                "WHERE invintrans.cdstatus = 'A' " +
                "AND invintrans.cdintransit = 'Y'";

        ArrayList<Transaction> validPickups = new ArrayList<Transaction>();

        PreparedStatement ps = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            result = ps.executeQuery();

            while (result.next()) {
                Transaction trans = parseTransaction(result);
                validPickups.add(trans);
            }
        } finally {
            closeResultSet(result);
            closeStatement(ps);
            closeConnection(conn);
        }

        return validPickups;
    }

    public void completeDelivery(DbConnect db, Transaction trans) throws SQLException, ClassNotFoundException {
        String query = "UPDATE fm12invintrans " +
                "SET CDINTRANSIT=?, " +
                "DTTXNUPDATE=SYSDATE, " +
                "NATXNUPDUSER=USER, " +
                "NUXRACCPTSIGN=?, " +
                "NADELIVERBY=?, " +
                "NAACCEPTBY=?, " +
                "DTDELIVERY=SYSDATE" + ", " + // TODO: use Transaction.deliveryDate
                "DEDELCOMMENTS=?, " +
                "DESHIPCOMMENTS=?, " +
                "DEVERCOMMENTS=?, " +
                "NUXREFEMPPWRK=?, " +
                "NUHELPREF=?, " +
                "NUXRVERMTHD=?, " +
                "NARELEASEBY=? " +
                "WHERE NUXRPD=?";

        PreparedStatement ps = null;
        CallableStatement cs = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);

            ps.setString(1, getCdintransit(trans));
            if (trans.getNuxraccptsign().equals("")) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, Integer.valueOf(trans.getNuxraccptsign()));
            }
            ps.setString(3, trans.getNadeliverby());
            ps.setString(4, trans.getNaacceptby());
            ps.setString(5, trans.getDeliveryComments());
            ps.setString(6, trans.getShipComments());
            ps.setString(7, trans.getVerificationComments());
            if (trans.getEmployeeId() <= 0) {
                ps.setNull(8, java.sql.Types.INTEGER);
            } else {
                ps.setInt(8, trans.getEmployeeId());
            }
            if (trans.getHelpReferenceNum() == null || trans.getHelpReferenceNum().equals("")) {
                ps.setNull(9, java.sql.Types.VARCHAR);
            } else {
                ps.setString(9, trans.getHelpReferenceNum());
            }
            if (getTransVerId(conn, trans) == 0) {
                ps.setNull(10, java.sql.Types.INTEGER);
            } else {
                ps.setInt(10, getTransVerId(conn, trans));
            }
            ps.setString(11, trans.getNareleaseby());
            ps.setInt(12, trans.getNuxrpd());

            ps.executeUpdate();

            // Update FD12Issue corresponding tables for each delivered item.
            for (String item : trans.getCheckedItems()) {
                String nusenate = item;
                cs = conn.prepareCall("{?=call inv_app.move_inventory_item(?,?,?,?,?,?)}");
                cs.registerOutParameter(1, Types.VARCHAR);
                cs.setString(2, nusenate);
                cs.setString(3, trans.getOrigin().getCdlocat());
                cs.setString(4, trans.getOrigin().getCdloctype());
                cs.setString(5, trans.getDestination().getCdlocat());
                cs.setString(6, trans.getDestination().getCdloctype());
                cs.setString(7, String.valueOf(trans.getNuxrpd()));
                cs.executeUpdate();
            }

            // Delete non delivered items from FD12InvInTrans
            if (trans.getNotCheckedItems().size() > 0) {
                log.info("Deleting the following not delivered items: " + Arrays.toString(trans.getNotCheckedItems().toArray()));
                for (String nusenate : trans.getNotCheckedItems()) {
                    String delQuery = "DELETE FROM FD12INVINTRANS WHERE nuxrpd=" + trans.getNuxrpd() + "AND nusenate = '" + nusenate + "'";
                    ps = conn.prepareStatement(delQuery);
                    ps.executeUpdate();
                }
            }
        } finally {
            closeStatement(ps);
            closeStatement(cs);
            closeConnection(conn);
        }

        if (trans.getNotCheckedItems().size() > 0) {
            ArrayList<InvItem> items = getNotDeliveredItems(trans);
            Transaction newTrans = trans.shallowCopy();
            newTrans.setPickupItems(items);
            if (newTrans.isRemotePickup()) {
                newTrans.setNareleaseby("");
            }
            // Insert a new pickup that is the same as the original but only contains the non delivered items.
            insertPickup(db, newTrans, newTrans.getNuxrpd());
        }
    }

    private ArrayList<InvItem> getNotDeliveredItems(Transaction trans) {
        ArrayList<InvItem> items = new ArrayList<InvItem>();
        for (String str: trans.getNotCheckedItems()) {
            InvItem item = new InvItem();
            item.setNusenate(str);
            items.add(item);
        }
        return items;
    }

    public void insertRemotePickupRemoteUserInfo(DbConnect db, Transaction trans) throws SQLException, ClassNotFoundException {
        String query = "UPDATE FM12INVINTRANS SET " +
                "NARELEASEBY = ?, " +
                "NADELIVERBY = ? " +
                "WHERE NUXRPD = ?";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, trans.getNareleaseby());
            ps.setString(2, trans.getNadeliverby());
            ps.setInt(3, trans.getNuxrpd());

            ps.executeUpdate();

        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public void insertRemoteDeliveryRemoteUserInfo(DbConnect db, Transaction trans) throws ClassNotFoundException, SQLException {
        String query = "UPDATE FM12INVINTRANS SET " +
                "NAACCEPTBY = ? " +
                "WHERE NUXRPD = ?";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            ps.setString(1, trans.getNaacceptby());
            ps.setInt(2, trans.getNuxrpd());

            ps.executeUpdate();

        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    public void insertRemoteInfo(DbConnect db, Transaction trans) throws ClassNotFoundException, SQLException {
        String query = "UPDATE FM12INVINTRANS SET " +
                "NUXREFEMPPWRK = ?, " +
                "NUXRVERMTHD = ?, " +
                "NUHELPREF = ?, " +
                "DEVERCOMMENTS = ?, " +
                "DTTXNUPDATE = SYSDATE, " +
                "NATXNUPDUSER=USER, " +
                "CDINTRANSIT = 'N' " +
                "WHERE NUXRPD = ?";

        PreparedStatement ps = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            if (trans.getEmployeeId() <= 0) {
                ps.setNull(1, java.sql.Types.INTEGER);
            } else {
                ps.setInt(1, trans.getEmployeeId());
            }
            if (getTransVerId(conn, trans) == 0) {
                ps.setNull(2, java.sql.Types.INTEGER);
            } else {
                ps.setInt(2, getTransVerId(conn, trans));
            }
            ps.setString(3, trans.getHelpReferenceNum());
            ps.setString(4, trans.getVerificationComments());
            ps.setInt(5, trans.getNuxrpd());

            ps.executeUpdate();

        } finally {
            closeStatement(ps);
            closeConnection(conn);
        }
    }

    private String getCdintransit(Transaction trans) {
        if (trans.getNareleaseby().length() < 1 || trans.getNaacceptby().length() < 1)
            return "O";

        return "N";
    }

    private Transaction parseTransaction(ResultSet result) throws SQLException {
        Transaction trans = new Transaction();
        Location origin = new Location();
        Location dest = new Location();

        trans.setNuxrpd(Integer.parseInt(result.getString(1)));
        try {
            trans.setPickupDate(sdf.parse(result.getString(2)));
        } catch (ParseException e) {
            e.printStackTrace();
        }
        trans.setNapickupby(result.getString(3));
        trans.setNareleaseby(result.getString(4));

        trans.setNadeliverby(result.getString(5));
        trans.setNaacceptby(result.getString(6));

        trans.setPickupComments(result.getString(7));
        trans.setShipId(result.getInt(8));
        trans.setShipType(result.getString(9));
        trans.setShipComments(result.getString(10));
        trans.setVerificationId(result.getInt(11));
        trans.setVerificationMethod(result.getString(12));
        trans.setVerificationComments(result.getString(13));
        origin.setCdlocat(result.getString(14));
        origin.setCdloctype(result.getString(15));
        origin.setAdstreet1(result.getString(16));
        origin.setAdcity(result.getString(17));
        origin.setAdstate(result.getString(18));
        origin.setAdzipcode(result.getString(19));
        dest.setCdlocat(result.getString(20));
        dest.setCdloctype(result.getString(21));
        dest.setAdstreet1(result.getString(22));
        dest.setAdcity(result.getString(23));
        dest.setAdstate(result.getString(24));
        dest.setAdzipcode(result.getString(25));
        trans.setEmployeeId(result.getInt(26));
        trans.setNuxrrelsign(result.getString(27));
        trans.setCount(result.getInt(28));
        trans.setShipTypeDesc(result.getString(29));

        trans.setOrigin(origin);
        trans.setDestination(dest);

        return trans;
    }

    private int getTransShipId(Connection conn, Transaction trans) throws SQLException {
        if (trans.getShipType().equals("")) {
            return 0;
        }

        String query = "SELECT nuxrshiptyp " +
                "FROM FL12SHIPTYP " +
                "WHERE cdshiptyp = ?";

        int id;
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, trans.getShipType());
            result = ps.executeQuery();
            result.next();
            id = result.getInt(1);
        } finally {
            closeResultSet(result);
            closeStatement(ps);
        }
        return id;
    }

    private int getTransVerId(Connection conn, Transaction trans) throws SQLException {
        if (trans.getVerificationMethod().equals("")) {
            return 0;
        }

        String query = "SELECT nuxrvermthd " +
                "FROM FL12VERMTHD " +
                "WHERE cdvermthd = ?";

        int id;
        PreparedStatement ps = null;
        ResultSet result = null;
        try {
            ps = conn.prepareStatement(query);
            ps.setString(1, trans.getVerificationMethod());
            result = ps.executeQuery();
            result.next();
            id = result.getInt(1);
        } finally {
            closeResultSet(result);
            closeStatement(ps);
        }
        return id;
    }

    private String getNotNullEmployeeId(Transaction trans) {
        int id = trans.getEmployeeId();
        if (id == 0) {
            return "null";
        }
        return Integer.toString(id);
    }

    private Timestamp getCurrentDate() {
        Date date = new Date();
        return new Timestamp(date.getTime());
    }

    private Timestamp getSqlDate(Date date) {
        return new Timestamp(date.getTime());
    }

    public Collection<Transaction> queryDeliveriesMissingRemoteInfo(DbConnect db) throws ClassNotFoundException, SQLException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, " + oracleDateString + " ) dtpickup, " +
                "invintrans.napickupby, invintrans.nareleaseby, invintrans.nadeliverby, invintrans.naacceptby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adstate fromstate, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adstate tostate, loc2.adzipcode tozip, " +
                "invintrans.NUXREFEMPPWRK, invintrans.nuxrrelsign, " +
                "(SELECT count(nusenate) from fd12invintrans d where d.nuxrpd = invintrans.nuxrpd and d.cdstatus = 'A') cnt, shiptyp.deshiptyp " +
                "FROM fm12invintrans invintrans " +
                "LEFT OUTER JOIN fl12shiptyp shiptyp " +
                "ON invintrans.nuxrshiptyp = shiptyp.nuxrshiptyp " +
                "LEFT OUTER JOIN fl12vermthd vermthd " +
                "ON invintrans.nuxrvermthd = vermthd.nuxrvermthd " +
                "INNER JOIN sl16location loc1 " +
                "ON invintrans.cdlocatfrom = loc1.cdlocat " +
                "LEFT OUTER JOIN sl16location loc2 " +
                "ON invintrans.cdlocatto = loc2.cdlocat " +
                "WHERE invintrans.cdstatus = 'A' " +
                "AND invintrans.cdintransit = 'O'";

        ArrayList<Transaction> deliveries = new ArrayList<Transaction>();

        PreparedStatement ps = null;
        ResultSet result = null;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            ps = conn.prepareStatement(query);
            result = ps.executeQuery();

            while (result.next()) {
                Transaction trans = parseTransaction(result);
                deliveries.add(trans);
            }
        } finally {
            closeResultSet(result);
            closeStatement(ps);
            closeConnection(conn);
        }

        return deliveries;
    }

}
