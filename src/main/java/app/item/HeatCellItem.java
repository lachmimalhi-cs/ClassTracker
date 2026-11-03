package app.item;

/** One day of the 28-day heat map. status is P, A, L or null when nothing was marked. */
public record HeatCellItem(String date, String status) { }
