package ui;

import database.DatabaseConnection;
import operations.DispatchModeComparer;
import operations.DispatchModeComparer.ComparisonResult;
import operations.UrgentDispatcher;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Module M5 — lets the examiner compare FIFO queue, circular queue,
 * and priority-heap (UrgentDispatcher) ordering side by side, either using
 * sample batches or real pending requests from campus_hub.db.
 */
public class DispatchPanel extends JPanel {

    private final DefaultTableModel tableModel = new DefaultTableModel(
            new Object[]{"Request ID", "Category", "Source", "Destination", "Urgency", "Priority Score", "Deadline Passed?"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea resultArea = new JTextArea();
    private final UrgentDispatcher dispatcher = new UrgentDispatcher();

    public DispatchPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        // Format Deadline Passed column as checkbox
        int checkCol = 6;
        table.getColumnModel().getColumn(checkCol).setCellEditor(
                new DefaultCellEditor(new JCheckBox()) {
                    { setClickCountToStart(1); }
                });
        table.getColumnModel().getColumn(checkCol).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JCheckBox box = new JCheckBox();
            box.setSelected(Boolean.TRUE.equals(value));
            box.setHorizontalAlignment(SwingConstants.CENTER);
            return box;
        });

        // Set column widths for readability
        table.getColumnModel().getColumn(0).setPreferredWidth(90);
        table.getColumnModel().getColumn(1).setPreferredWidth(100);
        table.getColumnModel().getColumn(2).setPreferredWidth(160);
        table.getColumnModel().getColumn(3).setPreferredWidth(160);
        table.getColumnModel().getColumn(4).setPreferredWidth(80);
        table.getColumnModel().getColumn(5).setPreferredWidth(90);
        table.getColumnModel().getColumn(6).setPreferredWidth(100);

        loadSampleBatch();

        JPanel top = new JPanel(new BorderLayout(4, 4));
        JLabel infoLabel = new JLabel("<html><b>Priority Score Formula:</b> Base Urgency (CRITICAL: 40, HIGH: 30, MEDIUM: 20, LOW: 10) + Deadline Proximity Boost (up to +25) + <b>Rule 3 IT Support Boost (+10)</b></html>");
        infoLabel.setBorder(BorderFactory.createEmptyBorder(0, 0, 4, 0));
        top.add(infoLabel, BorderLayout.NORTH);
        top.add(new JScrollPane(table), BorderLayout.CENTER);
        top.setPreferredSize(new Dimension(0, 240));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowButton = new JButton("Add Custom Row");
        JButton removeRowButton = new JButton("Remove Selected Row");
        JButton resetButton = new JButton("Reset to Sample Batch");
        JButton loadDbButton = new JButton("Load Real Requests from Database");
        JButton compareButton = new JButton("Compare Dispatch Modes");

        buttonRow.add(addRowButton);
        buttonRow.add(removeRowButton);
        buttonRow.add(resetButton);
        buttonRow.add(loadDbButton);
        buttonRow.add(compareButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(top, BorderLayout.CENTER);
        northPanel.add(buttonRow, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        addRowButton.addActionListener(e -> addCustomRow());
        removeRowButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
        });
        resetButton.addActionListener(e -> loadSampleBatch());
        loadDbButton.addActionListener(e -> loadRealRequestsFromDb());
        compareButton.addActionListener(e -> runComparison());
    }

    private void addCustomRow() {
        int nextId = tableModel.getRowCount() + 1;
        String reqId = "REQ-CUST-" + nextId;
        String category = "IT_SUPPORT";
        String source = "Balme Library [LOC-001]";
        String dest = "JQB Computer Lab [LOC-012]";
        String urgency = "HIGH";
        int score = 40; // Base HIGH (30) + Rule 3 IT Boost (10)
        tableModel.addRow(new Object[]{reqId, category, source, dest, urgency, score, Boolean.FALSE});
    }

    /** Sample batch for clear queue vs priority heap demonstrations. */
    private void loadSampleBatch() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"REQ-S01", "GENERAL", "Balme Library [LOC-001]", "Volta Hall [LOC-011]", "MEDIUM", 20, Boolean.FALSE});
        tableModel.addRow(new Object[]{"REQ-S02", "IT_SUPPORT", "JQB Lab [LOC-012]", "N-Block [LOC-015]", "HIGH", 40, Boolean.TRUE}); // 30+10
        tableModel.addRow(new Object[]{"REQ-S03", "SANITATION", "Night Market [LOC-030]", "Legon Hall [LOC-005]", "LOW", 10, Boolean.FALSE});
        tableModel.addRow(new Object[]{"REQ-S04", "SECURITY", "Commonwealth [LOC-002]", "Main Gate [LOC-050]", "CRITICAL", 40, Boolean.FALSE});
        tableModel.addRow(new Object[]{"REQ-S05", "IT_SUPPORT", "UG Hospital [LOC-020]", "Balme Library [LOC-001]", "MEDIUM", 30, Boolean.TRUE}); // 20+10
        tableModel.addRow(new Object[]{"REQ-S06", "MAINTENANCE", "Akuafo Hall [LOC-006]", "Mensah Sarbah [LOC-007]", "LOW", 10, Boolean.FALSE});
        tableModel.addRow(new Object[]{"REQ-S07", "GENERAL", "International House [LOC-018]", "CS Dept [LOC-013]", "HIGH", 30, Boolean.FALSE});
        
        resultArea.setText("Sample batch loaded with human-readable location names.\n"
                + "Click \"Compare Dispatch Modes\" to view FIFO Queue, Circular Ring Queue, and Priority Max-Heap side by side.");
    }

    /** Loads real pending service requests directly from campus_hub.db SQLite tables. */
    private void loadRealRequestsFromDb() {
        tableModel.setRowCount(0);
        resultArea.setText("Loading real pending requests from database...");

        SwingWorker<List<Object[]>, Void> worker = new SwingWorker<>() {
            @Override
            protected List<Object[]> doInBackground() {
                List<Object[]> rows = new ArrayList<>();
                String sql = "SELECT r.requestId, r.source, r.destination, r.category, r.urgency, r.deadline, "
                           + "l1.name AS srcName, l2.name AS dstName "
                           + "FROM service_requests r "
                           + "LEFT JOIN locations l1 ON r.source = l1.locationId "
                           + "LEFT JOIN locations l2 ON r.destination = l2.locationId "
                           + "WHERE r.status = 'PENDING' LIMIT 30";

                try (Connection conn = DatabaseConnection.connect();
                     Statement stmt = conn.createStatement();
                     ResultSet rs = stmt.executeQuery(sql)) {

                    while (rs.next()) {
                        String id = rs.getString("requestId");
                        String cat = rs.getString("category");
                        String srcId = rs.getString("source");
                        String srcName = rs.getString("srcName");
                        String dstId = rs.getString("destination");
                        String dstName = rs.getString("dstName");
                        String urgency = rs.getString("urgency");
                        Timestamp deadline = rs.getTimestamp("deadline");

                        String srcDisplay = (srcName != null ? srcName : "Location") + " [" + srcId + "]";
                        String dstDisplay = (dstName != null ? dstName : "Location") + " [" + dstId + "]";

                        models.ServiceRequest reqObj = new models.ServiceRequest(id, srcId, dstId, cat, urgency, null, deadline, "PENDING");
                        int score = dispatcher.calculatePriorityScore(reqObj);
                        boolean overdue = (deadline != null && deadline.getTime() <= System.currentTimeMillis());

                        rows.add(new Object[]{id, cat, srcDisplay, dstDisplay, urgency, score, overdue});
                    }
                } catch (SQLException ex) {
                    System.err.println("DB Error in DispatchPanel: " + ex.getMessage());
                }
                return rows;
            }

            @Override
            protected void done() {
                try {
                    List<Object[]> rows = get();
                    if (rows.isEmpty()) {
                        resultArea.setText("No PENDING requests found in database. All requests may already be ASSIGNED.");
                    } else {
                        for (Object[] r : rows) {
                            tableModel.addRow(r);
                        }
                        resultArea.setText("Loaded " + rows.size() + " real pending service requests from campus_hub.db!\n"
                                + "Location names and IDs are displayed for clarity. Click \"Compare Dispatch Modes\" to evaluate priority ordering.");
                    }
                } catch (Exception ex) {
                    resultArea.setText("Error loading database requests: " + ex.getMessage());
                }
            }
        };
        worker.execute();
    }

    private void runComparison() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<DispatchModeComparer.ServiceRequest> requests = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String id = String.valueOf(tableModel.getValueAt(i, 0));
                String category = String.valueOf(tableModel.getValueAt(i, 1));
                String src = String.valueOf(tableModel.getValueAt(i, 2));
                String dst = String.valueOf(tableModel.getValueAt(i, 3));
                
                int score = Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 5)).trim());
                boolean deadlinePassed = Boolean.TRUE.equals(tableModel.getValueAt(i, 6));

                // Include human-readable details in display label
                String displayLabel = id + " (" + category + " from " + src + " to " + dst + ")";
                requests.add(new DispatchModeComparer.ServiceRequest(displayLabel, score, deadlinePassed));
            } catch (NumberFormatException ex) {
                resultArea.setText("Row " + (i + 1) + " has an invalid priority score. Priority score must be a whole number.");
                return;
            }
        }

        if (requests.isEmpty()) {
            resultArea.setText("Add or load at least one request before comparing dispatch modes.");
            return;
        }

        ComparisonResult result = DispatchModeComparer.compareDispatchModes(requests);
        resultArea.setText(result.toString());
    }
}

