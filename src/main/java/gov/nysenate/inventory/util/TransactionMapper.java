package gov.nysenate.inventory.util;

import java.sql.CallableStatement;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Types;
import java.util.ArrayList;
import java.util.Collection;

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

    public int insertTransaction(DbConnect db, Transaction trans) throws SQLException {
        return insertTransaction(db, trans, 0);
    }

    /**
     * @param db
     * @param trans
     * @param oldNuxrpd if this pickup is auto generated from a partial delivery, this is the original deliveries unique id.
     * @return unique id of inserted row.
     * @throws SQLException 
     */
    public int insertTransaction(DbConnect db, Transaction trans, int oldNuxrpd) throws SQLException {
        String oldNuxStr = Integer.toString(oldNuxrpd);
        if (oldNuxStr.equals("0")) {
            oldNuxStr = "null";
        }

        Connection conn = db.getDbConnection();
        String query = "SELECT FM12INVINTRANS_SEQN.nextval FROM dual ";
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet result = ps.executeQuery();
        result.next();
        trans.setNuxrpd(result.getInt(1));

        query = "INSERT INTO FM12INVINTRANS ( " + fm12invintransColumns + ") " +
                "VALUES( " + trans.getNuxrpd() + ", '" + trans.getDestinationCdLoc() + "', '" + trans.getOriginCdLoc() + "', " +
                "'Y'" + ", '" + trans.getNapickupby() + "', '" + trans.getNareleaseby() + "', '" + trans.getNuxrrelsign() + "', " +
                "'A'" + ", " + "SYSDATE" + ", " + "SYSDATE" + ", " + "USER" + ", " + "USER" + ", '" + trans.getPickupComments() + "', '" +
                trans.getNuxraccptsign() + "', '" + trans.getNadeliverby() + "', '" + trans.getNaacceptby() + "', " +
                oldNuxStr + ", '" + trans.getPickupDate() + "', '" + trans.getDeliveryDate() + "', '" + trans.getDeliveryComments() + "', '" +
                trans.getOriginCdLocType() + "', '" + trans.getDestinationCdLocType() + "', '" +
                trans.getShipComments() + "', '" + trans.getVerificationComments() + "', '" + trans.getHelpReferenceNum() + "', " +
                getNotNullEmployeeId(trans) + ", " +getTransShipId(conn, trans) + ", '" + trans.getVerificationMethod() + "' ) ";

        log.info(query);
        ps = conn.prepareStatement(query);
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
    public Transaction queryTransaction(DbConnect db, int nuxrpd) throws SQLException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, 'MM/DD/RR HH:MI:SSAM- Day') dtpickup, " +
                "invintrans.napickupby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adzipcode tozip, " +
                "invintrans.nuxrefem " +
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
        ArrayList<InvItem> items = db.getDeliveryDetails(Integer.toString(nuxrpd), ""); // TODO: do this here?
        trans.setPickupItems(items);
        conn.close();
        return trans;
    }

    // TODO: does this do everything we want for the transaction?? ... delivery info?
    public Collection<Transaction> queryAllValidTransactions(DbConnect db) throws SQLException {
        final String query = "SELECT invintrans.nuxrpd, TO_CHAR(invintrans.dtpickup, 'MM/DD/RR HH:MI:SSAM- Day') dtpickup, " +
                "invintrans.napickupby, invintrans.depucomments, " +
                "invintrans.nuxrshiptyp, shiptyp.cdshiptyp, invintrans.deshipcomments, " +
                "invintrans.nuxrvermthd, vermthd.cdvermthd, invintrans.devercomments, " +
                "invintrans.cdlocatfrom, loc1.cdloctype fromloctype, loc1.adstreet1 fromstreet1, loc1.adcity fromcity, loc1.adzipcode fromzip, " +
                "invintrans.cdlocatto, loc2.cdloctype toloctype, loc2.adstreet1 tostreet1, loc2.adcity tocity, loc2.adzipcode tozip, " +
                "invintrans.nuxrefem, " +
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
            trans.setCount(result.getInt(22));

            validPickups.add(trans);
        }

        conn.close();
        return validPickups;
    }

    public void completeDelivery(DbConnect db, Transaction trans) throws SQLException {
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
                "DESHIPCOMMENTS='" + trans.getShipComments() + "', " +
                "DEVERCOMMENTS='" + trans.getVerificationComments() + "', " +
                "NUXREFEM=" + getNotNullEmployeeId(trans) + ", " + // <-- TODO: will we have nuxrefem/ how will we get it?
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
            // TODO: this is setting the new Pickup's dtpickup to a different value than the original pickup... do we want that???
            // TODO: eventually, trans should have all pickup info in it as well as delivery info... can just do a simple insert.
            // For now:
            db.createNewPickup(trans, "userFallback"); // TODO: fallbackuser...
        }
    }

    private Transaction parseTransaction(ResultSet result) throws SQLException {
        Transaction trans = new Transaction();
        Location origin = new Location();
        Location dest = new Location();

        trans.setNuxrpd(Integer.parseInt(result.getString(1)));
        trans.setPickupDate(result.getString(2));
        trans.setNapickupby(result.getString(3));
        trans.setPickupComments(result.getString(4));
        trans.setShipId(result.getInt(5));
        trans.setShipType(result.getString(6));
        trans.setShipComments(result.getString(7));
        trans.setVerificationId(result.getInt(8));
        trans.setVerificationMethod(result.getString(9));
        trans.setVerificationComments(result.getString(10));
        origin.setCdlocat(result.getString(11));
        origin.setCdloctype(result.getString(12));
        origin.setAdstreet1(result.getString(13));
        origin.setAdcity(result.getString(14));
        origin.setAdzipcode(result.getString(15));
        dest.setCdlocat(result.getString(16));
        dest.setCdloctype(result.getString(17));
        dest.setAdstreet1(result.getString(18));
        dest.setAdcity(result.getString(19));
        dest.setAdzipcode(result.getString(20));
        trans.setEmployeeId(result.getInt(21));

        trans.setOrigin(origin);
        trans.setDestination(dest);

        return trans;
    }

    private String getTransShipId(Connection conn, Transaction trans) throws SQLException {
        if (trans.getShipType().equals("")) {
            return "null";
        }

        String query = "SELECT nuxrshiptyp " +
                "FROM FL12SHIPTYP " +
                "WHERE cdshiptype = " + trans.getShipType();
        PreparedStatement ps = conn.prepareStatement(query);
        ResultSet result = ps.executeQuery();
        result.next();
        int id = result.getInt(1);
        return Integer.toString(id);
    }

    private String getTransVerId(Connection conn, Transaction trans) throws SQLException {
        if (trans.getVerificationMethod().equals("")) {
            return "null"; // TODO: return ""; ??
        }
        
        String query = "SELECT nuxrvermthd " +
                "FROM FL12VERMTHD " +
                "WHERE cdvermthd = " + trans.getVerificationMethod();
        PreparedStatement ps = conn.prepareStatement(query);
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
}
