package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/** Part 13 correctness evidence for the Binary Search Tree (Section 6). */
class BSTTest {

    @Test
    void normal_insertAndSearch_findsInsertedKeys() {
        BST<Integer, String> tree = new BST<>();
        tree.insert(50, "fifty");
        tree.insert(30, "thirty");
        tree.insert(70, "seventy");
        tree.insert(20, "twenty");
        tree.insert(40, "forty");

        assertEquals("fifty", tree.search(50));
        assertEquals("twenty", tree.search(20));
        assertEquals("forty", tree.search(40));
    }

    @Test
    void normal_inorderTraversal_returnsValuesInAscendingKeyOrder() {
        BST<Integer, Integer> tree = new BST<>();
        int[] keys = {50, 30, 70, 20, 40, 60, 80};
        for (int k : keys) tree.insert(k, k);

        List<Integer> inorder = tree.inorderTraversal();
        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), inorder);
    }

    @Test
    void boundary_emptyTree_searchReturnsNullAndHeightIsZero() {
        BST<Integer, String> tree = new BST<>();
        assertNull(tree.search(1));
        assertEquals(0, tree.height());
        assertTrue(tree.isEmpty());
    }

    @Test
    void boundary_duplicateKey_replacesValueRatherThanInsertingTwice() {
        BST<Integer, String> tree = new BST<>();
        tree.insert(5, "first");
        tree.insert(5, "second");
        assertEquals("second", tree.search(5));
        assertEquals(1, tree.inorderTraversal().size());
    }

    @Test
    void invalidLookup_missingKey_returnsNull() {
        BST<Integer, String> tree = new BST<>();
        tree.insert(10, "ten");
        assertNull(tree.search(999));
        assertFalse(tree.contains(999));
        assertTrue(tree.contains(10));
    }
}
