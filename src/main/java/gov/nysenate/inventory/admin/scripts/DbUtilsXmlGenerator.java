package gov.nysenate.inventory.admin.scripts;

import org.dbunit.database.DatabaseConnection;
import org.dbunit.database.IDatabaseConnection;
import org.dbunit.dataset.IDataSet;
import org.dbunit.dataset.xml.FlatXmlDataSet;

import java.io.FileOutputStream;
import java.sql.Connection;
import java.sql.DriverManager;

public class DbUtilsXmlGenerator {

    public static void main(String[] args) throws Exception
    {
        Class.forName("oracle.jdbc.driver.OracleDriver");
        Connection conn = DriverManager.getConnection("jdbc:oracle:thin:@localhost:1520/xe", "INVENTORY", "inventory");
        IDatabaseConnection connection = new DatabaseConnection(conn, "INVENTORY");

        // partial database export
//        QueryDataSet partialDataSet = new QueryDataSet(connection);
//        partialDataSet.addTable("FOO", "SELECT * FROM TABLE WHERE COL='VALUE'");
//        partialDataSet.addTable("BAR");
//        FlatXmlDataSet.write(partialDataSet, new FileOutputStream("partial.xml"));

        // full database export
        IDataSet fullDataSet = connection.createDataSet();
        FlatXmlDataSet.write(fullDataSet, new FileOutputStream("full_test_database_dump.xml"));

        // dependent tables database export: export table X and all tables that
        // have a PK which is a FK on X, in the right order for insertion
//        String[] depTableNames =
//                TablesDependencyHelper.getAllDependentTables(connection, "X");
//        IDataSet depDataset = connection.createDataSet( depTableNames );
//        FlatXmlDataSet.write(depDataSet, new FileOutputStream("dependents.xml"));
    }
}
