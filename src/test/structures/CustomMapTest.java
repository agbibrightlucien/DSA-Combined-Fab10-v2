package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 13 correctness evidence for CustomMap (Section 6: "Set, map: custom
 * set/map on top of hash table or BST -- membership and lookup use case").
 *
 * Use case: looking up a location's human-readable name by its locationId,
 * the same kind of id -> record lookup TicketLookupEngine performs for
 * service requests.
 */
class CustomMapTest {

    private CustomMap<String, String> locationNames() {
        CustomMap<String, String> names = new CustomMap<>();
        names.put("LOC-001", "Balme Library");
        names.put("LOC-002", "Commonwealth Hall");
        names.put("LOC-003", "Legon Hall");
        return names;
    }

    @Test
    void normal_lookup_returnsValueForExistingKey() {
        CustomMap<String, String> names = locationNames();
        assertEquals("Balme Library", names.get("LOC-001"));
    }

    @Test
    void normal_lookup_returnsNullForMissingKey() {
        CustomMap<String, String> names = locationNames();
        assertNull(names.get("LOC-999"));
    }

    @Test
    void normal_membershipCheck_containsKeyMatchesGet() {
        CustomMap<String, String> names = locationNames();
        assertTrue(names.containsKey("LOC-002"));
        assertFalse(names.containsKey("LOC-999"));
    }

    @Test
    void normal_put_onExistingKeyUpdatesRatherThanDuplicates() {
        CustomMap<String, String> names = locationNames();
        int sizeBefore = names.size();
        names.put("LOC-001", "Balme Library (Main Campus)");
        assertEquals(sizeBefore, names.size());
        assertEquals("Balme Library (Main Campus)", names.get("LOC-001"));
    }

    @Test
    void boundary_emptyMap_getReturnsNullAndIsEmptyTrue() {
        CustomMap<String, String> empty = new CustomMap<>();
        assertTrue(empty.isEmpty());
        assertNull(empty.get("anything"));
    }

    @Test
    void normal_remove_dropsKeyFromMap() {
        CustomMap<String, String> names = locationNames();
        assertTrue(names.remove("LOC-003"));
        assertNull(names.get("LOC-003"));
        assertFalse(names.containsKey("LOC-003"));
    }
}
