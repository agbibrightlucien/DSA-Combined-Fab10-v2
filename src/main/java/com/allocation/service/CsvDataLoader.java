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
        List<String> lines = Files.readAllLines(path);
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
}
