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
 * Part 13 correctness evidence for Prim's and Kruskal's MST algorithms
 * (Part 11). Reuses the exact 5-node subgraph hand-traced in
 * docs/performance_results/CorrectnessEvidence.md (S, A, B, C, D), so this
 * test's expected total cost of 7 is independently cross-checked against
 * that hand-worked trace table.
 */
class MSTTest {

    private Connection conn;

    @AfterEach
    void closeConnection() throws SQLException {
        if (conn != null) conn.close();
    }

    /**
     * S-A=4, S-B=1, B-A=2, B-C=5, A-C=1, C-D=3, B-D=8 -- inserted in both
     * directions so Prim/Kruskal see a proper undirected graph, matching
     * MST's own class Javadoc note on directionality.
     */
    private Graph buildFiveNodeGraph() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE locations (locationId VARCHAR(50) PRIMARY KEY, name VARCHAR(100), area VARCHAR(50), type VARCHAR(50), coordinates VARCHAR(100))");
            stmt.execute("CREATE TABLE roads (fromLocationId VARCHAR(50), toLocationId VARCHAR(50), distance FLOAT, travelTime INT, roadConditionWeight FLOAT, PRIMARY KEY (fromLocationId, toLocationId))");

            for (String id : new String[]{"S", "A", "B", "C", "D"}) {
                stmt.execute("INSERT INTO locations VALUES ('" + id + "','Node " + id + "','Central','HUB','0,0')");
            }

            insertBidirectional(stmt, "S", "A", 4);
            insertBidirectional(stmt, "S", "B", 1);
            insertBidirectional(stmt, "B", "A", 2);
            insertBidirectional(stmt, "B", "C", 5);
            insertBidirectional(stmt, "A", "C", 1);
            insertBidirectional(stmt, "C", "D", 3);
            insertBidirectional(stmt, "B", "D", 8);
        }
        Graph graph = new Graph();
        graph.loadGraph(conn);
        return graph;
    }

    private void insertBidirectional(Statement stmt, String from, String to, int weight) throws SQLException {
        stmt.execute("INSERT INTO roads VALUES ('" + from + "','" + to + "'," + weight + ".0," + weight + ",1.0)");
        stmt.execute("INSERT INTO roads VALUES ('" + to + "','" + from + "'," + weight + ".0," + weight + ",1.0)");
    }

    @Test
    void normal_primFromS_producesMinimumSpanningTreeOfCost7() throws SQLException {
        Graph graph = buildFiveNodeGraph();
        MST.MSTResult result = MST.prim(graph, graph.getIndexForLocationId("S"));
        assertEquals(4, result.getEdges().length);
        assertEquals(7.0, result.getTotalCost(), 0.001);
    }

    @Test
    void normal_kruskal_producesMinimumSpanningTreeOfCost7() throws SQLException {
        Graph graph = buildFiveNodeGraph();
        MST.MSTResult result = MST.kruskal(graph);
        assertEquals(4, result.getEdges().length);
        assertEquals(7.0, result.getTotalCost(), 0.001);
    }

    @Test
    void primAndKruskal_onSameGraph_produceEqualTotalCost() throws SQLException {
        Graph graph = buildFiveNodeGraph();
        MST.MSTResult primResult = MST.prim(graph, graph.getIndexForLocationId("S"));
        MST.MSTResult kruskalResult = MST.kruskal(graph);
        assertEquals(primResult.getTotalCost(), kruskalResult.getTotalCost(), 0.001);
    }

    @Test
    void edgeCase_disconnectedGraph_kruskalReturnsSpanningForestNotFullTree() throws SQLException {
        conn = DriverManager.getConnection("jdbc:sqlite::memory:");
        try (Statement stmt = conn.createStatement()) {
            stmt.execute("CREATE TABLE locations (locationId VARCHAR(50) PRIMARY KEY, name VARCHAR(100), area VARCHAR(50), type VARCHAR(50), coordinates VARCHAR(100))");
            stmt.execute("CREATE TABLE roads (fromLocationId VARCHAR(50), toLocationId VARCHAR(50), distance FLOAT, travelTime INT, roadConditionWeight FLOAT, PRIMARY KEY (fromLocationId, toLocationId))");
            stmt.execute("INSERT INTO locations VALUES ('X','Node X','Central','HUB','0,0')");
            stmt.execute("INSERT INTO locations VALUES ('Y','Node Y','Central','HUB','0,1')");
            stmt.execute("INSERT INTO locations VALUES ('Z','Node Z (isolated)','Remote','HUB','9,9')");
            insertBidirectional(stmt, "X", "Y", 2);
            // Z has no roads at all -- disconnected component.
        }
        Graph graph = new Graph();
        graph.loadGraph(conn);

        MST.MSTResult result = MST.kruskal(graph);
        // 3 nodes would need 2 edges for a full spanning tree; Z is unreachable,
        // so only the X-Y edge can be accepted.
        assertEquals(1, result.getEdges().length);
        assertEquals(2.0, result.getTotalCost(), 0.001);
    }
}
