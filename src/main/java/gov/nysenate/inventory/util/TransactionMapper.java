package gov.nysenate.inventory.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collection;
import java.util.Locale;

import org.apache.log4j.Logger;

import gov.nysenate.inventory.model.Location;
import gov.nysenate.inventory.model.Transaction;
import gov.nysenate.inventory.server.DbConnect;
import gov.nysenate.inventory.server.InvItem;

public class TransactionMapper {

    private static final String fm12invintransColumns = "NUXRPD, CDLOCATTO, CDLOCATFROM, CDINTRANSIT, NAPICKUPBY, NARELEASEBY, " +
            "NUXRRELSIGN, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER, DEPUCOMMENTS, NUXRACCPTSIGN, NADELIVERBY, " +
            "NAACCEPTBY, NUXRPDORIG, DTPICKUP, DTDELIVERY, DEDELCOMMENTS, CDLOCTYPEFRM, CDLOCTYPETO, DESHIPCOMMENTS, DEVERCOMMENTS, " +
            "NUHELPREF, NUXREFEM, NUXRSHIPTYP, NUXRVERMTHD";

    private static final String fd12invintransColumns = "NUXRPD, NUSENATE, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER";

    private static final Logger log = Logger.getLogger(TransactionMapper.class.getName());
    private static final String oracleDateString = "'MM/DD/RR HH:MI:SSAM'";
    private static final SimpleDateFormat sdf = new SimpleDateFormat("MM/dd/yy hh:mm:ssa", Locale.US);

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
        Connection conn = db.getDbConnection();
        String query = "SELECT FM12INVINTRANS_SEQN.nextval FROM dual ";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet result = ps.executeQuery();
        result.next();
        trans.setNuxrpd(result.getInt(1));

        query = "INSERT INTO FM12INVINTRANS (NUXRPD, CDLOCATTO, CDLOCATFROM, CDINTRANSIT, NAPICKUPBY, NARELEASEBY, " +
                "NUXRRELSIGN, CDSTATUS, DTTXNORIGIN, DTTXNUPDATE, NATXNORGUSER, NATXNUPDUSER, DEPUCOMMENTS, " +
                "NUXRPDORIG, DTPICKUP, CDLOCTYPEFRM, CDLOCTYPETO, NUXRSHIPTYP" + ") " +
                "VALUES(?,?,?,?,?,?,?,?,?,?,USER,USER,?,?,?,?,?,?)";

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
        ps.setTime(9, getCurrentDate());
        ps.setTime(10, getCurrentDate());
        ps.setString(11, trans.getPickupComments());
        if (oldNuxrpd == 0) {
            ps.setNull(12, java.sql.Types.INTEGER);
        } else {
            ps.setInt(12, oldNuxrpd);
        }
        ps.setTime(13, getSqlDate(trans.getPickupDate()));
        ps.setString(14, trans.getOriginCdLocType());
        ps.setString(15, trans.getDestinationCdLocType());
        if (getTransShipId(conn, trans) == 0) {
            ps.setNull(16, java.sql.Types.INTEGER);
        } else {
            ps.setInt(16, getTransShipId(conn, trans));
        }

        log.info(query);
        log.info(sdf.format(trans.getPickupDate()));
        ps.executeUpdate();

        // Also insert each picked up item.
        for (String nusenate : trans.getPickupItemsNusenate()) {
            String insertQry = "INSERT INTO FD12INVINTRANS ( " + fd12invintransColumns + " ) " +
                    "VALUES( " + trans.getNuxrpd() + ", '" + nusenate + "', '" + "A" + "', " + "SYSDATE" + ", " +
                    "SYSDATE" + ", " + "USER" + ", " + "USER" + " ) ";
            ps = conn.prepareStatement(insertQry);
            ps.executeUpdate();
        }

        conn.close();
        return trans.getNuxrpd();
    }

    // TODO: also query delivery info
    public Transaction queryTransaction(DbConnect db, int nuxrpd) throws SQLException, ClassNotFoundException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, " + oracleDateString + " ) dtpickup, " +
                "invintrans.napickupby, invintrans.nareleaseby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adzipcode tozip, " +
                "invintrans.nuxrefem, invintrans.nuxrrelsign " +
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
                "AND invintrans.cdintransit = 'Y' " +
                "AND invintrans.nuxrpd = ?";

        Transaction trans = new Transaction();
        Connection conn = db.getDbConnection();
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setInt(1, nuxrpd);
        ResultSet result = ps.executeQuery();
        result.next();
        trans = parseTransaction(result);

        // Get pickup items
        ArrayList<InvItem> items = db.getDeliveryDetails(Integer.toString(nuxrpd), "");
        trans.setPickupItems(items);
        conn.close();
        return trans;
    }

    public Collection<Transaction> queryAllValidTransactions(DbConnect db) throws SQLException, ClassNotFoundException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, " + oracleDateString + " ) dtpickup, " +
                "invintrans.napickupby, invintrans.nareleaseby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adzipcode tozip, " +
                "invintrans.nuxrefem, invintrans.nuxrrelsign, " +
                "(SELECT count(nusenate) from fd12invintrans d where d.nuxrpd = invintrans.nuxrpd and d.cdstatus = 'A') cnt " +
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
        Connection conn = db.getDbConnection();
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet result = ps.executeQuery();

        while (result.next()) {
            Transaction trans = parseTransaction(result);
            trans.setCount(result.getInt(24));

            validPickups.add(trans);
        }

        conn.close();
        return validPickups;
    }

    public void completeDelivery(DbConnect db, Transaction trans) throws SQLException, ClassNotFoundException {
        Connection conn = db.getDbConnection();

        // Update FM12InvInTrans
        String query = "UPDATE fm12invintrans " +
                "SET CDINTRANSIT='N', " +
                "DTTXNUPDATE=SYSDATE, " +
                "NATXNUPDUSER=USER, " +
                "NUXRACCPTSIGN=" + trans.getNuxraccptsign() + ", " +
                "NADELIVERBY='" + trans.getNadeliverby() + "', " +
                "NAACCEPTBY='" + trans.getNaacceptby() + "', " +
                "DTDELIVERY=SYSDATE" + ", " + // TODO: use Transaction.deliveryDate
                "DEDELCOMMENTS='" + trans.getDeliveryComments() + "', " +
                "DESHIPCOMMENTS='" + trans.getShipComments() + "', " + // TODO: are ship comments done here?
                "DEVERCOMMENTS='" + trans.getVerificationComments() + "', " +
                "NUXREFEM=" + getNotNullEmployeeId(trans) + ", " +
                "NUHELPREF='" + trans.getHelpReferenceNum()  + "', " +
                "NUXRVERMTHD=" + getTransVerId(conn, trans)  + " " +
                "WHERE NUXRPD=" + trans.getNuxrpd();

        log.info(query);
        PreparedStatement ps = conn.prepareStatement(query);
        ps.executeUpdate();

        // Update FD12Issue corresponding tables for each delivered item.
        for (String item : trans.getCheckedItems()) {
            String nusenate = item;
            CallableStatement cs = conn.prepareCall("{?=call inv_app.move_inventory_item(?,?,?,?,?,?)}");
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
            for (String nusenate : trans.getNotCheckedItems()) {
                String delQuery = "DELETE FROM FD12INVINTRANS WHERE nuxrpd=" + trans.getNuxrpd() + "AND nusenate = '" + nusenate + "'";
                ps = conn.prepareStatement(delQuery);
                log.info("FD12INVINTRANS DELETE QUERY: " + delQuery);
                ps.executeUpdate();
            }
        }
        conn.close();

        if (trans.getNotCheckedItems().size() > 0) {
            // TODO: syncronize use of InvItem and other model objects in app.
            ArrayList<InvItem> items = new ArrayList<InvItem>();
            for (String str: trans.getNotCheckedItems()) {
                InvItem item = new InvItem();
                item.setNusenate(str);
                items.add(item);
            }
            trans.setPickupItems(items);
            // Insert a new pickup that is the same as the original but only contains the non delivered items.
            insertPickup(db, trans);
        }
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
        trans.setPickupComments(result.getString(5));
        trans.setShipId(result.getInt(6));
        trans.setShipType(result.getString(7));
        trans.setShipComments(result.getString(8));
        trans.setVerificationId(result.getInt(9));
        trans.setVerificationMethod(result.getString(10));
        trans.setVerificationComments(result.getString(11));
        origin.setCdlocat(result.getString(12));
        origin.setCdloctype(result.getString(13));
        origin.setAdstreet1(result.getString(14));
        origin.setAdcity(result.getString(15));
        origin.setAdzipcode(result.getString(16));
        dest.setCdlocat(result.getString(17));
        dest.setCdloctype(result.getString(18));
        dest.setAdstreet1(result.getString(19));
        dest.setAdcity(result.getString(20));
        dest.setAdzipcode(result.getString(21));
        trans.setEmployeeId(result.getInt(22));
        trans.setNuxrrelsign(result.getString(23));

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
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, trans.getShipType());
        ResultSet result = ps.executeQuery();
        result.next();
        int id = result.getInt(1);
        return id;
    }

    private String getTransVerId(Connection conn, Transaction trans) throws SQLException {
        if (trans.getVerificationMethod().equals("")) {
            return "null";
        }

        String query = "SELECT nuxrvermthd " +
                "FROM FL12VERMTHD " +
                "WHERE cdvermthd = ?";
        PreparedStatement ps = conn.prepareStatement(query);
        ps.setString(1, trans.getVerificationMethod());
        ResultSet result = ps.executeQuery();
        result.next();
        int id = result.getInt(1);
        return Integer.toString(id);
    }

    private String getNotNullEmployeeId(Transaction trans) {
        int id = trans.getEmployeeId();
        if (id == 0) {
            return "null";
        }
        return Integer.toString(id);
    }

    private java.sql.Time getCurrentDate() {
        java.util.Date date = new java.util.Date();
        return new java.sql.Time(date.getTime());
    }

    private java.sql.Time getSqlDate(java.util.Date date) {
        return new java.sql.Time(date.getTime());
    }

}
