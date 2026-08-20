package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CustomQueue (Part 5). */
class CustomQueueTest {

    @Test
    void normal_enqueueDequeue_isFIFOOrder() {
        CustomQueue<String> q = new CustomQueue<>();
        q.enqueue("first");
        q.enqueue("second");
        q.enqueue("third");
        assertEquals("first", q.dequeue());
        assertEquals("second", q.dequeue());
    }

    @Test
    void boundary_peekDoesNotRemoveElement() {
        CustomQueue<Integer> q = new CustomQueue<>();
        q.enqueue(99);
        assertEquals(99, q.peek());
        assertEquals(1, q.size());
        assertEquals(99, q.dequeue());
    }

    @Test
    void invalid_dequeueOnEmptyQueue_throwsIllegalState() {
        CustomQueue<Integer> q = new CustomQueue<>();
        assertThrows(IllegalStateException.class, q::dequeue);
    }
}
