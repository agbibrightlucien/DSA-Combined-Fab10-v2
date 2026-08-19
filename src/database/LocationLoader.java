package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Loads location records from Locations.csv into the SQLite locations table.
 *
 * Note: Locations.csv has coordinate fields enclosed in double quotes that
 * contain commas (e.g. "5.65..., -0.18..."). This loader uses a custom
 * quote-aware CSV parser to handle these fields correctly without relying on
 * any external CSV library.
 */
public class LocationLoader {

    public void loadData(String csvFilePath) {
        String sql = "INSERT OR IGNORE INTO locations "
                   + "(locationId, name, area, type, coordinates) "
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

                String[] fields = parseCSVLine(line);
                if (fields.length < 5) {
                    System.err.println("Skipping malformed row: " + line);
                    skipped++;
                    continue;
                }

                try {
                    stmt.setString(1, fields[0].trim());  // locationId
                    stmt.setString(2, fields[1].trim());  // name
                    stmt.setString(3, fields[2].trim());  // area
                    stmt.setString(4, fields[3].trim());  // type
                    stmt.setString(5, fields[4].trim());  // coordinates (may contain comma)
                    stmt.executeUpdate();
                    inserted++;
                } catch (SQLException e) {
                    System.err.println("Skipping row due to DB error: " + line + " -> " + e.getMessage());
                    skipped++;
                }
            }

            System.out.println("LocationLoader: inserted " + inserted
                    + " row(s), skipped " + skipped + " row(s).");

        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }

    /**
     * Custom quote-aware CSV line parser implemented from scratch.
     * Handles fields enclosed in double quotes that may contain commas.
     * No external library is used.
     *
     * @param line a single CSV line
     * @return array of field values with enclosing quotes stripped
     */
    private String[] parseCSVLine(String line) {
        // Use a fixed-size buffer (max 10 fields per row)
        String[] buffer = new String[10];
        int count = 0;
        StringBuilder current = new StringBuilder();
        boolean inQuotes = false;

        for (int i = 0; i < line.length(); i++) {
            char c = line.charAt(i);
            if (c == '"') {
                inQuotes = !inQuotes;           // toggle quote mode; strip the quote character itself
            } else if (c == ',' && !inQuotes) {
                if (count < buffer.length) {
                    buffer[count++] = current.toString();
                }
                current = new StringBuilder();
            } else {
                current.append(c);
            }
        }

        // Add the final field
        if (count < buffer.length) {
            buffer[count++] = current.toString();
        }

        // Trim to exact size
        String[] result = new String[count];
        System.arraycopy(buffer, 0, result, 0, count);
        return result;
    }
}
