package app;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.application.Platform;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.geometry.Pos;
import javafx.scene.Node;
import javafx.scene.Scene;
import javafx.scene.control.*;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.*;
import javafx.scene.paint.Color;
import javafx.util.Duration;

import java.time.LocalDate;
import java.util.*;

/**
 * All the logic for main.fxml.
 * Data structures: HashMap (roll -> student), Trie (name search), PriorityQueue = min-heap (needs attention).
 */
public class MainController {

    private static final String[] COLOUR_KEYS = {"acc", "ok", "amb", "red"};
    private static final Map<String, String> PRESETS = Map.of(
            "Teal", "#0F766E", "Blue", "#2563EB", "Purple", "#7C3AED", "Pink", "#DB2777", "Orange", "#EA580C");

    // ===== parts of main.fxml (names must match fx:id there) =====
    @FXML private BorderPane root;
    @FXML private DatePicker datePicker;
    @FXML private VBox colourPanel;
    @FXML private ColorPicker cpAcc, cpOk, cpAmb, cpRed;
    @FXML private ToggleButton navDash, navCall, navStu;
    @FXML private ScrollPane pageDash, pageCall, pageStu;

    // dashboard
    @FXML private VBox welcomeCard, dashContent, needBox;
    @FXML private Label todayDate, todayMarked, tileAvg, tileBelow, tileFull, tileTotal, chartNote;
    @FXML private Button startBtn;
    @FXML private HBox stackBar, pillsBox, chartBox;

    // roll call
    @FXML private VBox rollEmpty, rollDeck;
    @FXML private Label rollInfo, rollNo, rollName, rollPill, overlay;
    @FXML private ProgressBar rollProgress;
    @FXML private StackPane swipeCard;

    // students
    @FXML private TextField rollField, nameField, searchField;
    @FXML private Label addMsg, profName, profPct, profCounts, profStreak, profAdvice;
    @FXML private VBox listBox, profileCard;
    @FXML private GridPane heatmap;

    // ===== data =====
    private Database db;
    private Scene scene;
    private List<Student> students = new ArrayList<>();
    private final Map<String, Student> byRoll = new HashMap<>();   // HASH MAP
    private Trie trie = new Trie();                                 // TRIE
    private Map<String, String> status = new HashMap<>();           // today's P / A / L
    private Map<String, int[]> stats = new HashMap<>();             // roll -> {present, total}

    private String tab = "dash", search = "", selected = null;
    private int idx = 0;
    private boolean flying = false, dark = false, syncing = false;
    private final Map<String, String> custom = new HashMap<>();

    // =====================================================================
    //  START (called automatically after main.fxml is loaded)
    // =====================================================================
    @FXML
    private void initialize() {
        db = new Database();
        dark = "1".equals(db.getSetting("dark"));
        for (String k : COLOUR_KEYS) {
            String v = db.getSetting("c_" + k);
            if (v != null && !v.isEmpty()) custom.put(k, v);
        }

        datePicker.setValue(LocalDate.now());
        datePicker.valueProperty().addListener((obs, oldV, newV) -> {
            if (newV == null) { datePicker.setValue(oldV); return; }
            idx = 0;
            refresh();
        });
        searchField.textProperty().addListener((obs, oldV, newV) -> { search = newV.trim(); fillList(); });

        attachSwipe();
        applyColours();
        syncPickers();
        refresh();
    }

    /** called from Main once the window exists: arrow keys on the Roll Call tab */
    public void attach(Scene s) {
        this.scene = s;
        s.addEventFilter(KeyEvent.KEY_PRESSED, ev -> {
            if (!tab.equals("call") || students.isEmpty() || flying) return;
            Node focus = scene.getFocusOwner();
            if (focus instanceof TextInputControl || focus instanceof DatePicker) return;
            String k = ev.getCode() == KeyCode.RIGHT ? "P"
                    : ev.getCode() == KeyCode.LEFT ? "A"
                    : ev.getCode() == KeyCode.DOWN ? "L" : null;
            if (k != null) { ev.consume(); flyCard(k); }
        });
    }

    public void shutdown() {
        if (db != null) db.close();
    }

    // =====================================================================
    //  TABS + LOADING DATA
    // =====================================================================
    @FXML private void goDash() { showTab("dash"); }
    @FXML private void goCall() { showTab("call"); }
    @FXML private void goStu()  { showTab("stu"); }

    private void showTab(String t) {
        tab = t;
        navDash.setSelected(t.equals("dash"));
        navCall.setSelected(t.equals("call"));
        navStu.setSelected(t.equals("stu"));
        refresh();
    }

    private void refresh() {
        students = db.students();
        byRoll.clear();
        trie = new Trie();
        for (Student s : students) {
            byRoll.put(s.roll(), s);
            for (String w : s.name().split("\\s+")) trie.insert(w, s.roll());
        }
        status = db.dayStatuses(date());
        stats = db.statsAll();

        show(pageDash, tab.equals("dash"));
        show(pageCall, tab.equals("call"));
        show(pageStu, tab.equals("stu"));
        switch (tab) {
            case "call" -> buildRollCall();
            case "stu" -> buildStudents();
            default -> buildDashboard();
        }
    }

    private static void show(Node n, boolean on) {
        n.setVisible(on);
        n.setManaged(on);
    }

    private String date() { return datePicker.getValue().toString(); }

    /** attendance percentage, or null if nothing is marked yet */
    private Integer pct(String roll) {
        int[] s = stats.get(roll);
        if (s == null || s[1] == 0) return null;
        return Math.round(s[0] * 100f / s[1]);
    }

    private String nextRoll() {
        int max = 0;
        for (Student s : students) {
            try { max = Math.max(max, Integer.parseInt(s.roll())); } catch (NumberFormatException ignored) { }
        }
        return String.valueOf(max + 1);
    }

    // =====================================================================
    //  DASHBOARD
    // =====================================================================
    @FXML
    private void addFirstStudent() {
        showTab("stu");
        Platform.runLater(() -> nameField.requestFocus());
    }

    @FXML
    private void startRollCall() { showTab("call"); }

    private void buildDashboard() {
        boolean empty = students.isEmpty();
        show(welcomeCard, empty);
        show(dashContent, !empty);
        if (empty) return;

        int n = students.size(), p = 0, a = 0, l = 0;
        for (Student s : students) {
            String v = status.get(s.roll());
            if ("P".equals(v)) p++; else if ("A".equals(v)) a++; else if ("L".equals(v)) l++;
        }
        int marked = p + a + l, notMarked = n - marked;

        todayDate.setText("Attendance for " + date());
        todayMarked.setText(marked + " of " + n + " marked");
        startBtn.setText(notMarked > 0 ? "Start Roll Call" : "Review Roll Call");

        stackBar.getChildren().clear();
        addSegment(p, n, "seg-P");
        addSegment(l, n, "seg-L");
        addSegment(a, n, "seg-A");
        pillsBox.getChildren().setAll(lbl("Present " + p, "pill", "pill-P"), lbl("Late " + l, "pill", "pill-L"),
                lbl("Absent " + a, "pill", "pill-A"), lbl("Not marked " + notMarked, "mute"));

        int count = 0, sum = 0, below = 0, full = 0;
        for (Student s : students) {
            Integer v = pct(s.roll());
            if (v == null) continue;
            count++; sum += v;
            if (v < 75) below++;
            if (v == 100) full++;
        }
        tileAvg.setText((count == 0 ? 0 : Math.round(sum / (float) count)) + "%");
        tileBelow.setText(String.valueOf(below));
        tileFull.setText(String.valueOf(full));
        tileTotal.setText(String.valueOf(n));

        // last 7 days chart
        chartBox.getChildren().clear();
        List<Database.DayStat> days = db.lastDays(7);
        if (days.isEmpty()) {
            chartNote.setText("Mark attendance to see the chart.");
        } else {
            chartNote.setText("Red bars are days below 75%.");
            for (Database.DayStat d : days) {
                int v = d.total() == 0 ? 0 : Math.round(d.present() * 100f / d.total());
                Region b = new Region();
                b.setPrefSize(34, Math.max(3, v * 1.4));
                b.setMinHeight(3);
                b.getStyleClass().addAll("chart-bar", v < 75 ? "bar-low" : "bar-ok");
                VBox col = new VBox(4, b, lbl(v + "%", "bold"), lbl(d.date().substring(5), "mute"));
                col.setAlignment(Pos.BOTTOM_CENTER);
                chartBox.getChildren().add(col);
            }
        }

        // needs attention (MIN-HEAP: lowest percentage comes out first)
        needBox.getChildren().clear();
        PriorityQueue<Student> heap = new PriorityQueue<>(Comparator.comparingInt((Student s) -> pct(s.roll())));
        for (Student s : students) if (pct(s.roll()) != null) heap.add(s);
        if (heap.isEmpty()) needBox.getChildren().add(lbl("Mark some attendance first.", "mute"));
        for (int i = 0; i < 3 && !heap.isEmpty(); i++) {
            Student s = heap.poll();
            Button name = new Button(s.name());
            name.getStyleClass().add("link");
            name.setOnAction(e -> { selected = s.roll(); showTab("stu"); });
            Integer v = pct(s.roll());
            Region fill = new Region();
            HBox.setHgrow(fill, Priority.ALWAYS);
            HBox r = new HBox(8, name, fill, lbl(v + "%", "bold", v < 75 ? "txt-red" : "txt-ok"));
            r.setAlignment(Pos.CENTER_LEFT);
            r.getStyleClass().add("srow");
            needBox.getChildren().add(r);
        }
    }

    private void addSegment(int value, int total, String cls) {
        if (value == 0) return;
        Region r = new Region();
        r.getStyleClass().add(cls);
        r.setMinHeight(14);
        r.prefWidthProperty().bind(stackBar.widthProperty().multiply(value / (double) total));
        stackBar.getChildren().add(r);
    }

    // =====================================================================
    //  ROLL CALL (swipe the card)
    // =====================================================================
    private void buildRollCall() {
        boolean empty = students.isEmpty();
        show(rollEmpty, empty);
        show(rollDeck, !empty);
        if (empty) return;

        if (idx >= students.size()) idx = 0;
        if (idx < 0) idx = students.size() - 1;
        Student s = students.get(idx);
        String v = status.get(s.roll());
        long done = students.stream().filter(x -> status.containsKey(x.roll())).count();

        rollInfo.setText("Card " + (idx + 1) + " of " + students.size() + "   |   " + done + " marked");
        rollProgress.setProgress(done / (double) students.size());
        rollNo.setText("Roll " + s.roll());
        rollName.setText(s.name());
        setPill(rollPill, v);

        swipeCard.getStyleClass().removeAll("card-P", "card-A", "card-L");
        if (v != null) swipeCard.getStyleClass().add("card-" + v);
        swipeCard.setTranslateX(0);
        swipeCard.setTranslateY(0);
        swipeCard.setOpacity(1);
        overlay.setOpacity(0);
    }

    @FXML private void markPresent() { flyCard("P"); }
    @FXML private void markAbsent()  { flyCard("A"); }
    @FXML private void markLate()    { flyCard("L"); }

    @FXML private void rollBack() { idx--; refresh(); }

    @FXML
    private void rollClear() {
        if (students.isEmpty()) return;
        db.clearStatus(date(), students.get(idx).roll());
        refresh();
    }

    @FXML
    private void restPresent() {
        for (Student x : students) if (!status.containsKey(x.roll())) db.setStatus(date(), x.roll(), "P");
        refresh();
    }

    /** drag the card with the mouse or finger */
    private void attachSwipe() {
        double[] start = new double[2];
        String[] dir = new String[1];
        swipeCard.setOnMousePressed(e -> { start[0] = e.getSceneX(); start[1] = e.getSceneY(); dir[0] = null; });
        swipeCard.setOnMouseDragged(e -> {
            if (flying) return;
            double dx = e.getSceneX() - start[0];
            double dy = Math.max(0, e.getSceneY() - start[1]);
            boolean sideways = Math.abs(dx) > dy;
            swipeCard.setTranslateX(dx);
            swipeCard.setTranslateY(sideways ? 0 : dy);
            String k = sideways ? (dx > 0 ? "P" : "A") : "L";
            double moved = Math.max(Math.abs(dx), dy);
            dir[0] = moved > 90 ? k : null;
            overlay.setText(word(k).toUpperCase());
            overlay.getStyleClass().removeAll("ov-P", "ov-A", "ov-L");
            overlay.getStyleClass().add("ov-" + k);
            overlay.setOpacity(Math.min(1, moved / 110));
        });
        swipeCard.setOnMouseReleased(e -> {
            if (flying) return;
            if (dir[0] != null) {
                flyCard(dir[0]);
            } else {
                TranslateTransition back = new TranslateTransition(Duration.millis(180), swipeCard);
                back.setToX(0);
                back.setToY(0);
                back.play();
                overlay.setOpacity(0);
            }
        });
    }

    /** the card flies away, the status is saved, and the next student appears */
    private void flyCard(String k) {
        if (flying || students.isEmpty()) return;
        flying = true;
        Student s = students.get(idx);
        TranslateTransition move = new TranslateTransition(Duration.millis(250), swipeCard);
        move.setToX(k.equals("P") ? 700 : k.equals("A") ? -700 : 0);
        move.setToY(k.equals("L") ? 500 : 0);
        FadeTransition fade = new FadeTransition(Duration.millis(250), swipeCard);
        fade.setToValue(0);
        ParallelTransition both = new ParallelTransition(move, fade);
        both.setOnFinished(e -> {
            db.setStatus(date(), s.roll(), k);
            idx++;
            flying = false;
            refresh();
        });
        both.play();
    }

    // =====================================================================
    //  STUDENTS
    // =====================================================================
    private void buildStudents() {
        rollField.setText(nextRoll());
        fillList();
        refreshProfile();
    }

    @FXML
    private void addStudent() {
        String r = rollField.getText().trim(), n = nameField.getText().trim();
        if (r.isEmpty() || n.isEmpty()) { addMsg.setText("Enter both roll number and name."); return; }
        if (!db.addStudent(r, n)) { addMsg.setText("Roll " + r + " already exists."); return; }
        addMsg.setText("");
        nameField.clear();
        refresh();
        Platform.runLater(() -> nameField.requestFocus());
    }

    private void fillList() {
        listBox.getChildren().clear();
        List<Student> list = students;
        if (!search.isEmpty()) {
            if (search.contains(" ")) {
                String q = search.toLowerCase();
                list = students.stream().filter(x -> x.name().toLowerCase().contains(q)).toList();
            } else {
                Set<String> ids = trie.find(search);    // TRIE search
                list = students.stream().filter(x -> ids.contains(x.roll())).toList();
            }
        }
        if (list.isEmpty()) {
            listBox.getChildren().add(lbl(students.isEmpty() ? "No students yet. Add your first student above."
                    : "No student matches.", "mute"));
        }
        for (Student s : list) listBox.getChildren().add(studentRow(s));
    }

    private HBox studentRow(Student s) {
        Integer p = pct(s.roll());
        int[] st = stats.get(s.roll());

        Label r = lbl(s.roll(), "mute");
        r.setMinWidth(46);
        Button name = new Button(s.name());
        name.getStyleClass().add("link");
        name.setMinWidth(170);
        name.setOnAction(e -> { selected = s.roll(); refreshProfile(); });
        Label today = new Label();
        setPill(today, status.get(s.roll()));
        today.setMinWidth(90);

        ProgressBar pbar = new ProgressBar(p == null ? 0 : p / 100.0);
        pbar.getStyleClass().add(p != null && p < 75 ? "pb-low" : "pb-ok");
        pbar.setPrefWidth(120);
        pbar.setPrefHeight(10);
        Label ptxt = lbl(p == null ? "No data" : p + "%  (" + st[0] + " of " + st[1] + " days)", "mute");
        VBox pc = new VBox(3, pbar, ptxt);
        pc.setMinWidth(170);

        Region fill = new Region();
        HBox.setHgrow(fill, Priority.ALWAYS);
        Button edit = new Button("Edit");
        edit.setOnAction(e -> {
            TextInputDialog d = new TextInputDialog(s.name());
            d.setTitle("Edit student");
            d.setHeaderText("New name for roll " + s.roll());
            d.showAndWait().ifPresent(n -> {
                if (!n.trim().isEmpty()) { db.updateName(s.roll(), n.trim()); refresh(); }
            });
        });
        Button del = new Button("Delete");
        del.setOnAction(e -> {
            Alert a = new Alert(Alert.AlertType.CONFIRMATION,
                    "Delete " + s.name() + " and all of their attendance?", ButtonType.YES, ButtonType.NO);
            a.setHeaderText(null);
            a.showAndWait().filter(b -> b == ButtonType.YES).ifPresent(b -> {
                db.deleteStudent(s.roll());
                if (s.roll().equals(selected)) selected = null;
                refresh();
            });
        });

        HBox row = new HBox(12, r, name, today, pc, fill, edit, del);
        row.setAlignment(Pos.CENTER_LEFT);
        row.getStyleClass().add("srow");
        return row;
    }

    @FXML
    private void closeProfile() {
        selected = null;
        refreshProfile();
    }

    private void refreshProfile() {
        Student s = selected == null ? null : byRoll.get(selected);
        show(profileCard, s != null);
        if (s == null) return;

        Map<String, String> history = db.history(s.roll());
        int[] st = stats.get(s.roll());
        int present = st == null ? 0 : st[0], total = st == null ? 0 : st[1];
        Integer p = pct(s.roll());

        profName.setText(s.name() + "  (Roll " + s.roll() + ")");

        // 28-day heatmap
        heatmap.getChildren().clear();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 28; i++) {
            LocalDate d = today.minusDays(27 - i);
            String v = history.get(d.toString());
            Region cell = new Region();
            cell.setPrefSize(28, 28);
            cell.getStyleClass().addAll("cell", v == null ? "cell-N" : "cell-" + v);
            Tooltip.install(cell, new Tooltip(d + (v == null ? "" : "  -  " + word(v))));
            heatmap.add(cell, i % 7, i / 7);
        }

        // streak = days in a row (newest first) without an Absent
        List<String> dates = new ArrayList<>(history.keySet());
        Collections.reverse(dates);
        int streak = 0;
        for (String d : dates) {
            if (history.get(d).equals("A")) break;
            streak++;
        }

        String advice;
        if (total == 0) advice = "No attendance marked yet.";
        else if (present * 4 < total * 3)
            advice = "Attend the next " + (int) Math.ceil((0.75 * total - present) / 0.25) + " classes in a row to reach 75%.";
        else
            advice = "Can miss " + (int) Math.floor(present / 0.75 - total) + " more classes and stay at 75%.";

        profPct.setText(p == null ? "--" : p + "%");
        profCounts.setText(present + " present, " + (total - present) + " absent, " + total + " days");
        profStreak.setText("Current streak: " + streak + " days");
        profAdvice.setText(advice);
    }

    // =====================================================================
    //  COLOURS + DARK MODE
    // =====================================================================
    @FXML
    private void toggleColours() {
        show(colourPanel, !colourPanel.isVisible());
    }

    @FXML
    private void toggleTheme() {
        dark = !dark;
        db.setSetting("dark", dark ? "1" : "0");
        applyColours();
        syncPickers();
    }

    @FXML
    private void preset(ActionEvent e) {
        String hex = PRESETS.get(((Button) e.getSource()).getText());
        if (hex == null) return;
        custom.put("acc", hex);
        custom.put("ok", hex);
        saveColours();
        applyColours();
        syncPickers();
    }

    @FXML
    private void pickColour(ActionEvent e) {
        if (syncing) return;
        Object src = e.getSource();
        String key = src == cpAcc ? "acc" : src == cpOk ? "ok" : src == cpAmb ? "amb" : "red";
        custom.put(key, hex(((ColorPicker) src).getValue()));
        saveColours();
        applyColours();
    }

    @FXML
    private void resetColours() {
        custom.clear();
        saveColours();
        applyColours();
        syncPickers();
    }

    private void syncPickers() {
        syncing = true;
        cpAcc.setValue(Color.web(colour("acc")));
        cpOk.setValue(Color.web(colour("ok")));
        cpAmb.setValue(Color.web(colour("amb")));
        cpRed.setValue(Color.web(colour("red")));
        syncing = false;
    }

    private String colour(String key) {
        String[] light = {"#0F766E", "#0F766E", "#92400E", "#B91C1C"};
        String[] night = {"#4FD1C5", "#4FD1C5", "#FCD34D", "#FCA5A5"};
        int i = Arrays.asList(COLOUR_KEYS).indexOf(key);
        return custom.getOrDefault(key, dark ? night[i] : light[i]);
    }

    private void applyColours() {
        Color cardColour = Color.web(dark ? "#18222A" : "#FFFFFF");
        StringBuilder sb = new StringBuilder();
        for (String k : COLOUR_KEYS) {
            Color c = Color.web(colour(k));
            sb.append("-").append(k).append(":").append(hex(c)).append(";");
            if (!k.equals("acc")) {
                sb.append("-").append(k).append("-bg:").append(hex(c.interpolate(cardColour, 0.84))).append(";");
            }
        }
        root.setStyle(sb.toString());
        root.getStyleClass().remove("dark");
        if (dark) root.getStyleClass().add("dark");
    }

    private void saveColours() {
        for (String k : COLOUR_KEYS) db.setSetting("c_" + k, custom.get(k));
    }

    private static String hex(Color c) {
        return String.format("#%02X%02X%02X", Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }

    // =====================================================================
    //  SMALL HELPERS
    // =====================================================================
    private static String word(String k) {
        return switch (k) { case "P" -> "Present"; case "A" -> "Absent"; default -> "Late"; };
    }

    private static Label lbl(String text, String... classes) {
        Label l = new Label(text);
        for (String c : classes) if (!c.isEmpty()) l.getStyleClass().add(c);
        return l;
    }

    /** shows Present / Absent / Late as a coloured pill, or "Not marked" */
    private static void setPill(Label l, String v) {
        l.getStyleClass().removeAll("pill", "pill-P", "pill-L", "pill-A", "mute");
        if (v == null) {
            l.setText("Not marked");
            l.getStyleClass().add("mute");
        } else {
            l.setText(word(v));
            l.getStyleClass().addAll("pill", "pill-" + v);
        }
    }
}