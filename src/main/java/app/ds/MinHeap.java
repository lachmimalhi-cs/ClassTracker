package app.ds;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Comparator;
import java.util.List;
import java.util.NoSuchElementException;

/**
 * Binary min-heap stored in an array. The smallest item (by the comparator) is always at index 0.
 * It is generic and takes a Comparator, so it can be reused for any type.
 *
 * Parent of index i is (i - 1) / 2. Children are 2i + 1 and 2i + 2.
 *
 * Time and space (n = number of items):
 *   add        O(log n) time, O(1) extra space (the array doubles when it is full: O(1) amortized)
 *   peek       O(1)
 *   poll       O(log n) time, O(1) extra space
 *   size, isEmpty   O(1)
 *   clear      O(n) (the slots are set to null so the objects can be freed)
 *   toSortedList    O(n log n) time, O(n) extra space (works on a copy, the heap is not changed)
 *   whole structure: O(n) space
 */
public class MinHeap<T> {

    private final Comparator<? super T> order;
    private Object[] data;
    private int size;

    public MinHeap(Comparator<? super T> order) {
        this(order, 16);
    }

    public MinHeap(Comparator<? super T> order, int capacity) {
        if (order == null) {
            throw new IllegalArgumentException("The comparator must not be null.");
        }
        if (capacity < 1) {
            throw new IllegalArgumentException("The capacity must be at least 1.");
        }
        this.order = order;
        this.data = new Object[capacity];
    }

    /** Adds an item. Null is not allowed. */
    public void add(T item) {
        if (item == null) {
            throw new IllegalArgumentException("Cannot add null to the heap.");
        }
        if (size == data.length) {
            data = Arrays.copyOf(data, size * 2);
        }
        data[size] = item;
        siftUp(size);
        size++;
    }

    /** Returns the smallest item without removing it. */
    public T peek() {
        if (size == 0) {
            throw new NoSuchElementException("The heap is empty.");
        }
        return at(0);
    }

    /** Removes and returns the smallest item. */
    public T poll() {
        if (size == 0) {
            throw new NoSuchElementException("The heap is empty.");
        }
        T top = at(0);
        size--;
        data[0] = data[size];
        data[size] = null;
        if (size > 0) {
            siftDown(0);
        }
        return top;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public void clear() {
        Arrays.fill(data, 0, size, null);
        size = 0;
    }

    /** All items from smallest to largest. The heap itself is not changed. */
    public List<T> toSortedList() {
        MinHeap<T> copy = new MinHeap<>(order, Math.max(1, size));
        for (int i = 0; i < size; i++) {
            copy.add(at(i));
        }
        List<T> out = new ArrayList<>(size);
        while (!copy.isEmpty()) {
            out.add(copy.poll());
        }
        return out;
    }

    // ----- inside the heap -----

    @SuppressWarnings("unchecked")
    private T at(int i) {
        return (T) data[i];
    }

    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (order.compare(at(i), at(parent)) >= 0) {
                break;
            }
            swap(i, parent);
            i = parent;
        }
    }

    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = left + 1;
            int smallest = i;
            if (left < size && order.compare(at(left), at(smallest)) < 0) {
                smallest = left;
            }
            if (right < size && order.compare(at(right), at(smallest)) < 0) {
                smallest = right;
            }
            if (smallest == i) {
                return;
            }
            swap(i, smallest);
            i = smallest;
        }
    }

    private void swap(int a, int b) {
        Object t = data[a];
        data[a] = data[b];
        data[b] = t;
    }
}
