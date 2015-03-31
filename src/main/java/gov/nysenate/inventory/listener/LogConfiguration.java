package gov.nysenate.inventory.listener;

import org.apache.log4j.PropertyConfigurator;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import java.io.IOException;
import java.io.InputStream;
import java.util.Properties;

public class LogConfiguration implements ServletContextListener {

    @Override
    public void contextInitialized(ServletContextEvent servletContextEvent) {
        Properties appProperties = loadProperties("config.properties");
        String server = appProperties.getProperty("connectionString");
        try {
            String filename = "InventoryWebApp_" + server.split("/")[1] + ".log";
            Properties logProperties = loadProperties("log4j.properties");
            logProperties.put("log.filename", filename);
            PropertyConfigurator.configure(logProperties);
        }
        catch (Exception e) {
            System.out.println("Error customizing log file output. Will log to default file.");
        }
    }

    @Override
    public void contextDestroyed(ServletContextEvent servletContextEvent) {

    }

    private Properties loadProperties(String propertyFile) {
        Properties properties = new Properties();
        InputStream in = getClass().getClassLoader().getResourceAsStream(propertyFile);
        try {
            properties.load(in);
        } catch (IOException e) {
            e.printStackTrace();
        }
        return properties;
    }
}
