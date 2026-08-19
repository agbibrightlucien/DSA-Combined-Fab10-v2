package algorithms;

import structures.CustomHeap;
import structures.DisjointSet;
import structures.Graph;

import java.util.Comparator;

/**
 * Minimum Spanning Tree algorithms required by Section 7 of the brief:
 * Prim's algorithm (using the adjacency matrix from {@link Graph}) and
 * Kruskal's algorithm (using the adjacency list's edge set plus the
 * {@link DisjointSet} from Part 8).
 *
 * Both methods return an {@link MSTResult} holding the selected edges and
 * the total cost, so the report can show that both algorithms agree on the
 * same total cost for the same input graph -- the standard correctness
 * check for MST algorithms, and the Definition of Done for this part.
 *
 * A note on directionality: roads in this project are loaded as directed
 * edges (see Graph's own Javadoc). Both classic Prim and Kruskal assume an
 * undirected graph. In practice the campus road seed data is bidirectional
 * almost everywhere a spanning tree would be expected to reach, but if a
 * location only has one-way roads pointing away from it and none pointing
 * back, it may be unreachable by Prim from certain start points, and
 * Kruskal may treat (A->B) and (B->A) as two separate directed edges
 * rather than a single undirected one. This is documented here rather than
 * silently patched, since "fix" would mean inventing a reverse edge that
 * does not exist in the roads table -- exactly what Graph's own Javadoc
 * says it deliberately does not do.
 */
public class MST {

    private MST() {
        // static utility class, no instances
    }

    /** One selected edge plus the total cost of the spanning tree it belongs to. */
    public static class MSTResult {
        private final Graph.Edge[] edges;
        private final double totalCost;

        public MSTResult(Graph.Edge[] edges, double totalCost) {
            this.edges = edges;
            this.totalCost = totalCost;
        }

        public Graph.Edge[] getEdges() { return edges; }
        public double getTotalCost() { return totalCost; }

        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("MSTResult{edges=").append(edges.length)
              .append(", totalCost=").append(String.format("%.2f", totalCost)).append("}\n");
            for (Graph.Edge e : edges) {
                sb.append("  ").append(e).append("\n");
            }
            return sb.toString();
        }
    }

    // ------------------------------------------------------------------
    // Prim's algorithm
    // ------------------------------------------------------------------

    /**
     * Prim's algorithm, growing the tree one vertex at a time from
     * startIndex. Uses {@link CustomHeap} (from the existing operations
     * package pattern) to always pick the next cheapest edge that connects
     * a vertex already in the tree to one that is not.
     *
     * CustomHeap is a MAX-heap keyed by an int priority. Since Prim needs
     * the MINIMUM weight edge each step, candidate edges are inserted with
     * priority = -(weight scaled to an int), so the largest (least
     * negative) priority the heap extracts corresponds to the smallest
     * actual weight. Scaling by 1000 before rounding keeps two decimal
     * places of precision from the travelTime * roadConditionWeight
     * weight formula.
     */
    public static MSTResult prim(Graph graph, int startIndex) {
        validateGraph(graph);
        int n = graph.getLocationCount();
        if (startIndex < 0 || startIndex >= n) {
            throw new IndexOutOfBoundsException("prim: start index " + startIndex + " is out of range.");
        }

        boolean[] inTree = new boolean[n];
        Graph.Edge[] selected = new Graph.Edge[Math.max(0, n - 1)];
        int selectedCount = 0;
        double totalCost = 0.0;

        // heap entries are {fromIndex, toIndex}; weight is looked up again
        // at extraction time from the matrix, so the heap never goes stale
        // relative to Graph's own data.
        CustomHeap<int[]> candidateEdges = new CustomHeap<>();

        inTree[startIndex] = true;
        offerEdgesFrom(graph, startIndex, inTree, candidateEdges);

        while (!candidateEdges.isEmpty() && selectedCount < n - 1) {
            int[] edge = candidateEdges.extractMax();
            int from = edge[0];
            int to = edge[1];

            if (inTree[to]) {
                continue; // both endpoints already connected -- would form a cycle
            }

            double weight = graph.getEdgeWeight(from, to);
            inTree[to] = true;
            selected[selectedCount++] = new Graph.Edge(from, to, weight);
            totalCost += weight;

            offerEdgesFrom(graph, to, inTree, candidateEdges);
        }

        return new MSTResult(trim(selected, selectedCount), totalCost);
    }

    /** Adds every not-yet-in-tree edge out of `vertex` onto the candidate heap. */
    private static void offerEdgesFrom(Graph graph, int vertex, boolean[] inTree,
                                        CustomHeap<int[]> candidateEdges) {
        int n = graph.getLocationCount();
        for (int to = 0; to < n; to++) {
            if (to == vertex || inTree[to]) continue;
            double weight = graph.getEdgeWeight(vertex, to);
            if (weight == Graph.NO_EDGE) continue;

            int priority = -(int) Math.round(weight * 1000);
            candidateEdges.insert(new int[]{vertex, to}, priority);
        }
    }

    // ------------------------------------------------------------------
    // Kruskal's algorithm
    // ------------------------------------------------------------------

    /**
     * Kruskal's algorithm: sort every edge by weight ascending, then walk
     * them in order, using {@link DisjointSet} to skip any edge that would
     * connect two locations already in the same component (i.e. would
     * form a cycle).
     *
     * Sorting note: edges are sorted ascending by weight using Part 10's
     * {@link SortAlgorithms#quickSort(Object[], Comparator)}, the same
     * generic, comparator-based quicksort used elsewhere in the project,
     * satisfying the plan's requirement that Kruskal sort edges "using
     * SortAlgorithms from Part 10."
     */
    public static MSTResult kruskal(Graph graph) {
        validateGraph(graph);
        int n = graph.getLocationCount();

        Graph.Edge[] edges = sortEdgesByWeight(graph.getAllEdges());
        DisjointSet disjointSet = new DisjointSet(n);

        Graph.Edge[] selected = new Graph.Edge[Math.max(0, n - 1)];
        int selectedCount = 0;
        double totalCost = 0.0;

        for (Graph.Edge edge : edges) {
            if (selectedCount >= n - 1) break;

            if (disjointSet.find(edge.fromIndex) != disjointSet.find(edge.toIndex)) {
                disjointSet.union(edge.fromIndex, edge.toIndex);
                selected[selectedCount++] = edge;
                totalCost += edge.weight;
            }
        }

        return new MSTResult(trim(selected, selectedCount), totalCost);
    }

    /**
     * Same as {@link #kruskal(Graph)}, but also prints an ACCEPTED /
     * REJECTED line for every candidate edge, in the order considered,
     * with the reason for rejection. This produces the Kruskal trace
     * table required as report evidence in both Part 8 and Part 11 (which
     * edges were accepted or rejected and why, using the DisjointSet).
     */
    public static MSTResult kruskalWithTrace(Graph graph) {
        validateGraph(graph);
        int n = graph.getLocationCount();

        Graph.Edge[] edges = sortEdgesByWeight(graph.getAllEdges());
        DisjointSet disjointSet = new DisjointSet(n);

        Graph.Edge[] selected = new Graph.Edge[Math.max(0, n - 1)];
        int selectedCount = 0;
        double totalCost = 0.0;

        System.out.println("Kruskal trace (" + edges.length + " candidate edges, sorted by weight):");
        System.out.println(String.format("%-6s %-6s %-6s %-10s %-10s", "Step", "From", "To", "Weight", "Decision"));

        int step = 1;
        for (Graph.Edge edge : edges) {
            String decision;
            if (selectedCount >= n - 1) {
                decision = "SKIPPED (tree already has " + (n - 1) + " edges)";
            } else if (disjointSet.find(edge.fromIndex) != disjointSet.find(edge.toIndex)) {
                disjointSet.union(edge.fromIndex, edge.toIndex);
                selected[selectedCount++] = edge;
                totalCost += edge.weight;
                decision = "ACCEPTED";
            } else {
                decision = "REJECTED (would form a cycle)";
            }

            System.out.println(String.format("%-6d %-6d %-6d %-10s %-10s",
                    step++, edge.fromIndex, edge.toIndex,
                    String.format("%.2f", edge.weight), decision));
        }

        return new MSTResult(trim(selected, selectedCount), totalCost);
    }

    /** Ascending comparator by edge weight, used to drive SortAlgorithms.quickSort. */
    private static final Comparator<Graph.Edge> BY_WEIGHT_ASCENDING =
            (a, b) -> Double.compare(a.weight, b.weight);

    /**
     * Ascending sort of edges by weight, delegating to
     * {@link SortAlgorithms#quickSort(Object[], Comparator)} on a defensive
     * copy so the caller's array from graph.getAllEdges() is never mutated.
     */
    private static Graph.Edge[] sortEdgesByWeight(Graph.Edge[] edges) {
        Graph.Edge[] copy = new Graph.Edge[edges.length];
        System.arraycopy(edges, 0, copy, 0, edges.length);
        SortAlgorithms.quickSort(copy, BY_WEIGHT_ASCENDING);
        return copy;
    }

    private static Graph.Edge[] trim(Graph.Edge[] array, int count) {
        Graph.Edge[] trimmed = new Graph.Edge[count];
        System.arraycopy(array, 0, trimmed, 0, count);
        return trimmed;
    }

    private static void validateGraph(Graph graph) {
        if (!graph.isGraphLoaded()) {
            throw new IllegalStateException("MST: graph.loadGraph() must be called before running Prim/Kruskal.");
        }
    }
}