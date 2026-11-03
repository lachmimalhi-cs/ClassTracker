package app.view;

/** Implemented by the controller of a row template (for example student-row.fxml) so a repeater can give it its item. */
public interface ItemView<T> {
    void setItem(T item);
}
