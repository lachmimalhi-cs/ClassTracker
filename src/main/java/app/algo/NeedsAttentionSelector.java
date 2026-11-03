package app.algo;

import app.ds.MinHeap;
import app.model.Student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Comparator;
import java.util.List;
import java.util.function.Function;

/**
 * Picks the students with the lowest attendance percentage ("Needs attention" list).
 * Every student that has a percentage goes into a MinHeap; polling the heap k times gives the
 * k lowest. Ties are broken by roll number so the answer is always the same.
 *
 * Time and space (n = students, k = how many are wanted):
 *   select  O(n log n + k log n) time, O(n) space for the heap
 */
public class NeedsAttentionSelector {

    private record Entry(Student student, int percent) { }

    private final int count;

    /** count = how many students to return. It must be at least 1. */
    public NeedsAttentionSelector(int count) {
        if (count < 1) {
            throw new IllegalArgumentException("count must be at least 1.");
        }
        this.count = count;
    }

    /**
     * Returns up to count students, lowest percentage first.
     * percent gives the percentage of a student, or null when nothing is marked yet (that student is skipped).
     */
    public List<Student> select(Collection<Student> students, Function<Student, Integer> percent) {
        if (students == null || percent == null) {
            throw new IllegalArgumentException("students and percent must not be null.");
        }
        MinHeap<Entry> heap = new MinHeap<>(
                Comparator.comparingInt(Entry::percent).thenComparing(e -> e.student().roll()));
        for (Student s : students) {
            if (s == null) {
                throw new IllegalArgumentException("The student list must not contain null.");
            }
            Integer p = percent.apply(s);
            if (p != null) {
                heap.add(new Entry(s, p));
            }
        }
        List<Student> result = new ArrayList<>();
        while (result.size() < count && !heap.isEmpty()) {
            result.add(heap.poll().student());
        }
        return result;
    }
}
