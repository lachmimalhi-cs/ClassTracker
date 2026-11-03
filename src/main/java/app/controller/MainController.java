package app.controller;

import app.state.AppState;
import javafx.beans.property.BooleanProperty;
import javafx.beans.property.SimpleBooleanProperty;
import javafx.beans.property.StringProperty;
import javafx.event.ActionEvent;
import javafx.fxml.FXML;
import javafx.scene.control.DatePicker;

import java.time.LocalDate;

/**
 * Controller of main.fxml: the top bar, the three tab buttons and the three pages.
 * It does not build or change any node. It keeps the selected tab in AppState and exposes
 * "which tab is active" and the theme as properties that main.fxml binds to.
 */
public class MainController {

    private final AppState state = AppState.get();

    @FXML private DatePicker datePicker;

    private final BooleanProperty dashActive = new SimpleBooleanProperty();
    private final BooleanProperty callActive = new SimpleBooleanProperty();
    private final BooleanProperty stuActive = new SimpleBooleanProperty();
    private final BooleanProperty hasMessage = new SimpleBooleanProperty();

    @FXML
    private void initialize() {
        state.tabProperty().addListener((obs, o, n) -> updateTabs());
        state.messageProperty().addListener((obs, o, n) -> hasMessage.set(n != null && !n.isEmpty()));
        updateTabs();
    }

    @FXML private void goDash() { state.showTab("dash"); }

    @FXML private void goCall() { state.showTab("call"); }

    @FXML private void goStu() { state.showTab("stu"); }

    @FXML
    private void onDate(ActionEvent e) {
        LocalDate d = datePicker.getValue();
        if (d != null && !d.equals(state.dateProperty().get())) {
            state.setDate(d);
        }
    }

    @FXML
    private void toggleColours() {
        state.coloursOpenProperty().set(!state.coloursOpenProperty().get());
    }

    @FXML
    private void toggleTheme() {
        if (state.attempt(() -> state.theme().toggleDark())) {
            state.themeChanged();
        }
    }

    private void updateTabs() {
        String t = state.tabProperty().get();
        dashActive.set("dash".equals(t));
        callActive.set("call".equals(t));
        stuActive.set("stu".equals(t));
    }

    public StringProperty themeStyleProperty() { return state.themeStyleProperty(); }
    public String getThemeStyle() { return state.themeStyleProperty().get(); }
    public StringProperty messageProperty() { return state.messageProperty(); }
    public String getMessage() { return state.messageProperty().get(); }
    public BooleanProperty hasMessageProperty() { return hasMessage; }
    public boolean isHasMessage() { return hasMessage.get(); }
    public BooleanProperty dashActiveProperty() { return dashActive; }
    public boolean isDashActive() { return dashActive.get(); }
    public BooleanProperty callActiveProperty() { return callActive; }
    public boolean isCallActive() { return callActive.get(); }
    public BooleanProperty stuActiveProperty() { return stuActive; }
    public boolean isStuActive() { return stuActive.get(); }
}
