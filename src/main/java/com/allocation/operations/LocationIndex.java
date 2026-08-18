package com.allocation.operations;

import com.allocation.domain.Location;
import com.allocation.structures.BST;
import com.allocation.structures.RedBlackTree;
import com.allocation.service.CsvDataLoader;

import java.io.IOException;
import java.util.List;

/**
 * Builds a BST over all locations keyed by name to support alphabetical browsing.
 * Also builds a RedBlackTree for height comparison experiments.
 */
public class LocationIndex {

    private final BST<String, Location> bstByName = new BST<>();
    private final RedBlackTree<String, Location> rbtByName = new RedBlackTree<>();

    public void loadFromCsv(String csvPath) throws IOException {
        List<Location> locations = loadLocations(csvPath);
        for (Location loc : locations) {
            String key = loc.getName();
            bstByName.insert(key, loc);
            rbtByName.insert(key, loc);
        }
    }

    private List<Location> loadLocations(String csvPath) throws IOException {
        java.nio.file.Path path = java.nio.file.Paths.get(csvPath);
        java.util.List<String> lines;
        if (java.nio.file.Files.exists(path)) {
            lines = java.nio.file.Files.readAllLines(path);
        } else {
            String resourcePath = csvPath;
            if (resourcePath.startsWith("src/main/resources/")) {
                resourcePath = resourcePath.substring("src/main/resources/".length());
            }
            try (java.io.InputStream is = CsvDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) throw new IOException("Locations CSV not found: " + csvPath);
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                lines = new java.util.ArrayList<>();
                String ln;
                while ((ln = br.readLine()) != null) lines.add(ln);
            }
        }
        java.util.List<Location> out = new java.util.ArrayList<>();
        if (lines.size() <= 1) return out;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 5) continue;
            Location loc = new Location(parts[0].trim(), parts[1].trim(), parts[2].trim(), parts[3].trim(), parts[4].trim());
            out.add(loc);
        }
        return out;
    }

    public List<Location> alphabeticalLocations() {
        return bstByName.inorderTraversal();
    }

    public int bstHeight() { return bstByName.height(); }
    public int rbtHeight() { return rbtByName.height(); }
}
