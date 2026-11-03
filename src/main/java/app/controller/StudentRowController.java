package app.controller;

import app.item.StudentRowItem;
import app.view.ItemView;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;
import javafx.scene.control.TextField;

/**
 * Controller of student-row.fxml. A row is in one of three modes: viewing (Edit / Delete buttons),
 * editing (a text box with Save / Cancel) or confirming (the delete question). The FXML shows or hides
 * each part by binding to the three boolean properties.
 */
public class StudentRowController implements ItemView<StudentRowItem> {

    private final StringProperty roll = new SimpleStringProperty("");
    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty status = new SimpleStringProperty();
    private final DoubleProperty progress = new SimpleDoubleProperty();
    private final StringProperty progressText = new SimpleStringProperty("");
    private final StringProperty tone = new SimpleStringProperty("ok");
    private final StringProperty editSeed = new SimpleStringProperty("");
    private final StringProperty confirmText = new SimpleStringProperty("");
    private final BooleanProperty viewing = new SimpleBooleanProperty(true);
    private final BooleanProperty editing = new SimpleBooleanProperty(false);
    private final BooleanProperty confirming = new SimpleBooleanProperty(false);

    @FXML private TextField editField;

    private StudentRowItem item;

    @Override
    public void setItem(StudentRowItem item) {
        this.item = item;
        roll.set(item.roll());
        name.set(item.name());
        status.set(item.status());
        progress.set(item.percent() == null ? 0 : item.percent() / 100.0);
        progressText.set(item.percent() == null ? "No data"
                : item.percent() + "%  (" + item.present() + " of " + item.total() + " days)");
        tone.set(item.low() ? "low" : "ok");
        confirmText.set("Delete " + item.name() + " and all of their attendance?");
        mode(true, false, false);
    }

    @FXML
    private void select() {
        if (item != null) {
            item.onSelect().run();
        }
    }

    @FXML
    private void startEdit() {
        reseed(name.get());
        mode(false, true, false);
    }

    @FXML
    private void saveEdit() {
        if (item != null && item.onRename().test(editField.getText())) {
            mode(true, false, false);
        }
    }

    @FXML
    private void cancel() {
        reseed(null);
        mode(true, false, false);
    }

    @FXML
    private void askDelete() {
        mode(false, false, true);
    }

    @FXML
    private void confirmDelete() {
        if (item != null) {
            item.onDelete().run();
        }
    }

    private void mode(boolean view, boolean edit, boolean confirm) {
        viewing.set(view);
        editing.set(edit);
        confirming.set(confirm);
    }

    /** Sets the seed to null first, so the same name is shown again when Edit is pressed twice. */
    private void reseed(String text) {
        editSeed.set(null);
        editSeed.set(text == null ? "" : text);
    }

    public StringProperty rollProperty() { return roll; }
    public String getRoll() { return roll.get(); }
    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public DoubleProperty progressProperty() { return progress; }
    public double getProgress() { return progress.get(); }
    public StringProperty progressTextProperty() { return progressText; }
    public String getProgressText() { return progressText.get(); }
    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
    public StringProperty editSeedProperty() { return editSeed; }
    public String getEditSeed() { return editSeed.get(); }
    public StringProperty confirmTextProperty() { return confirmText; }
    public String getConfirmText() { return confirmText.get(); }
    public BooleanProperty viewingProperty() { return viewing; }
    public boolean isViewing() { return viewing.get(); }
    public BooleanProperty editingProperty() { return editing; }
    public boolean isEditing() { return editing.get(); }
    public BooleanProperty confirmingProperty() { return confirming; }
    public boolean isConfirming() { return confirming.get(); }
}
