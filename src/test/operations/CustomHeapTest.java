package operations;

import structures.CustomHeap;
import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CustomHeap / Priority Queue (dispatch view - Seth's structure). */
class CustomHeapTest {

    @Test
    void normal_insertAndExtractTop_returnsMaxFirst() {
        // Max-heap by integer priority, matching UrgentDispatcher's usage pattern.
        // Per CustomHeap's documented contract, a max-heap comparator must return
        // negative when a > b, so this is (a, b) -> Integer.compare(b, a).
        CustomHeap<Integer> heap = new CustomHeap<>(10, (a, b) -> Integer.compare(b, a));
        heap.insert(5);
        heap.insert(9);
        heap.insert(2);
        heap.insert(7);
        assertEquals(9, heap.extractTop());
        assertEquals(7, heap.extractTop());
    }

    @Test
    void boundary_singleElementHeap_extractLeavesHeapEmpty() {
        CustomHeap<String> heap = new CustomHeap<>(4, (a, b) -> a.compareTo(b));
        heap.insert("only");
        assertEquals("only", heap.extractTop());
        assertTrue(heap.isEmpty());
        assertEquals(0, heap.size());
    }

    @Test
    void heapifyOrder_dispatchScoreTrace_matchesExpectedExtractionOrder() {
        // Trace evidence: insert scores 3,8,1,9,5 -> extraction order must be
        // strictly descending: 9,8,5,3,1
        CustomHeap<Integer> heap = new CustomHeap<>(10, (a, b) -> Integer.compare(b, a));
        int[] scores = {3, 8, 1, 9, 5};
        for (int s : scores) heap.insert(s);

        int[] expectedOrder = {9, 8, 5, 3, 1};
        for (int expected : expectedOrder) {
            assertEquals(expected, heap.extractTop());
        }
        assertTrue(heap.isEmpty());
    }
}
