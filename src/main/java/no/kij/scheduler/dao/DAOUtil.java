package no.kij.scheduler.dao;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * This class exclusively contains static methods to be used by the DAO classes when doing SQL operations.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class DAOUtil {

    /**
     * Prepares a statement to be used in a query.
     *
     * @param conn Connection to create the PreparedStatement on
     * @param query Query to be prepared
     * @param returnKeys Whether to return the generated keys or not
     * @param values Values to be inserted into the prepared statement
     * @return Returns a prepared statement with bound values
     * @throws SQLException Throws SQL Exception if anything goes wrong while preparing the statement
     */
    public static PreparedStatement prepareStatement
            (Connection conn, String query, boolean returnKeys, Object... values) throws SQLException {
        int returnGeneratedKeys = returnKeys ? Statement.RETURN_GENERATED_KEYS : Statement.NO_GENERATED_KEYS;
        PreparedStatement stmt = conn.prepareStatement(query, returnGeneratedKeys);
        setValues(stmt, values);
        return stmt;
    }

    /**
     * Binds given values to the given statement.
     *
     * @param stmt Statement to insert values into
     * @param values Values to insert into statement
     * @throws SQLException Throws SQL Exception if anything goes wrong while binding the values to the statement
     */
    public static void setValues(PreparedStatement stmt, Object... values) throws SQLException {
        for (int i = 0; i < values.length; i++) {
            stmt.setObject(i + 1, values[i]);
        }
    }
}
