package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.model.Commodity;
import org.apache.commons.dbutils.DbUtils;

import java.sql.Connection;
import java.sql.SQLException;
import java.util.List;

public class CommodityService
{
    public List<Commodity> getCommoditiesByKeywords(DbConnect db, String keywords) throws SQLException, ClassNotFoundException {
        List<Commodity> commodities;
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            commodities = new CommodityDAO().getCommodityByKeywords(conn, keywords);
        } finally {
            DbUtils.close(conn);
        }
        return commodities;
    }

    public Commodity getCommodityByItemId(Connection conn, int id) throws SQLException {
        return new CommodityDAO().getCommodityByItemId(conn, id);
    }
}
