package app.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.control.ColorPicker;
import javafx.scene.paint.Color;

/**
 * A ColorPicker that works with #RRGGBB text. The "hex" property shows a colour (set from the controller);
 * onPicked fires only when the USER picks a colour, never when "hex" changes by itself.
 * FXML:  &lt;ColourField hex="${controller.accHex}" onPicked="#pickAcc"/&gt;
 */
public class ColourField extends ColorPicker {

    private final StringProperty hex = new SimpleStringProperty();
    private final ObjectProperty<EventHandler<HexEvent>> onPicked = new SimpleObjectProperty<>();
    private boolean showing;

    public ColourField() {
        hex.addListener((obs, o, n) -> {
            if (n == null) {
                return;
            }
            showing = true;
            try {
                setValue(Color.web(n));
            } finally {
                showing = false;
            }
        });
        valueProperty().addListener((obs, o, n) -> {
            EventHandler<HexEvent> handler = onPicked.get();
            if (!showing && n != null && handler != null) {
                handler.handle(new HexEvent(this, toHex(n)));
            }
        });
    }

    public StringProperty hexProperty() { return hex; }
    public String getHex() { return hex.get(); }
    public void setHex(String h) { hex.set(h); }

    public ObjectProperty<EventHandler<HexEvent>> onPickedProperty() { return onPicked; }
    public EventHandler<HexEvent> getOnPicked() { return onPicked.get(); }
    public void setOnPicked(EventHandler<HexEvent> h) { onPicked.set(h); }

    private static String toHex(Color c) {
        return String.format("#%02X%02X%02X", Math.round(c.getRed() * 255), Math.round(c.getGreen() * 255),
                Math.round(c.getBlue() * 255));
    }
}
