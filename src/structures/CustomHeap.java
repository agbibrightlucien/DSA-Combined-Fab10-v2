package structures;

/**
 * Generic array-based binary heap implemented completely from scratch.
 * This is the project's single canonical heap / priority-queue implementation
 * (Section 6 of the brief) — every module that needs a heap (Dijkstra's
 * min-heap, the resource-dispatch max-heap, Prim's candidate-edge heap, the
 * dispatch-mode comparison demo) builds on this one class instead of each
 * keeping its own copy.
 *
 * Two equivalent ways to use it, matching the two patterns different callers
 * needed historically:
 *
 * <ol>
 *   <li><b>Comparator-driven</b> — supply a {@link HeapComparator} up front
 *       (via {@link #CustomHeap(int, HeapComparator)}), then call
 *       {@link #insert(Object)} / {@link #extractTop()}. The comparator
 *       decides ordering: return negative from {@code compare(a, b)} when
 *       {@code a} should sit closer to the top.</li>
 *   <li><b>Explicit-priority</b> — use the no-arg {@link #CustomHeap()}
 *       constructor, then call {@link #insert(Object, int)} with an integer
 *       priority and {@link #extractMax()}. This is always a max-heap by
 *       priority — the item with the highest priority comes out first.</li>
 * </ol>
 *
 * Time complexity: insert() O(log n), extractTop()/extractMax() O(log n),
 * peek() O(1).
 *
 * @param <T> element type stored in the heap
 */
public class CustomHeap<T> {

    private static final class Entry<T> {
        T data;
        int priority;

        Entry(T data, int priority) {
            this.data = data;
            this.priority = priority;
        }
    }

    private Object[] heap;
    private int size;
    private int capacity;
    private final HeapComparator<T> comparator;

    /**
     * Explicit-priority mode: a max-heap ordered by the integer priority
     * passed to {@link #insert(Object, int)}.
     */
    public CustomHeap() {
        this(4, null);
    }

    /**
     * Comparator-driven mode: ordering is entirely decided by {@code comparator},
     * applied to the stored elements. Pass a comparator that returns negative
     * when {@code a} should be extracted before {@code b} (min-heap for
     * natural ordering, max-heap by inverting the comparison).
     *
     * @param initialCapacity starting array size (auto-doubles when full)
     * @param comparator      defines the heap ordering; may be {@code null}
     *                        to use explicit-priority mode instead
     */
    public CustomHeap(int initialCapacity, HeapComparator<T> comparator) {
        this.capacity = Math.max(initialCapacity, 4);
        this.heap = new Object[this.capacity];
        this.size = 0;
        this.comparator = comparator;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /** Comparator-driven insert. Time: O(log n) */
    public void insert(T item) {
        insertEntry(new Entry<>(item, 0));
    }

    /** Explicit-priority insert; higher priority rises to the top. Time: O(log n) */
    public void insert(T item, int priority) {
        insertEntry(new Entry<>(item, priority));
    }

    private void insertEntry(Entry<T> entry) {
        if (size == capacity) resize();
        heap[size] = entry;
        siftUp(size);
        size++;
    }

    /**
     * Removes and returns the top element under the comparator supplied at
     * construction. Returns null if the heap is empty. Time: O(log n)
     */
    public T extractTop() {
        return extractRaw();
    }

    /**
     * Explicit-priority alias for {@link #extractTop()} — removes and returns
     * the highest-priority element. Throws if the heap is empty, matching
     * the historical max-heap contract used by Prim's algorithm and the
     * dispatch-mode demo. Time: O(log n)
     */
    public T extractMax() {
        if (size == 0) {
            throw new IllegalStateException("Heap is empty");
        }
        return extractRaw();
    }

    @SuppressWarnings("unchecked")
    private T extractRaw() {
        if (size == 0) return null;
        Entry<T> top = (Entry<T>) heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
        }
        heap[size] = null; // help GC
        if (size > 0) siftDown(0);
        return top.data;
    }

    /** Returns the top element without removing it. Time: O(1) */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (size == 0) return null;
        return ((Entry<T>) heap[0]).data;
    }

    public boolean isEmpty() { return size == 0; }
    public int size() { return size; }

    // -------------------------------------------------------------------------
    // Internal heap operations
    // -------------------------------------------------------------------------

    @SuppressWarnings("unchecked")
    private int compareEntries(Entry<T> a, Entry<T> b) {
        if (comparator != null) {
            return comparator.compare(a.data, b.data);
        }
        // Explicit-priority mode: max-heap, so a "wins" (is treated as smaller,
        // i.e. rises higher) when its priority is greater.
        return Integer.compare(b.priority, a.priority);
    }

    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (compareEntries((Entry<T>) heap[i], (Entry<T>) heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        while (true) {
            int left = 2 * i + 1;
            int right = 2 * i + 2;
            int best = i;

            if (left < size && compareEntries((Entry<T>) heap[left], (Entry<T>) heap[best]) < 0) {
                best = left;
            }
            if (right < size && compareEntries((Entry<T>) heap[right], (Entry<T>) heap[best]) < 0) {
                best = right;
            }

            if (best == i) break;
            swap(i, best);
            i = best;
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i] = heap[j];
        heap[j] = tmp;
    }

    private void resize() {
        capacity *= 2;
        Object[] newHeap = new Object[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
