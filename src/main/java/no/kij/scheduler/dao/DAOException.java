package no.kij.scheduler.dao;

/**
 * Thrown to indicate that a DAO method failed.
 *
 * @author Kissor Jeyabalan
 * @since 1.0
 */
public class DAOException extends RuntimeException {
    public DAOException(String message) {
        super(message);
    }

    public DAOException(String message, Throwable cause) {
        super(message, cause);
    }

    public DAOException(Throwable cause) {
        super(cause);
    }
}
