package database;

import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;

/**
 * Loads resource records from a CSV file into the SQLite resources table.
 */
public class ResourceLoader {

    private final Connection connection;

    public ResourceLoader(Connection connection) {
        this.connection = connection;
    }

    /**
     * Reads Resources.csv line by line and inserts each row into the resources table.
     *
     * @param csvFilePath path to the Resources.csv seed file
     */
    public void loadData(String csvFilePath) {
        String sql = "INSERT INTO resources (resourceId, type, homeLocation, capacity, availabilityStatus) "
                + "VALUES (?, ?, ?, ?, ?)";

        try (BufferedReader reader = new BufferedReader(new FileReader(csvFilePath));
             PreparedStatement statement = connection.prepareStatement(sql)) {

            // Skip CSV header row
            String line = reader.readLine();

            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty()) {
                    continue;
                }

                String[] values = line.split(",");
                if (values.length < 5) {
                    continue;
                }

                statement.setString(1, values[0].trim());
                statement.setString(2, values[1].trim());
                statement.setString(3, values[2].trim());
                statement.setInt(4, Integer.parseInt(values[3].trim()));
                statement.setString(5, values[4].trim());
                statement.executeUpdate();
            }
        } catch (IOException | SQLException e) {
            e.printStackTrace();
        }
    }
}
