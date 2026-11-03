package app.data;

/** Thrown when a student with the same roll number already exists. */
public class DuplicateRollException extends DataAccessException {
    private static final long serialVersionUID = 1L;

    public DuplicateRollException(String roll, Throwable cause) {
        super("Roll " + roll + " already exists.", cause);
    }
}
