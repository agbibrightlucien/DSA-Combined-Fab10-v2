package algorithms;

import structures.CustomQueue;
import structures.Graph;

/**
 * Graph traversal algorithms: Breadth-First Search (BFS) and Depth-First
 * Search (DFS), operating on the shared {@link structures.Graph} built in
 * Part 8.
 *
 * These two algorithms answer the exact question posed in Section 3 of the
 * brief: "which locations are reachable from the current dispatch point?"
 * That question is answered directly by {@link #reachableFrom(Graph, String)}.
 *
 * Design notes:
 *   - Both traversals work over indices (the same 0..n-1 index space that
 *     Graph itself uses internally), and convert to/from locationId only
 *     at the public boundary, so the hot loops never touch the database
 *     or CustomHashTable.
 *   - Neither method uses java.util collections. BFS uses the
 *     {@link structures.CustomQueue} built in Part 5. DFS uses plain Java
 *     recursion, which the Part 11 task list explicitly allows as an
 *     alternative to a CustomStack ("Implement DFS using either recursion
 *     or a CustomStack from Part 4") -- recursion is used here so this
 *     class has no dependency on Part 4's CustomStack being finished or
 *     matching a particular method signature.
 *   - Results are returned as arrays rather than any java.util.List, sized
 *     up front to the graph's location count and trimmed once the real
 *     visited count is known, so no ArrayList is needed either.
 *   - Roads are directed (see Graph's own class Javadoc), so "reachable"
 *     here means reachable by following roads in their stored direction.
 *     This is the correct behaviour for a real dispatcher deciding where a
 *     resource can currently drive to.
 *
 * Manual verification example (small, hand-checkable):
 *   Given three locations A(0), B(1), C(2) with roads A->B and B->C only,
 *   bfs(graph, 0) and dfs(graph, 0) must both visit {0, 1, 2} and neither
 *   may visit an index not reachable from 0. A is not reachable from C in
 *   this example, since no reverse roads exist -- reachableFrom(graph,
 *   "C") should return only {"C"} itself.
 */
public class GraphTraversal {

    private GraphTraversal() {
        // static utility class, no instances
    }

    /**
     * Breadth-first traversal starting at startIndex, using a
     * {@link CustomQueue} to visit locations level by level.
     *
     * @return the indices of every location reachable from startIndex,
     *         in BFS visit order (startIndex is always first).
     */
    public static int[] bfs(Graph graph, int startIndex) {
        validateStart(graph, startIndex);

        int n = graph.getLocationCount();
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        int orderCount = 0;

        CustomQueue<Integer> queue = new CustomQueue<>();
        visited[startIndex] = true;
        queue.enqueue(startIndex);

        while (!queue.isEmpty()) {
            int current = queue.dequeue();
            order[orderCount++] = current;

            int[] neighbors = graph.getNeighbors(current);
            for (int neighbor : neighbors) {
                if (!visited[neighbor]) {
                    visited[neighbor] = true;
                    queue.enqueue(neighbor);
                }
            }
        }

        return trim(order, orderCount);
    }

    /**
     * Depth-first traversal starting at startIndex, implemented with plain
     * recursion (see class Javadoc for why recursion was chosen over a
     * CustomStack in this class).
     *
     * @return the indices of every location reachable from startIndex, in
     *         the order they were first visited (startIndex is always first).
     */
    public static int[] dfs(Graph graph, int startIndex) {
        validateStart(graph, startIndex);

        int n = graph.getLocationCount();
        boolean[] visited = new boolean[n];
        int[] order = new int[n];
        // orderCount is wrapped in a single-element array so the recursive
        // helper can advance a shared counter without any instance state.
        int[] orderCount = new int[1];

        dfsVisit(graph, startIndex, visited, order, orderCount);

        return trim(order, orderCount[0]);
    }

    private static void dfsVisit(Graph graph, int current, boolean[] visited,
                                  int[] order, int[] orderCount) {
        visited[current] = true;
        order[orderCount[0]++] = current;

        int[] neighbors = graph.getNeighbors(current);
        for (int neighbor : neighbors) {
            if (!visited[neighbor]) {
                dfsVisit(graph, neighbor, visited, order, orderCount);
            }
        }
    }

    /**
     * Direct answer to the Section 3 dispatcher question: every location
     * currently reachable from startLocationId, as locationId strings, in
     * BFS order. This is the single method the UI's DispatchPanel or
     * PathFinderPanel (Part 15) should call.
     *
     * @throws IllegalArgumentException if startLocationId is not in this graph
     */
    public static String[] reachableFrom(Graph graph, String startLocationId) {
        Integer startIndex = graph.getIndexForLocationId(startLocationId);
        if (startIndex == null) {
            throw new IllegalArgumentException(
                "reachableFrom: unknown locationId \"" + startLocationId + "\"");
        }

        int[] reachableIndices = bfs(graph, startIndex);
        String[] reachableIds = new String[reachableIndices.length];
        for (int i = 0; i < reachableIndices.length; i++) {
            reachableIds[i] = graph.getLocationIdAt(reachableIndices[i]);
        }
        return reachableIds;
    }

    private static void validateStart(Graph graph, int startIndex) {
        if (!graph.isGraphLoaded()) {
            throw new IllegalStateException(
                "GraphTraversal: graph.loadGraph() must be called before traversal.");
        }
        if (startIndex < 0 || startIndex >= graph.getLocationCount()) {
            throw new IndexOutOfBoundsException(
                "GraphTraversal: start index " + startIndex + " is out of range.");
        }
    }

    private static int[] trim(int[] array, int count) {
        int[] trimmed = new int[count];
        System.arraycopy(array, 0, trimmed, 0, count);
        return trimmed;
    }
}