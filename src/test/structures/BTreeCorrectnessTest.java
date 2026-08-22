package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/**
 * Part 13 correctness evidence for the B-Tree (Section 6). Kept separate
 * from the existing BTreeTest.java, which is a hand-run trace/demo script
 * (not JUnit) producing the "node split explanation" printout for the
 * report -- that file is left untouched as trace evidence.
 */
class BTreeCorrectnessTest {

    @Test
    void normal_insertAndSearch_findsAllInsertedKeys() {
        BTree<String, String> tree = new BTree<>(3);
        String[] ids = {"REQ005", "REQ001", "REQ009", "REQ003", "REQ007"};
        for (String id : ids) tree.insert(id, "value-" + id);

        for (String id : ids) {
            assertEquals("value-" + id, tree.search(id));
        }
    }

    @Test
    void boundary_emptyTree_searchReturnsNull() {
        BTree<String, String> tree = new BTree<>(3);
        assertNull(tree.search("ANY"));
    }

    @Test
    void invalidLookup_keyNeverInserted_returnsNull() {
        BTree<String, String> tree = new BTree<>(3);
        tree.insert("REQ001", "value-REQ001");
        assertNull(tree.search("REQ999"));
    }

    @Test
    void splitEvidence_sixthKeyIntoARootTriggersExactlyOneSplit() {
        // minDegree 3 -> a node holds up to 2*3-1 = 5 keys; the 6th insert
        // into the (still-single) root must trigger exactly one split.
        BTree<Integer, Integer> tree = new BTree<>(3);
        int[] keys = {10, 20, 30, 40, 50};
        for (int k : keys) tree.insert(k, k);
        assertEquals(0, tree.getSplitCount(), "no split expected before the node is full");

        tree.insert(60, 60);
        assertEquals(1, tree.getSplitCount());
        assertEquals(60, tree.getLastSplitTriggerKey());
    }

    @Test
    void invalidConstruction_minDegreeBelowTwo_throwsIllegalArgumentException() {
        assertThrows(IllegalArgumentException.class, () -> new BTree<Integer, Integer>(1));
    }
}
