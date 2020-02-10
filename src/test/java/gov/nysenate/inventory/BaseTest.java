package gov.nysenate.inventory;

import gov.nysenate.inventory.dao.DbConnect;
import gov.nysenate.inventory.dao.item.ItemDAO;
import gov.nysenate.inventory.model.Item;
import org.apache.commons.dbutils.DbUtils;
import org.dbunit.DatabaseUnitException;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.DataSetException;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSetBuilder;
import org.dbunit.ext.oracle.OracleConnection;
import org.dbunit.operation.DatabaseOperation;

import java.io.FileInputStream;
import java.io.FileNotFoundException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;

import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class BaseTest {

    protected Connection getConnection() throws DatabaseUnitException, SQLException, ClassNotFoundException {
        return getDbUtilConnection().getConnection();
    }

    protected DbConnect getMockDbConnect() throws Exception {
        DbConnect mockDbConnect = mock(DbConnect.class);
        when(mockDbConnect.getDbConnection()).thenReturn(getConnection());
        return mockDbConnect;
    }

    private IDatabaseConnection getDbUtilConnection() throws ClassNotFoundException, SQLException, DatabaseUnitException {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1520/xe", "INVENTORY", "inventory");
        return new OracleConnection(conn, "INVENTORY");
    }

    protected IDataSet getDataSet() throws FileNotFoundException, DataSetException {
        return new FlatXmlDataSetBuilder().build(new FileInputStream("full_test_database_dump.xml"));
    }

    protected void cleanInsert() throws Exception {
        IDatabaseConnection connection = getDbUtilConnection();
        try {
            DatabaseOperation.CLEAN_INSERT.execute(connection, getDataSet());
        }
        finally {
            connection.close();
        }
    }

}
