package operations;

import database.DatabaseConnection;
import structures.CustomHeap;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Operation 1: Shortest Path Finder — Dijkstra's Algorithm.
 *
 * The campus road network is loaded from the database and stored using a
 * custom adjacency-list structure (array of linked-list edge nodes).
 * No java.util collections are used anywhere in this class.
 *
 * Rule 1 (Traffic Penalty): A hardcoded 1.5x multiplier is applied to
 * the effective travel time of every road whose destination is an Academic
 * area location, simulating peak student traffic.
 *
 * Custom DSA used:
 *   - CustomHeap<DijkstraNode>  : min-heap for Dijkstra's PriorityQueue
 *   - CustomHashTable<K,V>      : maps locationId String -> array index
 *   - EdgeNode linked lists     : adjacency list representation
 *   - Plain arrays              : distances, previous-node tracking
 */
public class ShortestPathFinder {

    // Rule 1: Traffic penalty multiplier for roads entering Academic locations
    private static final double ACADEMIC_PENALTY = 1.5;

    // Maximum number of locations the graph supports
    private static final int MAX_LOCATIONS = 150;

    // -------------------------------------------------------------------------
    // Inner types
    // -------------------------------------------------------------------------

    /** An entry in the Dijkstra min-heap: (location index, cumulative distance). */
    private static class DijkstraNode {
        final int    index;
        final double distance;
        DijkstraNode(int index, double distance) {
            this.index    = index;
            this.distance = distance;
        }
    }

    /** A node in the adjacency linked list representing a single directed road edge. */
    private static class EdgeNode {
        final int      toIndex;   // destination location index
        final double   weight;    // effective travel time (travelTime × conditionWeight × penalty)
        EdgeNode       next;      // next edge in the linked list
        EdgeNode(int toIndex, double weight, EdgeNode next) {
            this.toIndex = toIndex;
            this.weight  = weight;
            this.next    = next;
        }
    }

    // -------------------------------------------------------------------------
    // Result type
    // -------------------------------------------------------------------------

    /**
     * The result of a shortest-path query.
     * Contains the ordered array of locationIds and the total weighted travel time.
     */
    public static class PathResult {
        public final String[] path;       // ordered locationIds from source to destination
        public final double   totalTime;  // total effective travel time (in minutes)

        public PathResult(String[] path, double totalTime) {
            this.path      = path;
            this.totalTime = totalTime;
        }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder("PathResult{ path=[");
            for (int i = 0; i < path.length; i++) {
                if (i > 0) sb.append(" -> ");
                sb.append(path[i]);
            }
            sb.append("], totalTime=")
              .append(String.format("%.2f", totalTime))
              .append(" min }");
            return sb.toString();
        }
    }

    // -------------------------------------------------------------------------
    // Cached graph state
    // -------------------------------------------------------------------------

    private String[]                        locationIds;   // index → locationId
    private String[]                        locationTypes; // index → type ("Academic" etc.)
    private EdgeNode[]                      adjList;       // index → head of edge linked list
    private CustomHashTable<String, Integer> indexMap;     // locationId → index
    private int                             locationCount;
    private boolean                         graphLoaded = false;

    // -------------------------------------------------------------------------
    // Graph loading
    // -------------------------------------------------------------------------

    /**
     * Loads the campus graph (locations and roads) from the database into
     * memory. Call once before running findShortestPath().
     *
     * @param conn an open database connection
     * @throws SQLException if a database error occurs
     */
    public void loadGraph(Connection conn) throws SQLException {
        locationIds   = new String[MAX_LOCATIONS];
        locationTypes = new String[MAX_LOCATIONS];
        adjList       = new EdgeNode[MAX_LOCATIONS];
        indexMap      = new CustomHashTable<>(10);  // capacity = max(11, 10*10) = 100
        locationCount = 0;

        // Step 1: Load all locations and build the id → index mapping
        String locSql = "SELECT locationId, type FROM locations";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(locSql)) {
            while (rs.next()) {
                String locId   = rs.getString("locationId");
                String locType = rs.getString("type");
                if (locationCount < MAX_LOCATIONS) {
                    locationIds  [locationCount] = locId;
                    locationTypes[locationCount] = locType;
                    indexMap.put(locId, locationCount);
                    locationCount++;
                }
            }
        }

        // Step 2: Load all roads and build the adjacency linked lists
        String roadSql = "SELECT fromLocationId, toLocationId, travelTime, roadConditionWeight FROM roads";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(roadSql)) {
            while (rs.next()) {
                String fromId = rs.getString("fromLocationId");
                String toId   = rs.getString("toLocationId");
                double travelTime       = rs.getDouble("travelTime");
                double conditionWeight  = rs.getDouble("roadConditionWeight");

                Integer fromIdx = indexMap.get(fromId);
                Integer toIdx   = indexMap.get(toId);
                if (fromIdx == null || toIdx == null) continue; // skip unknown locations

                double weight = travelTime * conditionWeight;

                // Rule 1: Apply 1.5x multiplier if the destination is Academic
                if ("Academic".equalsIgnoreCase(locationTypes[toIdx])) {
                    weight *= ACADEMIC_PENALTY;
                }

                // Prepend new EdgeNode to the linked list at fromIdx (O(1))
                adjList[fromIdx] = new EdgeNode(toIdx, weight, adjList[fromIdx]);
            }
        }

        graphLoaded = true;
        System.out.println("ShortestPathFinder: graph loaded with "
                + locationCount + " locations.");
    }

    // -------------------------------------------------------------------------
    // Dijkstra's Algorithm
    // -------------------------------------------------------------------------

    /**
     * Finds the shortest (lowest-weight) path between two campus locations.
     * The graph must be loaded first via {@link #loadGraph(Connection)}.
     *
     * @param startLocationId source location ID
     * @param endLocationId   destination location ID
     * @return a PathResult with path and total time, or null if unreachable
     */
    public PathResult findShortestPath(String startLocationId, String endLocationId) {
        if (!graphLoaded) {
            System.err.println("Graph not loaded. Call loadGraph(conn) first.");
            return null;
        }

        Integer startIdx = indexMap.get(startLocationId);
        Integer endIdx   = indexMap.get(endLocationId);

        if (startIdx == null) {
            System.err.println("Unknown start location: " + startLocationId);
            return null;
        }
        if (endIdx == null) {
            System.err.println("Unknown end location: " + endLocationId);
            return null;
        }
        if (startIdx.equals(endIdx)) {
            return new PathResult(new String[]{startLocationId}, 0.0);
        }

        // Initialise distance and previous-node arrays
        double[]  dist    = new double [locationCount];
        int[]     prev    = new int    [locationCount];
        boolean[] visited = new boolean[locationCount];

        for (int i = 0; i < locationCount; i++) {
            dist[i]    = Double.MAX_VALUE;
            prev[i]    = -1;
            visited[i] = false;
        }
        dist[startIdx] = 0.0;

        // Min-heap ordered by distance — implemented with our custom CustomHeap
        CustomHeap<DijkstraNode> minHeap = new CustomHeap<>(
            locationCount + 1,
            (a, b) -> Double.compare(a.distance, b.distance)   // min-heap: smaller distance first
        );
        minHeap.insert(new DijkstraNode(startIdx, 0.0));

        // Main Dijkstra loop
        while (!minHeap.isEmpty()) {
            DijkstraNode current = minHeap.extractTop();
            int u = current.index;

            if (visited[u]) continue;    // stale entry in heap — skip
            visited[u] = true;

            if (u == endIdx) break;      // destination reached — stop early

            // Traverse the adjacency linked list for node u
            EdgeNode edge = adjList[u];
            while (edge != null) {
                int    v       = edge.toIndex;
                double newDist = dist[u] + edge.weight;
                if (!visited[v] && newDist < dist[v]) {
                    dist[v] = newDist;
                    prev[v] = u;
                    minHeap.insert(new DijkstraNode(v, newDist));
                }
                edge = edge.next;
            }
        }

        // Check reachability
        if (dist[endIdx] == Double.MAX_VALUE) {
            System.err.println("No path from " + startLocationId + " to " + endLocationId);
            return null;
        }

        return new PathResult(reconstructPath(prev, startIdx, endIdx), dist[endIdx]);
    }

    /**
     * Convenience overload that loads the graph (if not already loaded)
     * using the provided connection, then runs Dijkstra.
     * Use this in single-query scenarios; prefer loadGraph() + findShortestPath()
     * in batch scenarios to avoid reloading the graph each time.
     */
    public PathResult findShortestPath(Connection conn,
                                       String startLocationId,
                                       String endLocationId) {
        try {
            if (!graphLoaded) loadGraph(conn);
        } catch (SQLException e) {
            System.err.println("Error loading graph: " + e.getMessage());
            return null;
        }
        return findShortestPath(startLocationId, endLocationId);
    }

    // -------------------------------------------------------------------------
    // Path reconstruction
    // -------------------------------------------------------------------------

    /**
     * Walks backwards through prev[] to reconstruct the shortest path as an
     * ordered array of locationIds from start to end.
     * Uses a plain int[] array as a stack — no java.util.Stack or ArrayList.
     */
    private String[] reconstructPath(int[] prev, int startIdx, int endIdx) {
        // Stack (array) to hold indices in reverse order
        int[] stack = new int[locationCount];
        int   top   = 0;
        int   cur   = endIdx;

        while (cur != startIdx && cur != -1) {
            stack[top++] = cur;
            cur = prev[cur];
        }
        if (cur == -1) return new String[0]; // no path found
        stack[top++] = startIdx;

        // Reverse the stack into the result array
        String[] path = new String[top];
        for (int i = 0; i < top; i++) {
            path[i] = locationIds[stack[top - 1 - i]];
        }
        return path;
    }

    public boolean isGraphLoaded() { return graphLoaded; }
    public int     getLocationCount() { return locationCount; }
}
