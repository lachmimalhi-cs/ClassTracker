package app.controller;

import app.state.AppState;
import app.state.ThemeService;
import app.view.HexEvent;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.Node;

/**
 * Controller of colours.fxml (the Colours panel). The panel is shown when "open" is true.
 * The preset buttons carry their colour in userData, which is written in the FXML.
 */
public class ColoursController {

    private final AppState state = AppState.get();
    private final ThemeService theme = state.theme();

    private final StringProperty accHex = new SimpleStringProperty();
    private final StringProperty okHex = new SimpleStringProperty();
    private final StringProperty ambHex = new SimpleStringProperty();
    private final StringProperty redHex = new SimpleStringProperty();

    @FXML
    private void initialize() {
        state.themeStyleProperty().addListener((obs, o, n) -> sync());
        sync();
    }

    @FXML
    private void preset(ActionEvent e) {
        Object hex = ((Node) e.getSource()).getUserData();
        if (hex != null) {
            changeTheme(() -> theme.applyPreset(hex.toString()));
        }
    }

    @FXML private void pickAcc(HexEvent e) { changeTheme(() -> theme.setColour("acc", e.getHex())); }

    @FXML private void pickOk(HexEvent e) { changeTheme(() -> theme.setColour("ok", e.getHex())); }

    @FXML private void pickAmb(HexEvent e) { changeTheme(() -> theme.setColour("amb", e.getHex())); }

    @FXML private void pickRed(HexEvent e) { changeTheme(() -> theme.setColour("red", e.getHex())); }

    @FXML
    private void resetColours() {
        changeTheme(theme::reset);
    }

    private void changeTheme(Runnable change) {
        if (state.attempt(change)) {
            state.themeChanged();
        }
    }

    private void sync() {
        accHex.set(theme.colour("acc"));
        okHex.set(theme.colour("ok"));
        ambHex.set(theme.colour("amb"));
        redHex.set(theme.colour("red"));
    }

    public BooleanProperty openProperty() { return state.coloursOpenProperty(); }
    public boolean isOpen() { return state.coloursOpenProperty().get(); }
    public StringProperty accHexProperty() { return accHex; }
    public String getAccHex() { return accHex.get(); }
    public StringProperty okHexProperty() { return okHex; }
    public String getOkHex() { return okHex.get(); }
    public StringProperty ambHexProperty() { return ambHex; }
    public String getAmbHex() { return ambHex.get(); }
    public StringProperty redHexProperty() { return redHex; }
    public String getRedHex() { return redHex.get(); }
}
