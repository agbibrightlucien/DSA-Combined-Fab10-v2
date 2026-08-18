package database;

public class DatabaseInitializerTest {

    public static void main(String[] args) throws Exception {
        Class.forName("org.sqlite.JDBC");
        System.out.println("SQLite JDBC driver loaded successfully.");

        DatabaseInitializer.createTables();
    }
}