package com.allocation.service;

import com.allocation.domain.Resource;
import com.allocation.domain.Status;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class CsvDataLoader {

    public static List<Resource> loadResources(String csvPath) throws IOException {
        Path path = Paths.get(csvPath);
        List<String> lines;
        if (Files.exists(path)) {
            lines = Files.readAllLines(path);
        } else {
            String resourcePath = csvPath;
            if (resourcePath.startsWith("src/main/resources/")) {
                resourcePath = resourcePath.substring("src/main/resources/".length());
            }
            try (java.io.InputStream is = CsvDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IOException("CSV not found at " + csvPath + " or classpath:" + resourcePath);
                }
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                lines = new ArrayList<>();
                String ln;
                while ((ln = br.readLine()) != null) lines.add(ln);
            }
        }
        List<Resource> resources = new ArrayList<>();
        if (lines.size() <= 1) return resources;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 5) continue;
            String resourceId = parts[0].trim();
            String type = parts[1].trim();
            String homeLocation = parts[2].trim();
            int capacity = 1;
            try {
                capacity = Integer.parseInt(parts[3].trim());
            } catch (NumberFormatException ex) {
                capacity = 1;
            }
            String statusStr = parts[4].trim().toUpperCase();
            Status status;
            switch (statusStr) {
                case "AVAILABLE":
                    status = Status.AVAILABLE;
                    break;
                case "BUSY":
                    status = Status.BUSY;
                    break;
                case "IN_TRANSIT":
                case "MAINTENANCE":
                    status = Status.BUSY;
                    break;
                default:
                    try {
                        status = Status.valueOf(statusStr);
                    } catch (Exception ex) {
                        status = Status.PENDING;
                    }
            }
            Resource r = new Resource(resourceId, type, homeLocation, capacity, status);
            resources.add(r);
        }
        return resources;
    }

    public static GreedyResourceMatcher.CampusGraph loadGraph(String csvPath) throws IOException {
        GreedyResourceMatcher.CampusGraph graph = new GreedyResourceMatcher.CampusGraph();
        Path path = Paths.get(csvPath);
        List<String> lines;
        if (Files.exists(path)) {
            lines = Files.readAllLines(path);
        } else {
            String resourcePath = csvPath;
            if (resourcePath.startsWith("src/main/resources/")) {
                resourcePath = resourcePath.substring("src/main/resources/".length());
            }
            try (java.io.InputStream is = CsvDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IOException("CSV not found at " + csvPath + " or classpath:" + resourcePath);
                }
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                lines = new ArrayList<>();
                String ln;
                while ((ln = br.readLine()) != null) lines.add(ln);
            }
        }
        if (lines.size() <= 1) return graph;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 5) continue;
            String from = parts[0].trim();
            String to = parts[1].trim();
            double travelTime = 1.0;
            double roadCondition = 1.0;
            try {
                travelTime = Double.parseDouble(parts[3].trim());
            } catch (Exception ex) {
                travelTime = 1.0;
            }
            try {
                roadCondition = Double.parseDouble(parts[4].trim());
            } catch (Exception ex) {
                roadCondition = 1.0;
            }
            graph.addEdge(from, to, travelTime, roadCondition);
        }
        return graph;
    }

    public static List<com.allocation.domain.ServiceRequest> loadServiceRequests(String csvPath) throws IOException {
        Path path = Paths.get(csvPath);
        List<String> lines;
        if (Files.exists(path)) {
            lines = Files.readAllLines(path);
        } else {
            String resourcePath = csvPath;
            if (resourcePath.startsWith("src/main/resources/")) {
                resourcePath = resourcePath.substring("src/main/resources/".length());
            }
            try (java.io.InputStream is = CsvDataLoader.class.getClassLoader().getResourceAsStream(resourcePath)) {
                if (is == null) {
                    throw new IOException("CSV not found at " + csvPath + " or classpath:" + resourcePath);
                }
                java.io.BufferedReader br = new java.io.BufferedReader(new java.io.InputStreamReader(is));
                lines = new ArrayList<>();
                String ln;
                while ((ln = br.readLine()) != null) lines.add(ln);
            }
        }
        List<com.allocation.domain.ServiceRequest> requests = new ArrayList<>();
        if (lines.size() <= 1) return requests;
        for (int i = 1; i < lines.size(); i++) {
            String line = lines.get(i).trim();
            if (line.isEmpty()) continue;
            String[] parts = line.split(",", -1);
            if (parts.length < 8) continue;
            String requestId = parts[0].trim();
            String source = parts[1].trim();
            String destination = parts[2].trim();
            String category = parts[3].trim();
            String urgencyStr = parts[4].trim().toUpperCase();
            com.allocation.domain.Priority priority;
            try {
                priority = com.allocation.domain.Priority.valueOf(urgencyStr);
            } catch (Exception ex) {
                priority = com.allocation.domain.Priority.LOW;
            }
            java.time.Instant timeSubmitted = java.time.Instant.now();
            java.time.Instant deadline = java.time.Instant.now().plusSeconds(3600);
            try {
                timeSubmitted = java.time.Instant.parse(parts[5].trim());
            } catch (Exception ex) {
            }
            try {
                deadline = java.time.Instant.parse(parts[6].trim());
            } catch (Exception ex) {
            }
            com.allocation.domain.Status status;
            try {
                status = com.allocation.domain.Status.valueOf(parts[7].trim().toUpperCase());
            } catch (Exception ex) {
                status = com.allocation.domain.Status.PENDING;
            }
            com.allocation.domain.ServiceRequest r = new com.allocation.domain.ServiceRequest(requestId, source, destination, category, priority, timeSubmitted, deadline, status);
            if (r.getStatus() == com.allocation.domain.Status.PENDING) requests.add(r);
        }
        return requests;
    }
}
