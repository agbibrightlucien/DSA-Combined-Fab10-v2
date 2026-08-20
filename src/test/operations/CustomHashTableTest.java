package operations;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 13 correctness evidence for CustomHashTable (Part 3 / Operation 3).
 * Capacity parameter 156 matches Rule 2 (sum of Level-300 members' last-two
 * index digits), same value used by TicketLookupEngine in the Part 15 UI.
 */
class CustomHashTableTest {

    @Test
    void normal_putAndGet_returnsStoredValue() {
        CustomHashTable<String, String> table = new CustomHashTable<>(156);
        table.put("REQ-001", "Broken projector");
        assertEquals("Broken projector", table.get("REQ-001"));
        assertEquals(1, table.getSize());
    }

    @Test
    void boundary_getOnMissingKey_returnsNull() {
        CustomHashTable<String, String> table = new CustomHashTable<>(156);
        table.put("REQ-001", "value");
        assertNull(table.get("REQ-999"));
    }

    @Test
    void invalid_removeThenGet_confirmsKeyNoLongerPresent() {
        CustomHashTable<String, Integer> table = new CustomHashTable<>(156);
        table.put("K1", 100);
        assertTrue(table.remove("K1"));
        assertNull(table.get("K1"));
        assertFalse(table.remove("K1"), "removing an already-removed key should return false");
    }
}
