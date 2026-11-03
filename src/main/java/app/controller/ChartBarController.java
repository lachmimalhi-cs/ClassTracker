package app.controller;

import app.algo.AttendanceAdvisor;
import app.item.ChartBarItem;
import app.view.ItemView;
import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;

/** Controller of chart-bar.fxml. It only turns one ChartBarItem into values; the bar itself is drawn by the FXML. */
public class ChartBarController implements ItemView<ChartBarItem> {

    private static final double PIXELS_PER_PERCENT = 1.4;
    private static final double MIN_HEIGHT = 3;

    private static final AttendanceAdvisor ADVISOR = new AttendanceAdvisor();

    private final DoubleProperty barHeight = new SimpleDoubleProperty(MIN_HEIGHT);
    private final StringProperty percentText = new SimpleStringProperty("");
    private final StringProperty dateText = new SimpleStringProperty("");
    private final StringProperty tone = new SimpleStringProperty("ok");

    @Override
    public void setItem(ChartBarItem item) {
        Integer shown = ADVISOR.percent(item.present(), item.total());
        int percent = shown == null ? 0 : shown;
        boolean low = ADVISOR.belowTarget(item.present(), item.total());
        barHeight.set(Math.max(MIN_HEIGHT, percent * PIXELS_PER_PERCENT));
        percentText.set(percent + "%");
        dateText.set(item.date().length() > 5 ? item.date().substring(5) : item.date());
        tone.set(low ? "low" : "ok");
    }

    public DoubleProperty barHeightProperty() { return barHeight; }
    public double getBarHeight() { return barHeight.get(); }
    public StringProperty percentTextProperty() { return percentText; }
    public String getPercentText() { return percentText.get(); }
    public StringProperty dateTextProperty() { return dateText; }
    public String getDateText() { return dateText.get(); }
    public StringProperty toneProperty() { return tone; }
    public String getTone() { return tone.get(); }
}
