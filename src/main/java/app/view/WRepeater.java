package app.view;

import javafx.beans.property.ObjectProperty;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.collections.ListChangeListener;
import javafx.collections.ObservableList;
import javafx.scene.layout.FlowPane;

/**
 * A wrapping grid that shows one FXML template per item.
 * In FXML:  &lt;WRepeater items="${controller.rows}" template="/fxml/some-row.fxml"/&gt;
 * The list can change at any time; the content is rebuilt when it does.
 */
@SuppressWarnings("unchecked")
public class WRepeater extends FlowPane {

    private final ObjectProperty<ObservableList<?>> items = new SimpleObjectProperty<>();
    private final StringProperty template = new SimpleStringProperty();
    private final ListChangeListener<Object> onChange = c -> rebuild();

    public WRepeater() {
        items.addListener((obs, oldList, newList) -> {
            if (oldList != null) {
                ((ObservableList<Object>) oldList).removeListener(onChange);
            }
            if (newList != null) {
                ((ObservableList<Object>) newList).addListener(onChange);
            }
            rebuild();
        });
        template.addListener((obs, o, n) -> rebuild());
    }

    public ObjectProperty<ObservableList<?>> itemsProperty() { return items; }
    public ObservableList<?> getItems() { return items.get(); }
    public void setItems(ObservableList<?> list) { items.set(list); }

    public StringProperty templateProperty() { return template; }
    public String getTemplate() { return template.get(); }
    public void setTemplate(String t) { template.set(t); }

    private void rebuild() {
        TemplateFiller.fill(this, items.get(), template.get());
    }
}
