package structures;

import database.DatabaseConnection;
import models.Location;
import models.Roads;
import operations.CustomHashTable;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * General purpose campus road-network graph, holding BOTH an adjacency
 * list and an adjacency matrix over the same set of locations, satisfying
 * the Section 6 requirement that the graph be represented both ways.
 *
 * This class is deliberately separate from the private, Dijkstra-specific
 * structures inside {@link operations.ShortestPathFinder}. That class
 * stays untouched and keeps working exactly as it does today. Graph is
 * the new shared source of truth that Part 9 (BFS/DFS), Part 10 (Prim,
 * via the matrix) and Part 11 (Kruskal, via the edge list + DisjointSet)
 * all read from, so every traversal and MST algorithm in this project
 * works over one consistent view of the campus map.
 *
 * Data source and loading:
 *   - Locations are read from the same `locations` table ShortestPathFinder
 *     reads from, and stored as {@link models.Location} objects.
 *   - Roads are read from the same `roads` table, and stored as
 *     {@link models.Roads} objects while building the edge weights.
 *   - The id -> index mapping uses {@link operations.CustomHashTable}, the
 *     same custom hash table ShortestPathFinder already uses, so lookups
 *     stay O(1) average case with no java.util collections involved.
 *
 * Directionality:
 *   The `roads` table's primary key is the composite (fromLocationId,
 *   toLocationId), and each row is loaded as exactly one directed edge
 *   from -> to, matching ShortestPathFinder's own loading behaviour. If the
 *   seed data contains a row in both directions between two locations, the
 *   graph is effectively bidirectional there; if only one direction is
 *   seeded, that road is one-way in this graph. Graph does not invent a
 *   reverse edge that is not present in the table.
 *
 * Edge weight:
 *   weight = travelTime * roadConditionWeight
 *   This is the same base formula ShortestPathFinder computes before it
 *   applies its Rule 1 Academic-destination traffic penalty. That 1.5x
 *   penalty is a Dijkstra-specific business rule, not a property of the
 *   road network itself, so it is intentionally left out of this shared
 *   Graph class. Algorithms that need the penalised weight (i.e. only
 *   ShortestPathFinder) keep computing it themselves, exactly as they do
 *   today.
 */
public class Graph {

    // Maximum number of locations supported while loading (mirrors the
    // same style and limit ShortestPathFinder uses).
    private static final int MAX_LOCATIONS = 150;

    /** A single directed edge, used by getAllEdges() for Kruskal in Part 11. */
    public static class Edge {
        public final int    fromIndex;
        public final int    toIndex;
        public final double weight;

        public Edge(int fromIndex, int toIndex, double weight) {
            this.fromIndex = fromIndex;
            this.toIndex   = toIndex;
            this.weight    = weight;
        }

        @Override
        public String toString() {
            return "Edge{" + fromIndex + " -> " + toIndex + ", weight=" +
                    String.format("%.2f", weight) + "}";
        }
    }

    /** A node in the adjacency linked list, mirroring ShortestPathFinder's EdgeNode. */
    private static class EdgeNode {
        final int      toIndex;
        final double   weight;
        EdgeNode       next;

        EdgeNode(int toIndex, double weight, EdgeNode next) {
            this.toIndex = toIndex;
            this.weight  = weight;
            this.next    = next;
        }
    }

    private Location[]                      locations;   // index -> Location
    private EdgeNode[]                      adjList;     // index -> head of edge linked list
    private double[][]                      matrix;      // [from][to] -> weight, or NO_EDGE
    private CustomHashTable<String, Integer> indexMap;    // locationId -> index
    private int                             locationCount;
    private int                             edgeCount;
    private boolean                         graphLoaded = false;

    /** Sentinel used in the matrix where no direct road exists between two locations. */
    public static final double NO_EDGE = Double.POSITIVE_INFINITY;

    

    /**
     * Loads all locations and roads from the database, building the
     * adjacency list and adjacency matrix together. Opens and closes its
     * own connection via DatabaseConnection.connect(), the same helper
     * used across the rest of the project.
     */
    public void loadGraph() throws SQLException {
        Connection conn = DatabaseConnection.connect();
        if (conn == null) {
            throw new SQLException("Graph.loadGraph(): could not obtain a database connection.");
        }
        try {
            loadGraph(conn);
        } finally {
            conn.close();
        }
    }

    /**
     * Loads all locations and roads using an already-open connection.
     * Useful when the caller wants to reuse one connection across several
     * loads (e.g. BenchmarkRunner in Part 14).
     */
    public void loadGraph(Connection conn) throws SQLException {
        Location[] tempLocations = new Location[MAX_LOCATIONS];
        indexMap      = new CustomHashTable<>(10);
        locationCount = 0;

        // Step 1: load all locations, same table ShortestPathFinder reads from
        String locSql = "SELECT locationId, name, area, type, coordinates FROM locations";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(locSql)) {
            while (rs.next()) {
                if (locationCount >= MAX_LOCATIONS) break;

                Location loc = new Location(
                        rs.getString("locationId"),
                        rs.getString("name"),
                        rs.getString("area"),
                        rs.getString("type"),
                        rs.getString("coordinates")
                );
                tempLocations[locationCount] = loc;
                indexMap.put(loc.getLocationId(), locationCount);
                locationCount++;
            }
        }

        // Trim locations array and set up the adjacency list + matrix now that
        // the exact location count is known.
        locations = new Location[locationCount];
        System.arraycopy(tempLocations, 0, locations, 0, locationCount);

        adjList = new EdgeNode[locationCount];
        matrix  = new double[locationCount][locationCount];
        for (int i = 0; i < locationCount; i++) {
            for (int j = 0; j < locationCount; j++) {
                matrix[i][j] = (i == j) ? 0.0 : NO_EDGE;
            }
        }

        // Step 2: load all roads, same table ShortestPathFinder reads from
        edgeCount = 0;
        String roadSql = "SELECT fromLocationId, toLocationId, distance, travelTime, roadConditionWeight FROM roads";
        try (Statement stmt = conn.createStatement();
             ResultSet rs   = stmt.executeQuery(roadSql)) {
            while (rs.next()) {
                Roads road = new Roads(
                        rs.getString("fromLocationId"),
                        rs.getString("toLocationId"),
                        rs.getFloat("distance"),
                        rs.getInt("travelTime"),
                        rs.getFloat("roadConditionWeight")
                );

                Integer fromIdx = indexMap.get(road.getFromLocationId());
                Integer toIdx   = indexMap.get(road.getToLocationId());
                if (fromIdx == null || toIdx == null) continue; // skip unknown locations

                double weight = road.getTravelTime() * road.getRoadConditionWeight();

                // Adjacency list: prepend, same O(1) style as ShortestPathFinder
                adjList[fromIdx] = new EdgeNode(toIdx, weight, adjList[fromIdx]);

                // Adjacency matrix: direct lookup cell
                matrix[fromIdx][toIdx] = weight;

                edgeCount++;
            }
        }

        graphLoaded = true;
        System.out.println("Graph: loaded " + locationCount + " locations and "
                + edgeCount + " roads (adjacency list + adjacency matrix built).");
    }

   

    public boolean isGraphLoaded() { return graphLoaded; }
    public int     getLocationCount() { return locationCount; }
    public int     getEdgeCount() { return edgeCount; }

    public Location getLocationAt(int index) {
        validateIndex(index);
        return locations[index];
    }

    public String getLocationIdAt(int index) {
        return getLocationAt(index).getLocationId();
    }

    /** Returns the index for a locationId, or null if it is not in this graph. */
    public Integer getIndexForLocationId(String locationId) {
        return indexMap.get(locationId);
    }

    /**
     * Returns the indices of every location directly reachable from index
     * by one road, in the order they appear in the adjacency list (most
     * recently loaded road first, since the list is built by prepending).
     * Used by BFS/DFS in Part 11.
     */
    public int[] getNeighbors(int index) {
        validateIndex(index);
        int count = 0;
        for (EdgeNode e = adjList[index]; e != null; e = e.next) count++;

        int[] neighbors = new int[count];
        int i = 0;
        for (EdgeNode e = adjList[index]; e != null; e = e.next) {
            neighbors[i++] = e.toIndex;
        }
        return neighbors;
    }

    /** Returns the direct-road weight from `from` to `to`, or NO_EDGE if there is none. */
    public double getEdgeWeight(int from, int to) {
        validateIndex(from);
        validateIndex(to);
        return matrix[from][to];
    }

    /** Returns the full adjacency matrix. Callers should not mutate the returned array. */
    public double[][] getAdjacencyMatrix() {
        return matrix;
    }

    /**
     * Returns every directed edge in the graph as a flat array, for Kruskal
     * in Part 11 to sort by weight (using SortAlgorithms from Part 10) and
     * walk in order with DisjointSet.
     */
    public Edge[] getAllEdges() {
        Edge[] edges = new Edge[edgeCount];
        int i = 0;
        for (int from = 0; from < locationCount; from++) {
            for (EdgeNode e = adjList[from]; e != null; e = e.next) {
                edges[i++] = new Edge(from, e.toIndex, e.weight);
            }
        }
        return edges;
    }

    

    /** Prints the adjacency list representation, one line per location. */
    public void printAdjacencyList() {
        System.out.println("Adjacency List (" + locationCount + " locations, " + edgeCount + " edges):");
        for (int i = 0; i < locationCount; i++) {
            StringBuilder sb = new StringBuilder();
            sb.append(locations[i].getLocationId()).append(" -> ");
            EdgeNode e = adjList[i];
            if (e == null) {
                sb.append("(no outgoing roads)");
            }
            boolean first = true;
            while (e != null) {
                if (!first) sb.append(", ");
                sb.append(locations[e.toIndex].getLocationId())
                  .append(" [").append(String.format("%.2f", e.weight)).append("]");
                first = false;
                e = e.next;
            }
            System.out.println(sb);
        }
    }

    /** Prints the adjacency matrix representation as a labelled grid. */
    public void printAdjacencyMatrix() {
        System.out.println("Adjacency Matrix (" + locationCount + " x " + locationCount + "):");
        System.out.print(String.format("%-10s", ""));
        for (int j = 0; j < locationCount; j++) {
            System.out.print(String.format("%-10s", locations[j].getLocationId()));
        }
        System.out.println();

        for (int i = 0; i < locationCount; i++) {
            System.out.print(String.format("%-10s", locations[i].getLocationId()));
            for (int j = 0; j < locationCount; j++) {
                String cell = (matrix[i][j] == NO_EDGE) ? "-" : String.format("%.1f", matrix[i][j]);
                System.out.print(String.format("%-10s", cell));
            }
            System.out.println();
        }
    }

    private void validateIndex(int index) {
        if (!graphLoaded) {
            throw new IllegalStateException("Graph has not been loaded; call loadGraph() first.");
        }
        if (index < 0 || index >= locationCount) {
            throw new IndexOutOfBoundsException(
                "Location index " + index + " is out of range for a graph of size " + locationCount);
        }
    }
}