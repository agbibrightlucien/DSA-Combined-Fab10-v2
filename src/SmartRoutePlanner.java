package src;

import java.util.*;

public class SmartRoutePlanner {

    public static class Location {
        private final String id;
        private final String name;
        private final double latitude;
        private final double longitude;
        private final String category;

        public Location(String id, String name, double latitude, double longitude, String category) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category;
        }

        public String getId() { return id; }
        public String getName() { return name; }
        public double getLatitude() { return latitude; }
        public double getLongitude() { return longitude; }
        public String getCategory() { return category; }
    }

    public static class Road {
        private final String id;
        private final String startLocation;
        private final String endLocation;
        private final double distance;
        private String status;

        public Road(String id, String startLocation, String endLocation, double distance) {
            this(id, startLocation, endLocation, distance, "Open");
        }

        public Road(String id, String startLocation, String endLocation, double distance, String status) {
            this.id = id;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.distance = distance;
            this.status = status;
        }

        public String getId() { return id; }
        public String getStartLocation() { return startLocation; }
        public String getEndLocation() { return endLocation; }
        public double getDistance() { return distance; }
        public String getStatus() { return status; }
        public void setStatus(String status) { this.status = status; }
    }

    private static class Edge {
        final String to;
        final String roadId;
        Edge(String to, String roadId) { this.to = to; this.roadId = roadId; }
    }

    public static class RouteResult {
        public final List<String> path;
        public final List<String> roads;
        public final double totalDistance;
        public final double estimatedTimeMinutes;

        public RouteResult(List<String> path, List<String> roads, double totalDistance, double estimatedTimeMinutes) {
            this.path = path;
            this.roads = roads;
            this.totalDistance = totalDistance;
            this.estimatedTimeMinutes = estimatedTimeMinutes;
        }

        @Override
        public String toString() {
            return "RouteResult{" +
                    "path=" + path +
                    ", roads=" + roads +
                    ", totalDistance=" + totalDistance +
                    ", estimatedTimeMinutes=" + estimatedTimeMinutes +
                    '}';
        }
    }

    private final Map<String, Location> locations = new HashMap<>();
    private final Map<String, Road> roads = new HashMap<>();
    private final Map<String, List<Edge>> adjacency = new HashMap<>();
    private final List<String> indexNumbers;
    private final int hashTableSize;

    public SmartRoutePlanner() {
        this(Collections.emptyList());
    }

    public SmartRoutePlanner(List<String> indexNumbers) {
        this.indexNumbers = indexNumbers != null ? new ArrayList<>(indexNumbers) : new ArrayList<>();
        this.hashTableSize = computeHashTableSize();
    }

    private int computeHashTableSize() {
        if (indexNumbers.isEmpty()) {
            return 3;
        }
        double sum = 0.0;
        int count = 0;
        for (String s : indexNumbers) {
            try {
                double v = Double.parseDouble(s);
                sum += v;
                count++;
            } catch (NumberFormatException ignored) {
            }
        }
        double average = count == 0 ? 0.0 : sum / count;
        int size = Math.max(3, (int) Math.round(average % 100));
        return nextPrime(size);
    }

    private static int nextPrime(int value) {
        int candidate = Math.max(2, value);
        while (true) {
            if (isPrime(candidate)) return candidate;
            candidate++;
        }
    }

    private static boolean isPrime(int value) {
        if (value < 2) return false;
        int limit = (int) Math.sqrt(value);
        for (int i = 2; i <= limit; i++) {
            if (value % i == 0) return false;
        }
        return true;
    }

    public boolean addLocation(Location location) {
        if (location == null) throw new IllegalArgumentException("Invalid location object");
        if (locations.containsKey(location.getId())) throw new IllegalArgumentException("Duplicate location ID");
        String lcName = location.getName() == null ? "" : location.getName().toLowerCase();
        for (Location loc : locations.values()) {
            if (loc.getName() != null && loc.getName().toLowerCase().equals(lcName)) {
                throw new IllegalArgumentException("Duplicate location name");
            }
        }
        locations.put(location.getId(), location);
        adjacency.putIfAbsent(location.getId(), new ArrayList<>());
        return true;
    }

    public boolean addRoad(Road road) {
        if (road == null) throw new IllegalArgumentException("Invalid road object");
        if (!locations.containsKey(road.getStartLocation()) || !locations.containsKey(road.getEndLocation())) {
            throw new IllegalArgumentException("Invalid location IDs for road");
        }
        if (!(road.getDistance() > 0)) throw new IllegalArgumentException("Road distance must be greater than zero");
        roads.put(road.getId(), road);
        addEdge(road.getStartLocation(), road.getEndLocation(), road.getId());
        addEdge(road.getEndLocation(), road.getStartLocation(), road.getId());
        return true;
    }

    private void addEdge(String fromId, String toId, String roadId) {
        adjacency.putIfAbsent(fromId, new ArrayList<>());
        adjacency.get(fromId).add(new Edge(toId, roadId));
    }

    public boolean updateRoadStatus(String roadId, String status) {
        List<String> allowed = Arrays.asList("Open", "Busy", "Closed");
        if (!allowed.contains(status)) throw new IllegalArgumentException("Invalid road status");
        Road road = roads.get(roadId);
        if (road == null) throw new IllegalArgumentException("Road not found");
        road.setStatus(status);
        return true;
    }

    public List<Location> searchLocation(String keyword) {
        String lower = keyword == null ? "" : keyword.toLowerCase();
        List<Location> result = new ArrayList<>();
        for (Location loc : locations.values()) {
            if (loc.getName() != null && loc.getName().toLowerCase().contains(lower)) {
                result.add(loc);
            }
        }
        return result;
    }

    public RouteResult findShortestRoute(String sourceId, String destinationId) {
        if (!locations.containsKey(sourceId) || !locations.containsKey(destinationId)) {
            throw new IllegalArgumentException("Source or destination location does not exist");
        }
        Map<String, Double> distances = new HashMap<>();
        Map<String, Prev> previous = new HashMap<>();
        Set<String> visited = new HashSet<>();
        Set<String> queue = new HashSet<>();

        for (String id : locations.keySet()) {
            distances.put(id, Double.POSITIVE_INFINITY);
            queue.add(id);
        }
        distances.put(sourceId, 0.0);

        while (!queue.isEmpty()) {
            String current = null;
            double minDistance = Double.POSITIVE_INFINITY;
            for (String nodeId : queue) {
                double nodeDistance = distances.getOrDefault(nodeId, Double.POSITIVE_INFINITY);
                if (nodeDistance < minDistance) {
                    minDistance = nodeDistance;
                    current = nodeId;
                }
            }
            if (current == null || minDistance == Double.POSITIVE_INFINITY) break;
            queue.remove(current);
            visited.add(current);
            if (current.equals(destinationId)) break;
            List<Edge> neighbours = adjacency.getOrDefault(current, Collections.emptyList());
            for (Edge e : neighbours) {
                String to = e.to;
                String roadId = e.roadId;
                if (visited.contains(to)) continue;
                Road road = roads.get(roadId);
                if (road == null || "Closed".equals(road.getStatus())) continue;
                double weight = weightedCost(road.getDistance());
                double alt = distances.get(current) + weight;
                if (alt < distances.getOrDefault(to, Double.POSITIVE_INFINITY)) {
                    distances.put(to, alt);
                    previous.put(to, new Prev(current, roadId));
                }
            }
        }

        if (!previous.containsKey(destinationId) && !sourceId.equals(destinationId)) {
            return null;
        }

        List<String> path = new ArrayList<>();
        List<String> route = new ArrayList<>();
        String node = destinationId;
        while (node != null) {
            path.add(0, node);
            Prev prev = previous.get(node);
            if (prev == null) break;
            route.add(0, prev.roadId);
            node = prev.node;
        }

        double totalDistance = sumRouteDistance(route);
        double estimatedTimeMinutes = estimateTime(totalDistance);
        return new RouteResult(path, route, totalDistance, estimatedTimeMinutes);
    }

    private static class Prev { final String node; final String roadId; Prev(String node, String roadId) { this.node = node; this.roadId = roadId; } }

    private double sumRouteDistance(List<String> route) {
        double sum = 0.0;
        for (String roadId : route) {
            Road r = roads.get(roadId);
            if (r != null) sum += r.getDistance();
        }
        return sum;
    }

    private double estimateTime(double distanceKm) {
        double averageSpeedKmH = 40.0;
        return (distanceKm / averageSpeedKmH) * 60.0;
    }

    private double weightedCost(double distance) {
        int penalty = sumDigits(indexNumbers) % 5;
        double factor = travelTimeFactor();
        return distance * factor + penalty;
    }

    private double travelTimeFactor() {
        if (indexNumbers.isEmpty()) return 1.2;
        String firstIndex = indexNumbers.get(0) == null ? "" : indexNumbers.get(0).trim();
        if (firstIndex.isEmpty()) return 1.2;
        char lastChar = firstIndex.charAt(firstIndex.length() - 1);
        if (!Character.isDigit(lastChar)) return 1.2;
        int lastDigit = Character.getNumericValue(lastChar);
        return (lastDigit % 2 == 0) ? 1.5 : 1.2;
    }

    private static int sumDigits(List<String> indexNumbers) {
        int sum = 0;
        for (String idx : indexNumbers) {
            if (idx == null) continue;
            for (char c : idx.toCharArray()) {
                if (Character.isDigit(c)) sum += Character.getNumericValue(c);
            }
        }
        return sum;
    }
}
