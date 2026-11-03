package app.view;

import javafx.event.Event;
import javafx.event.EventType;

/** Fired by SeededTextField every time its text changes. getText() is the new text. */
public class TextChangedEvent extends Event {

    public static final EventType<TextChangedEvent> CHANGED = new EventType<>(Event.ANY, "TEXT_CHANGED");

    private final String text;

    public TextChangedEvent(Object source, String text) {
        super(source, Event.NULL_SOURCE_TARGET, CHANGED);
        this.text = text;
    }

    public String getText() {
        return text;
    }
}
