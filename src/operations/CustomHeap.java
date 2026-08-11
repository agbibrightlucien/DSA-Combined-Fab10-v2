package operations;

/**
 * Generic array-based binary heap implemented completely from scratch.
 * Supports both MIN-HEAP and MAX-HEAP behaviour through a HeapComparator.
 *
 * Usage:
 *   MIN-HEAP: comparator returns negative when a < b  → (a, b) -> a.value - b.value
 *   MAX-HEAP: comparator returns negative when a > b  → (a, b) -> b.value - a.value
 *
 * Time complexity:
 *   insert()      O(log n)
 *   extractTop()  O(log n)
 *   peek()        O(1)
 *
 * @param <T> element type stored in the heap
 */
public class CustomHeap<T> {

    private Object[] heap;
    private int size;
    private int capacity;
    private final HeapComparator<T> comparator;

    /**
     * Creates a new heap with the given initial capacity and ordering comparator.
     *
     * @param initialCapacity starting array size (auto-doubles when full)
     * @param comparator      defines the heap ordering (min or max)
     */
    public CustomHeap(int initialCapacity, HeapComparator<T> comparator) {
        this.capacity   = Math.max(initialCapacity, 4);
        this.heap       = new Object[this.capacity];
        this.size       = 0;
        this.comparator = comparator;
    }

    // -------------------------------------------------------------------------
    // Public API
    // -------------------------------------------------------------------------

    /**
     * Inserts a new element into the heap. Resizes automatically if full.
     * Time: O(log n)
     */
    public void insert(T item) {
        if (size == capacity) resize();
        heap[size] = item;
        siftUp(size);
        size++;
    }

    /**
     * Removes and returns the top element (min or max depending on comparator).
     * Returns null if the heap is empty.
     * Time: O(log n)
     */
    @SuppressWarnings("unchecked")
    public T extractTop() {
        if (size == 0) return null;
        T top = (T) heap[0];
        size--;
        if (size > 0) {
            heap[0] = heap[size];
        }
        heap[size] = null;          // help GC
        if (size > 0) siftDown(0);
        return top;
    }

    /**
     * Returns the top element without removing it.
     * Time: O(1)
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        return size == 0 ? null : (T) heap[0];
    }

    public boolean isEmpty() { return size == 0; }
    public int size()        { return size; }

    // -------------------------------------------------------------------------
    // Internal heap operations
    // -------------------------------------------------------------------------

    /**
     * Moves the element at index i upward until the heap property is restored.
     */
    @SuppressWarnings("unchecked")
    private void siftUp(int i) {
        while (i > 0) {
            int parent = (i - 1) / 2;
            if (comparator.compare((T) heap[i], (T) heap[parent]) < 0) {
                swap(i, parent);
                i = parent;
            } else {
                break;
            }
        }
    }

    /**
     * Moves the element at index i downward until the heap property is restored.
     */
    @SuppressWarnings("unchecked")
    private void siftDown(int i) {
        while (true) {
            int left  = 2 * i + 1;
            int right = 2 * i + 2;
            int best  = i;

            if (left < size && comparator.compare((T) heap[left], (T) heap[best]) < 0) {
                best = left;
            }
            if (right < size && comparator.compare((T) heap[right], (T) heap[best]) < 0) {
                best = right;
            }

            if (best == i) break;
            swap(i, best);
            i = best;
        }
    }

    private void swap(int i, int j) {
        Object tmp = heap[i];
        heap[i]    = heap[j];
        heap[j]    = tmp;
    }

    /**
     * Doubles the backing array capacity when the heap is full.
     */
    private void resize() {
        capacity *= 2;
        Object[] newHeap = new Object[capacity];
        System.arraycopy(heap, 0, newHeap, 0, size);
        heap = newHeap;
    }
}
