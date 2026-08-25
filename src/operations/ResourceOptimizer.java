package operations;

import database.DatabaseConnection;
import models.AuditEvent;
import models.Resource;
import models.ServiceRequest;
import structures.CustomHeap;
import structures.CustomIterator;
import structures.CustomLinkedList;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Operation 4: Resource Allocation Optimization — Greedy Algorithm.
 *
 * Processes all PENDING service requests in descending priority order
 * (using UrgentDispatcher's max-heap) and greedily assigns the nearest
 * compatible AVAILABLE resource to each one.
 *
 * Requests that cannot be assigned because no compatible resource is free
 * are marked QUEUED in the database.
 *
 * Custom DSA used:
 *   - CustomHeap<ServiceRequest>  via UrgentDispatcher (max-heap)
 *   - ShortestPathFinder          for travel-time distance between resource and destination
 *   - CustomLinkedList<String>    for tracking in-memory assignment history with CustomIterator
 *   - Plain arrays                for loading PENDING requests and AVAILABLE resources
 *     (count-then-load pattern avoids java.util.ArrayList)
 *   - boolean[] busyInSession     to track in-session resource availability
 *
 * Known limitation - greedy sub-optimality (documented per Section 7):
 * Always giving the CURRENT top-priority request its nearest compatible
 * resource can leave a worse total outcome than a different order would.
 */
public class ResourceOptimizer {

    private final UrgentDispatcher dispatcher = new UrgentDispatcher();

    // Audit logger records every status change made by this optimizer.
    private final AuditLogger auditLogger = new AuditLogger();

    // In-memory assignment history tracked via CustomLinkedList (Part 3 requirement)
    private final CustomLinkedList<String> assignmentHistory = new CustomLinkedList<>();

    public CustomLinkedList<String> getAssignmentHistory() {
        return assignmentHistory;
    }

    /**
     * Exposes this optimizer's AuditLogger so callers (e.g. the Part 15 UI's
     * undo button) can call undoLast() against the exact CustomStack that
     * recorded this run's status changes. Added for Part 15 wiring; no
     * change to existing optimizeAllocations() behaviour.
     */
    public AuditLogger getAuditLogger() {
        return auditLogger;
    }


    // -------------------------------------------------------------------------
    // Main optimisation entry point
    // -------------------------------------------------------------------------

    /**
     * Loads PENDING requests and AVAILABLE resources from the database,
     * then greedily assigns the nearest compatible resource to each request
     * in priority order. DB statuses are updated in-place and audit events
     * are recorded for each status change.
     */
    public void optimizeAllocations() {
        assignmentHistory.clear();
        try (Connection conn = DatabaseConnection.connect()) {

            // 1. Load data from DB into plain arrays
            ServiceRequest[] pending = loadPendingRequests(conn);
            Resource[] available = loadAvailableResources(conn);

            if (pending.length == 0) {
                System.out.println("ResourceOptimizer: no PENDING requests found.");
                return;
            }

            // 2. Load the campus graph once
            ShortestPathFinder pathFinder = new ShortestPathFinder();
            pathFinder.loadGraph(conn);

            // 3. Build the max-heap of pending requests by priority score
            CustomHeap<ServiceRequest> priorityQueue =
                    dispatcher.buildMaxHeap(pending);

            // 4. Track which resources become busy during this session
            boolean[] busyInSession = new boolean[available.length];
            int assigned = 0;
            int queued = 0;

            // 5. Greedy loop: process requests in priority order
            while (!priorityQueue.isEmpty()) {
                ServiceRequest request = priorityQueue.extractTop();

                int bestIdx = -1;
                double bestTime = Double.MAX_VALUE;

                // Find the nearest compatible AVAILABLE resource
                for (int i = 0; i < available.length; i++) {

                    if (busyInSession[i]) {
                        continue;
                    }

                    if (!isCompatible(
                            available[i].getType(),
                            request.getCategory())) {
                        continue;
                    }

                    ShortestPathFinder.PathResult result =
                            pathFinder.findShortestPath(
                                    available[i].getHomeLocation(),
                                    request.getDestination());

                    if (result != null && result.totalTime < bestTime) {
                        bestTime = result.totalTime;
                        bestIdx = i;
                    }
                }

                if (bestIdx != -1) {

                    // ---------------------------------------------------------
                    // Assign the nearest resource
                    // ---------------------------------------------------------

                    busyInSession[bestIdx] = true;

                    // Change request status
                    updateRequestStatus(
                            conn,
                            request.getRequestId(),
                            "ASSIGNED");

                    // Record request status change
                    auditLogger.logEvent(new AuditEvent(
                            getNextAuditEventId(conn),
                            "REQUEST_STATUS_CHANGE",
                            "PENDING -> ASSIGNED",
                            request.getRequestId(),
                            java.time.LocalDateTime.now().toString()
                    ));

                    // Change resource status
                    updateResourceStatus(
                            conn,
                            available[bestIdx].getResourceId(),
                            "BUSY");

                    // Record resource status change
                    auditLogger.logEvent(new AuditEvent(
                            getNextAuditEventId(conn),
                            "RESOURCE_STATUS_CHANGE",
                            available[bestIdx].getResourceId()
                                    + " AVAILABLE -> BUSY",
                            request.getRequestId(),
                            java.time.LocalDateTime.now().toString()
                    ));

                    String srcName = pathFinder.getFormattedLocation(available[bestIdx].getHomeLocation());
                    String dstName = pathFinder.getFormattedLocation(request.getDestination());

                    String assignMsg = "ASSIGNED Resource: " + available[bestIdx].getResourceId()
                            + " (" + available[bestIdx].getType() + ", Home: " + srcName + ")"
                            + "  ->  Request: " + request.getRequestId()
                            + " (" + request.getCategory() + " at " + dstName + ")"
                            + " | Travel Time: " + String.format("%.2f", bestTime) + " min";
                    assignmentHistory.addLast(assignMsg);
                    System.out.println(assignMsg);

                    assigned++;

                } else {

                    // ---------------------------------------------------------
                    // No compatible resource available — queue the request
                    // ---------------------------------------------------------

                    updateRequestStatus(
                            conn,
                            request.getRequestId(),
                            "QUEUED");

                    // Record request status change
                    auditLogger.logEvent(new AuditEvent(
                            getNextAuditEventId(conn),
                            "REQUEST_STATUS_CHANGE",
                            "PENDING -> QUEUED",
                            request.getRequestId(),
                            java.time.LocalDateTime.now().toString()
                    ));

                    String queueMsg = "QUEUED   "
                            + request.getRequestId()
                            + " (" + request.getCategory()
                            + " / " + request.getUrgency() + ")"
                            + " — no compatible resource available";
                    assignmentHistory.addLast(queueMsg);
                    System.out.println(queueMsg);

                    queued++;
                }
            }

            System.out.println(
                    "\nResourceOptimizer complete: "
                            + assigned
                            + " assigned, "
                            + queued
                            + " queued.");

            // ---------------------------------------------------------
            // Part 3 Demonstration: Iterate through CustomLinkedList history
            // ---------------------------------------------------------
            System.out.println("\n=== In-Memory Assignment History (CustomLinkedList & CustomIterator) ===");
            CustomIterator<String> it = assignmentHistory.customIterator();
            while (it.hasNext()) {
                System.out.println("  " + it.next());
            }

        } catch (SQLException e) {
            System.err.println(
                    "ResourceOptimizer error: " + e.getMessage());
            e.printStackTrace();
        }
    }

    // -------------------------------------------------------------------------
    // Compatibility check
    // -------------------------------------------------------------------------

    /**
     * Determines whether a resource type is compatible with a service
     * request category.
     * Matching is case-insensitive and uses substring checks for flexibility.
     */
    private boolean isCompatible(
            String resourceType,
            String requestCategory) {

        if (resourceType == null || requestCategory == null) {
            return false;
        }

        String rt = resourceType.toLowerCase();
        String cat = requestCategory.toUpperCase();

        switch (cat) {

            case "IT_SUPPORT":
                return rt.contains("it") || rt.contains("tech");

            case "TRANSPORT":
                return rt.contains("shuttle")
                        || rt.contains("transport");

            case "MAINTENANCE":
                return rt.contains("maintenance")
                        || rt.contains("electrician")
                        || rt.contains("plumber")
                        || rt.contains("cleaning");

            case "EMERGENCY":
                return rt.contains("ambulance")
                        || rt.contains("security")
                        || rt.contains("patrol");

            default:
                return true;
        }
    }

    // -------------------------------------------------------------------------
    // Database helpers — count-then-load pattern
    // -------------------------------------------------------------------------

    private ServiceRequest[] loadPendingRequests(
            Connection conn) throws SQLException {

        int count = 0;

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM service_requests WHERE status='PENDING'")) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        }

        ServiceRequest[] arr = new ServiceRequest[count];
        int i = 0;

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT * FROM service_requests WHERE status='PENDING'")) {

            while (rs.next() && i < count) {

                arr[i++] = new ServiceRequest(
                        rs.getString("requestId"),
                        rs.getString("source"),
                        rs.getString("destination"),
                        rs.getString("category"),
                        rs.getString("urgency"),
                        rs.getTimestamp("timeSubmitted"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"));
            }
        }

        return arr;
    }

    private Resource[] loadAvailableResources(
            Connection conn) throws SQLException {

        int count = 0;

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT COUNT(*) FROM resources WHERE availabilityStatus='AVAILABLE'")) {

            if (rs.next()) {
                count = rs.getInt(1);
            }
        }

        Resource[] arr = new Resource[count];
        int i = 0;

        try (Statement s = conn.createStatement();
             ResultSet rs = s.executeQuery(
                     "SELECT * FROM resources WHERE availabilityStatus='AVAILABLE'")) {

            while (rs.next() && i < count) {

                arr[i++] = new Resource(
                        rs.getString("resourceId"),
                        rs.getString("type"),
                        rs.getString("homeLocation"),
                        rs.getInt("capacity"),
                        rs.getString("availabilityStatus"));
            }
        }

        return arr;
    }

    // -------------------------------------------------------------------------
    // Status update methods
    // -------------------------------------------------------------------------

    private void updateRequestStatus(
            Connection conn,
            String requestId,
            String status) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE service_requests SET status = ? WHERE requestId = ?")) {

            ps.setString(1, status);
            ps.setString(2, requestId);

            ps.executeUpdate();
        }
    }

    private void updateResourceStatus(
            Connection conn,
            String resourceId,
            String status) throws SQLException {

        try (PreparedStatement ps = conn.prepareStatement(
                "UPDATE resources SET availabilityStatus = ? WHERE resourceId = ?")) {

            ps.setString(1, status);
            ps.setString(2, resourceId);

            ps.executeUpdate();
        }
    }

    // -------------------------------------------------------------------------
    // Audit event ID helper
    // -------------------------------------------------------------------------

    /**
     * Generates the next audit event ID.
     */
    private int getNextAuditEventId(Connection conn)
            throws SQLException {

        String sql =
                "SELECT COALESCE(MAX(event_id), 0) + 1 FROM audit_events";

        try (Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql)) {

            if (rs.next()) {
                return rs.getInt(1);
            }
        }

        return 1;
    }
} 