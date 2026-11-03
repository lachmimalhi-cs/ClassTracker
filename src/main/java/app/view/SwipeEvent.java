package app.view;

import javafx.event.Event;
import javafx.event.EventType;

/** Fired by SwipeCard when the card has flown away. getStatus() is P, A or L. */
public class SwipeEvent extends Event {

    public static final EventType<SwipeEvent> SWIPED = new EventType<>(Event.ANY, "SWIPED");

    private final String status;

    public SwipeEvent(Object source, String status) {
        super(source, Event.NULL_SOURCE_TARGET, SWIPED);
        this.status = status;
    }

    public String getStatus() {
        return status;
    }
}
