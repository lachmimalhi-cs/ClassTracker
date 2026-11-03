package app.controller;

import app.model.Student;
import app.state.AppState;
import app.view.FlyRequest;
import app.view.SwipeEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.input.KeyEvent;

import java.util.List;

/**
 * Controller of rollcall.fxml. It keeps the number of the current card, saves the status when a card
 * has been swiped, and asks the swipe card to fly through the flyRequest property. The card, its animation
 * and the arrow keys are in app.view and rollcall.fxml.
 */
public class RollCallController {

    private final AppState state = AppState.get();

    private final BooleanProperty empty = new SimpleBooleanProperty(true);
    private final BooleanProperty filled = new SimpleBooleanProperty(false);
    private final BooleanProperty active = new SimpleBooleanProperty(false);
    private final StringProperty infoText = new SimpleStringProperty("");
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty rollNo = new SimpleStringProperty("");
    private final StringProperty rollName = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<FlyRequest> flyRequest = new SimpleObjectProperty<>();

    private int idx;

    @FXML
    private void initialize() {
        state.versionProperty().addListener((obs, o, n) -> update());
        state.dateProperty().addListener((obs, o, n) -> idx = 0);
        update();
    }

    // ---------- buttons, keys and swipes ----------

    @FXML private void markPresent() { request("P"); }

    @FXML private void markAbsent() { request("A"); }

    @FXML private void markLate() { request("L"); }

    @FXML
    private void onShortcut(KeyEvent e) {
        switch (e.getCode()) {
            case RIGHT -> request("P");
            case LEFT -> request("A");
            case DOWN -> request("L");
            default -> { }
        }
    }

    /** The swipe card has flown away: save the status and show the next student. */
    @FXML
    private void onSwiped(SwipeEvent e) {
        List<Student> students = state.students();
        if (students.isEmpty()) {
            return;
        }
        Student s = students.get(idx);
        if (state.attempt(() -> state.mark(s.roll(), e.getStatus()))) {
            idx++;
            update();
        }
    }

    @FXML
    private void rollBack() {
        idx--;
        update();
    }

    @FXML
    private void rollClear() {
        List<Student> students = state.students();
        if (!students.isEmpty()) {
            state.attempt(() -> state.clearMark(students.get(idx).roll()));
        }
    }

    @FXML
    private void restPresent() {
        state.attempt(state::markRestPresent);
    }

    private void request(String code) {
        if (!state.students().isEmpty()) {
            flyRequest.set(new FlyRequest(code));
        }
    }

    private void update() {
        List<Student> students = state.students();
        boolean none = students.isEmpty();
        empty.set(none);
        filled.set(!none);
        active.set("call".equals(state.tabProperty().get()) && !none);
        if (none) {
            return;
        }
        if (idx >= students.size()) {
            idx = 0;
        }
        if (idx < 0) {
            idx = students.size() - 1;
        }
        Student s = students.get(idx);
        long done = students.stream().filter(x -> state.isMarked(x.roll())).count();
        infoText.set("Card " + (idx + 1) + " of " + students.size() + "   |   " + done + " marked");
        progress.set(done / (double) students.size());
        rollNo.set("Roll " + s.roll());
        rollName.set(s.name());
        status.set(state.statusOf(s.roll()));
    }

    public BooleanProperty emptyProperty() { return empty; }
    public boolean isEmpty() { return empty.get(); }
    public BooleanProperty filledProperty() { return filled; }
    public boolean isFilled() { return filled.get(); }
    public BooleanProperty activeProperty() { return active; }
    public boolean isActive() { return active.get(); }
    public StringProperty infoTextProperty() { return infoText; }
    public String getInfoText() { return infoText.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public double getProgress() { return progress.get(); }
    public StringProperty rollNoProperty() { return rollNo; }
    public String getRollNo() { return rollNo.get(); }
    public StringProperty rollNameProperty() { return rollName; }
    public String getRollName() { return rollName.get(); }
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public ObjectProperty<FlyRequest> flyRequestProperty() { return flyRequest; }
    public FlyRequest getFlyRequest() { return flyRequest.get(); }
}
