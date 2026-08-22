package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for CustomDeque (Part 5). */
class CustomDequeTest {

    @Test
    void normal_addFrontAndAddRear_peeksReturnCorrectEnds() {
        CustomDeque<Integer> dq = new CustomDeque<>();
        dq.addRear(2);
        dq.addFront(1);
        dq.addRear(3);
        assertEquals(1, dq.peekFront());
        assertEquals(3, dq.peekRear());
        assertEquals(3, dq.size());
    }

    @Test
    void boundary_urgentRequestPushedToFront_dequeuedBeforeNormalRequests() {
        // Simulates UrgentDispatcher's use case: normal requests added to
        // the rear, an urgent one jumps to the front ahead of them.
        CustomDeque<String> dq = new CustomDeque<>();
        dq.addRear("normal-1");
        dq.addRear("normal-2");
        dq.addFront("URGENT");
        assertEquals("URGENT", dq.removeFront());
        assertEquals("normal-1", dq.removeFront());
    }

    @Test
    void invalid_removeFrontOnEmptyDeque_throwsIllegalState() {
        CustomDeque<Integer> dq = new CustomDeque<>();
        assertThrows(IllegalStateException.class, dq::removeFront);
    }
}
