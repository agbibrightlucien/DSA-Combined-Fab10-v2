package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CustomStack (Part 4). */
class CustomStackTest {

    @Test
    void normal_pushPop_isLIFOOrder() {
        CustomStack<Integer> stack = new CustomStack<>();
        stack.push(1);
        stack.push(2);
        stack.push(3);
        assertEquals(3, stack.pop());
        assertEquals(2, stack.pop());
        assertEquals(1, stack.pop());
    }

    @Test
    void boundary_popOnEmptyStack_returnsNull() {
        CustomStack<String> stack = new CustomStack<>();
        assertTrue(stack.isEmpty());
        assertNull(stack.pop());
    }

    @Test
    void resize_pushBeyondInitialCapacity_growsAndPreservesOrder() {
        CustomStack<Integer> stack = new CustomStack<>(2);
        for (int i = 0; i < 20; i++) stack.push(i);
        assertEquals(20, stack.size());
        for (int i = 19; i >= 0; i--) assertEquals(i, stack.pop());
    }
}
