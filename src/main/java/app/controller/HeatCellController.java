package app.controller;

import app.item.HeatCellItem;
import app.state.AppState;
import app.view.ItemView;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Controller of heat-cell.fxml (one day in the 28-day heat map). */
public class HeatCellController implements ItemView<HeatCellItem> {

    private final StringProperty tone = new SimpleStringProperty("N");
    private final StringProperty tip = new SimpleStringProperty("");

    @Override
    public void setItem(HeatCellItem item) {
        tone.set(item.status() == null ? "N" : item.status());
        tip.set(item.status() == null ? item.date() : item.date() + "  -  " + AppState.word(item.status()));
    }

    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
    public StringProperty tipProperty() { return tip; }
    public String getTip() { return tip.get(); }
}
