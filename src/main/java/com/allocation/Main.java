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
        CampusGraph graph = new CampusGraph();
        graph.addEdge("A", "B", 5.0, 1.0);
        graph.addEdge("B", "C", 4.0, 1.2);
        graph.addEdge("A", "C", 12.0, 0.8);

        ServiceRequest request1 = new ServiceRequest("REQ1", "X", "C", "IT_SUPPORT", Priority.HIGH, Instant.now(), Instant.now().plusSeconds(3600), Status.PENDING);
        ServiceRequest request2 = new ServiceRequest("REQ2", "Y", "C", "SANITATION", Priority.MEDIUM, Instant.now(), Instant.now().plusSeconds(7200), Status.PENDING);

        List<ServiceRequest> requests = Arrays.asList(request1, request2);
        List<Resource> resources;
        try {
            resources = CsvDataLoader.loadResources("src/main/resources/seed_data/Resources.csv");
        } catch (Exception e) {
            e.printStackTrace();
            Resource resource1 = new Resource("R1", "Technician", "A", 1, Status.AVAILABLE);
            Resource resource2 = new Resource("R2", "Cleaner", "B", 1, Status.AVAILABLE);
            resources = Arrays.asList(resource1, resource2);
        }

        AllocationResult result = GreedyResourceMatcher.optimizeResourceAllocation(requests, resources, graph);
        System.out.println(result.getAllocationMap());
        System.out.println("Unassigned: " + result.getUnassignedRequests().size());
    }
}
