package app.algo;

import app.ds.Trie;
import app.model.Student;

import java.util.ArrayList;
import java.util.Collection;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Optional;
import java.util.Set;

/**
 * MAIN ALGORITHM of ClassTrack: search students by the beginning of any word of the name.
 *
 * It builds a Trie of every word of every name once (the "index"). A search then follows one
 * child per typed letter, so the time depends on the typed text and on the matches,
 * not on the number of students that do not match.
 *
 * Time and space (n = students, W = total letters of all names, P = length of the text,
 * M = nodes below the prefix, R = number of results):
 *   constructor (build the index)  O(W) time, O(W) space
 *   search with one word           O(P + M + R log R) time (the R log R part keeps the class order)
 *   search with a space            O(n * L) time (plain contains on each name, L = name length)
 *   byRoll                         O(1) (HashMap lookup)
 *   size                           O(1)
 */
public class StudentSearch {

    private final List<Student> students;
    private final Map<String, Integer> position = new HashMap<>();   // roll -> place in the class list
    private final Trie<String> index = new Trie<>();                 // word -> roll numbers

    /** Builds the index. Throws IllegalArgumentException for a null list, a null student or a repeated roll number. */
    public StudentSearch(Collection<Student> source) {
        if (source == null) {
            throw new IllegalArgumentException("The student list must not be null.");
        }
        List<Student> copy = new ArrayList<>(source.size());
        for (Student s : source) {
            if (s == null) {
                throw new IllegalArgumentException("The student list must not contain null.");
            }
            if (position.putIfAbsent(s.roll(), copy.size()) != null) {
                throw new IllegalArgumentException("Repeated roll number: " + s.roll());
            }
            copy.add(s);
            for (String word : s.name().split("\\s+")) {
                index.insert(word, s.roll());
            }
        }
        this.students = Collections.unmodifiableList(copy);
    }

    /**
     * Returns the matching students in class order.
     * Empty text returns everyone. Text with a space is searched with contains(), because a
     * name with a space is not one Trie word. Anything else is a prefix search in the Trie.
     */
    public List<Student> search(String query) {
        if (query == null) {
            throw new IllegalArgumentException("The search text must not be null.");
        }
        String text = query.trim();
        if (text.isEmpty()) {
            return students;
        }
        if (text.indexOf(' ') >= 0) {
            String low = text.toLowerCase(Locale.ROOT);
            List<Student> found = new ArrayList<>();
            for (Student s : students) {
                if (s.name().toLowerCase(Locale.ROOT).contains(low)) {
                    found.add(s);
                }
            }
            return found;
        }
        Set<String> rolls = index.startsWith(text);
        List<Student> found = new ArrayList<>(rolls.size());
        for (String roll : rolls) {
            found.add(students.get(position.get(roll)));
        }
        found.sort(Comparator.comparingInt(s -> position.get(s.roll())));
        return found;
    }

    /** Finds one student by roll number in O(1). */
    public Optional<Student> byRoll(String roll) {
        Integer i = roll == null ? null : position.get(roll.trim());
        return i == null ? Optional.empty() : Optional.of(students.get(i));
    }

    public int size() {
        return students.size();
    }
}
