package app.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.event.EventHandler;
import javafx.scene.Scene;
import javafx.scene.control.ComboBoxBase;
import javafx.scene.control.TextInputControl;
import javafx.scene.input.KeyCode;
import javafx.scene.input.KeyEvent;
import javafx.scene.layout.Region;

/**
 * An invisible helper for the arrow keys. While "enabled" is true it passes RIGHT, LEFT and DOWN key
 * presses to onShortcut, unless the user is typing in a text field or date picker.
 * FXML:  &lt;KeyShortcuts enabled="${controller.active}" onShortcut="#onShortcut"/&gt;
 */
public class KeyShortcuts extends Region {

    private final BooleanProperty enabled = new SimpleBooleanProperty();
    private final ObjectProperty<EventHandler<KeyEvent>> onShortcut = new SimpleObjectProperty<>();
    private final EventHandler<KeyEvent> filter = this::onKey;

    public KeyShortcuts() {
        setManaged(false);
        setMouseTransparent(true);
        setPrefSize(0, 0);
        sceneProperty().addListener((obs, oldScene, newScene) -> {
            if (oldScene != null) {
                oldScene.removeEventFilter(KeyEvent.KEY_PRESSED, filter);
            }
            if (newScene != null) {
                newScene.addEventFilter(KeyEvent.KEY_PRESSED, filter);
            }
        });
    }

    public BooleanProperty enabledProperty() { return enabled; }
    public boolean isEnabled() { return enabled.get(); }
    public void setEnabled(boolean v) { enabled.set(v); }

    public ObjectProperty<EventHandler<KeyEvent>> onShortcutProperty() { return onShortcut; }
    public EventHandler<KeyEvent> getOnShortcut() { return onShortcut.get(); }
    public void setOnShortcut(EventHandler<KeyEvent> h) { onShortcut.set(h); }

    private void onKey(KeyEvent ev) {
        EventHandler<KeyEvent> handler = onShortcut.get();
        Scene scene = getScene();
        if (!enabled.get() || handler == null || scene == null) {
            return;
        }
        Object focus = scene.getFocusOwner();
        if (focus instanceof TextInputControl || focus instanceof ComboBoxBase) {
            return;
        }
        KeyCode c = ev.getCode();
        if (c == KeyCode.RIGHT || c == KeyCode.LEFT || c == KeyCode.DOWN) {
            ev.consume();
            handler.handle(ev);
        }
    }
}
