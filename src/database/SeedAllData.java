package database;

import java.sql.Connection;
import java.sql.SQLException;

public class SeedAllData {

    private static final String LOCATIONS_CSV = "src/main/resources/seed_data/Locations.csv";
    private static final String ROADS_CSV = "src/main/resources/seed_data/Roads.csv";
    private static final String RESOURCES_CSV = "src/main/resources/seed_data/Resources.csv";
    private static final String SERVICE_REQUESTS_CSV = "src/main/resources/seed_data/ServiceRequests.csv";

    public static void main(String[] args) {
        System.out.println("Creating tables (if they don't already exist)...");
        DatabaseInitializer.createTables();

        System.out.println("\nLoading locations...");
        new LocationLoader().loadData(LOCATIONS_CSV);

        System.out.println("\nLoading roads...");
        new RoadLoader().loadData(ROADS_CSV);

        System.out.println("\nLoading resources...");
        try (Connection conn = DatabaseConnection.connect()) {
            new ResourceLoader(conn).loadData(RESOURCES_CSV);
        } catch (SQLException e) {
            System.err.println("Error loading resources: " + e.getMessage());
        }

        System.out.println("\nLoading service requests...");
        new ServiceRequestLoader().loadData(SERVICE_REQUESTS_CSV);

        System.out.println("\nDone. campus_hub.db is now seeded.");
    }
}