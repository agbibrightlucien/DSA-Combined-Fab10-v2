package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Loads road records from Roads.csv into the SQLite roads table.
 * Each row represents a directed edge (path) between two campus locations.
 */
public class RoadLoader {

    public void loadData(String csvFilePath) {
        String sql = "INSERT OR IGNORE INTO roads "
                   + "(fromLocationId, toLocationId, distance, travelTime, roadConditionWeight) "
                   + "VALUES (?, ?, ?, ?, ?)";

        try (Connection conn = DatabaseConnection.connect();
             BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
             PreparedStatement stmt = conn.prepareStatement(sql)) {

            String line;
            boolean isHeader = true;
            int inserted = 0;
            int skipped  = 0;

            while ((line = reader.readLine()) != null) {
                if (isHeader) { isHeader = false; continue; }   // skip header row
                if (line.trim().isEmpty()) continue;

                String[] fields = line.split(",");
                if (fields.length < 5) {
                    System.err.println("Skipping malformed row: " + line);
                    skipped++;
                    continue;
                }

                try {
                    stmt.setString(1, fields[0].trim());                          // fromLocationId
                    stmt.setString(2, fields[1].trim());                          // toLocationId
                    stmt.setFloat (3, Float.parseFloat(fields[2].trim()));        // distance
                    stmt.setFloat (4, Float.parseFloat(fields[3].trim()));        // travelTime
                    stmt.setFloat (5, Float.parseFloat(fields[4].trim()));        // roadConditionWeight
                    stmt.executeUpdate();
                    inserted++;
                } catch (SQLException | NumberFormatException e) {
                    System.err.println("Skipping row due to error: " + line + " -> " + e.getMessage());
                    skipped++;
                }
            }

            System.out.println("RoadLoader: inserted " + inserted
                    + " row(s), skipped " + skipped + " row(s).");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
