package structures;

/**
 * In-memory B-Tree simulating how SQLite maintains an index page structure
 * over the service_requests table, keyed by requestId.
 *
 * This is a simplified, in-memory model built for demonstration purposes.
 * A real database engine like SQLite persists B-tree pages to disk and
 * manages far more complexity (page caching, WAL, freelist management,
 * variable-length records, etc). Here, each BTreeNode plays the role of
 * one index page, and a "split" corresponds to the page becoming full
 * and dividing into two pages plus a promoted key in the parent page.
 *
 * Minimum degree t = 3, chosen for this project:
 *   - Every non-root node holds between (t - 1) = 2 and (2t - 1) = 5 keys.
 *   - Every internal node has between t = 3 and 2t = 6 children.
 *   - A node splits the moment a 6th key would be inserted into it.
 *
 * Key type is generic and Comparable rather than a fixed int, because the
 * real service_requests.requestId column is a String (e.g. "REQ001"), not
 * a numeric id. Ordering therefore follows String's natural (lexicographic)
 * ordering, matching how SQLite would order a TEXT primary key column.
 *
 * Values stored are direct references to the associated object (e.g. a
 * ServiceRequest). A real database index would instead store a pointer
 * (row id / page offset) into the underlying table rather than the full
 * row, but storing the reference directly keeps this simulation simple
 * while still demonstrating correct B-tree mechanics.
 *
 * No use of java.util.TreeMap or any built-in ordered map.
 *
 * @param <K> the key type, e.g. the String requestId
 * @param <V> the type of value associated with each key
 */
public class BTree<K extends Comparable<K>, V> {

    /** Minimum degree of the tree. Every node holds up to (2*MIN_DEGREE - 1) keys. */
    private final int minDegree;

    private BTreeNode<K, V> root;

    /**
     * Number of node splits that have occurred so far. Used by
     * RequestIndexSimulator to detect and log exactly which insertion
     * triggered a split.
     */
    private int splitCount;

    /**
     * The key that most recently caused a split, or null if the last
     * insert did not cause a split. Read by RequestIndexSimulator right
     * after calling insert().
     */
    private K lastSplitTriggerKey;

    public BTree(int minDegree) {
        if (minDegree < 2) {
            throw new IllegalArgumentException("Minimum degree must be at least 2, got: " + minDegree);
        }
        this.minDegree = minDegree;
        this.root = new BTreeNode<>(minDegree, true);
        this.splitCount = 0;
        this.lastSplitTriggerKey = null;
    }

    /** Convenience constructor using the project's chosen minimum degree of 3. */
    public BTree() {
        this(3);
    }

    public int getMinDegree() {
        return minDegree;
    }

    public int getSplitCount() {
        return splitCount;
    }

    /** Returns the key that triggered the most recent split, or null if none. */
    public K getLastSplitTriggerKey() {
        return lastSplitTriggerKey;
    }

    public BTreeNode<K, V> getRoot() {
        return root;
    }

    // ------------------------------------------------------------------
    // Search
    // ------------------------------------------------------------------

    /**
     * Searches the tree for the given key.
     *
     * @param key the requestId to search for
     * @return the associated value, or null if the key is not present
     */
    public V search(K key) {
        return searchNode(root, key);
    }

    private V searchNode(BTreeNode<K, V> node, K key) {
        int i = 0;
        while (i < node.numKeys && key.compareTo(node.keys[i]) > 0) {
            i++;
        }

        if (i < node.numKeys && key.compareTo(node.keys[i]) == 0) {
            return node.values[i];
        }

        if (node.isLeaf) {
            return null;
        }

        return searchNode(node.children[i], key);
    }

    // ------------------------------------------------------------------
    // Insert
    // ------------------------------------------------------------------

    /**
     * Inserts a key/value pair into the tree, splitting nodes as needed.
     * After calling this, {@link #getSplitCount()} and
     * {@link #getLastSplitTriggerKey()} report whether this specific
     * insertion caused a split, so callers (e.g. RequestIndexSimulator)
     * can log exactly which insertion triggered which split.
     *
     * @param key   the requestId to insert
     * @param value the associated value
     */
    public void insert(K key, V value) {
        lastSplitTriggerKey = null;
        int splitsBefore = splitCount;

        BTreeNode<K, V> r = root;

        if (r.numKeys == 2 * minDegree - 1) {
            // Root is full: it must split first, growing the tree by one level.
            BTreeNode<K, V> newRoot = new BTreeNode<>(minDegree, false);
            newRoot.children[0] = r;
            splitChild(newRoot, 0);
            root = newRoot;
            recordSplit(key);
            insertNonFull(root, key, value);
        } else {
            insertNonFull(r, key, value);
        }

        if (splitCount > splitsBefore && lastSplitTriggerKey == null) {
            lastSplitTriggerKey = key;
        }
    }

    private void recordSplit(K triggerKey) {
        splitCount++;
        lastSplitTriggerKey = triggerKey;
    }

    /**
     * Inserts key/value into the subtree rooted at node, which is
     * guaranteed to NOT be full when this method is called.
     */
    private void insertNonFull(BTreeNode<K, V> node, K key, V value) {
        int i = node.numKeys - 1;

        if (node.isLeaf) {
            // Shift keys/values greater than key to the right, then insert.
            while (i >= 0 && node.keys[i].compareTo(key) > 0) {
                node.keys[i + 1] = node.keys[i];
                node.values[i + 1] = node.values[i];
                i--;
            }
            node.keys[i + 1] = key;
            node.values[i + 1] = value;
            node.numKeys++;
        } else {
            // Find the child that should receive the new key.
            while (i >= 0 && node.keys[i].compareTo(key) > 0) {
                i--;
            }
            i++;

            if (node.children[i].numKeys == 2 * minDegree - 1) {
                splitChild(node, i);
                recordSplit(key);
                if (key.compareTo(node.keys[i]) > 0) {
                    i++;
                }
            }
            insertNonFull(node.children[i], key, value);
        }
    }

    /**
     * Splits the full child at index childIndex of parent into two nodes,
     * promoting the child's median key up into parent.
     *
     * Before: parent.children[childIndex] is a full node with
     *         (2*minDegree - 1) keys.
     * After:  parent gains one key (the median) and one new child;
     *         the original child keeps the left half of its keys, and a
     *         new sibling node holds the right half.
     */
    private void splitChild(BTreeNode<K, V> parent, int childIndex) {
        BTreeNode<K, V> fullChild = parent.children[childIndex];
        int mid = minDegree - 1;

        BTreeNode<K, V> rightSibling = new BTreeNode<>(minDegree, fullChild.isLeaf);
        rightSibling.numKeys = minDegree - 1;

        // Copy the right half of fullChild's keys/values into rightSibling.
        for (int j = 0; j < minDegree - 1; j++) {
            rightSibling.keys[j] = fullChild.keys[j + mid + 1];
            rightSibling.values[j] = fullChild.values[j + mid + 1];
        }

        // If not a leaf, also copy the right half of the children pointers.
        if (!fullChild.isLeaf) {
            for (int j = 0; j < minDegree; j++) {
                rightSibling.children[j] = fullChild.children[j + mid + 1];
            }
        }

        K medianKey = fullChild.keys[mid];
        V medianValue = fullChild.values[mid];
        fullChild.numKeys = mid;

        // Make room in parent for the new child pointer, shifting right.
        for (int j = parent.numKeys; j >= childIndex + 1; j--) {
            parent.children[j + 1] = parent.children[j];
        }
        parent.children[childIndex + 1] = rightSibling;

        // Make room in parent for the promoted median key, shifting right.
        for (int j = parent.numKeys - 1; j >= childIndex; j--) {
            parent.keys[j + 1] = parent.keys[j];
            parent.values[j + 1] = parent.values[j];
        }
        parent.keys[childIndex] = medianKey;
        parent.values[childIndex] = medianValue;
        parent.numKeys++;
    }

    // ------------------------------------------------------------------
    // Printing / structure inspection (for report evidence)
    // ------------------------------------------------------------------

    /**
     * Prints the tree structure level by level-ish (depth-first with
     * indentation), for use as printed "before/after split" evidence in
     * the report.
     */
    public void printStructure() {
        printNode(root, 0);
    }

    private void printNode(BTreeNode<K, V> node, int depth) {
        StringBuilder indent = new StringBuilder();
        for (int d = 0; d < depth; d++) {
            indent.append("  ");
        }

        StringBuilder keysStr = new StringBuilder();
        for (int i = 0; i < node.numKeys; i++) {
            if (i > 0) keysStr.append(", ");
            keysStr.append(node.keys[i]);
        }
        System.out.println(indent + "[" + keysStr + "]" + (node.isLeaf ? " (leaf)" : ""));

        if (!node.isLeaf) {
            for (int i = 0; i <= node.numKeys; i++) {
                if (node.children[i] != null) {
                    printNode(node.children[i], depth + 1);
                }
            }
        }
    }

    /**
     * Inner node class. Package-visible with public fields for simplicity
     * within this data-structure package, mirroring the style of
     * CustomHeap's internal array-backed representation.
     */
    public static class BTreeNode<K extends Comparable<K>, V> {
        K[] keys;
        V[] values;
        BTreeNode<K, V>[] children;
        int numKeys;
        boolean isLeaf;

        @SuppressWarnings("unchecked")
        BTreeNode(int minDegree, boolean isLeaf) {
            this.isLeaf = isLeaf;
            this.keys = (K[]) new Comparable[2 * minDegree - 1];
            this.values = (V[]) new Object[2 * minDegree - 1];
            this.children = new BTreeNode[2 * minDegree];
            this.numKeys = 0;
        }

        public int getNumKeys() {
            return numKeys;
        }

        @SuppressWarnings("unchecked")
        public K[] getKeys() {
            K[] copy = (K[]) new Comparable[numKeys];
            System.arraycopy(keys, 0, copy, 0, numKeys);
            return copy;
        }

        public boolean isLeaf() {
            return isLeaf;
        }
    }
}
