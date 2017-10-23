package no.kij.scheduler;

import org.junit.After;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import static org.junit.Assert.*;
import static org.mockito.Mockito.*;


import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.sql.Connection;
import java.sql.DatabaseMetaData;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class DatabaseInitializerTest {
    private final ByteArrayOutputStream outContent = new ByteArrayOutputStream();
    private final ByteArrayOutputStream errContent = new ByteArrayOutputStream();
    private static DatabaseConnector connector;
    private DatabaseInitializer dbInit;
    private Connection conn;

    @BeforeClass
    public static void setupOnce() {
        Properties creds = ResourceFetcher.getCredentials();
        connector = new DatabaseConnector(
                creds.getProperty("test_user"),
                creds.getProperty("test_pass"),
                creds.getProperty("test_host"),
                creds.getProperty("test_db")
        );

    }

    @Before
    public void setUp() {
        System.setOut(new PrintStream(outContent));
        System.setErr(new PrintStream(errContent));
        dbInit = new DatabaseInitializer(connector);
        try {
            conn = connector.getConnection();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
    }

    @After
    public void tearDown() {
        try {
            conn.close();
        } catch (SQLException e) {
            System.err.println(e.getMessage());
        }
        System.setOut(null);
        System.setErr(null);
    }


    @Test
    public void testDatabaseTableFirstRunInitialization() {
        // if first run text file exists, we delete it
        File file = new File("firstrun.txt");
        if (file.exists()) {
            file.delete();
        }

        boolean initSuccess = dbInit.initializeDatabase(false);
        assertDatabaseTables();
        assertTrue(initSuccess);
    }

    @Test
    public void testDatabaseTableOverwriteInitialization() {
        boolean initSuccess = dbInit.initializeDatabase(true);
        assertDatabaseTables();
        assertTrue(initSuccess);
    }

    @Test
    public void testDatabaseInitializationFailure() {
        DatabaseConnector failConnector = new DatabaseConnector(null, null, null, null);
        DatabaseInitializer failInit = new DatabaseInitializer(failConnector);
        failInit.initializeDatabase(true);
        String failureText = "Something went wrong while initializing the database structure.\n" +
                "Access denied for user ''@'localhost' (using password: NO)";
        String error = errContent.toString().toLowerCase().replaceAll("\\s","");
        failureText = failureText.toLowerCase().replaceAll("\\s", "");
        assertEquals(failureText, error);
    }

    @Test
    public void testDatabaseInitializationDoesNotOverwrite() {
        File file = new File("firstrun.txt");
        if (!file.exists()) {
            try {
                file.createNewFile();
            } catch (IOException e) {
                fail(e.getMessage());
            }
        }
        boolean initSuccess = dbInit.initializeDatabase(false);
        assertFalse(initSuccess);
    }

    private void assertDatabaseTables() {
        List<String> tableNames = new ArrayList<>();
        try {
            DatabaseMetaData dbMeta = conn.getMetaData();
            ResultSet rs = dbMeta.getTables(null, null, "%", null);

            while (rs.next()) {
                tableNames.add(rs.getString("TABLE_NAME"));
            }
        } catch (SQLException e) {
            fail(e.getMessage());
        }
        assertTrue(tableNames.contains("available"));
        assertTrue(tableNames.contains("contact"));
        assertTrue(tableNames.contains("lecturer"));
        assertTrue(tableNames.contains("room"));
        assertTrue(tableNames.contains("subject"));
        assertEquals(6, tableNames.size());
    }
}
