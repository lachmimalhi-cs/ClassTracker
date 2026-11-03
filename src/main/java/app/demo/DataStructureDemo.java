package app.demo;

import app.algo.AttendanceAdvisor;
import app.algo.NeedsAttentionSelector;
import app.algo.StudentSearch;
import app.ds.MinHeap;
import app.ds.Trie;
import app.model.Student;

import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Shows every operation of the data structures and algorithm classes in one place.
 * It does not use JavaFX or the database, so it can be run from the command line:
 *   java -cp out app.demo.DataStructureDemo
 */
public class DataStructureDemo {

    public static void main(String[] args) {
        trieDemo();
        heapDemo();
        searchDemo();
        adviceDemo();
        errorDemo();
    }

    private static void trieDemo() {
        System.out.println("=== Trie<String> ===");
        Trie<String> trie = new Trie<>();
        trie.insert("Shelli", "4420250973");
        trie.insert("Sharmila", "4420250978");
        trie.insert("Bai", "4420250980");
        trie.insert("Lachmi", "4420250980");
        System.out.println("size() after 4 inserts        = " + trie.size());
        System.out.println("startsWith(\"sh\")              = " + trie.startsWith("sh"));
        System.out.println("startsWith(\"SH\") (upper case) = " + trie.startsWith("SH"));
        System.out.println("startsWith(\"xyz\")             = " + trie.startsWith("xyz"));
        System.out.println("startsWith(\"\") (everything)   = " + trie.startsWith(""));
        System.out.println("containsWord(\"bai\")           = " + trie.containsWord("bai"));
        System.out.println("containsWord(\"ba\")            = " + trie.containsWord("ba"));
        System.out.println("remove(\"Bai\", roll)           = " + trie.remove("Bai", "4420250980"));
        System.out.println("containsWord(\"bai\") after     = " + trie.containsWord("bai"));
        trie.clear();
        System.out.println("isEmpty() after clear()       = " + trie.isEmpty());
        System.out.println();
    }

    private static void heapDemo() {
        System.out.println("=== MinHeap<Integer> ===");
        MinHeap<Integer> heap = new MinHeap<>(Comparator.naturalOrder());
        for (int v : new int[]{90, 50, 100, 60, 75}) {
            heap.add(v);
        }
        System.out.println("size() after 5 adds   = " + heap.size());
        System.out.println("peek() (smallest)     = " + heap.peek());
        System.out.println("toSortedList()        = " + heap.toSortedList() + "  (heap not changed, size " + heap.size() + ")");
        System.out.println("poll() three times    = " + heap.poll() + ", " + heap.poll() + ", " + heap.poll());
        System.out.println("isEmpty()             = " + heap.isEmpty() + ", size() = " + heap.size());
        heap.clear();
        System.out.println("isEmpty() after clear = " + heap.isEmpty());
        System.out.println();
    }

    private static List<Student> sampleClass() {
        return List.of(
                new Student("4420250973", "Shelli"),
                new Student("4420250978", "Sharmila"),
                new Student("4420250980", "Bai Lachmi"),
                new Student("4420250981", "Memoona"),
                new Student("4420250984", "Mariyam"));
    }

    private static void searchDemo() {
        System.out.println("=== StudentSearch (main algorithm) and NeedsAttentionSelector ===");
        StudentSearch search = new StudentSearch(sampleClass());
        System.out.println("search(\"sh\")        = " + names(search.search("sh")));
        System.out.println("search(\"lach\")      = " + names(search.search("lach")));
        System.out.println("search(\"bai lac\")   = " + names(search.search("bai lac")) + "  (text with a space)");
        System.out.println("search(\"\")          = " + names(search.search("")));
        System.out.println("byRoll(\"4420250981\") = " + search.byRoll("4420250981").map(Student::name).orElse("none"));
        System.out.println("byRoll(\"1\")          = " + search.byRoll("1").map(Student::name).orElse("none"));

        AttendanceAdvisor advisor = new AttendanceAdvisor();
        int[][] days = {{2, 2}, {1, 2}, {2, 2}, {1, 2}, {0, 2}};   // present, total for the 5 students
        NeedsAttentionSelector selector = new NeedsAttentionSelector(3);
        List<Student> low = selector.select(sampleClass(), s -> {
            int i = sampleClass().indexOf(s);
            return advisor.percent(days[i][0], days[i][1]);
        });
        System.out.println("Needs attention (lowest 3) = " + names(low));
        System.out.println();
    }

    private static void adviceDemo() {
        System.out.println("=== AttendanceAdvisor ===");
        AttendanceAdvisor a = new AttendanceAdvisor();
        System.out.println("percent(9, 10)         = " + a.percent(9, 10));
        System.out.println("percent(0, 0)          = " + a.percent(0, 0) + "  (nothing marked)");
        System.out.println("belowTarget(56, 75)    = " + a.belowTarget(56, 75) + "  (74.67%, shown as 75%)");
        System.out.println("classesNeeded(6, 10)   = " + a.classesNeeded(6, 10));
        System.out.println("classesCanMiss(9, 10)  = " + a.classesCanMiss(9, 10));
        System.out.println("advice(6, 10)          = " + a.advice(6, 10));
        System.out.println("advice(9, 10)          = " + a.advice(9, 10));
        System.out.println("advice(0, 0)           = " + a.advice(0, 0));
        System.out.println("streak(P,A,P,L,P)      = " + a.streak(List.of("P", "A", "P", "L", "P")));
        System.out.println();
    }

    /** Every class rejects bad input with a clear message instead of failing later. */
    private static void errorDemo() {
        System.out.println("=== Error handling ===");
        tryIt("Trie.insert(null, ...)", () -> new Trie<String>().insert(null, "1"));
        tryIt("Trie.startsWith(null)", () -> new Trie<String>().startsWith(null));
        tryIt("MinHeap.poll() on empty heap", () -> new MinHeap<Integer>(Comparator.naturalOrder()).poll());
        tryIt("MinHeap.add(null)", () -> new MinHeap<Integer>(Comparator.naturalOrder()).add(null));
        tryIt("new Student(\"\", \"Ali\")", () -> new Student("", "Ali"));
        tryIt("search(null)", () -> new StudentSearch(sampleClass()).search(null));
        tryIt("repeated roll number", () -> new StudentSearch(List.of(new Student("1", "A"), new Student("1", "B"))));
        tryIt("advice(11, 10)", () -> new AttendanceAdvisor().advice(11, 10));
        tryIt("streak with status \"X\"", () -> new AttendanceAdvisor().streak(List.of("P", "X")));
        tryIt("new NeedsAttentionSelector(0)", () -> new NeedsAttentionSelector(0));
    }

    private static void tryIt(String what, Runnable action) {
        try {
            action.run();
            System.out.println(what + " -> no error (unexpected)");
        } catch (IllegalArgumentException | NoSuchElementException e) {
            System.out.println(what + " -> " + e.getClass().getSimpleName() + ": " + e.getMessage());
        }
    }

    private static String names(List<Student> list) {
        return list.stream().map(Student::name).toList().toString();
    }
}
