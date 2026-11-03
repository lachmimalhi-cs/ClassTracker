package app.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

/**
 * Shows P / A / L as a coloured pill ("Present", "Absent", "Late") and null as grey "Not marked".
 * FXML:  &lt;StatusPill status="${controller.status}"/&gt;
 */
public class StatusPill extends Label {

    private final StringProperty status = new SimpleStringProperty();

    public StatusPill() {
        status.addListener((obs, o, n) -> show(n));
        show(null);
    }

    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String s) { status.set(s); }

    private void show(String code) {
        getStyleClass().removeAll("pill", "pill-P", "pill-A", "pill-L", "mute");
        if (code == null) {
            setText("Not marked");
            getStyleClass().add("mute");
            return;
        }
        setText(switch (code) {
            case "P" -> "Present";
            case "A" -> "Absent";
            case "L" -> "Late";
            default -> throw new IllegalArgumentException("Unknown status: " + code);
        });
        getStyleClass().addAll("pill", "pill-" + code);
    }
}
