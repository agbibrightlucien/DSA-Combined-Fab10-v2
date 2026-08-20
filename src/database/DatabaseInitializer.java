package database;

import java.sql.Connection;
import java.sql.Statement;
import java.sql.SQLException;

public class DatabaseInitializer {

    public static void createTables() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {

            // Locations table (M1 exact schema)
            String locationsTable = """
                CREATE TABLE IF NOT EXISTS locations (
                    locationId VARCHAR(50) PRIMARY KEY,
                    name VARCHAR(100),
                    area VARCHAR(50),
                    type VARCHAR(50),
                    coordinates VARCHAR(100)
                );
            """;

            // Roads table (M1 exact schema)
            String roadsTable = """
                CREATE TABLE IF NOT EXISTS roads (
                    fromLocationId VARCHAR(50),
                    toLocationId VARCHAR(50),
                    distance FLOAT,
                    travelTime INT,
                    roadConditionWeight FLOAT,
                    PRIMARY KEY (fromLocationId, toLocationId)
                );
            """;

            // Resources table (M1 exact schema)
            String resourcesTable = """
                CREATE TABLE IF NOT EXISTS resources (
                    resourceId VARCHAR(50) PRIMARY KEY,
                    type VARCHAR(50),
                    homeLocation VARCHAR(50),
                    capacity INT,
                    availabilityStatus VARCHAR(20)
                );
            """;

            // Service Requests table (M1 exact schema)
            String serviceRequestsTable = """
                CREATE TABLE IF NOT EXISTS service_requests (
                    requestId VARCHAR(50) PRIMARY KEY,
                    source VARCHAR(50),
                    destination VARCHAR(50),
                    category VARCHAR(50),
                    urgency VARCHAR(20),
                    timeSubmitted TIMESTAMP,
                    deadline TIMESTAMP,
                    status VARCHAR(20)
                );
            """;

            // Algorithm Runs table
            String algorithmRunsTable = """
                CREATE TABLE IF NOT EXISTS algorithm_runs (
                    runId INTEGER PRIMARY KEY,
                    algorithmName VARCHAR(100),
                    inputSize INTEGER,
                    timeNs BIGINT,
                    memoryKb BIGINT,
                    dateRun TIMESTAMP
                );
            """;  

            // Audit Events table
            // NOTE (Part 15 fix): column names below use snake_case to match
            // what AuditLogger.logEvent()/undoLast() and ResourceOptimizer's
            // getNextAuditEventId() already insert/query against. The
            // original camelCase columns here (eventId, eventType,
            // relatedRequestId) never matched those statements, so every
            // audit-log write was silently throwing an SQLException and the
            // undo feature never actually persisted or reversed anything.
            String auditEventsTable = """
                CREATE TABLE IF NOT EXISTS audit_events (
                    event_id INTEGER PRIMARY KEY,
                    event_type VARCHAR(50),
                    description VARCHAR(255),
                    related_request_id VARCHAR(50),
                    timestamp TIMESTAMP
                 );
            """;

            // Execute all CREATE TABLE statements
            stmt.execute(locationsTable);
            stmt.execute(roadsTable);
            stmt.execute(resourcesTable);
            stmt.execute(serviceRequestsTable);
            stmt.execute(algorithmRunsTable);
            stmt.execute(auditEventsTable);

            System.out.println("All tables created successfully.");

        } catch (SQLException e) {
            e.printStackTrace();
        }
    }
}