package algorithms;

import models.ServiceRequest;

import java.util.Comparator;

/**
 * Part 10: Sorting Algorithms — Selection Sort, Insertion Sort and Quicksort.
 * Owner: mariodebest00
 *
 * <p>This class provides three classic, from-scratch sorting algorithms that
 * operate generically on any array via a {@link Comparator}, so they can be
 * used to sort an array of {@link ServiceRequest} objects (for example by
 * {@code timeSubmitted} or {@code urgency}) or any other comparable data.
 *
 * <p>None of the methods in this class call {@code java.util.Arrays.sort}
 * or any other built-in sorting utility — every comparison and swap is
 * implemented directly.
 *
 * <p><b>Relationship to the existing merge sort:</b> {@code AuditLogSorter.java}
 * already implements merge sort correctly elsewhere in this project and is
 * intentionally left untouched. Merge sort is the fourth sort in the required
 * comparison set for the report; it is only referenced here, never
 * reimplemented.
 *
 * <p><b>Sorting comparison summary (for the report):</b>
 * <table border="1">
 *   <tr><th>Algorithm</th><th>Stable?</th><th>In place?</th><th>Avg time</th></tr>
 *   <tr><td>Selection Sort</td><td>No</td><td>Yes</td><td>O(n^2)</td></tr>
 *   <tr><td>Insertion Sort</td><td>Yes</td><td>Yes</td><td>O(n^2), O(n) best case</td></tr>
 *   <tr><td>Quicksort</td><td>No</td><td>Yes</td><td>O(n log n) avg, O(n^2) worst</td></tr>
 *   <tr><td>Merge Sort (AuditLogSorter)</td><td>Yes</td><td>No (needs O(n) buffer)</td><td>O(n log n)</td></tr>
 * </table>
 */
public final class SortAlgorithms {

    private SortAlgorithms() {
        // Static utility class; no instances.
    }

    /**
     * Sorts {@code array} in place using selection sort: repeatedly finds the
     * minimum element (according to {@code comparator}) in the unsorted
     * remainder of the array and swaps it into place at the front.
     *
     * <p><b>Stability: NOT STABLE.</b> Selection sort can swap an element past
     * an equal element that appeared earlier in the array, so the relative
     * order of elements that compare as equal is not guaranteed to be
     * preserved.
     *
     * <p><b>In place:</b> yes — only a constant amount of extra space is used
     * beyond the input array (index variables and one temporary swap slot).
     *
     * <p>Time complexity: O(n^2) comparisons and O(n) swaps in every case
     * (best, average and worst), since the algorithm always scans the
     * remaining unsorted portion to find the minimum regardless of the
     * array's initial order.
     *
     * @param array      the array to sort, modified in place
     * @param comparator defines the sort order between two elements
     * @param <T>        the element type
     */
    public static <T> void selectionSort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length < 2) {
            return;
        }
        int n = array.length;
        for (int i = 0; i < n - 1; i++) {
            int minIndex = i;
            for (int j = i + 1; j < n; j++) {
                if (comparator.compare(array[j], array[minIndex]) < 0) {
                    minIndex = j;
                }
            }
            if (minIndex != i) {
                swap(array, i, minIndex);
            }
        }
    }

    /**
     * Sorts {@code array} in place using insertion sort: builds up the sorted
     * portion of the array one element at a time, shifting larger elements to
     * the right to make room for each newly inserted element.
     *
     * <p><b>Stability: STABLE.</b> An element is only shifted past elements
     * that are strictly greater than it (according to {@code comparator}), so
     * two elements that compare as equal never have their relative order
     * swapped.
     *
     * <p><b>In place:</b> yes — sorting is done by shifting elements within
     * the same array, using only one temporary variable to hold the element
     * currently being inserted.
     *
     * <p>Time complexity: O(n^2) worst and average case, O(n) best case when
     * the input is already sorted, since each element is compared against a
     * shrinking number of already-sorted neighbours.
     *
     * @param array      the array to sort, modified in place
     * @param comparator defines the sort order between two elements
     * @param <T>        the element type
     */
    public static <T> void insertionSort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length < 2) {
            return;
        }
        for (int i = 1; i < array.length; i++) {
            T current = array[i];
            int j = i - 1;
            while (j >= 0 && comparator.compare(array[j], current) > 0) {
                array[j + 1] = array[j];
                j--;
            }
            array[j + 1] = current;
        }
    }

    /**
     * Sorts {@code array} in place using quicksort with the last element of
     * each partition chosen as the pivot (Lomuto partition scheme).
     *
     * <p><b>Stability: NOT STABLE.</b> The partitioning step swaps elements
     * across the pivot based on comparison alone, which can reorder equal
     * elements relative to each other.
     *
     * <p><b>In place vs recursive decomposition:</b> this implementation
     * sorts <b>in place</b> — partitioning rearranges elements within the
     * original array using only a constant amount of extra space per call
     * (a few index variables), rather than allocating new sub-arrays for the
     * left and right partitions the way a naive "copying" quicksort would.
     * However, the algorithm is still expressed <b>recursively</b>: each
     * recursive call processes a sub-range of the same array (denoted by
     * {@code low} and {@code high} indices), and the recursion depth adds
     * O(log n) additional space on the call stack in the average case (O(n)
     * in the worst case, for example on an already-sorted input with this
     * pivot strategy). So while no auxiliary array is used, quicksort is not
     * "free" of extra space — the cost simply moves from heap-allocated
     * arrays to call-stack frames.
     *
     * <p>Time complexity: O(n log n) on average; O(n^2) worst case, which
     * last-element pivoting can trigger on already-sorted or reverse-sorted
     * input.
     *
     * @param array      the array to sort, modified in place
     * @param comparator defines the sort order between two elements
     * @param <T>        the element type
     */
    public static <T> void quickSort(T[] array, Comparator<T> comparator) {
        if (array == null || array.length < 2) {
            return;
        }
        quickSort(array, 0, array.length - 1, comparator);
    }

    private static <T> void quickSort(T[] array, int low, int high, Comparator<T> comparator) {
        if (low < high) {
            int pivotIndex = partition(array, low, high, comparator);
            quickSort(array, low, pivotIndex - 1, comparator);
            quickSort(array, pivotIndex + 1, high, comparator);
        }
    }

    /**
     * Lomuto partition scheme: uses {@code array[high]} as the pivot, moves
     * every element less than the pivot to the left side, then places the
     * pivot in its final sorted position.
     *
     * @return the final index of the pivot element after partitioning
     */
    private static <T> int partition(T[] array, int low, int high, Comparator<T> comparator) {
        T pivot = array[high];
        int i = low - 1;
        for (int j = low; j < high; j++) {
            if (comparator.compare(array[j], pivot) <= 0) {
                i++;
                swap(array, i, j);
            }
        }
        swap(array, i + 1, high);
        return i + 1;
    }

    private static <T> void swap(T[] array, int i, int j) {
        T temp = array[i];
        array[i] = array[j];
        array[j] = temp;
    }

    /**
     * Convenience comparator: orders service requests by ascending
     * {@code timeSubmitted}.
     */
    public static Comparator<ServiceRequest> byTimeSubmitted() {
        return (a, b) -> {
            if (a == null || b == null) return 0;
            if (a.getTimeSubmitted() == null && b.getTimeSubmitted() == null) return 0;
            if (a.getTimeSubmitted() == null) return 1;
            if (b.getTimeSubmitted() == null) return -1;
            return a.getTimeSubmitted().compareTo(b.getTimeSubmitted());
        };
    }

    /**
     * Convenience comparator: orders service requests by descending
     * {@code urgency} (most urgent first).
     */
    public static Comparator<ServiceRequest> byUrgencyDescending() {
        return (a, b) -> {
            if (a == null || b == null) return 0;
            return Integer.compare(b.getUrgencyScore(), a.getUrgencyScore());
        };
    }

    /**
     * Small manual demonstration of all three sorts, for report evidence and
     * quick sanity checking.
     */
    public static void main(String[] args) {
        System.out.println("=== selectionSort by timeSubmitted ===");
        ServiceRequest[] bySelection = sampleData();
        selectionSort(bySelection, byTimeSubmitted());
        printAll(bySelection);

        System.out.println("\n=== insertionSort by timeSubmitted ===");
        ServiceRequest[] byInsertion = sampleData();
        insertionSort(byInsertion, byTimeSubmitted());
        printAll(byInsertion);

        System.out.println("\n=== quickSort by urgency (descending) ===");
        ServiceRequest[] byQuick = sampleData();
        quickSort(byQuick, byUrgencyDescending());
        printAll(byQuick);
    }

    private static ServiceRequest[] sampleData() {
        long now = System.currentTimeMillis();
        return new ServiceRequest[] {
                new ServiceRequest("REQ-101", "LOC-001", "LOC-002", "IT_SUPPORT", "LOW",
                        new java.sql.Timestamp(now - 50000), new java.sql.Timestamp(now + 100000), "PENDING"),
                new ServiceRequest("REQ-102", "LOC-003", "LOC-004", "MAINTENANCE", "CRITICAL",
                        new java.sql.Timestamp(now - 100000), new java.sql.Timestamp(now + 50000), "PENDING"),
                new ServiceRequest("REQ-103", "LOC-005", "LOC-006", "TRANSPORT", "MEDIUM",
                        new java.sql.Timestamp(now - 30000), new java.sql.Timestamp(now + 200000), "PENDING"),
                new ServiceRequest("REQ-104", "LOC-007", "LOC-008", "EMERGENCY", "HIGH",
                        new java.sql.Timestamp(now - 80000), new java.sql.Timestamp(now + 80000), "PENDING"),
                new ServiceRequest("REQ-105", "LOC-009", "LOC-010", "IT_SUPPORT", "MEDIUM",
                        new java.sql.Timestamp(now - 40000), new java.sql.Timestamp(now + 150000), "PENDING")
        };
    }

    private static void printAll(ServiceRequest[] requests) {
        for (ServiceRequest r : requests) {
            System.out.println(r);
        }
    }
}

