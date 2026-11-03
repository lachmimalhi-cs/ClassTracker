package app.data;

/** Thrown when the database cannot be read or written. The message is safe to show to the user. */
public class DataAccessException extends RuntimeException {
    private static final long serialVersionUID = 1L;

    public DataAccessException(String message, Throwable cause) {
        super(message, cause);
    }
}
