package gov.nysenate.inventory.dao;

import gov.nysenate.inventory.model.Commodity;
import org.apache.commons.dbutils.DbUtils;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.log4j.Logger;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommodityDAO extends DbManager
{
    private static final Logger log = Logger.getLogger(CommodityDAO.class.getName());

    public List<Commodity> getCommoditiesByKeywords(DbConnect db, String keywords) throws SQLException, ClassNotFoundException {
        // FIXME: improve this query... keyword of 'table' -> first result is 'tablet'... method takes 1.5 sec to run.
        String SELECT_BY_KEYWORDS = " WITH results AS "
                + " (SELECT a.nuxrefco, a.cdcommodity, b.cdissunit, b.cdcategory, b.cdtype, b.decommodityf, c.keyword"
                + " FROM fm12comxref a, fm12commodty b, (select column_value keyword"
                + " FROM TABLE(split(UPPER('" + keywords + "')))) c  "
                + " WHERE a.nuxrefco = b.nuxrefco "
                + " AND a.cdstatus = 'A'"
                + " AND b.cdstatus = 'A'"
                + " AND b.decommodityf LIKE '%'||c.keyword||'%')"
                + " SELECT count(*) nucnt, a.decommodityf, a.nuxrefco, a.cdcommodity, a.cdissunit, a.cdcategory, a.cdissunit, a.cdtype "
                + " FROM results a"
                + " GROUP BY  a.nuxrefco, a.cdcommodity, a.cdissunit, a.cdcategory, a.cdissunit, a.cdtype, a.decommodityf "
                + " ORDER BY 1 DESC, 2";

        List<Commodity> commodities;
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            commodities = run.query(conn, SELECT_BY_KEYWORDS, new CommodityHandler());
        } finally {
            DbUtils.close(conn);
        }
        return commodities;
    }

    private String SELECT_COMMODITY_BY_ID =
            "SELECT fm12commodty.nuxrefco, cdtype, cdcategory, cdcommodity, decommodityf \n" +
            "FROM fm12commodty INNER JOIN fm12comxref ON fm12commodty.nuxrefco = fm12comxref.nuxrefco \n" +
            "WHERE fm12commodty.cdstatus = 'A' \n" +
            "AND fm12comxref.cdstatus = 'A' \n" +
            "AND fm12commodty.nuxrefco = ?";

    public List<Commodity> getCommodityById(DbConnect db, int id) throws SQLException, ClassNotFoundException {
        List<Commodity> commodities;
        QueryRunner run = new QueryRunner();
        Connection conn = null;
        try {
            conn = db.getDbConnection();
            commodities = run.query(conn, SELECT_COMMODITY_BY_ID, new CommodityHandler(), id);
        } finally {
            DbUtils.close(conn);
        }
        return commodities;
    }

    private class CommodityHandler implements org.apache.commons.dbutils.ResultSetHandler<List<Commodity>> {

        @Override
        public List<Commodity> handle(ResultSet rs) throws SQLException {
            List<Commodity> commodities = new ArrayList<Commodity>();
            while (rs.next()) {
                int id = rs.getInt("nuxrefco");
                String type = rs.getString("cdtype");
                String category = rs.getString("cdcategory");
                String code = rs.getString("cdcommodity");
                String description = rs.getString("decommodityf");

                Commodity c = new Commodity(id, type, category, code);
                c.setDescription(description);

                commodities.add(c);
            }
            return commodities;
        }
    }
}
