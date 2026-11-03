package app.algo;

import java.util.List;

/**
 * Attendance rules with a 75% target. Present and Late both count as present.
 * Everything is done with whole numbers, so there are no rounding errors.
 *
 * With p days present and t days marked:
 *   below the target       when 4p < 3t
 *   classes to attend      x = 3t - 4p      (then (p + x) / (t + x) is at least 3/4)
 *   classes that can be missed   m = floor((4p - 3t) / 3)   (then p / (t + m) is still at least 3/4)
 *
 * Time and space:
 *   percent, belowTarget, classesNeeded, classesCanMiss, advice   O(1) time, O(1) space
 *   streak   O(d) time for d days, O(1) space
 */
public class AttendanceAdvisor {

    public static final int TARGET_PERCENT = 75;

    /** Attendance percentage rounded to a whole number, or null when no day is marked. */
    public Integer percent(int present, int total) {
        check(present, total);
        if (total == 0) {
            return null;
        }
        return (200 * present + total) / (2 * total);   // round half up with whole numbers
    }

    /** True when the exact attendance (not the rounded one) is under 75%. */
    public boolean belowTarget(int present, int total) {
        check(present, total);
        return total > 0 && 4 * present < 3 * total;
    }

    /** How many classes in a row must be attended to reach 75%. It is 0 when the student is already there. */
    public int classesNeeded(int present, int total) {
        check(present, total);
        return Math.max(0, 3 * total - 4 * present);
    }

    /** How many classes can be missed and still stay at 75%. It is 0 when the student is below the target. */
    public int classesCanMiss(int present, int total) {
        check(present, total);
        return Math.max(0, Math.floorDiv(4 * present - 3 * total, 3));
    }

    /** One sentence of advice for the student profile. */
    public String advice(int present, int total) {
        check(present, total);
        if (total == 0) {
            return "No attendance marked yet.";
        }
        if (belowTarget(present, total)) {
            int n = classesNeeded(present, total);
            return "Attend the next " + n + (n == 1 ? " class" : " classes") + " in a row to reach 75%.";
        }
        int m = classesCanMiss(present, total);
        return "Can miss " + m + (m == 1 ? " more class" : " more classes") + " and stay at 75%.";
    }

    /**
     * Current streak: days in a row without an Absent, counted from the newest day backwards.
     * statuses is oldest first and may only contain "P", "A" or "L".
     */
    public int streak(List<String> statuses) {
        if (statuses == null) {
            throw new IllegalArgumentException("The status list must not be null.");
        }
        int streak = 0;
        for (int i = statuses.size() - 1; i >= 0; i--) {
            String s = statuses.get(i);
            if (!"P".equals(s) && !"A".equals(s) && !"L".equals(s)) {
                throw new IllegalArgumentException("Unknown status: " + s);
            }
            if (s.equals("A")) {
                break;
            }
            streak++;
        }
        return streak;
    }

    private static void check(int present, int total) {
        if (present < 0 || total < 0 || present > total) {
            throw new IllegalArgumentException(
                    "Need 0 <= present <= total, but present=" + present + " and total=" + total + ".");
        }
    }
}
