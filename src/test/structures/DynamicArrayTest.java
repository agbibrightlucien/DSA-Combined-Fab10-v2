package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for DynamicArray (Part 2). */
class DynamicArrayTest {

    @Test
    void normal_insertAndGet_returnsCorrectValues() {
        DynamicArray<String> arr = new DynamicArray<>();
        arr.add("a");
        arr.add("b");
        arr.add("c");
        assertEquals(3, arr.size());
        assertEquals("a", arr.get(0));
        assertEquals("c", arr.get(2));
    }

    @Test
    void boundary_resizeBeyondInitialCapacity_growsAndKeepsData() {
        DynamicArray<Integer> arr = new DynamicArray<>(2); // tiny capacity to force resize
        for (int i = 0; i < 10; i++) arr.add(i);
        assertEquals(10, arr.size());
        assertTrue(arr.capacity() >= 10, "backing array must have grown to fit all elements");
        for (int i = 0; i < 10; i++) assertEquals(i, arr.get(i));
    }

    @Test
    void invalid_getOutOfRangeIndex_throwsIndexOutOfBounds() {
        DynamicArray<String> arr = new DynamicArray<>();
        arr.add("only");
        assertThrows(IndexOutOfBoundsException.class, () -> arr.get(5));
    }
}
