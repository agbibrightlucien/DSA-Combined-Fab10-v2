package com.allocation;

import com.allocation.domain.Priority;
import com.allocation.domain.Resource;
import com.allocation.domain.ServiceRequest;
import com.allocation.domain.Status;
import com.allocation.service.CsvDataLoader;
import com.allocation.service.GreedyResourceMatcher;
import com.allocation.service.GreedyResourceMatcher.AllocationResult;
import com.allocation.service.GreedyResourceMatcher.CampusGraph;
import java.time.Instant;
import java.util.Arrays;
import java.util.List;

public class Main {
    public static void main(String[] args) {
        CampusGraph graph;
        List<ServiceRequest> requests;
        List<Resource> resources;
        try {
            graph = CsvDataLoader.loadGraph("src/main/resources/seed_data/Roads.csv");
        } catch (Exception e) {
            e.printStackTrace();
            graph = new CampusGraph();
        }

        try {
            requests = CsvDataLoader.loadServiceRequests("src/main/resources/seed_data/ServiceRequests.csv");
        } catch (Exception e) {
            e.printStackTrace();
            ServiceRequest request1 = new ServiceRequest("REQ1", "X", "LOC-003", "IT_SUPPORT", Priority.HIGH, Instant.now(), Instant.now().plusSeconds(3600), Status.PENDING);
            ServiceRequest request2 = new ServiceRequest("REQ2", "Y", "LOC-004", "SANITATION", Priority.MEDIUM, Instant.now(), Instant.now().plusSeconds(7200), Status.PENDING);
            requests = Arrays.asList(request1, request2);
        }

        try {
            resources = CsvDataLoader.loadResources("src/main/resources/seed_data/Resources.csv");
        } catch (Exception e) {
            e.printStackTrace();
            Resource resource1 = new Resource("R1", "Technician", "LOC-001", 1, Status.AVAILABLE);
            Resource resource2 = new Resource("R2", "Cleaner", "LOC-002", 1, Status.AVAILABLE);
            resources = Arrays.asList(resource1, resource2);
        }

        AllocationResult result = GreedyResourceMatcher.optimizeResourceAllocation(requests, resources, graph);
        System.out.println(result.getAllocationMap());
        System.out.println("Unassigned: " + result.getUnassignedRequests().size());
    }
}
