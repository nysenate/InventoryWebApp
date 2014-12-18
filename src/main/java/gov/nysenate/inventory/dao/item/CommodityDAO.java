package gov.nysenate.inventory.dao.item;

import gov.nysenate.inventory.dao.base.DbManager;
import gov.nysenate.inventory.model.Commodity;
import org.apache.commons.dbutils.QueryRunner;
import org.apache.commons.dbutils.ResultSetHandler;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

public class CommodityDAO extends DbManager
{
    protected List<Commodity> getCommodityByKeywords(Connection conn, String keywords) throws SQLException, ClassNotFoundException {
        // FIXME: improve this query... keyword of 'table' -> first result is 'tablet'
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
        commodities = run.query(conn, SELECT_BY_KEYWORDS, new CommodityHandler());
        return commodities;
    }

    private String SELECT_COMMODITY_BY_ID_SQL =
            "SELECT fm12commodty.nuxrefco, cdtype, cdcategory, cdcommodity, decommodityf \n" +
            "FROM fm12commodty INNER JOIN fm12comxref ON fm12commodty.nuxrefco = fm12comxref.nuxrefco \n" +
            "WHERE fm12commodty.cdstatus = 'A' \n" +
            "AND fm12comxref.cdstatus = 'A' \n" +
            "AND fm12commodty.nuxrefco = ?";

    protected Commodity getCommodityById(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Commodity> commodities = run.query(conn, SELECT_COMMODITY_BY_ID_SQL, new CommodityHandler(), id);
        return commodities.get(0);
    }

    private String SELECT_COMMODITY_BY_ITEM_ID_SQL =
            "SELECT fm12commodty.nuxrefco, cdtype, cdcategory, cdcommodity, decommodityf\n" +
            "FROM fm12commodty INNER JOIN fm12comxref ON fm12commodty.nuxrefco = fm12comxref.nuxrefco \n" +
            "INNER JOIN fd12issue ON fd12issue.nuxrefco = fm12commodty.nuxrefco\n" +
            "WHERE nuxrefsn = ?";

    protected Commodity getCommodityByItemId(Connection conn, int id) throws SQLException {
        QueryRunner run = new QueryRunner();
        List<Commodity> commodities = run.query(conn, SELECT_COMMODITY_BY_ITEM_ID_SQL, new CommodityHandler(), id);
        return commodities.get(0);
    }

    private class CommodityHandler implements ResultSetHandler<List<Commodity>> {

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
