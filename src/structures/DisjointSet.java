package structures;

/**
 * Disjoint Set (Union-Find) data structure.
 *
 * Supports the classic three operations required by Section 6 of the brief:
 *   - makeSet(n): initialise n singleton sets, one per element 0..n-1
 *   - find(x): return the representative (root) of the set containing x,
 *              using path compression so repeated finds get faster over time
 *   - union(x, y): merge the sets containing x and y, using union by rank
 *              so the resulting tree stays shallow
 *
 * This structure is the backbone of Kruskal's minimum spanning tree
 * algorithm in Part 11: before adding a road (edge) to the spanning tree,
 * Kruskal calls find() on both endpoints. If they already share a root,
 * adding that edge would create a cycle, so it is skipped.
 *
 * Manual trace (8 elements), for the Part 8 report evidence:
 *
 *   makeSet(8)              -> parent = [0,1,2,3,4,5,6,7], rank = [0,0,0,0,0,0,0,0]
 *   union(0, 1)             -> find(0)=0, find(1)=1, ranks equal (0==0)
 *                               parent[1] = 0, rank[0] becomes 1
 *                               parent = [0,0,2,3,4,5,6,7]
 *   union(2, 3)             -> parent[3] = 2, rank[2] becomes 1
 *                               parent = [0,0,2,2,4,5,6,7]
 *   union(0, 2)             -> find(0)=0 (rank 1), find(2)=2 (rank 1), ranks equal
 *                               parent[2] = 0, rank[0] becomes 2
 *                               parent = [0,0,0,2,4,5,6,7]
 *   find(3)                 -> 3 -> parent[3]=2 -> parent[2]=0 -> root is 0
 *                               path compression sets parent[3] = 0 directly
 *                               parent = [0,0,0,0,4,5,6,7]
 *   union(4, 5)             -> parent[5] = 4, rank[4] becomes 1
 *   union(6, 7)             -> parent[7] = 6, rank[6] becomes 1
 *   union(4, 6)             -> ranks equal (1==1), parent[6] = 4, rank[4] becomes 2
 *   find(0) vs find(4)      -> 0 and 4 are different roots -> {0,1,2,3} and {4,5,6,7}
 *                               are still two separate components after this trace
 */
public class DisjointSet {

    private int[] parent;
    private int[] rank;
    private int count; // number of elements currently tracked

    /**
     * Creates a disjoint set already initialised with n singleton sets
     * (equivalent to calling makeSet for elements 0..n-1 immediately).
     */
    public DisjointSet(int n) {
        makeSet(n);
    }

    /**
     * Initialises n singleton sets, one per element 0..n-1.
     * Each element starts out as its own representative with rank 0.
     * Calling this again resets the structure to n fresh singleton sets.
     */
    public void makeSet(int n) {
        if (n < 0) {
            throw new IllegalArgumentException("makeSet size cannot be negative: " + n);
        }
        parent = new int[n];
        rank = new int[n];
        for (int i = 0; i < n; i++) {
            parent[i] = i;
            rank[i] = 0;
        }
        count = n;
    }

    /**
     * Finds the representative (root) of the set containing x, compressing
     * the path along the way so every visited node points directly at the
     * root afterwards.
     */
    public int find(int x) {
        validateIndex(x);
        if (parent[x] != x) {
            parent[x] = find(parent[x]); // path compression
        }
        return parent[x];
    }

    /**
     * Merges the sets containing x and y using union by rank.
     * Returns true if a merge happened, false if x and y were already
     * in the same set (this is exactly the "would create a cycle" signal
     * that Kruskal's algorithm relies on in Part 11).
     */
    public boolean union(int x, int y) {
        int rootX = find(x);
        int rootY = find(y);

        if (rootX == rootY) {
            return false; // already connected -> adding this edge would form a cycle
        }

        if (rank[rootX] < rank[rootY]) {
            parent[rootX] = rootY;
        } else if (rank[rootX] > rank[rootY]) {
            parent[rootY] = rootX;
        } else {
            parent[rootY] = rootX;
            rank[rootX]++;
        }
        return true;
    }

    /** Returns true if x and y are currently in the same set. */
    public boolean connected(int x, int y) {
        return find(x) == find(y);
    }

    /** Returns how many elements this structure currently tracks. */
    public int size() {
        return count;
    }

    private void validateIndex(int x) {
        if (parent == null) {
            throw new IllegalStateException("DisjointSet has not been initialised; call makeSet(n) first.");
        }
        if (x < 0 || x >= parent.length) {
            throw new IndexOutOfBoundsException(
                "Index " + x + " is out of range for a disjoint set of size " + parent.length);
        }
    }
}