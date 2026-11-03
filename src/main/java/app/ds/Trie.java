package app.ds;

import java.util.ArrayDeque;
import java.util.ArrayList;
import java.util.Collections;
import java.util.Deque;
import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

/**
 * Trie (prefix tree) that maps words to values.
 * It is generic, so it can be reused for any kind of value (here: roll numbers).
 * Letters are stored in lower case, so searching is not case sensitive.
 *
 * Time and space (L = length of the word, P = length of the prefix,
 * M = number of nodes below the prefix node, N = total letters stored):
 *   insert        O(L) time, O(L) extra space
 *   startsWith    O(P + M) time, O(M) extra space for the result
 *   containsWord  O(L) time, O(1) extra space
 *   remove        O(L) time, O(L) extra space (path list)
 *   size / isEmpty / clear  O(1)
 *   whole structure: O(N) space
 */
public class Trie<V> {

    /** One letter position. Children are kept in a HashMap, so a child lookup is O(1). */
    private static final class Node<V> {
        final Map<Character, Node<V>> next = new HashMap<>();
        final Set<V> values = new LinkedHashSet<>();   // values of words that end here
    }

    private Node<V> root = new Node<>();
    private int size;   // number of (word, value) pairs stored

    /** Stores a word and the value that belongs to it. Adding the same pair twice changes nothing. */
    public void insert(String word, V value) {
        requireText(word, "word");
        if (value == null) {
            throw new IllegalArgumentException("value must not be null.");
        }
        Node<V> node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            node = node.next.computeIfAbsent(c, k -> new Node<>());
        }
        if (node.values.add(value)) {
            size++;
        }
    }

    /**
     * Returns the values of all words that start with the prefix (the order is not fixed; StudentSearch sorts it).
     * An empty prefix returns everything. A prefix that is not found returns an empty set.
     */
    public Set<V> startsWith(String prefix) {
        if (prefix == null) {
            throw new IllegalArgumentException("prefix must not be null.");
        }
        Node<V> node = root;
        for (char c : prefix.toLowerCase().toCharArray()) {
            node = node.next.get(c);          // O(1) child lookup
            if (node == null) {
                return Collections.emptySet();
            }
        }
        Set<V> result = new LinkedHashSet<>();
        Deque<Node<V>> stack = new ArrayDeque<>();   // iterative walk: no deep recursion
        stack.push(node);
        while (!stack.isEmpty()) {
            Node<V> n = stack.pop();
            result.addAll(n.values);
            for (Node<V> child : n.next.values()) {
                stack.push(child);
            }
        }
        return result;
    }

    /** True if at least one value is stored for exactly this word. */
    public boolean containsWord(String word) {
        requireText(word, "word");
        Node<V> node = root;
        for (char c : word.toLowerCase().toCharArray()) {
            node = node.next.get(c);
            if (node == null) {
                return false;
            }
        }
        return !node.values.isEmpty();
    }

    /** Removes one (word, value) pair and deletes nodes that are no longer needed. Returns false if it was not stored. */
    public boolean remove(String word, V value) {
        requireText(word, "word");
        String w = word.toLowerCase();
        List<Node<V>> path = new ArrayList<>();
        Node<V> node = root;
        path.add(node);
        for (char c : w.toCharArray()) {
            node = node.next.get(c);
            if (node == null) {
                return false;
            }
            path.add(node);
        }
        if (!node.values.remove(value)) {
            return false;
        }
        size--;
        for (int i = w.length(); i > 0; i--) {   // cut empty branches from the bottom up
            Node<V> current = path.get(i);
            if (current.values.isEmpty() && current.next.isEmpty()) {
                path.get(i - 1).next.remove(w.charAt(i - 1));
            } else {
                break;
            }
        }
        return true;
    }

    /** Number of (word, value) pairs stored. */
    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    /** Removes everything. */
    public void clear() {
        root = new Node<>();
        size = 0;
    }

    private static void requireText(String text, String what) {
        if (text == null || text.isBlank()) {
            throw new IllegalArgumentException(what + " must not be null or empty.");
        }
    }
}
