package ui;

import database.DatabaseInitializer;

import javax.swing.*;
import java.awt.*;

/**
 * Part 15 — Graphical demonstration application.
 *
 * Replaces the console menu the brief would otherwise require with a single
 * tabbed Swing window that reaches every module built in Parts 1 through 14
 * without the examiner touching a keyboard shortcut or editing source code.
 *
 * Each tab is a thin panel that calls directly into the existing operations
 * classes (ShortestPathFinder, DispatchModeComparer, TicketLookupEngine,
 * ResourceOptimizer, AuditLogger, BenchmarkRunner). No business logic lives
 * in the UI layer itself.
 */
public class MainUI {

    /**
     * Builds and shows the main application window.
     * Safe to call multiple times; only ever creates one visible frame
     * per call.
     */
    public static void launch() {
        // CREATE TABLE IF NOT EXISTS is idempotent — safe to call on every
        // launch. This does NOT reseed CSV data; it only guarantees the six
        // required tables exist if this is the very first run on a machine.
        DatabaseInitializer.createTables();

        SwingUtilities.invokeLater(MainUI::buildAndShow);
    }

    private static void buildAndShow() {
        JFrame frame = new JFrame("Ghana Smart Service Operations Optimizer — Campus Service Hub");
        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setSize(950, 650);
        frame.setLocationRelativeTo(null);

        JTabbedPane tabs = new JTabbedPane();
        tabs.addTab("Path Finder", new PathFinderPanel());
        tabs.addTab("Dispatch Modes", new DispatchPanel());
        tabs.addTab("Ticket Lookup", new LookupPanel());
        tabs.addTab("Resource Optimizer", new OptimizerPanel());
        tabs.addTab("Benchmarks", new BenchmarkPanel());

        frame.setLayout(new BorderLayout());
        frame.add(tabs, BorderLayout.CENTER);

        JLabel footer = new JLabel(
                "  University of Ghana — DCIT 204/308 Joint DSA Project  |  campus_hub.db",
                SwingConstants.LEFT);
        footer.setFont(footer.getFont().deriveFont(11f));
        footer.setBorder(BorderFactory.createEmptyBorder(4, 6, 4, 6));
        frame.add(footer, BorderLayout.SOUTH);

        frame.setVisible(true);
    }

    public static void main(String[] args) {
        launch();
    }
}
