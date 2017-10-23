package no.kij.scheduler;

import static org.junit.Assert.*;
import org.junit.Test;

import java.sql.Connection;
import java.sql.SQLException;

public class DatabaseConnectorTest {
    private final String USERNAME = ResourceFetcher.getCredentials().getProperty("test_user");
    private final String PASSWORD = ResourceFetcher.getCredentials().getProperty("test_pass");
    private final String HOST = ResourceFetcher.getCredentials().getProperty("test_host");
    private final String DB_NAME = ResourceFetcher.getCredentials().getProperty("test_db");

    @Test
    public void testConnection() {
        DatabaseConnector connector = new DatabaseConnector(USERNAME, PASSWORD, HOST, DB_NAME);
        Connection connection = null;
        try {
            connection = connector.getConnection();
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        assertNotNull(connection);
    }

    @Test
    public void testConnectionWithInvalidCredentials() {
        DatabaseConnector connector = new DatabaseConnector("invalid", "invalid", "invalid", "invalid");
        Connection connection;
        try {
            connection = connector.getConnection();
        } catch (SQLException e) {
            connection = null;
        }
        assertNull(connection);
    }
}
