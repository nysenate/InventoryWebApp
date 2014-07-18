package gov.nysenate.inventory.service;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.RemovalRequestDAO;
import gov.nysenate.inventory.model.RemovalRequest;

import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class RemovalRequestService
{
    public List<RemovalRequest> getPendingRemovalRequests(DbConnect db) throws SQLException, ClassNotFoundException {
        List<RemovalRequest> rrs = new ArrayList<RemovalRequest>();
        RemovalRequestDAO rrDao = new RemovalRequestDAO();
        rrs = rrDao.getPendingRequests(db);

        for (RemovalRequest rr : rrs) {
            // get nusenate from details table.
            // get item info from itemsDAO
                //itemDAO can get items from a nusenate or nuxrefsn!
                //itemDAO logic - Pickup2Activity line: ~600.
        }
        return null;  // Coded for now, not being used at the moment.
    }


    // itemservice -> itemDAO
    //              - commodityDAO
    //
}
