package app;

import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.*;
import java.util.*;

/**
 * Everything that talks to SQLite lives here.
 * The database file is created automatically at:  (your home folder)/ClassTrack/classtrack.db
 */
public class Database {

    /** present = Present + Late, total = days marked */
    public record DayStat(String date, int present, int total) { }

    private Connection conn;

    public Database() {
        try {
            Path dir = Paths.get(System.getProperty("user.home"), "ClassTrack");
            Files.createDirectories(dir);
            conn = DriverManager.getConnection("jdbc:sqlite:" + dir.resolve("classtrack.db"));
            try (Statement st = conn.createStatement()) {
                st.execute("CREATE TABLE IF NOT EXISTS students(roll TEXT PRIMARY KEY, name TEXT NOT NULL)");
                st.execute("CREATE TABLE IF NOT EXISTS attendance(date TEXT NOT NULL, roll TEXT NOT NULL, "
                        + "status TEXT NOT NULL, PRIMARY KEY(date, roll))");
                st.execute("CREATE TABLE IF NOT EXISTS settings(key TEXT PRIMARY KEY, value TEXT)");
            }
        } catch (Exception e) {
            throw new RuntimeException("Could not open the database", e);
        }
    }

    public void close() {
        try { conn.close(); } catch (SQLException ignored) { }
    }

    // ---------- students ----------

    public List<Student> students() {
        List<Student> list = new ArrayList<>();
        String sql = "SELECT roll, name FROM students ORDER BY CAST(roll AS INTEGER), roll";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) list.add(new Student(rs.getString(1), rs.getString(2)));
        } catch (SQLException e) { throw new RuntimeException(e); }
        return list;
    }

    /** returns false if this roll number already exists */
    public boolean addStudent(String roll, String name) {
        try (PreparedStatement ps = conn.prepareStatement("INSERT INTO students(roll, name) VALUES(?, ?)")) {
            ps.setString(1, roll);
            ps.setString(2, name);
            ps.executeUpdate();
            return true;
        } catch (SQLException e) {
            return false; // duplicate roll
        }
    }

    public void updateName(String roll, String name) {
        run("UPDATE students SET name = ? WHERE roll = ?", name, roll);
    }

    public void deleteStudent(String roll) {
        run("DELETE FROM attendance WHERE roll = ?", roll);
        run("DELETE FROM students WHERE roll = ?", roll);
    }

    // ---------- attendance ----------

    /** roll -> status ("P", "A" or "L") for one date */
    public Map<String, String> dayStatuses(String date) {
        Map<String, String> m = new HashMap<>();
        try (PreparedStatement ps = conn.prepareStatement("SELECT roll, status FROM attendance WHERE date = ?")) {
            ps.setString(1, date);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) m.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return m;
    }

    public void setStatus(String date, String roll, String status) {
        run("INSERT OR REPLACE INTO attendance(date, roll, status) VALUES(?, ?, ?)", date, roll, status);
    }

    public void clearStatus(String date, String roll) {
        run("DELETE FROM attendance WHERE date = ? AND roll = ?", date, roll);
    }

    /** roll -> {daysPresentOrLate, daysMarked} */
    public Map<String, int[]> statsAll() {
        Map<String, int[]> m = new HashMap<>();
        String sql = "SELECT roll, SUM(CASE WHEN status <> 'A' THEN 1 ELSE 0 END), COUNT(*) "
                + "FROM attendance GROUP BY roll";
        try (Statement st = conn.createStatement(); ResultSet rs = st.executeQuery(sql)) {
            while (rs.next()) m.put(rs.getString(1), new int[]{rs.getInt(2), rs.getInt(3)});
        } catch (SQLException e) { throw new RuntimeException(e); }
        return m;
    }

    /** the last n dates that have attendance, oldest first */
    public List<DayStat> lastDays(int n) {
        List<DayStat> list = new ArrayList<>();
        String sql = "SELECT date, SUM(CASE WHEN status <> 'A' THEN 1 ELSE 0 END), COUNT(*) "
                + "FROM attendance GROUP BY date ORDER BY date DESC LIMIT ?";
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setInt(1, n);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) list.add(new DayStat(rs.getString(1), rs.getInt(2), rs.getInt(3)));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        Collections.reverse(list);
        return list;
    }

    /** date -> status for one student, oldest first */
    public Map<String, String> history(String roll) {
        Map<String, String> m = new LinkedHashMap<>();
        try (PreparedStatement ps = conn.prepareStatement(
                "SELECT date, status FROM attendance WHERE roll = ? ORDER BY date")) {
            ps.setString(1, roll);
            try (ResultSet rs = ps.executeQuery()) {
                while (rs.next()) m.put(rs.getString(1), rs.getString(2));
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
        return m;
    }

    // ---------- settings (dark mode, colours) ----------

    public String getSetting(String key) {
        try (PreparedStatement ps = conn.prepareStatement("SELECT value FROM settings WHERE key = ?")) {
            ps.setString(1, key);
            try (ResultSet rs = ps.executeQuery()) {
                return rs.next() ? rs.getString(1) : null;
            }
        } catch (SQLException e) { throw new RuntimeException(e); }
    }

    public void setSetting(String key, String value) {
        if (value == null) run("DELETE FROM settings WHERE key = ?", key);
        else run("INSERT OR REPLACE INTO settings(key, value) VALUES(?, ?)", key, value);
    }

    // ---------- helper ----------

    private void run(String sql, String... params) {
        try (PreparedStatement ps = conn.prepareStatement(sql)) {
            for (int i = 0; i < params.length; i++) ps.setString(i + 1, params[i]);
            ps.executeUpdate();
        } catch (SQLException e) { throw new RuntimeException(e); }
    }
}