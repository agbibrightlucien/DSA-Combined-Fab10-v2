package structures;

/**
 * Custom functional comparator interface for the generic CustomHeap.
 * Implemented from scratch to replace java.util.Comparator.
 *
 * @param <T> the type of objects being compared
 */
@FunctionalInterface
public interface HeapComparator<T> {
    /**
     * Compares two objects.
     * @return negative if a should come before b in the heap, positive if b should come before a, 0 if equal
     */
    int compare(T a, T b);
}
