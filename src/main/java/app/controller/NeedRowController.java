package app.controller;

import app.item.NeedItem;
import app.view.ItemView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.fxml.FXML;

/** Controller of need-row.fxml (one line of "Needs attention"). */
public class NeedRowController implements ItemView<NeedItem> {

    private final StringProperty name = new SimpleStringProperty("");
    private final StringProperty percentText = new SimpleStringProperty("");
    private final StringProperty tone = new SimpleStringProperty("ok");
    private NeedItem item;

    @Override
    public void setItem(NeedItem item) {
        this.item = item;
        name.set(item.name());
        percentText.set(item.percent() + "%");
        tone.set(item.low() ? "low" : "ok");
    }

    @FXML
    private void select() {
        if (item != null) {
            item.onSelect().run();
        }
    }

    public StringProperty nameProperty() { return name; }
    public String getName() { return name.get(); }
    public StringProperty percentTextProperty() { return percentText; }
    public String getPercentText() { return percentText.get(); }
    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
}
