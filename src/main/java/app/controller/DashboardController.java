package app.controller;

import app.algo.AttendanceAdvisor;
import app.algo.NeedsAttentionSelector;
import app.data.Database;
import app.item.ChartBarItem;
import app.item.NeedItem;
import app.model.Student;
import app.state.AppState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ListProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleListProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.FXCollections;
import javafx.fxml.FXML;

import java.util.List;

/**
 * Controller of dashboard.fxml. It only calculates numbers and texts and exposes them as properties;
 * dashboard.fxml shows them. There are no nodes in this class.
 */
public class DashboardController {

    private final AppState state = AppState.get();
    private final AttendanceAdvisor advisor = state.advisor();
    private final NeedsAttentionSelector selector = new NeedsAttentionSelector(3);

    private final BooleanProperty empty = new SimpleBooleanProperty(true);
    private final BooleanProperty filled = new SimpleBooleanProperty(false);
    private final StringProperty dateText = new SimpleStringProperty("");
    private final StringProperty markedText = new SimpleStringProperty("");
    private final StringProperty startText = new SimpleStringProperty("Start Roll Call");
    private final DoubleProperty present = new SimpleDoubleProperty();
    private final DoubleProperty late = new SimpleDoubleProperty();
    private final DoubleProperty absent = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final StringProperty presentText = new SimpleStringProperty("");
    private final StringProperty lateText = new SimpleStringProperty("");
    private final StringProperty absentText = new SimpleStringProperty("");
    private final StringProperty notMarkedText = new SimpleStringProperty("");
    private final StringProperty avgText = new SimpleStringProperty("0%");
    private final StringProperty belowText = new SimpleStringProperty("0");
    private final StringProperty fullText = new SimpleStringProperty("0");
    private final StringProperty totalText = new SimpleStringProperty("0");
    private final StringProperty chartNote = new SimpleStringProperty("");
    private final ListProperty<ChartBarItem> chartBars = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final ListProperty<NeedItem> needItems = new SimpleListProperty<>(FXCollections.observableArrayList());
    private final BooleanProperty needEmpty = new SimpleBooleanProperty(true);

    @FXML
    private void initialize() {
        state.versionProperty().addListener((obs, o, n) -> update());
        update();
    }

    @FXML
    private void addFirstStudent() {
        state.showTab("stu");
    }

    @FXML
    private void startRollCall() {
        state.showTab("call");
    }

    private void update() {
        List<Student> students = state.students();
        empty.set(students.isEmpty());
        filled.set(!students.isEmpty());
        if (students.isEmpty()) {
            return;
        }
        int n = students.size();
        int p = 0;
        int a = 0;
        int l = 0;
        for (Student s : students) {
            String v = state.statusOf(s.roll());
            if ("P".equals(v)) {
                p++;
            } else if ("A".equals(v)) {
                a++;
            } else if ("L".equals(v)) {
                l++;
            }
        }
        int marked = p + a + l;
        int notMarked = n - marked;

        dateText.set("Attendance for " + state.dateText());
        markedText.set(marked + " of " + n + " marked");
        startText.set(notMarked > 0 ? "Start Roll Call" : "Review Roll Call");
        present.set(p);
        late.set(l);
        absent.set(a);
        total.set(n);
        presentText.set("Present " + p);
        lateText.set("Late " + l);
        absentText.set("Absent " + a);
        notMarkedText.set("Not marked " + notMarked);

        int count = 0;
        int sum = 0;
        int below = 0;
        int full = 0;
        for (Student s : students) {
            int[] st = state.statsOf(s.roll());
            Integer v = advisor.percent(st[0], st[1]);
            if (v == null) {
                continue;
            }
            count++;
            sum += v;
            if (advisor.belowTarget(st[0], st[1])) {
                below++;
            }
            if (st[0] == st[1]) {
                full++;
            }
        }
        avgText.set((count == 0 ? 0 : (2 * sum + count) / (2 * count)) + "%");
        belowText.set(String.valueOf(below));
        fullText.set(String.valueOf(full));
        totalText.set(String.valueOf(n));

        List<Database.DayStat> days = state.lastDays(7);
        chartNote.set(days.isEmpty() ? "Mark attendance to see the chart." : "Red bars are days below 75%.");
        chartBars.setAll(days.stream().map(d -> new ChartBarItem(d.date(), d.present(), d.total())).toList());

        List<Student> needs = selector.select(students, s -> state.percentOf(s.roll()));
        needEmpty.set(needs.isEmpty());
        needItems.setAll(needs.stream().map(s -> {
            int[] st = state.statsOf(s.roll());
            return new NeedItem(s.name(), advisor.percent(st[0], st[1]), advisor.belowTarget(st[0], st[1]),
                    () -> {
                        state.selectedRollProperty().set(s.roll());
                        state.showTab("stu");
                    });
        }).toList());
    }

    public BooleanProperty emptyProperty() { return empty; }
    public boolean isEmpty() { return empty.get(); }
    public BooleanProperty filledProperty() { return filled; }
    public boolean isFilled() { return filled.get(); }
    public StringProperty dateTextProperty() { return dateText; }
    public String getDateText() { return dateText.get(); }
    public StringProperty markedTextProperty() { return markedText; }
    public String getMarkedText() { return markedText.get(); }
    public StringProperty startTextProperty() { return startText; }
    public String getStartText() { return startText.get(); }
    public DoubleProperty presentProperty() { return present; }
    public double getPresent() { return present.get(); }
    public DoubleProperty lateProperty() { return late; }
    public double getLate() { return late.get(); }
    public DoubleProperty absentProperty() { return absent; }
    public double getAbsent() { return absent.get(); }
    public DoubleProperty totalProperty() { return total; }
    public double getTotal() { return total.get(); }
    public StringProperty presentTextProperty() { return presentText; }
    public String getPresentText() { return presentText.get(); }
    public StringProperty lateTextProperty() { return lateText; }
    public String getLateText() { return lateText.get(); }
    public StringProperty absentTextProperty() { return absentText; }
    public String getAbsentText() { return absentText.get(); }
    public StringProperty notMarkedTextProperty() { return notMarkedText; }
    public String getNotMarkedText() { return notMarkedText.get(); }
    public StringProperty avgTextProperty() { return avgText; }
    public String getAvgText() { return avgText.get(); }
    public StringProperty belowTextProperty() { return belowText; }
    public String getBelowText() { return belowText.get(); }
    public StringProperty fullTextProperty() { return fullText; }
    public String getFullText() { return fullText.get(); }
    public StringProperty totalTextProperty() { return totalText; }
    public String getTotalText() { return totalText.get(); }
    public StringProperty chartNoteProperty() { return chartNote; }
    public String getChartNote() { return chartNote.get(); }
    public ListProperty<ChartBarItem> chartBarsProperty() { return chartBars; }
    public javafx.collections.ObservableList<ChartBarItem> getChartBars() { return chartBars.get(); }
    public ListProperty<NeedItem> needItemsProperty() { return needItems; }
    public javafx.collections.ObservableList<NeedItem> getNeedItems() { return needItems.get(); }
    public BooleanProperty needEmptyProperty() { return needEmpty; }
    public boolean isNeedEmpty() { return needEmpty.get(); }
}
