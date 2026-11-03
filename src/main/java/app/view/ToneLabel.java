package app.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.Label;

/**
 * A Label whose colour comes from a "tone" name (for example ok, low, P, A, L, none).
 * Used for coloured numbers, chart bars and heat-map cells. FXML:  &lt;ToneLabel tone="${controller.tone}"/&gt;
 */
public class ToneLabel extends Label {

    private final StringProperty tone = new SimpleStringProperty();

    public ToneLabel() {
        tone.addListener((obs, o, n) -> ToneSupport.change(this, o, n));
    }

    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
    public void setTone(String t) { tone.set(t); }
}
