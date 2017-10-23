package no.kij.scheduler;

import com.mysql.jdbc.jdbc2.optional.MysqlDataSource;

import java.sql.Connection;
import java.sql.SQLException;

import static no.kij.scheduler.ResourceFetcher.*;
/**
 * DatabaseConnector is the class responsible for opening and closing connections to the database.
 * It is used to pool connections.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class DatabaseConnector {
    private MysqlDataSource ds;

    public DatabaseConnector(String user, String password, String host, String db) {
        createDataSource(user, password, host, db);
    }

    private void createDataSource(String user, String password, String host, String db) {
        ds = new MysqlDataSource();
        ds.setServerName(host);
        ds.setDatabaseName(db);
        ds.setUser(user);
        ds.setPassword(password);
    }

    public Connection getConnection() throws SQLException {
        return ds.getConnection();
    }
}
