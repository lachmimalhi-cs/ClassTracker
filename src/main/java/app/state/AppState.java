package app.state;

import app.algo.AttendanceAdvisor;
import app.algo.StudentSearch;
import app.data.DataAccessException;
import app.data.Database;
import app.model.Student;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.IntegerProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleIntegerProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

import java.time.LocalDate;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * The one object that all controllers share. It owns the database, the theme and the current data
 * (students, today's marks, totals) and offers a few observable properties. It has no UI nodes.
 * Controllers listen to {@link #versionProperty()} and rebuild their own properties when it changes.
 */
public final class AppState {

    private static AppState instance;

    public static synchronized AppState get() {
        if (instance == null) {
            instance = new AppState(new Database());
        }
        return instance;
    }

    public static synchronized void shutdown() {
        if (instance != null) {
            instance.db.close();
            instance = null;
        }
    }

    private final Database db;
    private final ThemeService theme;
    private final AttendanceAdvisor advisor = new AttendanceAdvisor();

    private final ObjectProperty<LocalDate> date = new SimpleObjectProperty<>(LocalDate.now());
    private final StringProperty tab = new SimpleStringProperty("dash");
    private final StringProperty selectedRoll = new SimpleStringProperty();
    private final StringProperty message = new SimpleStringProperty("");
    private final IntegerProperty version = new SimpleIntegerProperty();
    private final StringProperty themeStyle = new SimpleStringProperty();
    private final BooleanProperty coloursOpen = new SimpleBooleanProperty();

    private StudentSearch search = new StudentSearch(List.of());
    private Map<String, String> status = new HashMap<>();
    private Map<String, int[]> stats = new HashMap<>();

    public AppState(Database db) {
        this.db = db;
        this.theme = new ThemeService(db);
        this.themeStyle.set(theme.style());
        refresh();
    }

    // ---------- observable properties ----------

    public ObjectProperty<LocalDate> dateProperty() { return date; }
    public StringProperty tabProperty() { return tab; }
    public StringProperty selectedRollProperty() { return selectedRoll; }
    public StringProperty messageProperty() { return message; }
    public IntegerProperty versionProperty() { return version; }
    public StringProperty themeStyleProperty() { return themeStyle; }
    public BooleanProperty coloursOpenProperty() { return coloursOpen; }
    public ThemeService theme() { return theme; }
    public AttendanceAdvisor advisor() { return advisor; }

    public String dateText() { return date.get().toString(); }

    /** Call after the theme was changed, so the screen gets the new CSS variables. */
    public void themeChanged() {
        themeStyle.set(theme.style());
    }

    // ---------- reading ----------

    public List<Student> students() { return search.search(""); }

    public boolean isEmpty() { return search.size() == 0; }

    public StudentSearch search() { return search; }

    public String statusOf(String roll) { return status.get(roll); }

    public boolean isMarked(String roll) { return status.containsKey(roll); }

    /** {daysPresentOrLate, daysMarked}; {0, 0} when nothing is marked. */
    public int[] statsOf(String roll) { return stats.getOrDefault(roll, new int[]{0, 0}); }

    /** Attendance percentage, or null when nothing is marked yet. */
    public Integer percentOf(String roll) {
        int[] s = statsOf(roll);
        return advisor.percent(s[0], s[1]);
    }

    public String nextRoll() {
        int max = 0;
        for (Student s : students()) {
            try {
                max = Math.max(max, Integer.parseInt(s.roll()));
            } catch (NumberFormatException ignored) {
                // roll numbers that are not numbers are ignored
            }
        }
        return String.valueOf(max + 1);
    }

    public Map<String, String> history(String roll) { return db.history(roll); }

    public List<Database.DayStat> lastDays(int n) { return db.lastDays(n); }

    // ---------- changing ----------

    /** Reloads everything from the database and tells the controllers. */
    public void refresh() {
        List<Student> list = db.students();
        search = new StudentSearch(list);
        status = db.dayStatuses(dateText());
        stats = db.statsAll();
        version.set(version.get() + 1);
    }

    public void setDate(LocalDate d) {
        if (d == null) {
            throw new IllegalArgumentException("The date must not be null.");
        }
        date.set(d);
        refresh();
    }

    public void showTab(String t) {
        tab.set(t);
        refresh();
    }

    public void addStudent(String roll, String name) {
        db.addStudent(new Student(roll, name));
        refresh();
    }

    public void rename(String roll, String name) {
        db.updateName(roll, new Student(roll, name).name());
        refresh();
    }

    public void delete(String roll) {
        db.deleteStudent(roll);
        if (roll.equals(selectedRoll.get())) {
            selectedRoll.set(null);
        }
        refresh();
    }

    public void mark(String roll, String statusCode) {
        db.setStatus(dateText(), roll, statusCode);
        refresh();
    }

    public void clearMark(String roll) {
        db.clearStatus(dateText(), roll);
        refresh();
    }

    public void markRestPresent() {
        for (Student s : students()) {
            if (!isMarked(s.roll())) {
                db.setStatus(dateText(), s.roll(), "P");
            }
        }
        refresh();
    }

    /**
     * Runs an action and shows a readable message if it fails.
     * Returns true when the action worked.
     */
    public boolean attempt(Runnable action) {
        message.set("");
        try {
            action.run();
            return true;
        } catch (DataAccessException | IllegalArgumentException e) {
            message.set(e.getMessage());
            return false;
        }
    }

    public static String word(String code) {
        return switch (code) {
            case "P" -> "Present";
            case "A" -> "Absent";
            case "L" -> "Late";
            default -> throw new IllegalArgumentException("Unknown status: " + code);
        };
    }

    /** Test helper: list of the current statuses, unmodifiable. */
    public Map<String, String> todayStatuses() { return Collections.unmodifiableMap(status); }
}
