package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CircularQueue (Part 5). */
class CircularQueueTest {

    @Test
    void normal_enqueueDequeue_isFIFOOrder() {
        CircularQueue<String> q = new CircularQueue<>(3);
        q.enqueue("a");
        q.enqueue("b");
        assertEquals("a", q.dequeue());
    }

    @Test
    void boundary_enqueueOnFullQueue_throwsIllegalState() {
        CircularQueue<Integer> q = new CircularQueue<>(2);
        q.enqueue(1);
        q.enqueue(2);
        assertTrue(q.isFull());
        assertThrows(IllegalStateException.class, () -> q.enqueue(3));
    }

    @Test
    void wrapAround_dequeueThenEnqueueAgain_wrapsFrontAndRearCorrectly() {
        // Trace: capacity 3. enqueue A,B,C (full) -> dequeue A -> enqueue D
        // (D must wrap into the slot A vacated) -> dequeue order must be B,C,D
        CircularQueue<String> q = new CircularQueue<>(3);
        q.enqueue("A");
        q.enqueue("B");
        q.enqueue("C");
        assertEquals("A", q.dequeue());
        q.enqueue("D"); // wraps around to reuse A's slot
        assertEquals("B", q.dequeue());
        assertEquals("C", q.dequeue());
        assertEquals("D", q.dequeue());
        assertTrue(q.isEmpty());
    }
}
