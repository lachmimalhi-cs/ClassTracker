package app.controller;

import app.algo.AttendanceAdvisor;
import app.data.DataAccessException;
import app.item.HeatCellItem;
import app.item.StudentRowItem;
import app.model.Student;
import app.state.AppState;
import app.view.TextChangedEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.collections.ObservableList;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

/**
 * Controller of students.fxml: add form, search, student list and profile.
 * The search uses StudentSearch (Trie). No nodes are created here; the rows come from student-row.fxml
 * and the heat map from heat-cell.fxml, both filled by repeaters.
 */
public class StudentsController {

    private final AppState state = AppState.get();
    private final AttendanceAdvisor advisor = state.advisor();

    @FXML private TextField rollField;
    @FXML private TextField nameField;

    private final StringProperty rollSeed = new SimpleStringProperty("");
    private final StringProperty nameSeed = new SimpleStringProperty("");
    private final StringProperty addMsg = new SimpleStringProperty("");
    private final ListProperty<StudentRowItem> rows = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final StringProperty listNote = new SimpleStringProperty("");
    private final BooleanProperty hasNote = new SimpleBooleanProperty(false);

    private final BooleanProperty profileVisible = new SimpleBooleanProperty(false);
    private final StringProperty profName = new SimpleStringProperty("");
    private final StringProperty profPct = new SimpleStringProperty("--");
    private final StringProperty profCounts = new SimpleStringProperty("");
    private final StringProperty profStreak = new SimpleStringProperty("");
    private final StringProperty profAdvice = new SimpleStringProperty("");
    private final ListProperty<HeatCellItem> heatCells = new SimpleListProperty<>(FXCollections.observableArrayList());

    private String query = "";

    @FXML
    private void initialize() {
        state.versionProperty().addListener((obs, o, n) -> update());
        state.selectedRollProperty().addListener((obs, o, n) -> updateProfile());
        update();
    }

    // ---------- add ----------

    @FXML
    private void addStudent() {
        try {
            state.addStudent(rollField.getText(), nameField.getText());
            addMsg.set("");
            reseed(nameSeed, "");
        } catch (DataAccessException | IllegalArgumentException e) {
            addMsg.set(e.getMessage());
        }
    }

    // ---------- search ----------

    @FXML
    private void onSearch(TextChangedEvent e) {
        query = e.getText().trim();
        updateList();
    }

    // ---------- profile ----------

    @FXML
    private void closeProfile() {
        state.selectedRollProperty().set(null);
    }

    // ---------- updating the properties ----------

    private void update() {
        if ("stu".equals(state.tabProperty().get())) {
            reseed(rollSeed, state.nextRoll());
        }
        updateList();
        updateProfile();
    }

    private void updateList() {
        List<Student> found = state.search().search(query);
        List<StudentRowItem> items = new ArrayList<>();
        for (Student s : found) {
            items.add(rowFor(s));
        }
        rows.setAll(items);
        boolean note = found.isEmpty();
        hasNote.set(note);
        listNote.set(!note ? "" : state.isEmpty() ? "No students yet. Add your first student above." : "No student matches.");
    }

    private StudentRowItem rowFor(Student s) {
        int[] st = state.statsOf(s.roll());
        return new StudentRowItem(s.roll(), s.name(), state.statusOf(s.roll()), advisor.percent(st[0], st[1]),
                st[0], st[1], advisor.belowTarget(st[0], st[1]),
                () -> state.selectedRollProperty().set(s.roll()),
                newName -> state.attempt(() -> state.rename(s.roll(), newName)),
                () -> state.attempt(() -> state.delete(s.roll())));
    }

    private void updateProfile() {
        String roll = state.selectedRollProperty().get();
        Student s = roll == null ? null : state.search().byRoll(roll).orElse(null);
        profileVisible.set(s != null);
        if (s == null) {
            return;
        }
        Map<String, String> history = state.history(s.roll());
        int[] st = state.statsOf(s.roll());
        Integer p = advisor.percent(st[0], st[1]);

        profName.set(s.name() + "  (Roll " + s.roll() + ")");
        profPct.set(p == null ? "--" : p + "%");
        profCounts.set(st[0] + " present, " + (st[1] - st[0]) + " absent, " + st[1] + " days");
        profStreak.set("Current streak: " + advisor.streak(new ArrayList<>(history.values())) + " days");
        profAdvice.set(advisor.advice(st[0], st[1]));

        List<HeatCellItem> cells = new ArrayList<>();
        LocalDate today = LocalDate.now();
        for (int i = 0; i < 28; i++) {
            String d = today.minusDays(27 - i).toString();
            cells.add(new HeatCellItem(d, history.get(d)));
        }
        heatCells.setAll(cells);
    }

    /** Sets the seed to null first, so the same text is shown again. */
    private static void reseed(StringProperty seed, String text) {
        seed.set(null);
        seed.set(text);
    }

    public StringProperty rollSeedProperty() { return rollSeed; }
    public String getRollSeed() { return rollSeed.get(); }
    public StringProperty nameSeedProperty() { return nameSeed; }
    public String getNameSeed() { return nameSeed.get(); }
    public StringProperty addMsgProperty() { return addMsg; }
    public String getAddMsg() { return addMsg.get(); }
    public ListProperty<StudentRowItem> rowsProperty() { return rows; }
    public ObservableList<StudentRowItem> getRows() { return rows.get(); }
    public StringProperty listNoteProperty() { return listNote; }
    public String getListNote() { return listNote.get(); }
    public BooleanProperty hasNoteProperty() { return hasNote; }
    public boolean isHasNote() { return hasNote.get(); }
    public BooleanProperty profileVisibleProperty() { return profileVisible; }
    public boolean isProfileVisible() { return profileVisible.get(); }
    public StringProperty profNameProperty() { return profName; }
    public String getProfName() { return profName.get(); }
    public StringProperty profPctProperty() { return profPct; }
    public String getProfPct() { return profPct.get(); }
    public StringProperty profCountsProperty() { return profCounts; }
    public String getProfCounts() { return profCounts.get(); }
    public StringProperty profStreakProperty() { return profStreak; }
    public String getProfStreak() { return profStreak.get(); }
    public StringProperty profAdviceProperty() { return profAdvice; }
    public String getProfAdvice() { return profAdvice.get(); }
    public ListProperty<HeatCellItem> heatCellsProperty() { return heatCells; }
    public ObservableList<HeatCellItem> getHeatCells() { return heatCells.get(); }
}
