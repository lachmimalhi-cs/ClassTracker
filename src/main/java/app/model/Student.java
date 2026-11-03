package app.model;

/**
 * One student: a roll number and a name.
 * The constructor rejects empty values, so a Student object is always valid.
 */
public record Student(String roll, String name) {

    public Student {
        if (roll == null || roll.isBlank()) {
            throw new IllegalArgumentException("Roll number must not be empty.");
        }
        if (name == null || name.isBlank()) {
            throw new IllegalArgumentException("Name must not be empty.");
        }
        roll = roll.trim();
        name = name.trim();
    }
}
