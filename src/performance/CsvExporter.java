package performance;

import database.DatabaseConnection;

import java.io.BufferedWriter;
import java.io.FileWriter;
import java.io.IOException;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

public class CsvExporter {

    public int exportAlgorithmRuns(String outputPath) {
        String sql = "SELECT runId, algorithmName, inputSize, timeNs, memoryKb, dateRun "
                + "FROM algorithm_runs ORDER BY runId";

        int rows = 0;
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery(sql);
             BufferedWriter writer = new BufferedWriter(new FileWriter(outputPath))) {

            writer.write("runId,algorithmName,inputSize,timeNs,memoryKb,dateRun");
            writer.newLine();

            while (rs.next()) {
                writer.write(rs.getInt("runId") + ","
                        + escape(rs.getString("algorithmName")) + ","
                        + rs.getInt("inputSize") + ","
                        + rs.getLong("timeNs") + ","
                        + rs.getLong("memoryKb") + ","
                        + escape(rs.getString("dateRun")));
                writer.newLine();
                rows++;
            }
            writer.flush();
        } catch (SQLException | IOException e) {
            e.printStackTrace();
        }
        return rows;
    }

    private static String escape(String value) {
        if (value == null) {
            return "";
        }
        if (value.contains(",") || value.contains("\"") || value.contains("\n")) {
            return "\"" + value.replace("\"", "\"\"") + "\"";
        }
        return value;
    }
}
