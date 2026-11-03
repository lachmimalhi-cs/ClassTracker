package app.view;

import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.event.ActionEvent;
import javafx.scene.control.ToggleButton;

/**
 * A tab button. It looks selected when "active" is true. Clicking it only fires onAction; the
 * controller decides which tab is active, so the button never changes itself.
 * FXML:  &lt;TabButton text="Roll Call" active="${controller.callActive}" onAction="#goCall"/&gt;
 */
public class TabButton extends ToggleButton {

    private final BooleanProperty active = new SimpleBooleanProperty();

    public TabButton() {
        getStyleClass().add("nav");
        active.addListener((obs, o, n) -> setSelected(n));
    }

    public BooleanProperty activeProperty() { return active; }
    public boolean isActive() { return active.get(); }
    public void setActive(boolean v) { active.set(v); }

    @Override
    public void fire() {
        if (!isDisabled()) {
            fireEvent(new ActionEvent());
        }
    }
}
