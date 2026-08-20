package structures;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Part 13 correctness evidence for DisjointSet (Part 8) - Kruskal connectivity trace. */
class DisjointSetTest {

    @Test
    void normal_unionTwoElements_areConnected() {
        DisjointSet ds = new DisjointSet(5);
        assertFalse(ds.connected(0, 1));
        ds.union(0, 1);
        assertTrue(ds.connected(0, 1));
    }

    @Test
    void boundary_disjointElementsWithNoUnion_remainUnconnected() {
        DisjointSet ds = new DisjointSet(4);
        ds.union(0, 1);
        assertFalse(ds.connected(0, 2), "elements 0 and 2 were never unioned");
        assertFalse(ds.connected(2, 3), "elements 2 and 3 were never unioned");
    }

    @Test
    void trace_chainOfUnionsWithPathCompression_allEightElementsEndUpConnected() {
        // 8-element trace: union(0,1) union(2,3) union(4,5) union(6,7)
        // union(1,3) union(5,7) union(3,7) -> all 8 in one set
        DisjointSet ds = new DisjointSet(8);
        ds.union(0, 1);
        ds.union(2, 3);
        ds.union(4, 5);
        ds.union(6, 7);
        assertTrue(ds.connected(0, 1));
        assertTrue(ds.connected(2, 3));
        assertFalse(ds.connected(0, 3), "not yet merged at this point in the trace");

        ds.union(1, 3); // merges {0,1} with {2,3}
        assertTrue(ds.connected(0, 3));
        assertFalse(ds.connected(0, 7));

        ds.union(5, 7); // merges {4,5} with {6,7}
        ds.union(3, 7); // merges everything into one set
        for (int i = 1; i < 8; i++) {
            assertTrue(ds.connected(0, i), "element " + i + " must be connected to 0 after full merge");
        }
    }
}
