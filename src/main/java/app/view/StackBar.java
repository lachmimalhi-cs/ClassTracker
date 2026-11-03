package app.view;

import javafx.beans.property.DoubleProperty;
import javafx.beans.property.SimpleDoubleProperty;
import javafx.scene.layout.Region;

/**
 * A thin bar split into Present / Late / Absent parts. Each part is as wide as its share of "total".
 * FXML:  &lt;StackBar present="${controller.present}" late="${controller.late}"
 *                absent="${controller.absent}" total="${controller.total}"/&gt;
 */
public class StackBar extends Region {

    private final DoubleProperty present = new SimpleDoubleProperty();
    private final DoubleProperty late = new SimpleDoubleProperty();
    private final DoubleProperty absent = new SimpleDoubleProperty();
    private final DoubleProperty total = new SimpleDoubleProperty();
    private final Region presentPart = part("seg-P");
    private final Region latePart = part("seg-L");
    private final Region absentPart = part("seg-A");

    public StackBar() {
        getStyleClass().add("stack-bar");
        getChildren().addAll(presentPart, latePart, absentPart);
        present.addListener((obs, o, n) -> requestLayout());
        late.addListener((obs, o, n) -> requestLayout());
        absent.addListener((obs, o, n) -> requestLayout());
        total.addListener((obs, o, n) -> requestLayout());
    }

    public DoubleProperty presentProperty() { return present; }
    public double getPresent() { return present.get(); }
    public void setPresent(double v) { present.set(v); }

    public DoubleProperty lateProperty() { return late; }
    public double getLate() { return late.get(); }
    public void setLate(double v) { late.set(v); }

    public DoubleProperty absentProperty() { return absent; }
    public double getAbsent() { return absent.get(); }
    public void setAbsent(double v) { absent.set(v); }

    public DoubleProperty totalProperty() { return total; }
    public double getTotal() { return total.get(); }
    public void setTotal(double v) { total.set(v); }

    @Override
    protected void layoutChildren() {
        double t = total.get();
        double w = getWidth();
        double h = getHeight();
        double x = 0;
        x += place(presentPart, x, present.get(), t, w, h);
        x += place(latePart, x, late.get(), t, w, h);
        place(absentPart, x, absent.get(), t, w, h);
    }

    private static double place(Region part, double x, double value, double total, double w, double h) {
        double width = total <= 0 ? 0 : w * Math.max(0, value) / total;
        part.setVisible(width > 0);
        part.resizeRelocate(x, 0, width, h);
        return width;
    }

    private static Region part(String styleClass) {
        Region r = new Region();
        r.getStyleClass().add(styleClass);
        return r;
    }
}
