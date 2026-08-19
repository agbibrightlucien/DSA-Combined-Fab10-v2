package operations;

import database.DatabaseConnection;
import structures.BTree;
import models.ServiceRequest;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;

/**
 * Simulates how SQLite would maintain an index page structure over the
 * service_requests table, keyed by requestId (a String, e.g. "REQ001").
 * Loads every row from service_requests, sorts by requestId, and inserts
 * each row into an in-memory BTree one at a time, printing the tree
 * structure and logging exactly which insertion triggers each node split.
 *
 * This class talks to the database directly via DatabaseConnection,
 * matching the pattern already used elsewhere in the project (see
 * ServiceRequestLoader). It does not duplicate any business logic from
 * ResourceOptimizer or TicketLookupEngine; it only reads rows for the
 * purpose of building the B-tree index demonstration.
 */
public class RequestIndexSimulator {

    private static final String SELECT_ALL_SQL =
            "SELECT requestId, source, destination, category, urgency, " +
            "timeSubmitted, deadline, status FROM service_requests";

    private final BTree<String, ServiceRequest> index;

    public RequestIndexSimulator() {
        // Minimum degree 3, matching the design decision documented in BTree.java.
        this.index = new BTree<>(3);
    }

    /**
     * Loads every row from service_requests, sorts by requestId, inserts
     * each one into the BTree in order, and prints a log line every time
     * an insertion causes a node split.
     *
     * This is the method to run for Part 7's report evidence: the printed
     * output is the "which insertion triggered which split" log required
     * by the Part 7 definition of done.
     */
    public void runSimulation() {
        List<ServiceRequest> requests = loadAllServiceRequests();
        requests.sort((a, b) -> a.getRequestId().compareTo(b.getRequestId()));

        System.out.println("=== B-Tree Index Simulation over service_requests ===");
        System.out.println("Minimum degree: " + index.getMinDegree()
                + " (max " + (2 * index.getMinDegree() - 1) + " keys per node before a split)");
        System.out.println("Total requests to insert: " + requests.size());
        System.out.println();

        int insertionNumber = 0;
        for (ServiceRequest request : requests) {
            insertionNumber++;
            String requestId = request.getRequestId();

            index.insert(requestId, request);

            if (index.getLastSplitTriggerKey() != null) {
                System.out.println(">>> Insertion #" + insertionNumber
                        + " (requestId " + requestId + ") triggered SPLIT #"
                        + index.getSplitCount());
                System.out.println("--- Tree structure immediately after this split ---");
                index.printStructure();
                System.out.println();
            }
        }

        System.out.println("=== Simulation complete ===");
        System.out.println("Total insertions: " + insertionNumber);
        System.out.println("Total splits triggered: " + index.getSplitCount());
        System.out.println();
        System.out.println("=== Final tree structure ===");
        index.printStructure();
    }

    /**
     * Confirms a single lookup works against the fully-built index,
     * as a quick live demonstration alongside the split log.
     */
    public ServiceRequest lookup(String requestId) {
        return index.search(requestId);
    }

    public BTree<String, ServiceRequest> getIndex() {
        return index;
    }

    /**
     * Queries every row from service_requests using the shared
     * DatabaseConnection, in the same style as ServiceRequestLoader,
     * and builds a ServiceRequest object per row.
     */
    private List<ServiceRequest> loadAllServiceRequests() {
        List<ServiceRequest> requests = new ArrayList<>();

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(SELECT_ALL_SQL);
             ResultSet rs = stmt.executeQuery()) {

            while (rs.next()) {
                ServiceRequest request = new ServiceRequest(
                        rs.getString("requestId"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("category"),
                        rs.getString("urgency"),
                        rs.getTimestamp("timeSubmitted"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status")
                );
                requests.add(request);
            }

        } catch (SQLException e) {
            System.err.println("Database error while loading service requests for B-tree index: "
                    + e.getMessage());
            e.printStackTrace();
        }

        return requests;
    }

    public static void main(String[] args) {
        RequestIndexSimulator simulator = new RequestIndexSimulator();
        simulator.runSimulation();
    }
}
