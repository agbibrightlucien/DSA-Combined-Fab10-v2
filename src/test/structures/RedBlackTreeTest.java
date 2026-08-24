package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import java.util.List;

/** Part 13 correctness evidence for the Red-Black Tree (Section 6). */
class RedBlackTreeTest {

    @Test
    void normal_insertAndSearch_findsInsertedKeys() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        int[] keys = {10, 20, 30, 15, 25, 5, 1};
        for (int k : keys) tree.insert(k, "v" + k);

        assertEquals("v10", tree.search(10));
        assertEquals("v1", tree.search(1));
        assertEquals("v25", tree.search(25));
    }

    @Test
    void normal_inorderTraversal_returnsValuesInAscendingKeyOrder() {
        RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
        int[] keys = {50, 30, 70, 20, 40, 60, 80};
        for (int k : keys) tree.insert(k, k);

        List<Integer> inorder = tree.inorderTraversal();
        assertEquals(List.of(20, 30, 40, 50, 60, 70, 80), inorder);
    }

    @Test
    void balanceEvidence_ascendingInsertOrder_staysLogarithmicallyShort() {
        // A plain (unbalanced) BST fed 1..15 in ascending order degenerates
        // into a straight line of height 15. A red-black tree must stay
        // close to the theoretical minimum height (~log2(15) ~= 4), which is
        // exactly the height/rotation evidence Section 6 asks for.
        RedBlackTree<Integer, Integer> rbt = new RedBlackTree<>();
        BST<Integer, Integer> bst = new BST<>();
        for (int i = 1; i <= 15; i++) {
            rbt.insert(i, i);
            bst.insert(i, i);
        }

        assertEquals(15, bst.height(), "sanity check: ascending insert degenerates a plain BST");
        assertTrue(rbt.height() <= 6, "red-black tree height should stay close to log2(n), was " + rbt.height());
    }

    @Test
    void boundary_emptyTree_searchReturnsNullAndHeightIsZero() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        assertNull(tree.search(1));
        assertEquals(0, tree.height());
        assertTrue(tree.isEmpty());
    }

    @Test
    void boundary_duplicateKey_replacesValueRatherThanInsertingTwice() {
        RedBlackTree<Integer, String> tree = new RedBlackTree<>();
        tree.insert(5, "first");
        tree.insert(5, "second");
        assertEquals("second", tree.search(5));
        assertEquals(1, tree.inorderTraversal().size());
    }
}
