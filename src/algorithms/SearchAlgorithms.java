package algorithms;

/**
 * Part 9: Searching Algorithms.
 *
 * Both methods are written from scratch (no java.util.Arrays.binarySearch
 * or Collections helpers) and are generic so they can search an array of
 * any Comparable type, including an array of service requests ordered by
 * requestId (e.g. by wrapping ServiceRequest.requestId comparisons in a
 * small Comparable adapter, or by pulling requestId values into a
 * String[] before searching).
 */
public class SearchAlgorithms {

    private SearchAlgorithms() {
    }

    /**
     * Scans the array from the front and returns the index of the first
     * element equal to target, or -1 if target is not present.
     *
     * No precondition on ordering: linearSearch works on sorted or
     * unsorted input, at O(n) cost either way.
     *
     * @throws IllegalArgumentException if array is null
     */
    public static <T extends Comparable<T>> int linearSearch(T[] array, T target) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        for (int i = 0; i < array.length; i++) {
            if (array[i] == null ? target == null : array[i].compareTo(target) == 0) {
                return i;
            }
        }
        return -1;
    }

    /**
     * Binary search over array, which MUST already be sorted in ascending
     * order (this is binarySearch's precondition). In this codebase, that
     * means the array should already have been produced by
     * SortAlgorithms.mergeSort (or an equivalent Part 10 sort) before it is
     * passed in here; binarySearch does not sort a copy itself, because
     * doing so on every call would silently hide O(n log n) sort cost
     * behind what is supposed to be an O(log n) operation.
     *
     * The precondition is actively checked: the array is scanned once
     * (O(n)) to confirm it is non-decreasing, and if it is not,
     * binarySearch throws IllegalArgumentException rather than silently
     * returning a wrong or misleading index. Without this check, calling
     * binarySearch on unsorted input would not necessarily throw anything
     * -- it would just walk the wrong half of the array on each comparison
     * and could return -1 (not found) for a target that is actually
     * present, or -1 for one that is legitimately absent, with no way for
     * the caller to tell the two apart. See Part 13's invalid
     * precondition test for a demonstration.
     *
     * @throws IllegalArgumentException if array is null, or array is not sorted ascending
     */
    public static <T extends Comparable<T>> int binarySearch(T[] array, T target) {
        if (array == null) {
            throw new IllegalArgumentException("array must not be null");
        }
        checkSortedPrecondition(array);

        int low = 0;
        int high = array.length - 1;

        while (low <= high) {
            int mid = low + (high - low) / 2;
            int cmp = array[mid].compareTo(target);
            if (cmp == 0) {
                return mid;
            } else if (cmp < 0) {
                low = mid + 1;
            } else {
                high = mid - 1;
            }
        }
        return -1;
    }

    private static <T extends Comparable<T>> void checkSortedPrecondition(T[] array) {
        for (int i = 1; i < array.length; i++) {
            if (array[i - 1].compareTo(array[i]) > 0) {
                throw new IllegalArgumentException(
                        "binarySearch precondition violated: array is not sorted ascending "
                                + "at index " + (i - 1) + " (" + array[i - 1] + " > " + array[i] + ")");
            }
        }
    }
}
