package performance;

import algorithms.GraphTraversal;
import algorithms.MST;
import algorithms.SearchAlgorithms;
import algorithms.SortAlgorithms;
import com.allocation.structures.BST;
import com.allocation.structures.RedBlackTree;
import database.DatabaseConnection;
import database.DatabaseInitializer;
import database.LocationLoader;
import database.RoadLoader;
import operations.CustomHashTable;
import operations.CustomHeap;
import operations.ShortestPathFinder;
import structures.CustomQueue;
import structures.DisjointSet;
import structures.Graph;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.Comparator;
import java.util.Random;

public class BenchmarkRunner {

    private static final int REPEATS = 3;
    private static final int[] SEARCH_SORT_SIZES = {100, 500, 1000, 5000, 10000};
    private static final int[] TREE_SIZES = {100, 500, 1000, 5000, 10000};
    private static final int[] HASH_INSERT_SIZES = {100, 500, 1000, 5000, 10000, 20000};
    private static final int[] HASH_CAPACITY_FACTORS = {10, 50, 100, 200};
    private static final int[] HEAP_SIZES = {100, 500, 1000, 5000, 10000, 20000};
    private static final int[] GRAPH_SIZES = {50, 100, 200, 500};

    private static final String LOCATIONS_CSV = "src/main/resources/seed_data/Locations.csv";
    private static final String ROADS_CSV = "src/main/resources/seed_data/Roads.csv";

    private final Random random = new Random(204);

    public static void main(String[] args) {
        DatabaseInitializer.createTables();
        ensureCampusSeedData();

        BenchmarkRunner runner = new BenchmarkRunner();
        runner.clearPreviousRuns();
        runner.runAll();

        CsvExporter exporter = new CsvExporter();
        exporter.exportAlgorithmRuns("docs/performance_results/results.csv");
        System.out.println("Done. Results exported.");
    }

    public void runAll() {
        runSearchComparison();
        runSortingComparison();
        runHashLoadFactorExperiment();
        runBstVsBalancedTreeExperiment();
        runHeapDispatchExperiment();
        runGraphAlgorithmsExperiment();
    }

    public void runSearchComparison() {
        for (int n : SEARCH_SORT_SIZES) {
            Integer[] sorted = sortedIntegers(n);
            Integer target = sorted[n / 2];

            long linearAvg = averageNs(() -> {
                Integer[] data = copyOf(sorted);
                SearchAlgorithms.linearSearch(data, target);
            });
            recordRun("linearSearch", n, linearAvg);

            long binaryAvg = averageNs(() -> {
                Integer[] data = copyOf(sorted);
                SearchAlgorithms.binarySearch(data, target);
            });
            recordRun("binarySearch", n, binaryAvg);
        }
    }

    public void runSortingComparison() {
        Comparator<Integer> cmp = Integer::compareTo;

        for (int n : SEARCH_SORT_SIZES) {
            Integer[] base = shuffledIntegers(n);

            long selectionAvg = averageNs(() -> {
                Integer[] data = copyOf(base);
                SortAlgorithms.selectionSort(data, cmp);
            });
            recordRun("selectionSort", n, selectionAvg);

            long insertionAvg = averageNs(() -> {
                Integer[] data = copyOf(base);
                SortAlgorithms.insertionSort(data, cmp);
            });
            recordRun("insertionSort", n, insertionAvg);

            long quickAvg = averageNs(() -> {
                Integer[] data = copyOf(base);
                SortAlgorithms.quickSort(data, cmp);
            });
            recordRun("quickSort", n, quickAvg);
        }
    }

    public void runHashLoadFactorExperiment() {
        for (int capacityFactor : HASH_CAPACITY_FACTORS) {
            int initialCapacity = Math.max(11, capacityFactor * 10);
            for (int n : HASH_INSERT_SIZES) {
                final int[] collisionsHolder = new int[1];

                long insertAvg = averageNs(() -> {
                    CustomHashTable<String, Integer> table = new CustomHashTable<>(capacityFactor);
                    for (int i = 0; i < n; i++) {
                        table.put("KEY-" + i, i);
                    }
                    collisionsHolder[0] = table.getTotalCollisions();
                });

                recordRun("hashInsert_cap" + initialCapacity, n, insertAvg);
                recordRun("hashCollisions_cap" + initialCapacity, n,
                        collisionsHolder[0], currentMemoryKb());
            }
        }
    }

    public void runBstVsBalancedTreeExperiment() {
        for (int n : TREE_SIZES) {
            Integer[] keys = shuffledIntegers(n);
            Integer target = keys[n / 2];

            long bstInsertAvg = averageNs(() -> {
                BST<Integer, Integer> tree = new BST<>();
                for (Integer key : keys) {
                    tree.insert(key, key);
                }
            });
            recordRun("bstInsert", n, bstInsertAvg);

            long rbtInsertAvg = averageNs(() -> {
                RedBlackTree<Integer, Integer> tree = new RedBlackTree<>();
                for (Integer key : keys) {
                    tree.insert(key, key);
                }
            });
            recordRun("rbtInsert", n, rbtInsertAvg);

            BST<Integer, Integer> bst = new BST<>();
            RedBlackTree<Integer, Integer> rbt = new RedBlackTree<>();
            for (Integer key : keys) {
                bst.insert(key, key);
                rbt.insert(key, key);
            }

            long bstSearchAvg = averageNs(() -> bst.search(target));
            long rbtSearchAvg = averageNs(() -> rbt.search(target));
            recordRun("bstSearch", n, bstSearchAvg);
            recordRun("rbtSearch", n, rbtSearchAvg);

            recordRun("bstHeight", n, bst.height(), currentMemoryKb());
            recordRun("rbtHeight", n, rbt.height(), currentMemoryKb());
        }
    }

    public void runHeapDispatchExperiment() {
        for (int n : HEAP_SIZES) {
            Integer[] values = shuffledIntegers(n);

            long insertAvg = averageNs(() -> {
                CustomHeap<Integer> heap = new CustomHeap<>(16, (a, b) -> Integer.compare(b, a));
                for (Integer v : values) {
                    heap.insert(v);
                }
            });
            recordRun("heapInsert", n, insertAvg);

            long extractOnlyAvg = averageExtractOnly(values);
            recordRun("heapExtract", n, extractOnlyAvg);

            long insertExtractAvg = averageNs(() -> {
                CustomHeap<Integer> heap = new CustomHeap<>(16, (a, b) -> Integer.compare(b, a));
                for (Integer v : values) {
                    heap.insert(v);
                }
                while (!heap.isEmpty()) {
                    heap.extractTop();
                }
            });
            recordRun("heapInsertExtract", n, insertExtractAvg);
        }
    }

    private long averageExtractOnly(Integer[] values) {
        long total = 0L;
        for (int r = 0; r < REPEATS; r++) {
            CustomHeap<Integer> heap = new CustomHeap<>(16, (a, b) -> Integer.compare(b, a));
            for (Integer v : values) {
                heap.insert(v);
            }
            long start = System.nanoTime();
            while (!heap.isEmpty()) {
                heap.extractTop();
            }
            total += System.nanoTime() - start;
        }
        return total / REPEATS;
    }

    public void runGraphAlgorithmsExperiment() {
        Graph campusGraph = null;
        ShortestPathFinder dijkstra = null;
        try (Connection conn = DatabaseConnection.connect()) {
            if (conn != null) {
                campusGraph = new Graph();
                campusGraph.loadGraph(conn);
                dijkstra = new ShortestPathFinder();
                dijkstra.loadGraph(conn);
            }
        } catch (SQLException e) {
            System.out.println("Graph load failed: " + e.getMessage());
        }

        boolean campusReady = campusGraph != null
                && campusGraph.isGraphLoaded()
                && campusGraph.getLocationCount() >= 2;

        for (int n : GRAPH_SIZES) {
            int size = n;
            boolean useCampus = campusReady && size <= campusGraph.getLocationCount();

            long bfsAvg;
            if (useCampus) {
                Graph g = campusGraph;
                bfsAvg = averageNs(() -> GraphTraversal.bfs(g, 0));
            } else {
                int[][] adj = syntheticAdj(size, Math.max(size * 2, size + 10));
                bfsAvg = averageNs(() -> bfsSynthetic(adj, 0));
            }
            recordRun("bfs", size, bfsAvg);

            long dfsAvg;
            if (useCampus) {
                Graph g = campusGraph;
                dfsAvg = averageNs(() -> GraphTraversal.dfs(g, 0));
            } else {
                int[][] adj = syntheticAdj(size, Math.max(size * 2, size + 10));
                dfsAvg = averageNs(() -> dfsSynthetic(adj, 0));
            }
            recordRun("dfs", size, dfsAvg);

            if (useCampus && dijkstra != null) {
                ShortestPathFinder spf = dijkstra;
                String start = campusGraph.getLocationIdAt(0);
                String end = campusGraph.getLocationIdAt(campusGraph.getLocationCount() - 1);
                long dijkstraAvg = averageNs(() -> spf.findShortestPath(start, end));
                recordRun("dijkstra", size, dijkstraAvg);
            }

            if (useCampus) {
                Graph g = campusGraph;
                long primAvg = averageNs(() -> MST.prim(g, 0));
                recordRun("prim", size, primAvg);

                long kruskalAvg = averageNs(() -> MST.kruskal(g));
                recordRun("kruskal", size, kruskalAvg);
            } else {
                int[][] edges = syntheticEdges(size, Math.max(size * 2, size + 10));
                long kruskalAvg = averageNs(() -> kruskalSynthetic(size, edges));
                recordRun("kruskal", size, kruskalAvg);
            }
        }
    }

    private static int bfsSynthetic(int[][] adj, int start) {
        boolean[] visited = new boolean[adj.length];
        CustomQueue<Integer> queue = new CustomQueue<>();
        int visitedCount = 0;
        queue.enqueue(start);
        visited[start] = true;
        while (!queue.isEmpty()) {
            int u = queue.dequeue();
            visitedCount++;
            for (int v : adj[u]) {
                if (!visited[v]) {
                    visited[v] = true;
                    queue.enqueue(v);
                }
            }
        }
        return visitedCount;
    }

    private static int dfsSynthetic(int[][] adj, int start) {
        boolean[] visited = new boolean[adj.length];
        return dfsSyntheticRec(adj, start, visited);
    }

    private static int dfsSyntheticRec(int[][] adj, int u, boolean[] visited) {
        visited[u] = true;
        int count = 1;
        for (int v : adj[u]) {
            if (!visited[v]) {
                count += dfsSyntheticRec(adj, v, visited);
            }
        }
        return count;
    }

    private static int kruskalSynthetic(int n, int[][] edges) {
        Integer[] order = new Integer[edges.length];
        for (int i = 0; i < edges.length; i++) {
            order[i] = i;
        }
        SortAlgorithms.quickSort(order, Comparator.comparingInt(i -> edges[i][2]));
        DisjointSet ds = new DisjointSet(n);
        int accepted = 0;
        for (int idx : order) {
            int u = edges[idx][0];
            int v = edges[idx][1];
            if (ds.find(u) != ds.find(v)) {
                ds.union(u, v);
                accepted++;
                if (accepted == n - 1) {
                    break;
                }
            }
        }
        return accepted;
    }

    private int[][] syntheticAdj(int n, int edgeTarget) {
        int[] degrees = new int[n];
        int[][] scratch = new int[edgeTarget][2];
        int edgeCount = 0;
        for (int i = 0; i < n - 1; i++) {
            scratch[edgeCount][0] = i;
            scratch[edgeCount][1] = i + 1;
            degrees[i]++;
            degrees[i + 1]++;
            edgeCount++;
        }
        while (edgeCount < edgeTarget) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u == v) {
                continue;
            }
            scratch[edgeCount][0] = u;
            scratch[edgeCount][1] = v;
            degrees[u]++;
            edgeCount++;
        }
        int[][] adj = new int[n][];
        int[] filled = new int[n];
        for (int i = 0; i < n; i++) {
            adj[i] = new int[degrees[i]];
        }
        for (int e = 0; e < edgeCount; e++) {
            int u = scratch[e][0];
            int v = scratch[e][1];
            adj[u][filled[u]++] = v;
        }
        return adj;
    }

    private int[][] syntheticEdges(int n, int edgeTarget) {
        int[][] edges = new int[edgeTarget][3];
        int edgeCount = 0;
        for (int i = 0; i < n - 1 && edgeCount < edgeTarget; i++) {
            edges[edgeCount][0] = i;
            edges[edgeCount][1] = i + 1;
            edges[edgeCount][2] = 1 + random.nextInt(20);
            edgeCount++;
        }
        while (edgeCount < edgeTarget) {
            int u = random.nextInt(n);
            int v = random.nextInt(n);
            if (u == v) {
                continue;
            }
            edges[edgeCount][0] = u;
            edges[edgeCount][1] = v;
            edges[edgeCount][2] = 1 + random.nextInt(50);
            edgeCount++;
        }
        return edges;
    }

    private void clearPreviousRuns() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement()) {
            stmt.executeUpdate("DELETE FROM algorithm_runs");
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static void ensureCampusSeedData() {
        try (Connection conn = DatabaseConnection.connect();
             Statement stmt = conn.createStatement();
             ResultSet rs = stmt.executeQuery("SELECT COUNT(*) AS c FROM locations")) {
            int count = rs.next() ? rs.getInt("c") : 0;
            if (count > 0) {
                return;
            }
        } catch (SQLException e) {
            System.out.println(e.getMessage());
            return;
        }

        new LocationLoader().loadData(LOCATIONS_CSV);
        new RoadLoader().loadData(ROADS_CSV);
    }

    private long averageNs(Runnable action) {
        long total = 0L;
        for (int i = 0; i < REPEATS; i++) {
            long start = System.nanoTime();
            action.run();
            total += System.nanoTime() - start;
        }
        return total / REPEATS;
    }

    private void recordRun(String algorithmName, int inputSize, long timeNs) {
        recordRun(algorithmName, inputSize, timeNs, currentMemoryKb());
    }

    private void recordRun(String algorithmName, int inputSize, long timeNs, long memoryKb) {
        String dateRun = new Timestamp(System.currentTimeMillis()).toString();
        String sql = "INSERT INTO algorithm_runs (algorithmName, inputSize, timeNs, memoryKb, dateRun) "
                + "VALUES (?, ?, ?, ?, ?)";
        try (Connection conn = DatabaseConnection.connect();
             PreparedStatement ps = conn.prepareStatement(sql)) {
            ps.setString(1, algorithmName);
            ps.setInt(2, inputSize);
            ps.setLong(3, timeNs);
            ps.setLong(4, memoryKb);
            ps.setString(5, dateRun);
            ps.executeUpdate();
        } catch (SQLException e) {
            System.out.println(e.getMessage());
        }
    }

    private static long currentMemoryKb() {
        Runtime rt = Runtime.getRuntime();
        return (rt.totalMemory() - rt.freeMemory()) / 1024L;
    }

    private Integer[] sortedIntegers(int n) {
        Integer[] data = new Integer[n];
        for (int i = 0; i < n; i++) {
            data[i] = i;
        }
        return data;
    }

    private Integer[] shuffledIntegers(int n) {
        Integer[] data = sortedIntegers(n);
        for (int i = n - 1; i > 0; i--) {
            int j = random.nextInt(i + 1);
            Integer tmp = data[i];
            data[i] = data[j];
            data[j] = tmp;
        }
        return data;
    }

    private static Integer[] copyOf(Integer[] source) {
        Integer[] copy = new Integer[source.length];
        System.arraycopy(source, 0, copy, 0, source.length);
        return copy;
    }
}
