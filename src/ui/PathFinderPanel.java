package ui;

import database.DatabaseConnection;
import operations.ShortestPathFinder;

import javax.swing.*;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Operation 1 — Dijkstra's shortest path between two campus
 * locations, via {@link ShortestPathFinder}.
 *
 * Location dropdowns are populated directly from the "locations" table so
 * the list always matches whatever data is currently loaded in the DB —
 * no dependency on a specific CSV path or a separate location index.
 */
public class PathFinderPanel extends JPanel {

    private final JComboBox<LocationItem> startBox = new JComboBox<>();
    private final JComboBox<LocationItem> endBox = new JComboBox<>();
    private final JTextArea resultArea = new JTextArea();
    private final JButton findButton = new JButton("Find Shortest Path");
    private final JButton refreshButton = new JButton("Reload Locations");

    public PathFinderPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel controls = new JPanel(new GridBagLayout());
        GridBagConstraints gc = new GridBagConstraints();
        gc.insets = new Insets(4, 4, 4, 4);
        gc.fill = GridBagConstraints.HORIZONTAL;

        gc.gridx = 0; gc.gridy = 0;
        controls.add(new JLabel("From:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        controls.add(startBox, gc);

        gc.gridx = 0; gc.gridy = 1; gc.weightx = 0;
        controls.add(new JLabel("To:"), gc);
        gc.gridx = 1; gc.weightx = 1;
        controls.add(endBox, gc);

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        buttonRow.add(findButton);
        buttonRow.add(refreshButton);
        gc.gridx = 0; gc.gridy = 2; gc.gridwidth = 2;
        controls.add(buttonRow, gc);

        add(controls, BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        findButton.addActionListener(e -> runPathFind());
        refreshButton.addActionListener(e -> loadLocations());

        loadLocations();
    }

    private void loadLocations() {
        findButton.setEnabled(false);
        resultArea.setText("Loading locations...");

        SwingWorker<List<LocationItem>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<LocationItem> doInBackground() {
                List<LocationItem> items = new ArrayList<>();
                try (Connection conn = DatabaseConnection.connect();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(
                             "SELECT locationId, name FROM locations ORDER BY name")) {
                    while (rs.next()) {
                        items.add(new LocationItem(rs.getString("locationId"), rs.getString("name")));
                    }
                } catch (SQLException ex) {
                    return null;
                }
                return items;
            }

            @Override
            protected void done() {
                findButton.setEnabled(true);
                try {
                    List<LocationItem> items = get();
                    startBox.removeAllItems();
                    endBox.removeAllItems();
                    if (items == null || items.isEmpty()) {
                        resultArea.setText("No locations found in the database. "
                                + "Make sure the locations table has been loaded.");
                        return;
                    }
                    for (LocationItem item : items) {
                        startBox.addItem(item);
                        endBox.addItem(item);
                    }
                    if (endBox.getItemCount() > 1) {
                        endBox.setSelectedIndex(1);
                    }
                    resultArea.setText("Loaded " + items.size() + " locations. Pick a start and end point, "
                            + "then click \"Find Shortest Path\".");
                } catch (Exception ex) {
                    resultArea.setText("Error loading locations: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void runPathFind() {
        LocationItem start = (LocationItem) startBox.getSelectedItem();
        LocationItem end = (LocationItem) endBox.getSelectedItem();

        if (start == null || end == null) {
            resultArea.setText("Select both a start and end location first.");
            return;
        }

        findButton.setEnabled(false);
        resultArea.setText("Running Dijkstra's algorithm...");

        SwingWorker<String, Void> worker = new SwingWorker<>() {
            @Override
            protected String doInBackground() {
                try (Connection conn = DatabaseConnection.connect()) {
                    ShortestPathFinder finder = new ShortestPathFinder();
                    finder.loadGraph(conn);
                    ShortestPathFinder.PathResult result =
                            finder.findShortestPath(start.id, end.id);

                    if (result == null) {
                        return "No path found between " + start.name + " and " + end.name + ".";
                    }

                    StringBuilder sb = new StringBuilder();
                    sb.append("Shortest path (Dijkstra):\n\n");
                    for (int i = 0; i < result.path.length; i++) {
                        if (i > 0) sb.append("  ->  ");
                        sb.append(result.path[i]);
                    }
                    sb.append("\n\nTotal effective travel time: ")
                      .append(String.format("%.2f", result.totalTime))
                      .append(" min");
                    return sb.toString();

                } catch (SQLException ex) {
                    return "Database error: " + ex.getMessage();
                }
            }

            @Override
            protected void done() {
                findButton.setEnabled(true);
                try {
                    resultArea.setText(get());
                } catch (Exception ex) {
                    resultArea.setText("Error: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    /** Small wrapper so the combo box shows a readable name but keeps the id. */
    private static class LocationItem {
        final String id;
        final String name;

        LocationItem(String id, String name) {
            this.id = id;
            this.name = name;
        }

        @Override
        public String toString() {
            return name + " (" + id + ")";
        }
    }
}
