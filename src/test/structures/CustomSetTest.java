package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 13 correctness evidence for CustomSet (Section 6: "Set, map: custom
 * set/map on top of hash table or BST -- membership and lookup use case").
 *
 * Use case: a dispatcher checking whether an incoming service-request
 * category is one of the categories this deployment actually supports,
 * before it is queued -- a direct membership check against real project
 * category names.
 */
class CustomSetTest {

    private CustomSet<String> supportedCategories() {
        CustomSet<String> categories = new CustomSet<>();
        categories.add("IT_SUPPORT");
        categories.add("MAINTENANCE");
        categories.add("TRANSPORT");
        categories.add("EMERGENCY");
        return categories;
    }

    @Test
    void normal_membershipCheck_recognisesSupportedCategory() {
        CustomSet<String> categories = supportedCategories();
        assertTrue(categories.contains("IT_SUPPORT"));
        assertTrue(categories.contains("EMERGENCY"));
    }

    @Test
    void normal_membershipCheck_rejectsUnsupportedCategory() {
        CustomSet<String> categories = supportedCategories();
        assertFalse(categories.contains("LAUNDRY"));
    }

    @Test
    void normal_add_returnsFalseForDuplicateAndDoesNotGrowSize() {
        CustomSet<String> categories = supportedCategories();
        int sizeBefore = categories.size();
        boolean wasNew = categories.add("IT_SUPPORT");
        assertFalse(wasNew);
        assertEquals(sizeBefore, categories.size());
    }

    @Test
    void boundary_emptySet_containsNothingAndIsEmpty() {
        CustomSet<String> empty = new CustomSet<>();
        assertTrue(empty.isEmpty());
        assertFalse(empty.contains("ANYTHING"));
    }

    @Test
    void normal_remove_dropsElementFromMembership() {
        CustomSet<String> categories = supportedCategories();
        assertTrue(categories.remove("TRANSPORT"));
        assertFalse(categories.contains("TRANSPORT"));
        assertFalse(categories.remove("TRANSPORT")); // already gone
    }
}
