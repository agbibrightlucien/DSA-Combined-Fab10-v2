import java.util.*;

public class ResourceOptimizer {

    public static class ServiceRequest {
        public final String requestId;
        public final String source;
        public final String destination;
        public final String category;
        public String status;

        public ServiceRequest(String requestId, String source, String destination, String category) {
            this.requestId = requestId;
            this.source = source;
            this.destination = destination;
            this.category = category;
            this.status = "PENDING";
        }
    }

    public static class Resource {
        public final String resourceId;
        public final String type;
        public final String homeLocation;
        public int capacity;
        public String availabilityStatus; // "AVAILABLE" or "BUSY"

        public Resource(String resourceId, String type, String homeLocation, int capacity, String availabilityStatus) {
            this.resourceId = resourceId;
            this.type = type;
            this.homeLocation = homeLocation;
            this.capacity = capacity;
            this.availabilityStatus = availabilityStatus;
        }
    }

    // Simple weighted graph supporting Dijkstra for travel time
    public static class Graph {
        static class Edge {
            final String to;
            final double travelTime;
            final double roadConditionWeight;

            Edge(String to, double travelTime, double roadConditionWeight) {
                this.to = to;
                this.travelTime = travelTime;
                this.roadConditionWeight = roadConditionWeight;
            }
        }

        private final Map<String, List<Edge>> adj = new HashMap<>();

        public void addEdge(String from, String to, double travelTime, double roadConditionWeight) {
            adj.computeIfAbsent(from, k -> new ArrayList<>()).add(new Edge(to, travelTime, roadConditionWeight));
        }

        // Dijkstra returning shortest travel time between two nodes; returns Double.POSITIVE_INFINITY when unreachable
        public double shortestTravelTime(String start, String end) {
            if (start == null || end == null) return Double.POSITIVE_INFINITY;
            if (start.equals(end)) return 0.0;

            Map<String, Double> dist = new HashMap<>();
            PriorityQueue<Map.Entry<String, Double>> pq = new PriorityQueue<>(Comparator.comparingDouble(Map.Entry::getValue));

            dist.put(start, 0.0);
            pq.add(new AbstractMap.SimpleEntry<>(start, 0.0));

            while (!pq.isEmpty()) {
                Map.Entry<String, Double> e = pq.poll();
                String node = e.getKey();
                double d = e.getValue();

                if (d > dist.getOrDefault(node, Double.POSITIVE_INFINITY)) continue;
                List<Edge> edges = adj.get(node);
                if (edges == null) continue;

                for (Edge edge : edges) {
                    double weight = edge.travelTime * edge.roadConditionWeight;
                    double nd = d + weight;
                    if (nd < dist.getOrDefault(edge.to, Double.POSITIVE_INFINITY)) {
                        dist.put(edge.to, nd);
                        pq.add(new AbstractMap.SimpleEntry<>(edge.to, nd));
                    }
                }
            }

            return dist.getOrDefault(end, Double.POSITIVE_INFINITY);
        }
    }

    public static class AllocationResult {
        public final Map<String, String> allocationMap;
        public final List<ServiceRequest> unassignedRequests;

        public AllocationResult(Map<String, String> allocationMap, List<ServiceRequest> unassignedRequests) {
            this.allocationMap = allocationMap;
            this.unassignedRequests = unassignedRequests;
        }
    }

    // Basic compatibility check between resource type and request category.
    // This is intentionally simple; replace or extend with domain rules as needed.
    private static boolean isCompatible(String resourceType, String requestCategory) {
        if (resourceType == null || requestCategory == null) return false;
        String r = resourceType.toLowerCase();
        String c = requestCategory.toLowerCase();

        if (c.contains("it") && r.contains("tech")) return true;
        if (c.contains("sanit") && (r.contains("clean") || r.contains("sanit"))) return true;
        if (c.contains("medical") && r.contains("med")) return true;
        // fallback: if resource type contains category token
        if (r.contains(c) || c.contains(r)) return true;
        return false;
    }

    // Greedy allocation per Operation 4 pseudocode
    public static AllocationResult optimizeResourceAllocation(List<ServiceRequest> pendingRequests,
                                                              List<Resource> resourcePool,
                                                              Graph graph) {
        Map<String, String> allocationMap = new HashMap<>();
        List<ServiceRequest> unassigned = new ArrayList<>();

        if (pendingRequests == null || resourcePool == null || graph == null) {
            return new AllocationResult(allocationMap, unassigned);
        }

        for (ServiceRequest request : pendingRequests) {
            Resource bestResource = null;
            double minTravelTime = Double.POSITIVE_INFINITY;

            for (Resource resource : resourcePool) {
                if (!"AVAILABLE".equalsIgnoreCase(resource.availabilityStatus)) continue;
                if (resource.capacity <= 0) continue;
                if (!isCompatible(resource.type, request.category)) continue;

                double travelTime = graph.shortestTravelTime(resource.homeLocation, request.destination);
                if (travelTime < minTravelTime) {
                    minTravelTime = travelTime;
                    bestResource = resource;
                }
            }

            if (bestResource != null && minTravelTime < Double.POSITIVE_INFINITY) {
                allocationMap.put(request.requestId, bestResource.resourceId);
                bestResource.capacity -= 1;
                if (bestResource.capacity <= 0) bestResource.availabilityStatus = "BUSY";
                request.status = "ASSIGNED";
            } else {
                request.status = "QUEUED";
                unassigned.add(request);
            }
        }

        return new AllocationResult(allocationMap, unassigned);
    }

    // Small helper example main for manual testing (does not modify repo files)
    public static void main(String[] args) {
        // Example usage (can be removed when integrating into larger system)
        Graph g = new Graph();
        g.addEdge("A", "B", 10, 1.0);
        g.addEdge("B", "C", 5, 1.0);

        Resource r1 = new Resource("R1", "Technician", "A", 1, "AVAILABLE");
        Resource r2 = new Resource("R2", "Cleaner", "C", 1, "AVAILABLE");

        ServiceRequest s1 = new ServiceRequest("REQ1", "X", "C", "IT_SUPPORT");
        ServiceRequest s2 = new ServiceRequest("REQ2", "Y", "C", "SANITATION");

        AllocationResult res = optimizeResourceAllocation(Arrays.asList(s1, s2), Arrays.asList(r1, r2), g);
        System.out.println("Allocations: " + res.allocationMap);
        System.out.println("Unassigned: " + res.unassignedRequests.size());
    }
}
