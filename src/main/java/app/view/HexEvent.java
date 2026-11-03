package app.view;

import javafx.event.Event;
import javafx.event.EventType;

/** Fired by ColourField when the user picks a colour. getHex() looks like #RRGGBB. */
public class HexEvent extends Event {

    public static final EventType<HexEvent> PICKED = new EventType<>(Event.ANY, "PICKED");

    private final String hex;

    public HexEvent(Object source, String hex) {
        super(source, Event.NULL_SOURCE_TARGET, PICKED);
        this.hex = hex;
    }

    public String getHex() {
        return hex;
    }
}
