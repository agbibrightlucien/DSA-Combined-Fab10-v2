package operations;

import database.DatabaseConnection;
import models.AuditEvent;
import structures.CustomStack;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Handles audit logging using a CustomStack of AuditEvent objects.
 *
 * Each logged event is pushed onto the stack and persisted
 * in the audit_events database table.
 */
public class AuditLogger {

    private final CustomStack<AuditEvent> auditStack;

    /**
     * Creates an empty AuditLogger.
     */
    public AuditLogger() {
        auditStack = new CustomStack<>();
    }

    /**
     * Logs an audit event by pushing it onto the stack
     * and inserting it into the audit_events table.
     *
     * @param event the audit event to log
     */
    public void logEvent(AuditEvent event) {
        if (event == null) {
            return;
        }

        auditStack.push(event);

        String sql = """
                INSERT INTO audit_events
                (event_id, event_type, description, related_request_id, timestamp)
                VALUES (?, ?, ?, ?, ?)
                """;

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, event.getEventId());
            stmt.setString(2, event.getEventType());
            stmt.setString(3, event.getDescription());
            stmt.setString(4, event.getRelatedRequestId());
            stmt.setString(5, event.getTimestamp());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                    "Failed to save audit event: " + e.getMessage());
        }
    }

    /**
     * Returns the most recent audit event without removing it.
     */
    public AuditEvent peek() {
        return auditStack.peek();
    }

    /**
     * Returns true if there are no audit events.
     */
    public boolean isEmpty() {
        return auditStack.isEmpty();
    }

    /**
     * Removes the most recent audit event and attempts
     * to reverse the corresponding database status change.
     */
    public AuditEvent undoLast() {

        AuditEvent event = auditStack.pop();

        if (event == null) {
            return null;
        }

        reverseDatabaseChange(event);
        removeAuditRecord(event);

        return event;
    }

    /**
     * Reverses the database change represented by the audit event.
     */
    private void reverseDatabaseChange(AuditEvent event) {

        String type = event.getEventType();
        String description = event.getDescription();

        try (Connection conn = DatabaseConnection.connect()) {

            if ("REQUEST_STATUS_CHANGE".equals(type)) {

                String previousStatus = "PENDING";

                if (description.contains("PENDING -> ASSIGNED")
                        || description.contains("PENDING -> QUEUED")) {

                    String sql =
                            "UPDATE service_requests "
                            + "SET status = ? "
                            + "WHERE requestId = ?";

                    try (PreparedStatement stmt =
                                 conn.prepareStatement(sql)) {

                        stmt.setString(1, previousStatus);
                        stmt.setString(2, event.getRelatedRequestId());

                        stmt.executeUpdate();
                    }
                }

            } else if ("RESOURCE_STATUS_CHANGE".equals(type)) {

                String resourceId = extractResourceId(description);

                if (resourceId != null) {

                    String sql =
                            "UPDATE resources "
                            + "SET availabilityStatus = ? "
                            + "WHERE resourceId = ?";

                    try (PreparedStatement stmt =
                                 conn.prepareStatement(sql)) {

                        stmt.setString(1, "AVAILABLE");
                        stmt.setString(2, resourceId);

                        stmt.executeUpdate();
                    }
                }
            }

        } catch (SQLException e) {
            System.out.println(
                    "Failed to reverse audit event: " + e.getMessage());
        }
    }

    /**
     * Extracts the resource ID from:
     *
     * RESOURCE_ID AVAILABLE -> BUSY
     */
    private String extractResourceId(String description) {

        if (description == null) {
            return null;
        }

        String marker = " AVAILABLE -> BUSY";

        int index = description.indexOf(marker);

        if (index > 0) {
            return description.substring(0, index);
        }

        return null;
    }

    /**
     * Removes the corresponding audit record from the database.
     */
    private void removeAuditRecord(AuditEvent event) {

        String sql =
                "DELETE FROM audit_events WHERE event_id = ?";

        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            stmt.setInt(1, event.getEventId());

            stmt.executeUpdate();

        } catch (SQLException e) {
            System.out.println(
                    "Failed to remove audit event: " + e.getMessage());
        }
    }
}