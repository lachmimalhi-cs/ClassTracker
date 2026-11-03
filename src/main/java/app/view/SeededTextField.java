package app.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.TextField;

/**
 * A TextField that can be filled from the controller with the "seed" property, while the user can still type.
 * The controller sets the seed to null and then to the new text, so even the same text is shown again.
 * onTextChanged fires every time the text changes (typing or seed).
 * FXML:  &lt;SeededTextField seed="${controller.nameSeed}" onAction="#add" onTextChanged="#onSearch"/&gt;
 */
public class SeededTextField extends TextField {

    private final StringProperty seed = new SimpleStringProperty();
    private final ObjectProperty<EventHandler<TextChangedEvent>> onTextChanged = new SimpleObjectProperty<>();

    public SeededTextField() {
        seed.addListener((obs, o, n) -> {
            setText(n == null ? "" : n);
            positionCaret(getLength());
        });
        textProperty().addListener((obs, o, n) -> {
            EventHandler<TextChangedEvent> handler = onTextChanged.get();
            if (handler != null) {
                handler.handle(new TextChangedEvent(this, n == null ? "" : n));
            }
        });
    }

    public StringProperty seedProperty() { return seed; }
    public String getSeed() { return seed.get(); }
    public void setSeed(String s) { seed.set(s); }

    public ObjectProperty<EventHandler<TextChangedEvent>> onTextChangedProperty() { return onTextChanged; }
    public EventHandler<TextChangedEvent> getOnTextChanged() { return onTextChanged.get(); }
    public void setOnTextChanged(EventHandler<TextChangedEvent> h) { onTextChanged.set(h); }
}
