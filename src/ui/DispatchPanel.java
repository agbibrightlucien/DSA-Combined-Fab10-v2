package ui;

import operations.DispatchModeComparer;
import operations.DispatchModeComparer.ComparisonResult;

import javax.swing.*;
import javax.swing.table.DefaultTableModel;
import java.awt.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Panel for Module M5 — lets the examiner build a batch of requests and see
 * FIFO queue, circular queue, and priority-heap (UrgentDispatcher) dispatch
 * orders side by side, backed by {@link DispatchModeComparer}.
 */
public class DispatchPanel extends JPanel {

    private final DefaultTableModel tableModel =
            new DefaultTableModel(new Object[]{"Request ID", "Priority Score", "Deadline Passed?"}, 0);
    private final JTable table = new JTable(tableModel);
    private final JTextArea resultArea = new JTextArea();

    public DispatchPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        table.getColumnModel().getColumn(2).setCellEditor(
                new DefaultCellEditor(new JCheckBox()) {
                    { setClickCountToStart(1); }
                });
        table.getColumnModel().getColumn(2).setCellRenderer((tbl, value, isSelected, hasFocus, row, column) -> {
            JCheckBox box = new JCheckBox();
            box.setSelected(Boolean.TRUE.equals(value));
            box.setHorizontalAlignment(SwingConstants.CENTER);
            return box;
        });

        loadSampleBatch();

        JPanel top = new JPanel(new BorderLayout());
        top.add(new JLabel("Batch of pending service requests (edit freely):"), BorderLayout.NORTH);
        top.add(new JScrollPane(table), BorderLayout.CENTER);
        top.setPreferredSize(new Dimension(0, 220));

        JPanel buttonRow = new JPanel(new FlowLayout(FlowLayout.LEFT));
        JButton addRowButton = new JButton("Add Row");
        JButton removeRowButton = new JButton("Remove Selected Row");
        JButton resetButton = new JButton("Reset to Sample Batch");
        JButton compareButton = new JButton("Compare Dispatch Modes");
        buttonRow.add(addRowButton);
        buttonRow.add(removeRowButton);
        buttonRow.add(resetButton);
        buttonRow.add(compareButton);

        JPanel northPanel = new JPanel(new BorderLayout());
        northPanel.add(top, BorderLayout.CENTER);
        northPanel.add(buttonRow, BorderLayout.SOUTH);
        add(northPanel, BorderLayout.NORTH);

        resultArea.setEditable(false);
        resultArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        add(new JScrollPane(resultArea), BorderLayout.CENTER);

        addRowButton.addActionListener(e ->
                tableModel.addRow(new Object[]{"R" + (tableModel.getRowCount() + 1), 5, Boolean.FALSE}));

        removeRowButton.addActionListener(e -> {
            int row = table.getSelectedRow();
            if (row >= 0) tableModel.removeRow(row);
        });

        resetButton.addActionListener(e -> loadSampleBatch());

        compareButton.addActionListener(e -> runComparison());
    }

    /** Mirrors the sample batch used in DispatchModeComparer's own main() demo. */
    private void loadSampleBatch() {
        tableModel.setRowCount(0);
        tableModel.addRow(new Object[]{"R1", 5, Boolean.FALSE});
        tableModel.addRow(new Object[]{"R2", 8, Boolean.TRUE});
        tableModel.addRow(new Object[]{"R3", 3, Boolean.FALSE});
        tableModel.addRow(new Object[]{"R4", 9, Boolean.FALSE});
        tableModel.addRow(new Object[]{"R5", 6, Boolean.TRUE});
        tableModel.addRow(new Object[]{"R6", 2, Boolean.FALSE});
        tableModel.addRow(new Object[]{"R7", 7, Boolean.FALSE});
        resultArea.setText("Sample batch loaded. Click \"Compare Dispatch Modes\" to see FIFO, "
                + "circular queue, and priority-heap ordering side by side.");
    }

    private void runComparison() {
        if (table.isEditing()) {
            table.getCellEditor().stopCellEditing();
        }

        List<DispatchModeComparer.ServiceRequest> requests = new ArrayList<>();
        for (int i = 0; i < tableModel.getRowCount(); i++) {
            try {
                String id = String.valueOf(tableModel.getValueAt(i, 0));
                int score = Integer.parseInt(String.valueOf(tableModel.getValueAt(i, 1)).trim());
                boolean deadlinePassed = Boolean.TRUE.equals(tableModel.getValueAt(i, 2));
                requests.add(new DispatchModeComparer.ServiceRequest(id, score, deadlinePassed));
            } catch (NumberFormatException ex) {
                resultArea.setText("Row " + (i + 1) + " has an invalid priority score. "
                        + "Priority score must be a whole number.");
                return;
            }
        }

        if (requests.isEmpty()) {
            resultArea.setText("Add at least one request before comparing dispatch modes.");
            return;
        }

        ComparisonResult result = DispatchModeComparer.compareDispatchModes(requests);
        resultArea.setText(result.toString());
    }
}
