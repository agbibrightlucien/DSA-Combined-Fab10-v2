package algorithms;

import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.AfterEach;
import static org.junit.jupiter.api.Assertions.*;

import structures.Graph;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Part 13 correctness evidence for BFS/DFS graph traversal (Part 11).
 *
 * Uses a private, in-memory SQLite connection seeded with a small
 * hand-checkable road network -- exactly the shape described in
 * GraphTraversal's own class Javadoc -- so these tests never touch the
 * real campus_hub.db and can run standalone.
 */
class GraphTraversalTest {

    private Connection conn;

    @AfterEach
    void closeConnection() throws SQLException {
        if (conn != null) conn.close();
    }

    /**
     * A(0) -> B(1) -> C(2), plus an isolated D(3) with no roads at all.
     * Matches the manual verification example in GraphTraversal's class Javadoc.
     */
    private Graph buildLinearGraphWithIsolatedNode() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE locations (locationId VARCHAR(50) PRIMARY KEY, name VARCHAR(100), area VARCHAR(50), type VARCHAR(50), coordinates VARCHAR(100))");
            stmt.execute("CREATE TABLE roads (fromLocationId VARCHAR(50), toLocationId VARCHAR(50), distance FLOAT, travelTime INT, roadConditionWeight FLOAT, PRIMARY KEY (fromLocationId, toLocationId))");

            stmt.execute("INSERT INTO locations VALUES ('A','Location A','Central','HUB','0,0')");
            stmt.execute("INSERT INTO locations VALUES ('B','Location B','Central','HUB','0,1')");
            stmt.execute("INSERT INTO locations VALUES ('C','Location C','Central','HUB','0,2')");
            stmt.execute("INSERT INTO locations VALUES ('D','Location D','Remote','HUB','9,9')");

            stmt.execute("INSERT INTO roads VALUES ('A','B',1.0,5,1.0)");
            stmt.execute("INSERT INTO roads VALUES ('B','C',1.0,5,1.0)");
        }
        Graph graph = new Graph();
        graph.loadGraph(conn);
        return graph;
    }

    @Test
    void normal_bfsFromA_visitsAllReachableInLevelOrder() throws SQLException {
        Graph graph = buildLinearGraphWithIsolatedNode();
        int[] order = GraphTraversal.bfs(graph, graph.getIndexForLocationId("A"));
        assertArrayEquals(new int[]{
                graph.getIndexForLocationId("A"),
                graph.getIndexForLocationId("B"),
                graph.getIndexForLocationId("C")
        }, order);
    }

    @Test
    void normal_dfsFromA_visitsAllReachableDepthFirst() throws SQLException {
        Graph graph = buildLinearGraphWithIsolatedNode();
        int[] order = GraphTraversal.dfs(graph, graph.getIndexForLocationId("A"));
        assertArrayEquals(new int[]{
                graph.getIndexForLocationId("A"),
                graph.getIndexForLocationId("B"),
                graph.getIndexForLocationId("C")
        }, order);
    }

    @Test
    void edgeCase_isolatedLocationWithNoRoads_reachableFromReturnsOnlyItself() throws SQLException {
        Graph graph = buildLinearGraphWithIsolatedNode();
        String[] reachable = GraphTraversal.reachableFrom(graph, "D");
        assertArrayEquals(new String[]{"D"}, reachable);
    }

    @Test
    void invalidPrecondition_unknownLocationId_throwsIllegalArgumentException() throws SQLException {
        Graph graph = buildLinearGraphWithIsolatedNode();
        assertThrows(IllegalArgumentException.class, () -> GraphTraversal.reachableFrom(graph, "DOES-NOT-EXIST"));
    }
}
