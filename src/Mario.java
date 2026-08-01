import java.util.ArrayList;
import java.util.HashMap;
import java.util.HashSet;
import java.util.List;
import java.util.Map;
import java.util.PriorityQueue;
import java.util.Set;

/**
 * Smart Route Planning System for the University of Ghana Campus.
 *
 * This class implements core operations from the Milestone 1 problem
 * specification document, including location management, road
 * management, shortest route calculation, road status updates, and
 * search functionality.
 */
public class Mario {
    static class Location {
        int id;
        String name;
        double latitude;
        double longitude;
        String category;

        Location(int id, String name, double latitude, double longitude, String category) {
            this.id = id;
            this.name = name;
            this.latitude = latitude;
            this.longitude = longitude;
            this.category = category;
        }
    }

    static class Road {
        int id;
        int startLocation;
        int endLocation;
        double distance;
        String status;

        Road(int id, int startLocation, int endLocation, double distance, String status) {
            this.id = id;
            this.startLocation = startLocation;
            this.endLocation = endLocation;
            this.distance = distance;
            this.status = status;
        }

        boolean isOpen() {
            return !"Closed".equalsIgnoreCase(status);
        }
    }

    static class RouteResult {
        List<Integer> path;
        double totalDistance;
        double estimatedTime;

        RouteResult(List<Integer> path, double totalDistance, double estimatedTime) {
            this.path = path;
            this.totalDistance = totalDistance;
            this.estimatedTime = estimatedTime;
        }
    }

    private final Map<Integer, Location> locations = new HashMap<>();
    private final Map<Integer, Road> roads = new HashMap<>();
    private final Map<Integer, List<Road>> adjacency = new HashMap<>();

    public boolean addLocation(int id, String name, double latitude, double longitude, String category) {
        if (locations.containsKey(id)) {
            return false;
        }
        Location location = new Location(id, name, latitude, longitude, category);
        locations.put(id, location);
        adjacency.putIfAbsent(id, new ArrayList<>());
        return true;
    }

    public boolean addRoad(int roadId, int startLocation, int endLocation, double distance, String status) {
        if (distance <= 0 || roads.containsKey(roadId)) {
            return false;
        }
        if (!locations.containsKey(startLocation) || !locations.containsKey(endLocation)) {
            return false;
        }
        Road road = new Road(roadId, startLocation, endLocation, distance, status);
        roads.put(roadId, road);
        adjacency.computeIfAbsent(startLocation, k -> new ArrayList<>()).add(road);
        adjacency.computeIfAbsent(endLocation, k -> new ArrayList<>()).add(road);
        return true;
    }

    public boolean updateRoadStatus(int roadId, String status) {
        Road road = roads.get(roadId);
        if (road == null) {
            return false;
        }
        String normalized = status.trim();
        if (!"Open".equalsIgnoreCase(normalized) && !"Busy".equalsIgnoreCase(normalized) && !"Closed".equalsIgnoreCase(normalized)) {
            return false;
        }
        road.status = normalized;
        return true;
    }

    public List<Location> searchLocation(String keyword) {
        List<Location> result = new ArrayList<>();
        if (keyword == null || keyword.isEmpty()) {
            return result;
        }
        String query = keyword.toLowerCase();
        for (Location location : locations.values()) {
            if (location.name.toLowerCase().contains(query)) {
                result.add(location);
            }
        }
        return result;
    }

    public RouteResult findShortestRoute(int source, int destination, List<Integer> indexNumbers) {
        if (!locations.containsKey(source) || !locations.containsKey(destination)) {
            return null;
        }

        Set<Integer> visited = new HashSet<>();
        Map<Integer, Double> distanceMap = new HashMap<>();
        Map<Integer, Integer> parentMap = new HashMap<>();

        for (int nodeId : locations.keySet()) {
            distanceMap.put(nodeId, Double.POSITIVE_INFINITY);
        }
        distanceMap.put(source, 0.0);

        PriorityQueue<int[]> queue = new PriorityQueue<>((a, b) -> Double.compare(a[1], b[1]));
        queue.add(new int[] { source, 0 });

        while (!queue.isEmpty()) {
            int[] entry = queue.poll();
            int current = entry[0];
            if (visited.contains(current)) {
                continue;
            }
            visited.add(current);
            if (current == destination) {
                break;
            }
            for (Road road : adjacency.getOrDefault(current, List.of())) {
                if (!road.isOpen()) {
                    continue;
                }
                int neighbor = road.startLocation == current ? road.endLocation : road.startLocation;
                if (visited.contains(neighbor)) {
                    continue;
                }
                double weightedDistance = applyIndexNumberRules(road.distance, indexNumbers);
                double newDistance = distanceMap.get(current) + weightedDistance;
                if (newDistance < distanceMap.get(neighbor)) {
                    distanceMap.put(neighbor, newDistance);
                    parentMap.put(neighbor, current);
                    queue.add(new int[] { neighbor, (int) newDistance });
                }
            }
        }

        if (!parentMap.containsKey(destination) && source != destination) {
            return null;
        }

        List<Integer> path = new ArrayList<>();
        int node = destination;
        path.add(node);
        while (node != source) {
            node = parentMap.get(node);
            path.add(0, node);
        }

        double totalDistance = calculatePathDistance(path);
        double estimatedTime = estimateTravelTime(totalDistance);
        return new RouteResult(path, totalDistance, estimatedTime);
    }

    private double applyIndexNumberRules(double distance, List<Integer> indexNumbers) {
        if (indexNumbers == null || indexNumbers.isEmpty()) {
            return distance;
        }
        boolean hasEvenLastDigit = false;
        int penalty = 0;
        for (int index : indexNumbers) {
            int lastDigit = Math.abs(index) % 10;
            if (lastDigit % 2 == 0) {
                hasEvenLastDigit = true;
            }
            penalty += sumDigits(index);
        }
        penalty = penalty % 5;
        double weightMultiplier = hasEvenLastDigit ? 1.5 : 1.2;
        return distance * weightMultiplier + penalty;
    }

    private int sumDigits(int value) {
        int sum = 0;
        int number = Math.abs(value);
        while (number > 0) {
            sum += number % 10;
            number /= 10;
        }
        return sum;
    }

    private double calculatePathDistance(List<Integer> path) {
        double total = 0;
        for (int i = 0; i < path.size() - 1; i++) {
            int start = path.get(i);
            int end = path.get(i + 1);
            for (Road road : adjacency.getOrDefault(start, List.of())) {
                if ((road.startLocation == start && road.endLocation == end) || (road.startLocation == end && road.endLocation == start)) {
                    total += road.distance;
                    break;
                }
            }
        }
        return total;
    }

    private double estimateTravelTime(double distanceKilometers) {
        double averageSpeedKmh = 5.0;
        return (distanceKilometers / averageSpeedKmh) * 60.0;
    }

    public int hashTableSize(List<Integer> indexNumbers) {
        if (indexNumbers == null || indexNumbers.isEmpty()) {
            return 101;
        }
        double average = indexNumbers.stream().mapToInt(Integer::intValue).average().orElse(0);
        int candidate = ((int) average) % 100;
        if (candidate < 2) {
            candidate = 2;
        }
        while (!isPrime(candidate)) {
            candidate++;
        }
        return candidate;
    }

    private boolean isPrime(int value) {
        if (value < 2) {
            return false;
        }
        for (int i = 2; i * i <= value; i++) {
            if (value % i == 0) {
                return false;
            }
        }
        return true;
    }
}
