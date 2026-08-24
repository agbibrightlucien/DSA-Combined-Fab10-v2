package algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for linear and binary search (Part 9). */
class SearchAlgorithmsTest {

    @Test
    void normal_linearSearch_findsTargetAtCorrectIndex() {
        Integer[] arr = {8, 3, 15, 1, 9};
        assertEquals(2, SearchAlgorithms.linearSearch(arr, 15));
    }

    @Test
    void normal_linearSearch_missingTargetReturnsMinusOne() {
        Integer[] arr = {8, 3, 15, 1, 9};
        assertEquals(-1, SearchAlgorithms.linearSearch(arr, 42));
    }

    @Test
    void normal_binarySearch_findsTargetInSortedArray() {
        Integer[] arr = {1, 3, 5, 7, 9, 11, 13, 15};
        assertEquals(6, SearchAlgorithms.binarySearch(arr, 13));
    }

    @Test
    void boundary_emptyArray_bothSearchesReturnMinusOne() {
        Integer[] arr = {};
        assertEquals(-1, SearchAlgorithms.linearSearch(arr, 5));
        assertEquals(-1, SearchAlgorithms.binarySearch(arr, 5));
    }

    @Test
    void boundary_singleElementArray_findsOrMissesCorrectly() {
        Integer[] arr = {42};
        assertEquals(0, SearchAlgorithms.binarySearch(arr, 42));
        assertEquals(-1, SearchAlgorithms.binarySearch(arr, 7));
    }

    @Test
    void invalidPrecondition_binarySearchOnUnsortedInput_throwsIllegalArgumentException() {
        // Precondition violation counterexample (Section 10): binarySearch requires
        // ascending-sorted input. The value 1 genuinely exists at index 3, but the
        // precondition check must reject the array before returning any answer.
        Integer[] unsorted = {9, 5, 13, 1, 3, 15, 7, 11};
        assertThrows(IllegalArgumentException.class, () -> SearchAlgorithms.binarySearch(unsorted, 1));
    }

    @Test
    void invalidPrecondition_nullArray_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> SearchAlgorithms.linearSearch(null, 1));
        assertThrows(IllegalArgumentException.class, () -> SearchAlgorithms.binarySearch(null, 1));
    }
}
