package ui;

import models.AuditEvent;
import operations.AuditLogger;
import operations.ResourceOptimizer;
import structures.CustomIterator;

import javax.swing.*;
import java.awt.*;

/**
 * Panel for Operation 4 — greedy resource allocation via
 * {@link ResourceOptimizer}, with an undo button wired to the exact
 * {@link AuditLogger} instance that recorded the run (Part 4).
 *
 * One ResourceOptimizer is kept for the panel's lifetime so "Undo Last
 * Assignment" pops from the same in-memory CustomStack that "Run Optimizer"
 * pushed onto — a fresh AuditLogger would have an empty stack and nothing
 * to undo.
 */
public class OptimizerPanel extends JPanel {

    private final ResourceOptimizer optimizer = new ResourceOptimizer();
    private final JTextArea outputArea = new JTextArea();
    private final JButton runButton = new JButton("Run Optimizer");
    private final JButton undoButton = new JButton("Undo Last Assignment");

    public OptimizerPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(runButton);
        top.add(undoButton);
        add(top, BorderLayout.NORTH);

        outputArea.setEditable(false);
        outputArea.setFont(new Font(Font.MONOSPACED, Font.PLAIN, 13));
        outputArea.setText("Click \"Run Optimizer\" to greedily assign the nearest compatible "
                + "resource to every PENDING request, in priority order.\n\n"
                + "Every status change made here is written to the audit_events table. "
                + "\"Undo Last Assignment\" reverses the most recent change live, both in "
                + "this session and in the database.");
        add(new JScrollPane(outputArea), BorderLayout.CENTER);

        runButton.addActionListener(e -> runOptimizer());
        undoButton.addActionListener(e -> undoLast());
    }

    private void runOptimizer() {
        runButton.setEnabled(false);
        undoButton.setEnabled(false);
        outputArea.setText("Running resource optimizer...\n");

        SwingWorker<Void, Void> worker = new SwingWorker<>() {
            @Override
            protected Void doInBackground() {
                optimizer.optimizeAllocations();
                return null;
            }

            @Override
            protected void done() {
                runButton.setEnabled(true);
                undoButton.setEnabled(true);

                StringBuilder sb = new StringBuilder();
                sb.append("=== Assignment History (CustomLinkedList) ===\n");
                CustomIterator<String> it = optimizer.getAssignmentHistory().customIterator();
                boolean any = false;
                while (it.hasNext()) {
                    sb.append(it.next()).append("\n");
                    any = true;
                }
                if (!any) {
                    sb.append("(no PENDING requests were found — nothing to assign)\n");
                }
                outputArea.setText(sb.toString());
            }
        };
        worker.execute();
    }

    private void undoLast() {
        undoButton.setEnabled(false);

        SwingWorker<AuditEvent, Void> worker = new SwingWorker<>() {
            @Override
            protected AuditEvent doInBackground() {
                AuditLogger logger = optimizer.getAuditLogger();
                if (logger.isEmpty()) {
                    return null;
                }
                return logger.undoLast();
            }

            @Override
            protected void done() {
                undoButton.setEnabled(true);
                try {
                    AuditEvent undone = get();
                    if (undone == null) {
                        outputArea.append("\nNothing to undo — run the optimizer first, "
                                + "or all changes from this session are already undone.\n");
                        return;
                    }
                    outputArea.append("\nUNDONE: [" + undone.getEventType() + "] "
                            + undone.getDescription()
                            + " (request " + undone.getRelatedRequestId() + ")\n");
                } catch (Exception ex) {
                    outputArea.append("\nError while undoing: " + ex.getMessage() + "\n");
                }
            }
        };
        worker.execute();
    }
}
