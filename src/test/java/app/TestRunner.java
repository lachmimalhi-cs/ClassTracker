package app;

import app.algo.AttendanceAdvisor;
import app.algo.NeedsAttentionSelector;
import app.algo.StudentSearch;
import app.data.SettingsStore;
import app.ds.MinHeap;
import app.state.ThemeService;
import app.ds.Trie;
import app.model.Student;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.Objects;
import java.util.Random;
import java.util.Set;
import java.util.TreeSet;

/**
 * Tests for the data structures and algorithm classes, with edge cases and timing.
 * No test library is needed. Run:
 *   javac -d out $(find src/main/java/app/{model,ds,algo} src/test -name "*.java")
 *   java -cp out app.TestRunner
 */
public class TestRunner {

    private static int passed, failed;

    public static void main(String[] args) {
        trieTests();
        heapTests();
        studentTests();
        searchTests();
        selectorTests();
        advisorTests();
        themeTests();
        timing();
        System.out.println("TOTAL pass=" + passed + " fail=" + failed);
        if (failed > 0) {
            System.exit(1);
        }
    }

    // ---------------------------------------------------------------- Trie
    private static void trieTests() {
        Trie<String> t = new Trie<>();
        t.insert("Shelli", "1");
        t.insert("Sharmila", "2");
        t.insert("Bai", "3");
        t.insert("Lachmi", "3");
        t.insert("Memoona", "4");
        check("TR01", "startsWith(\"sh\")", set("1", "2"), new TreeSet<>(t.startsWith("sh")));
        check("TR02", "startsWith(\"SH\") upper case", set("1", "2"), new TreeSet<>(t.startsWith("SH")));
        check("TR03", "startsWith(\"lach\") surname", set("3"), new TreeSet<>(t.startsWith("lach")));
        check("TR04", "startsWith(\"xyz\") no match", set(), new TreeSet<>(t.startsWith("xyz")));
        check("TR05", "startsWith(\"shelliX\") longer than any word", set(), new TreeSet<>(t.startsWith("shelliX")));
        check("TR06", "startsWith(\"\") returns all", set("1", "2", "3", "4"), new TreeSet<>(t.startsWith("")));
        check("TR07", "size() counts (word, value) pairs", 5, t.size());
        t.insert("Shelli", "1");
        check("TR08", "same pair twice: size unchanged", 5, t.size());
        t.insert("Shelli", "9");
        check("TR09", "same word, second value: size + 1", 6, t.size());
        check("TR10", "containsWord(\"bai\")", true, t.containsWord("bai"));
        check("TR11", "containsWord(\"ba\") is only a prefix", false, t.containsWord("ba"));
        check("TR12", "remove(\"Bai\", \"3\") returns true", true, t.remove("Bai", "3"));
        check("TR13", "containsWord(\"bai\") after remove", false, t.containsWord("bai"));
        check("TR14", "remove the same pair again returns false", false, t.remove("Bai", "3"));
        check("TR15", "remove a word that was never stored", false, t.remove("zzz", "1"));
        check("TR16", "other words still found after remove", set("1", "2", "9"), new TreeSet<>(t.startsWith("sh")));
        Trie<String> p = new Trie<>();
        p.insert("abc", "1");
        p.remove("abc", "1");
        check("TR17", "prune: empty after the only word is removed", set(), new TreeSet<>(p.startsWith("a")));
        check("TR18", "prune: isEmpty() is true", true, p.isEmpty());
        t.clear();
        check("TR19", "clear(): size 0", 0, t.size());
        Trie<String> u = new Trie<>();
        u.insert("a", "1");
        check("TR20", "one-letter word", set("1"), new TreeSet<>(u.startsWith("a")));
        throwsA("TR21", "insert(null word)", IllegalArgumentException.class, () -> new Trie<String>().insert(null, "1"));
        throwsA("TR22", "insert(blank word)", IllegalArgumentException.class, () -> new Trie<String>().insert("  ", "1"));
        throwsA("TR23", "insert(null value)", IllegalArgumentException.class, () -> new Trie<String>().insert("a", null));
        throwsA("TR24", "startsWith(null)", IllegalArgumentException.class, () -> new Trie<String>().startsWith(null));
        throwsA("TR25", "containsWord(null)", IllegalArgumentException.class, () -> new Trie<String>().containsWord(null));
        Trie<Integer> g = new Trie<>();
        g.insert("one", 1);
        check("TR26", "works with another value type (Integer)", new TreeSet<>(List.of(1)), new TreeSet<>(g.startsWith("o")));
    }

    // ---------------------------------------------------------------- MinHeap
    private static void heapTests() {
        MinHeap<Integer> h = new MinHeap<>(Comparator.naturalOrder());
        check("HP01", "new heap isEmpty()", true, h.isEmpty());
        check("HP02", "new heap size()", 0, h.size());
        for (int v : new int[]{90, 50, 100, 60, 75}) {
            h.add(v);
        }
        check("HP03", "size() after 5 adds", 5, h.size());
        check("HP04", "peek() is the smallest", 50, h.peek());
        check("HP05", "peek() does not remove", 5, h.size());
        check("HP06", "poll() order", List.of(50, 60, 75, 90, 100), drain(h));
        check("HP07", "heap empty after draining", true, h.isEmpty());
        throwsA("HP08", "poll() on empty heap", NoSuchElementException.class, () -> new MinHeap<Integer>(Comparator.naturalOrder()).poll());
        throwsA("HP09", "peek() on empty heap", NoSuchElementException.class, () -> new MinHeap<Integer>(Comparator.naturalOrder()).peek());
        throwsA("HP10", "add(null)", IllegalArgumentException.class, () -> new MinHeap<Integer>(Comparator.naturalOrder()).add(null));
        throwsA("HP11", "null comparator", IllegalArgumentException.class, () -> new MinHeap<Integer>(null));
        throwsA("HP12", "capacity 0", IllegalArgumentException.class, () -> new MinHeap<Integer>(Comparator.naturalOrder(), 0));

        Random rnd = new Random(7);
        List<Integer> data = new ArrayList<>();
        MinHeap<Integer> big = new MinHeap<>(Comparator.naturalOrder(), 1);   // capacity 1: forces many array doublings
        for (int i = 0; i < 5000; i++) {
            int v = rnd.nextInt(1000);   // many repeated values
            data.add(v);
            big.add(v);
        }
        Collections.sort(data);
        check("HP13", "5000 random numbers (with repeats) come out sorted, array grows from capacity 1", data, drain(big));

        MinHeap<Integer> one = new MinHeap<>(Comparator.naturalOrder());
        one.add(42);
        check("HP14", "one item: poll() returns it", 42, one.poll());
        check("HP15", "one item: heap is empty after", true, one.isEmpty());

        MinHeap<Integer> same = new MinHeap<>(Comparator.naturalOrder());
        for (int i = 0; i < 4; i++) {
            same.add(5);
        }
        check("HP16", "equal items: all four come out", List.of(5, 5, 5, 5), drain(same));

        MinHeap<Integer> s = new MinHeap<>(Comparator.naturalOrder());
        for (int v : new int[]{3, 1, 2}) {
            s.add(v);
        }
        check("HP17", "toSortedList() sorted", List.of(1, 2, 3), s.toSortedList());
        check("HP18", "toSortedList() leaves the heap unchanged", 3, s.size());
        s.clear();
        check("HP19", "clear(): isEmpty()", true, s.isEmpty());
        s.add(8);
        check("HP20", "heap can be used again after clear()", 8, s.peek());

        MinHeap<String> words = new MinHeap<>(Comparator.comparing(String::length).thenComparing(Comparator.naturalOrder()));
        for (String w : new String[]{"ccc", "a", "bb", "aa"}) {
            words.add(w);
        }
        check("HP21", "works with another type and comparator (String by length)", List.of("a", "aa", "bb", "ccc"), drain(words));
    }

    // ---------------------------------------------------------------- Student
    private static void studentTests() {
        check("ST01", "Student trims spaces", "Bai Lachmi", new Student(" 1 ", "  Bai Lachmi ").name());
        check("ST02", "Student trims the roll", "1", new Student(" 1 ", "A").roll());
        throwsA("ST03", "empty roll", IllegalArgumentException.class, () -> new Student("", "A"));
        throwsA("ST04", "blank name", IllegalArgumentException.class, () -> new Student("1", "   "));
        throwsA("ST05", "null name", IllegalArgumentException.class, () -> new Student("1", null));
    }

    // ---------------------------------------------------------------- StudentSearch
    private static List<Student> cls() {
        return List.of(new Student("4420250973", "Shelli"), new Student("4420250978", "Sharmila"),
                new Student("4420250980", "Bai Lachmi"), new Student("4420250981", "Memoona"),
                new Student("4420250984", "Mariyam"));
    }

    private static void searchTests() {
        StudentSearch s = new StudentSearch(cls());
        check("SE01", "search(\"sh\") in class order", List.of("Shelli", "Sharmila"), names(s.search("sh")));
        check("SE02", "search(\"SH\") upper case", List.of("Shelli", "Sharmila"), names(s.search("SH")));
        check("SE03", "search(\"lach\") matches the second word", List.of("Bai Lachmi"), names(s.search("lach")));
        check("SE04", "search(\"ma\")", List.of("Mariyam"), names(s.search("ma")));
        check("SE05", "search(\"\") returns everyone", 5, s.search("").size());
        check("SE06", "search(\"   \") spaces only returns everyone", 5, s.search("   ").size());
        check("SE07", "search(\"  sh  \") is trimmed", List.of("Shelli", "Sharmila"), names(s.search("  sh  ")));
        check("SE08", "search with a space uses contains()", List.of("Bai Lachmi"), names(s.search("bai lac")));
        check("SE09", "search with a space, no match", List.of(), names(s.search("bai xyz")));
        check("SE10", "search(\"xyz\") no match", List.of(), names(s.search("xyz")));
        check("SE11", "byRoll finds a student", "Memoona", s.byRoll("4420250981").map(Student::name).orElse("none"));
        check("SE12", "byRoll unknown roll", "none", s.byRoll("1").map(Student::name).orElse("none"));
        check("SE13", "byRoll(null) does not fail", "none", s.byRoll(null).map(Student::name).orElse("none"));
        check("SE14", "size()", 5, s.size());
        check("SE15", "empty class: search(\"a\")", List.of(), names(new StudentSearch(List.of()).search("a")));
        check("SE16", "same word in two names, class order kept",
                List.of("Ali Khan", "Sara Ali"),
                names(new StudentSearch(List.of(new Student("1", "Ali Khan"), new Student("2", "Sara Ali"))).search("ali")));
        check("SE17", "name with extra spaces is indexed by words",
                List.of("Ali   Khan"),
                names(new StudentSearch(List.of(new Student("1", "Ali   Khan"))).search("kha")));
        throwsA("SE18", "search(null)", IllegalArgumentException.class, () -> s.search(null));
        throwsA("SE19", "null student list", IllegalArgumentException.class, () -> new StudentSearch(null));
        throwsA("SE20", "null student inside the list", IllegalArgumentException.class, () -> {
            List<Student> bad = new ArrayList<>();
            bad.add(null);
            new StudentSearch(bad);
        });
        throwsA("SE21", "repeated roll number", IllegalArgumentException.class,
                () -> new StudentSearch(List.of(new Student("1", "A"), new Student("1", "B"))));
        throwsA("SE22", "result list cannot be changed", UnsupportedOperationException.class, () -> s.search("").add(null));
    }

    // ---------------------------------------------------------------- NeedsAttentionSelector
    private static void selectorTests() {
        List<Student> c = cls();
        int[] pct = {100, 50, 100, 50, 0};
        NeedsAttentionSelector sel = new NeedsAttentionSelector(3);
        check("NA01", "lowest 3, ties by roll number", List.of("Mariyam", "Sharmila", "Memoona"),
                names(sel.select(c, st -> pct[c.indexOf(st)])));
        check("NA02", "fewer students than count", List.of("Mariyam"),
                names(new NeedsAttentionSelector(3).select(List.of(c.get(4)), st -> 0)));
        check("NA03", "students with no data (null) are skipped", List.of("Shelli"),
                names(sel.select(c, st -> st.name().equals("Shelli") ? 80 : null)));
        check("NA04", "nobody has data: empty result", List.of(), names(sel.select(c, st -> null)));
        check("NA05", "empty class", List.of(), names(sel.select(List.of(), st -> 1)));
        check("NA06", "count 1", List.of("Mariyam"), names(new NeedsAttentionSelector(1).select(c, st -> pct[c.indexOf(st)])));
        throwsA("NA07", "count 0", IllegalArgumentException.class, () -> new NeedsAttentionSelector(0));
        throwsA("NA08", "null list", IllegalArgumentException.class, () -> sel.select(null, st -> 1));
        throwsA("NA09", "null function", IllegalArgumentException.class, () -> sel.select(c, null));
    }

    // ---------------------------------------------------------------- AttendanceAdvisor
    private static void advisorTests() {
        AttendanceAdvisor a = new AttendanceAdvisor();
        check("AD01", "percent(9, 10)", 90, a.percent(9, 10));
        check("AD02", "percent(2, 3) rounds to 67", 67, a.percent(2, 3));
        check("AD03", "percent(0, 0) is null", null, a.percent(0, 0));
        check("AD04", "percent(0, 5)", 0, a.percent(0, 5));
        check("AD05", "percent(5, 5)", 100, a.percent(5, 5));
        check("AD06", "percent(1, 2) rounds half up", 50, a.percent(1, 2));
        check("AD07", "percent(1, 8) = 12.5 rounds up", 13, a.percent(1, 8));
        check("AD08", "exactly 75% (3 of 4) is not below", false, a.belowTarget(3, 4));
        check("AD09", "just under 75% (56 of 75) is below", true, a.belowTarget(56, 75));
        check("AD10", "56 of 75 is shown as 75%", 75, a.percent(56, 75));
        check("AD11", "nothing marked is not below", false, a.belowTarget(0, 0));
        check("AD12", "classesNeeded(6, 10)", 6, a.classesNeeded(6, 10));
        check("AD13", "classesNeeded when already at 75%", 0, a.classesNeeded(3, 4));
        check("AD14", "6 of 10 then 6 more classes reaches 75%", 75, a.percent(6 + 6, 10 + 6));
        check("AD15", "classesCanMiss(9, 10)", 2, a.classesCanMiss(9, 10));
        check("AD16", "after missing 2 the student is exactly at 75%", 75, a.percent(9, 10 + 2));
        check("AD17", "classesCanMiss(3, 4) = 0", 0, a.classesCanMiss(3, 4));
        check("AD18", "classesCanMiss when below target", 0, a.classesCanMiss(6, 10));
        check("AD19", "advice(6, 10)", "Attend the next 6 classes in a row to reach 75%.", a.advice(6, 10));
        check("AD20", "advice(9, 10)", "Can miss 2 more classes and stay at 75%.", a.advice(9, 10));
        check("AD21", "advice(0, 0)", "No attendance marked yet.", a.advice(0, 0));
        check("AD22", "advice(56, 75) says 1 class (singular)", "Attend the next 1 class in a row to reach 75%.", a.advice(56, 75));
        check("AD23", "advice(7, 8) can miss 1 more class (singular)", "Can miss 1 more class and stay at 75%.", a.advice(7, 8));
        check("AD24", "streak P,A,P,L,P", 3, a.streak(List.of("P", "A", "P", "L", "P")));
        check("AD25", "streak with no Absent", 3, a.streak(List.of("P", "L", "P")));
        check("AD26", "streak ends with Absent", 0, a.streak(List.of("P", "P", "A")));
        check("AD27", "streak of an empty list", 0, a.streak(List.of()));
        throwsA("AD28", "present > total", IllegalArgumentException.class, () -> a.percent(11, 10));
        throwsA("AD29", "negative days", IllegalArgumentException.class, () -> a.advice(-1, 10));
        throwsA("AD30", "streak(null)", IllegalArgumentException.class, () -> a.streak(null));
        throwsA("AD31", "streak with unknown status", IllegalArgumentException.class, () -> a.streak(List.of("P", "X")));
    }

    // ---------------------------------------------------------------- ThemeService
    private static SettingsStore memory(java.util.Map<String, String> m) {
        return new SettingsStore() {
            public String getSetting(String k) { return m.get(k); }
            public void setSetting(String k, String v) { if (v == null) m.remove(k); else m.put(k, v); }
        };
    }

    private static void themeTests() {
        java.util.Map<String, String> m = new java.util.HashMap<>();
        ThemeService t = new ThemeService(memory(m));
        check("TH01", "default main colour (light)", "#0F766E", t.colour("acc"));
        check("TH02", "style contains light background", true, t.style().contains("-bg:#F4F6F8;"));
        t.toggleDark();
        check("TH03", "dark mode: default main colour", "#4FD1C5", t.colour("acc"));
        check("TH04", "dark mode: style contains dark background", true, t.style().contains("-bg:#10171D;"));
        check("TH05", "dark mode is saved", "1", m.get("dark"));
        t.setColour("acc", "#2563eb");
        check("TH06", "custom colour is upper-cased", "#2563EB", t.colour("acc"));
        check("TH07", "custom colour is saved", "#2563EB", m.get("c_acc"));
        check("TH08", "a new ThemeService loads the saved settings", "#2563EB", new ThemeService(memory(m)).colour("acc"));
        t.applyPreset("#7C3AED");
        check("TH09", "preset sets Present colour too", "#7C3AED", t.colour("ok"));
        t.reset();
        check("TH10", "reset: default colour again", "#4FD1C5", t.colour("acc"));
        check("TH11", "reset: saved colour removed", null, m.get("c_acc"));
        check("TH12", "mix white and black 50%", "#808080", ThemeService.mix("#FFFFFF", "#000000", 0.5));
        throwsA("TH13", "bad hex", IllegalArgumentException.class, () -> t.setColour("acc", "blue"));
        throwsA("TH14", "bad key", IllegalArgumentException.class, () -> t.setColour("zzz", "#000000"));
        throwsA("TH15", "null store", IllegalArgumentException.class, () -> new ThemeService(null));
        m.put("c_ok", "garbage");
        check("TH16", "garbage in the saved settings is ignored", t.colour("ok").startsWith("#"), true);
    }

    // ---------------------------------------------------------------- timing (Big-O check)
    private static void timing() {
        System.out.println("--- timing (ms, average of 5 runs, JIT warmed up) ---");
        Random rnd = new Random(1);
        for (int n : new int[]{1_000, 10_000, 100_000}) {
            List<Student> list = new ArrayList<>();
            for (int i = 0; i < n; i++) {
                list.add(new Student(String.valueOf(i), name(rnd)));
            }
            double build = 0, find = 0;
            int hits = 0;
            for (int run = 0; run < 5; run++) {
                long t0 = System.nanoTime();
                StudentSearch s = new StudentSearch(list);
                long t1 = System.nanoTime();
                hits = s.search("ri").size();
                long t2 = System.nanoTime();
                build += (t1 - t0) / 1e6;
                find += (t2 - t1) / 1e6;
            }
            System.out.printf("PERF search N=%d build=%.2f ms  search(\"ri\") hits=%d  %.3f ms%n", n, build / 5, hits, find / 5);
        }
        for (int n : new int[]{1_000, 10_000, 100_000, 1_000_000}) {
            double add = 0, poll = 0, peek = 0;
            for (int run = 0; run < 5; run++) {
                MinHeap<Integer> h = new MinHeap<>(Comparator.naturalOrder());
                int[] vals = rnd.ints(n, 0, 1_000_000).toArray();
                long t0 = System.nanoTime();
                for (int v : vals) {
                    h.add(v);
                }
                long t1 = System.nanoTime();
                int sink = 0;
                for (int i = 0; i < n; i++) {
                    sink += h.peek();
                }
                long t2 = System.nanoTime();
                while (!h.isEmpty()) {
                    sink += h.poll();
                }
                long t3 = System.nanoTime();
                add += (t1 - t0) / 1e6;
                peek += (t2 - t1) / 1e6;
                poll += (t3 - t2) / 1e6;
                if (sink == 42) {
                    System.out.print("");
                }
            }
            System.out.printf("PERF heap N=%d  n adds=%.2f ms  n peeks=%.2f ms  n polls=%.2f ms  (per op: add %.0f ns, peek %.0f ns, poll %.0f ns)%n",
                    n, add / 5, peek / 5, poll / 5, add / 5 * 1e6 / n, peek / 5 * 1e6 / n, poll / 5 * 1e6 / n);
        }
    }

    private static String name(Random r) {
        String[] first = {"Ri", "Sha", "Mar", "Mem", "Bai", "Ali", "Zara", "Rina", "Shel"};
        String[] last = {"ka", "li", "yam", "oona", "mi", "ra", "han"};
        return first[r.nextInt(first.length)] + last[r.nextInt(last.length)] + " " + first[r.nextInt(first.length)] + last[r.nextInt(last.length)];
    }

    // ---------------------------------------------------------------- helpers
    private static <T> List<T> drain(MinHeap<T> h) {
        List<T> out = new ArrayList<>();
        while (!h.isEmpty()) {
            out.add(h.poll());
        }
        return out;
    }

    private static List<String> names(List<Student> list) {
        return list.stream().map(Student::name).toList();
    }

    private static Set<String> set(String... v) {
        return new TreeSet<>(List.of(v));
    }

    private static void check(String id, String what, Object expected, Object observed) {
        boolean ok = Objects.equals(expected, observed);
        report(id, what, shorten(expected), shorten(observed), ok);
    }

    private static void throwsA(String id, String what, Class<? extends Throwable> type, Runnable action) {
        String observed;
        boolean ok;
        try {
            action.run();
            observed = "no exception";
            ok = false;
        } catch (Throwable e) {
            observed = e.getClass().getSimpleName();
            ok = type.isInstance(e) && e.getMessage() != null || (e instanceof UnsupportedOperationException && type == UnsupportedOperationException.class);
        }
        report(id, what, type.getSimpleName(), observed, ok);
    }

    private static String shorten(Object o) {
        String s = String.valueOf(o);
        return s.length() > 60 ? s.substring(0, 57) + "..." : s;
    }

    private static void report(String id, String what, String expected, String observed, boolean ok) {
        if (ok) {
            passed++;
        } else {
            failed++;
        }
        System.out.println(id + " | " + what + " | expected=" + expected + " | observed=" + observed + " | " + (ok ? "PASS" : "FAIL"));
    }
}
