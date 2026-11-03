package app.item;

/** One line of the "Needs attention" list. onSelect opens the student's profile. */
public record NeedItem(String name, int percent, boolean low, Runnable onSelect) { }
