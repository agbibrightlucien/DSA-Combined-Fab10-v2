package ui;

import performance.BenchmarkRunner;

import javax.swing.*;
import java.awt.*;
import java.io.File;
import java.util.Arrays;
import java.util.Comparator;

/**
 * Panel for Module M10 — triggers {@link BenchmarkRunner}'s full experiment
 * suite (search, sort, hash load factor, BST vs balanced tree, heap
 * dispatch, graph algorithms), which writes results into algorithm_runs and
 * exports docs/performance_results/results.csv.
 *
 * Graph images themselves are produced separately (Excel/Python, per the
 * brief) and already live under docs/performance_results/graphs — this
 * panel just displays whichever ones currently exist there.
 */
public class BenchmarkPanel extends JPanel {

    private static final String GRAPHS_DIR = "docs/performance_results/graphs";

    private final JButton runButton = new JButton("Run All Benchmarks");
    private final JComboBox<String> graphSelector = new JComboBox<>();
    private final JLabel imageLabel = new JLabel("", SwingConstants.CENTER);
    private final JLabel statusLabel = new JLabel(" ");

    public BenchmarkPanel() {
        setLayout(new BorderLayout(8, 8));
        setBorder(BorderFactory.createEmptyBorder(10, 10, 10, 10));

        JPanel top = new JPanel(new FlowLayout(FlowLayout.LEFT));
        top.add(runButton);
        top.add(new JLabel("View graph:"));
        top.add(graphSelector);
        add(top, BorderLayout.NORTH);

        imageLabel.setVerticalAlignment(SwingConstants.CENTER);
        add(new JScrollPane(imageLabel), BorderLayout.CENTER);
        add(statusLabel, BorderLayout.SOUTH);

        runButton.addActionListener(e -> runBenchmarks());
        graphSelector.addActionListener(e -> showSelectedGraph());

        refreshGraphList();
    }

    private void runBenchmarks() {
        runButton.setEnabled(false);
        statusLabel.setText("Running benchmarks — search, sort, hash load factor, "
                + "BST vs balanced tree, heap dispatch, graph algorithms. This can take a while...");

        SwingWorker<Boolean, Void> worker = new SwingWorker<>() {
            @Override
            protected Boolean doInBackground() {
                try {
                    BenchmarkRunner.main(new String[0]);
                    return true;
                } catch (Exception ex) {
                    return false;
                }
            }

            @Override
            protected void done() {
                runButton.setEnabled(true);
                try {
                    boolean ok = get();
                    statusLabel.setText(ok
                            ? "Done. Results written to algorithm_runs and docs/performance_results/results.csv."
                            : "Benchmark run failed — check the console for details.");
                } catch (Exception ex) {
                    statusLabel.setText("Benchmark run failed: " + ex.getMessage());
                }
                refreshGraphList();
            }
        };
        worker.execute();
    }

    private void refreshGraphList() {
        graphSelector.removeAllItems();
        File dir = new File(GRAPHS_DIR);
        File[] files = dir.exists() ? dir.listFiles((d, name) ->
                name.toLowerCase().endsWith(".png") || name.toLowerCase().endsWith(".jpg")) : null;

        if (files == null || files.length == 0) {
            imageLabel.setIcon(null);
            imageLabel.setText("No graph images found under " + GRAPHS_DIR
                    + ". Export them from results.csv first (Excel or Python).");
            return;
        }

        Arrays.sort(files, Comparator.comparing(File::getName));
        for (File f : files) {
            graphSelector.addItem(f.getName());
        }
        showSelectedGraph();
    }

    private void showSelectedGraph() {
        String name = (String) graphSelector.getSelectedItem();
        if (name == null) return;

        File file = new File(GRAPHS_DIR, name);
        if (!file.exists()) {
            imageLabel.setIcon(null);
            imageLabel.setText("Image not found: " + file.getPath());
            return;
        }

        ImageIcon icon = new ImageIcon(file.getPath());
        Image scaled = icon.getImage().getScaledInstance(700, -1, Image.SCALE_SMOOTH);
        imageLabel.setText(null);
        imageLabel.setIcon(new ImageIcon(scaled));
    }
}
