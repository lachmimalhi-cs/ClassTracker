package app.view;

import javafx.animation.FadeTransition;
import javafx.animation.ParallelTransition;
import javafx.animation.TranslateTransition;
import javafx.beans.property.ObjectProperty;
import javafx.beans.property.ReadOnlyDoubleProperty;
import javafx.beans.property.ReadOnlyDoubleWrapper;
import javafx.beans.property.ReadOnlyStringProperty;
import javafx.beans.property.ReadOnlyStringWrapper;
import javafx.beans.property.SimpleObjectProperty;
import javafx.beans.property.SimpleStringProperty;
import javafx.beans.property.StringProperty;
import javafx.event.EventHandler;
import javafx.scene.layout.StackPane;
import javafx.util.Duration;

/**
 * The roll-call card. The user can drag it (right = Present, left = Absent, down = Late), or the
 * controller can ask it to fly with the "request" property. When the card has flown away it fires
 * a SwipeEvent and puts itself back, so the controller only has to save the status.
 *
 * Read-only properties for the overlay that FXML draws on top of the card:
 *   hint (text such as PRESENT), hintTone (P, A or L) and hintOpacity (0 to 1).
 *
 * FXML:  &lt;SwipeCard fx:id="card" status="${controller.status}" request="${controller.flyRequest}"
 *                  onSwiped="#onSwiped"&gt; ...content... &lt;/SwipeCard&gt;
 */
public class SwipeCard extends StackPane {

    private static final double THRESHOLD = 90;
    private static final double FLY_X = 700;
    private static final double FLY_Y = 500;

    private final StringProperty status = new SimpleStringProperty();
    private final ObjectProperty<FlyRequest> request = new SimpleObjectProperty<>();
    private final ObjectProperty<EventHandler<SwipeEvent>> onSwiped = new SimpleObjectProperty<>();
    private final ReadOnlyStringWrapper hint = new ReadOnlyStringWrapper("");
    private final ReadOnlyStringWrapper hintTone = new ReadOnlyStringWrapper();
    private final ReadOnlyDoubleWrapper hintOpacity = new ReadOnlyDoubleWrapper(0);

    private double startX;
    private double startY;
    private String dragStatus;
    private boolean flying;

    public SwipeCard() {
        getStyleClass().add("swipe-card");
        status.addListener((obs, o, n) -> {
            getStyleClass().removeAll("card-P", "card-A", "card-L");
            if (n != null) {
                getStyleClass().add("card-" + n);
            }
        });
        request.addListener((obs, o, n) -> {
            if (n != null) {
                fly(n.getStatus());
            }
        });
        setOnMousePressed(e -> {
            startX = e.getSceneX();
            startY = e.getSceneY();
            dragStatus = null;
        });
        setOnMouseDragged(e -> {
            if (flying) {
                return;
            }
            double dx = e.getSceneX() - startX;
            double dy = Math.max(0, e.getSceneY() - startY);
            boolean sideways = Math.abs(dx) > dy;
            setTranslateX(dx);
            setTranslateY(sideways ? 0 : dy);
            String k = sideways ? (dx > 0 ? "P" : "A") : "L";
            double moved = Math.max(Math.abs(dx), dy);
            dragStatus = moved > THRESHOLD ? k : null;
            showHint(k, Math.min(1, moved / 110));
        });
        setOnMouseReleased(e -> {
            if (flying) {
                return;
            }
            if (dragStatus != null) {
                fly(dragStatus);
            } else {
                TranslateTransition back = new TranslateTransition(Duration.millis(180), this);
                back.setToX(0);
                back.setToY(0);
                back.play();
                hintOpacity.set(0);
            }
        });
    }

    // ---------- properties ----------

    public StringProperty statusProperty() { return status; }
    public String getStatus() { return status.get(); }
    public void setStatus(String s) { status.set(s); }

    public ObjectProperty<FlyRequest> requestProperty() { return request; }
    public FlyRequest getRequest() { return request.get(); }
    public void setRequest(FlyRequest r) { request.set(r); }

    public ObjectProperty<EventHandler<SwipeEvent>> onSwipedProperty() { return onSwiped; }
    public EventHandler<SwipeEvent> getOnSwiped() { return onSwiped.get(); }
    public void setOnSwiped(EventHandler<SwipeEvent> h) { onSwiped.set(h); }

    public ReadOnlyStringProperty hintProperty() { return hint.getReadOnlyProperty(); }
    public String getHint() { return hint.get(); }

    public ReadOnlyStringProperty hintToneProperty() { return hintTone.getReadOnlyProperty(); }
    public String getHintTone() { return hintTone.get(); }

    public ReadOnlyDoubleProperty hintOpacityProperty() { return hintOpacity.getReadOnlyProperty(); }
    public double getHintOpacity() { return hintOpacity.get(); }

    // ---------- animation ----------

    /** The card flies away, the SwipeEvent is fired, and the card goes back to the middle. */
    private void fly(String k) {
        if (flying) {
            return;
        }
        flying = true;
        showHint(k, 1);
        TranslateTransition move = new TranslateTransition(Duration.millis(250), this);
        move.setToX(k.equals("P") ? FLY_X : k.equals("A") ? -FLY_X : 0);
        move.setToY(k.equals("L") ? FLY_Y : 0);
        FadeTransition fade = new FadeTransition(Duration.millis(250), this);
        fade.setToValue(0);
        ParallelTransition both = new ParallelTransition(move, fade);
        both.setOnFinished(e -> {
            try {
                EventHandler<SwipeEvent> handler = onSwiped.get();
                if (handler != null) {
                    handler.handle(new SwipeEvent(this, k));
                }
            } finally {
                setTranslateX(0);
                setTranslateY(0);
                setOpacity(1);
                hintOpacity.set(0);
                flying = false;
            }
        });
        both.play();
    }

    private void showHint(String k, double opacity) {
        hint.set(switch (k) {
            case "P" -> "PRESENT";
            case "A" -> "ABSENT";
            default -> "LATE";
        });
        hintTone.set(k);
        hintOpacity.set(opacity);
    }
}
