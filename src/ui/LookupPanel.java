package ui;

import database.DatabaseConnection;
import models.ServiceRequest;
import operations.TicketLookupEngine;

import javax.swing.*;
import java.awt.*;
import java.awt.event.ActionEvent;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;

/**
 * Panel for Operation 3 — fast ticket lookup by requestId via
 * {@link TicketLookupEngine}'s custom hash table.
 *
 * Rule 2 (Hash Table Capacity Rule, per M1_Specification.md): initial
 * capacity = sum of the last two digits of each Level-300 member's index
 * number, times 10. Index numbers from the team roster: 22045903, 22244923,
 * 22050404, 22044996, 22146430 -> last-two-digit sum = 03+23+04+96+30 = 156.
 * (CustomHashTable applies the *10 internally.)
 */
public class LookupPanel extends JPanel {

    private static final int INDEX_DIGITS_SUM = 156;

    private final TicketLookupEngine<ServiceRequest> engine = new TicketLookupEngine<>(INDEX_DIGITS_SUM);
    private final JTextField searchField = new JTextField(20);
    private final JButton searchButton = new JButton("Search");
    private final JButton reindexButton = new JButton("Reload Index From Database");
    private final JTextArea resultArea = new JTextArea();
    private final JLabel statusLabel = new JLabel("Index not built yet.");

    private boolean indexed = false;

    public LookupPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(new JLabel("Request ID:"));
        top.add(searchField);
        top.add(searchButton);
        top.add(reindexButton);
        add(top, BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        resultArea.setLineWrap(true);
        resultArea.setWrapStyleWord(true);
        add(new JScrollPane(resultArea), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        searchField.addActionListener(this::onSearch);
        searchButton.addActionListener(this::onSearch);
        reindexButton.addActionListener(e -> buildIndex());

        searchButton.setEnabled(false);
        buildIndex();
    }

    private void buildIndex() {
        searchButton.setEnabled(false);
        reindexButton.setEnabled(false);
        statusLabel.setText("Building hash index from service_requests table...");

        SwingWorker<Integer, Void> worker = new SwingWorker<>() {
            @Override
            protected Integer doInBackground() {
                int count = 0;
                String sql = "SELECT requestId, source, destination, category, urgency, "
                        + "timeSubmitted, deadline, status FROM service_requests";

                try (Connection conn = DatabaseConnection.connect();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        ServiceRequest request = new ServiceRequest(
                                rs.getString("requestId"),
                                rs.getString("source"),
                                rs.getString("destination"),
                                rs.getString("category"),
                                rs.getString("urgency"),
                                rs.getTimestamp("timeSubmitted"),
                                rs.getTimestamp("deadline"),
                                rs.getString("status"));
                        engine.addTicket(request.getRequestId(), request);
                        count++;
                    }
                } catch (SQLException ex) {
                    return -1;
                }
                return count;
            }

            @Override
            protected void done() {
                reindexButton.setEnabled(true);
                try {
                    int count = get();
                    if (count < 0) {
                        statusLabel.setText("Failed to build index — check the database connection.");
                        return;
                    }
                    indexed = true;
                    searchButton.setEnabled(true);
                    statusLabel.setText("Indexed " + engine.getTotalIndexedTickets()
                            + " tickets. Hash collisions so far: " + engine.getCollisionCount() + ".");
                    resultArea.setText("Enter a request ID (e.g. REQ-001) and click Search.");
                } catch (Exception ex) {
                    statusLabel.setText("Error building index: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void onSearch(ActionEvent e) {
        if (!indexed) {
            resultArea.setText("Index is still building — please wait.");
            return;
        }
        String requestId = searchField.getText().trim();
        if (requestId.isEmpty()) {
            resultArea.setText("Enter a request ID first.");
            return;
        }
        resultArea.setText(engine.searchTicketFormatted(requestId));
    }
}
