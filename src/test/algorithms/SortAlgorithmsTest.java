package algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.Comparator;

/** Part 13 correctness evidence for selection sort, insertion sort and quicksort (Part 10). */
class SortAlgorithmsTest {

    private static final Comparator<Integer> ASCENDING = Integer::compareTo;

    @Test
    void normal_selectionSort_sortsSixElementArrayAscending() {
        Integer[] arr = {8, 3, 5, 1, 9, 2};
        SortAlgorithms.selectionSort(arr, ASCENDING);
        assertArrayEquals(new Integer[]{1, 2, 3, 5, 8, 9}, arr);
    }

    @Test
    void normal_insertionSort_sortsSixElementArrayAscending() {
        // Matches the trace table in CorrectnessEvidence.md exactly.
        Integer[] arr = {8, 3, 5, 1, 9, 2};
        SortAlgorithms.insertionSort(arr, ASCENDING);
        assertArrayEquals(new Integer[]{1, 2, 3, 5, 8, 9}, arr);
    }

    @Test
    void boundary_quickSortOnSixElementArray_matchesRequiredTraceSize() {
        // Matches the trace table in CorrectnessEvidence.md exactly.
        Integer[] arr = {8, 3, 5, 1, 9, 2};
        SortAlgorithms.quickSort(arr, ASCENDING);
        assertArrayEquals(new Integer[]{1, 2, 3, 5, 8, 9}, arr);
    }

    @Test
    void boundary_emptyAndSingleElementArrays_remainUnchanged() {
        Integer[] empty = {};
        Integer[] single = {7};
        SortAlgorithms.selectionSort(empty, ASCENDING);
        SortAlgorithms.insertionSort(single, ASCENDING);
        SortAlgorithms.quickSort(empty, ASCENDING);
        assertArrayEquals(new Integer[]{}, empty);
        assertArrayEquals(new Integer[]{7}, single);
    }

    @Test
    void boundary_duplicateKeys_allThreeSortsHandleThemCorrectly() {
        Integer[] a = {4, 2, 4, 1, 4, 3};
        Integer[] b = {4, 2, 4, 1, 4, 3};
        Integer[] c = {4, 2, 4, 1, 4, 3};
        SortAlgorithms.selectionSort(a, ASCENDING);
        SortAlgorithms.insertionSort(b, ASCENDING);
        SortAlgorithms.quickSort(c, ASCENDING);
        Integer[] expected = {1, 2, 3, 4, 4, 4};
        assertArrayEquals(expected, a);
        assertArrayEquals(expected, b);
        assertArrayEquals(expected, c);
    }

    @Test
    void stability_insertionSort_preservesRelativeOrderOfEqualKeys() {
        // Insertion sort is documented as stable: two equal-keyed elements
        // must keep their original relative order after sorting.
        String[] tagged = {"3a", "1a", "3b", "2a", "1b"};
        Comparator<String> byLeadingDigit = Comparator.comparingInt(s -> s.charAt(0) - '0');
        SortAlgorithms.insertionSort(tagged, byLeadingDigit);
        assertArrayEquals(new String[]{"1a", "1b", "2a", "3a", "3b"}, tagged);
    }
}
