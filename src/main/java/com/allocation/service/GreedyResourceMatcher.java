package com.allocation.service;

import com.allocation.domain.Priority;
import com.allocation.domain.Resource;
import com.allocation.domain.ServiceRequest;
import com.allocation.domain.Status;
import java.time.Duration;
import java.time.Instant;
import java.util.*;

public class GreedyResourceMatcher {

    public static class AllocationResult {
        private final Map<String, String> allocationMap;
        private final List<ServiceRequest> unassignedRequests;

        public AllocationResult(Map<String, String> allocationMap, List<ServiceRequest> unassignedRequests) {
            this.allocationMap = allocationMap;
            this.unassignedRequests = unassignedRequests;
        }

        public Map<String, String> getAllocationMap() {
            return allocationMap;
        }

        public List<ServiceRequest> getUnassignedRequests() {
            return unassignedRequests;
        }
    }

    public static AllocationResult optimizeResourceAllocation(List<ServiceRequest> pendingRequests,
                                                              List<Resource> resourcePool,
                                                              CampusGraph graph) {
        Map<String, String> allocationMap = new HashMap<>();
        List<ServiceRequest> unassignedRequests = new ArrayList<>();

        if (pendingRequests == null || resourcePool == null || graph == null) {
            return new AllocationResult(allocationMap, unassignedRequests);
        }
        List<ServiceRequest> pendingOnly = new ArrayList<>();
        for (ServiceRequest r : pendingRequests) {
            if (r.getStatus() == Status.PENDING) pendingOnly.add(r);
        }

        List<ServiceRequest> sortedRequests = new ArrayList<>(pendingOnly);
        Map<ServiceRequest, Double> scoreMap = new HashMap<>();
        Instant now = Instant.now();
        for (ServiceRequest request : sortedRequests) {
            double baseScore = getBaseScore(request.getPriority());
            double urgencyBoost = 0.0;
            try {
                long secondsRemaining = Duration.between(now, request.getDeadline()).getSeconds();
                if (secondsRemaining <= 0) urgencyBoost = 1000.0;
                else if (secondsRemaining <= 3600) urgencyBoost = 30.0;
                else if (secondsRemaining <= 21600) urgencyBoost = 15.0;
                else if (secondsRemaining <= 86400) urgencyBoost = 5.0;
            } catch (Exception ex) {
                urgencyBoost = 0.0;
            }
            double categoryBoost = 0.0;
            if ("IT_SUPPORT".equalsIgnoreCase(request.getCategory())) categoryBoost = 10.0;
            double total = baseScore + urgencyBoost + categoryBoost;
            scoreMap.put(request, total);
        }
        sortedRequests.sort((a, b) -> Double.compare(scoreMap.get(b), scoreMap.get(a)));

        for (ServiceRequest request : sortedRequests) {
            Resource bestResource = null;
            double minTravelTime = Double.POSITIVE_INFINITY;
            String reason = "no compatible available resource";

            for (Resource resource : resourcePool) {
                if (resource.getStatus() != Status.AVAILABLE || resource.getCapacity() <= 0) {
                    continue;
                }
                if (!isCompatible(resource.getType(), request.getCategory())) {
                    continue;
                }
                double travelTime = graph.shortestTravelTime(resource.getHomeLocation(), request.getDestination());
                if (Double.isFinite(travelTime) && travelTime < minTravelTime) {
                    minTravelTime = travelTime;
                    bestResource = resource;
                    reason = null;
                }
            }

            if (bestResource != null && minTravelTime < Double.POSITIVE_INFINITY) {
                allocationMap.put(request.getRequestId(), bestResource.getResourceId());
                bestResource.decrementCapacity();
                if (bestResource.getCapacity() <= 0) {
                    bestResource.setStatus(Status.BUSY);
                }
                request.setStatus(Status.ASSIGNED);
            } else {
                request.setStatus(Status.QUEUED);
                unassignedRequests.add(request);
            }
        }

        return new AllocationResult(allocationMap, unassignedRequests);
    }

    private static double getBaseScore(Priority p) {
        if (p == null) return 0.0;
        switch (p) {
            case CRITICAL:
                return 100.0;
            case HIGH:
                return 50.0;
            case MEDIUM:
                return 20.0;
            case LOW:
            default:
                return 5.0;
        }
    }

    private static boolean isCompatible(String resourceType, String requestCategory) {
        if (resourceType == null || requestCategory == null) {
            return false;
        }
        String type = resourceType.toLowerCase(Locale.ROOT);
        String category = requestCategory.toLowerCase(Locale.ROOT);

        if (category.contains("it") && type.contains("tech")) {
            return true;
        }
        if (category.contains("sanit") && (type.contains("clean") || type.contains("sanit"))) {
            return true;
        }
        if (category.contains("medical") && type.contains("med")) {
            return true;
        }
        if (type.contains(category) || category.contains(type)) {
            return true;
        }
        return false;
    }

    public static class CampusGraph {
        private final Map<String, List<Edge>> adjacency = new HashMap<>();

        public static class Edge {
            private final String to;
            private final double travelTime;
            private final double roadConditionWeight;

            public Edge(String to, double travelTime, double roadConditionWeight) {
                this.to = to;
                this.travelTime = travelTime;
                this.roadConditionWeight = roadConditionWeight;
            }

            public String getTo() {
                return to;
            }

            public double getWeight() {
                return travelTime * roadConditionWeight;
            }
        }

        public void addEdge(String from, String to, double travelTime, double roadConditionWeight) {
            adjacency.computeIfAbsent(from, key -> new ArrayList<>()).add(new Edge(to, travelTime, roadConditionWeight));
        }

        public double shortestTravelTime(String start, String end) {
            if (start == null || end == null) {
                return Double.POSITIVE_INFINITY;
            }
            if (start.equals(end)) {
                return 0.0;
            }
            Map<String, Double> distances = new HashMap<>();
            PriorityQueue<Map.Entry<String, Double>> queue = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));
            distances.put(start, 0.0);
            queue.add(new AbstractMap.SimpleEntry<>(start, 0.0));

            while (!queue.isEmpty()) {
                Map.Entry<String, Double> entry = queue.poll();
                String node = entry.getKey();
                double distance = entry.getValue();
                if (distance > distances.getOrDefault(node, Double.POSITIVE_INFINITY)) {
                    continue;
                }
                List<Edge> edges = adjacency.get(node);
                if (edges == null) {
                    continue;
                }
                for (Edge edge : edges) {
                    double nextDistance = distance + edge.getWeight();
                    if (nextDistance < distances.getOrDefault(edge.getTo(), Double.POSITIVE_INFINITY)) {
                        distances.put(edge.getTo(), nextDistance);
                        queue.add(new AbstractMap.SimpleEntry<>(edge.getTo(), nextDistance));
                    }
                }
            }
            return distances.getOrDefault(end, Double.POSITIVE_INFINITY);
        }
    }
}
