package app;

import java.util.*;

/** TRIE: fast "starts with" search. Typing "ri" finds Ritika, Rishabh, Rina... */
public class Trie {
    private static class Node {
        Map<Character, Node> next = new HashMap<>();
        List<String> rolls = new ArrayList<>();
    }

    private final Node root = new Node();

    /** Store a word (like "Ritika") and remember which roll number it belongs to. */
    public void insert(String word, String roll) {
        Node n = root;
        for (char c : word.toLowerCase().toCharArray()) {
            n = n.next.computeIfAbsent(c, k -> new Node());
        }
        n.rolls.add(roll);
    }

    /** All roll numbers whose words start with the given prefix. */
    public Set<String> find(String prefix) {
        Node n = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            n = n.next.get(c);
            if (n == null) return Collections.emptySet();
        }
        Set<String> out = new HashSet<>();
        collect(n, out);
        return out;
    }

    private void collect(Node n, Set<String> out) {
        out.addAll(n.rolls);
        for (Node child : n.next.values()) collect(child, out);
    }
}