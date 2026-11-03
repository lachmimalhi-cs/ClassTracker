package app.item;

import java.util.function.Predicate;

/**
 * One row of the student list. The three actions are given by the screen controller, so the row
 * controller does not need to know about the database.
 * onRename returns true when the new name was accepted.
 */
public record StudentRowItem(String roll, String name, String status, Integer percent, int present, int total,
                             boolean low, Runnable onSelect, Predicate<String> onRename, Runnable onDelete) { }
