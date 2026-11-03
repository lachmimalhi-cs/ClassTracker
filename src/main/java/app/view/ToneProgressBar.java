package app.view;

import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.scene.control.ProgressBar;

/** A ProgressBar whose bar colour comes from a tone name (ok or low). */
public class ToneProgressBar extends ProgressBar {

    private final StringProperty tone = new SimpleStringProperty();

    public ToneProgressBar() {
        tone.addListener((obs, o, n) -> ToneSupport.change(this, o, n));
    }

    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
    public void setTone(String t) { tone.set(t); }
}
