package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;

/**
 * Reads ServiceRequests.csv line by line and inserts each row into the
 * service_requests table in SQLite using a PreparedStatement.
 */
public class ServiceRequestLoader {

    // TODO: adjust this to match the actual SQLite DB file path used elsewhere
    // in the project (e.g. wherever Main.java or other loaders connect to).
    private static final String DB_URL = "jdbc:sqlite:src/database/optimizer.db";

    private static final String INSERT_SQL =
            "INSERT INTO service_requests " +
            "(requestId, source, destination, category, urgency, timeSubmitted, deadline, status) " +
            "VALUES (?, ?, ?, ?, ?, ?, ?, ?)";

    /**
     * Reads the CSV file at csvFilePath and inserts every data row into
     * the service_requests table.
     *
     * @param csvFilePath path to ServiceRequests.csv
     */
    public void loadData(String csvFilePath) {
        try (Connection conn = DriverManager.getConnection(DB_URL);
             BufferedReader reader = new BufferedReader(new FileReader(csvFilePath))) {

            String line;
            boolean isHeader = true;
            int insertedCount = 0;
            int skippedCount = 0;

            try (PreparedStatement stmt = conn.prepareStatement(INSERT_SQL)) {
                while ((line = reader.readLine()) != null) {
                    if (isHeader) {
                        // Skip the header row (requestId,source,destination,...)
                        isHeader = false;
                        continue;
                    }
                    if (line.trim().isEmpty()) {
                        continue;
                    }

                    String[] fields = line.split(",", -1);
                    if (fields.length != 8) {
                        System.err.println("Skipping malformed row (expected 8 fields, got "
                                + fields.length + "): " + line);
                        skippedCount++;
                        continue;
                    }

                    try {
                        String requestId = fields[0].trim();
                        String source = fields[1].trim();
                        String destination = fields[2].trim();
                        String category = fields[3].trim();
                        String urgency = fields[4].trim();
                        Timestamp timeSubmitted = parseTimestamp(fields[5].trim());
                        Timestamp deadline = parseTimestamp(fields[6].trim());
                        String status = fields[7].trim();

                        stmt.setString(1, requestId);
                        stmt.setString(2, source);
                        stmt.setString(3, destination);
                        stmt.setString(4, category);
                        stmt.setString(5, urgency);
                        stmt.setTimestamp(6, timeSubmitted);
                        stmt.setTimestamp(7, deadline);
                        stmt.setString(8, status);

                        stmt.executeUpdate();
                        insertedCount++;
                    } catch (SQLException | IllegalArgumentException rowEx) {
                        System.err.println("Skipping row due to error: " + line
                                + " -> " + rowEx.getMessage());
                        skippedCount++;
                    }
                }
            }

            System.out.println("ServiceRequestLoader: inserted " + insertedCount
                    + " row(s), skipped " + skippedCount + " row(s).");

        } catch (SQLException e) {
            System.err.println("Database error while loading service requests: " + e.getMessage());
            e.printStackTrace();
        } catch (IOException e) {
            System.err.println("Error reading CSV file at " + csvFilePath + ": " + e.getMessage());
            e.printStackTrace();
        }
    }

    /**
     * Converts an ISO 8601 timestamp string (YYYY-MM-DDTHH:MM:SS) from the
     * CSV into a java.sql.Timestamp, which expects "YYYY-MM-DD HH:MM:SS".
     */
    private Timestamp parseTimestamp(String isoTimestamp) {
        String normalized = isoTimestamp.replace("T", " ");
        return Timestamp.valueOf(normalized);
    }

    public static void main(String[] args) {
        String csvPath = (args.length > 0) ? args[0]
                : "src/main/resources/seed_data/ServiceRequests.csv";
        new ServiceRequestLoader().loadData(csvPath);
    }
}
