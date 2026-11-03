package app.item;

/** One bar of the "Last 7 days" chart: the date and how many students were present (or late) of those marked. */
public record ChartBarItem(String date, int present, int total) { }
