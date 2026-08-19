package algorithms;


import models.ServiceRequest;
import operations.ShortestPathFinder; // ASSUMPTION: ShortestPathFinder lives in
                                       // package "operations" (ResourceOptimizer.java
                                       // uses it with no import, meaning same package).
                                       // If it actually lives elsewhere, this is a
                                       // one-line fix.

/**
 * Part 12 - Dynamic Programming Module: Optimal Daily Request Selection.
 *
 * Selects the subset of PENDING service requests that maximises total
 * priority value while staying within a fixed daily capacity budget,
 * using a 0/1 knapsack formulation solved by bottom-up tabulation.
 *
 *   - value(request)  = urgency score + deadline-proximity boost
 *   - weight(request) = estimated service cost, taken as the travel time
 *                        (in whole minutes) from the request's source to
 *                        its destination, via ShortestPathFinder - the
 *                        same tool ResourceOptimizer already uses for its
 *                        own travel-time comparisons.
 *   - budget           = fixed daily capacity (see Rule 4 below)
 *
 * Rule 4 (The DP Budget Rule - M1_Specification.md Section 3 style):
 * Using the index number of the team member implementing this module
 * (Jerri, index number 22345323), all digits are summed
 * (2+2+3+4+5+3+2+3 = 24), then multiplied by 10 to produce the fixed
 * daily capacity constraint. Budget = 240.
 *
 * Custom DSA used:
 *   - Bottom-up DP tabulation table (int[][]) - no recursion, no memo map
 *   - Plain arrays throughout (count-then-fill pattern), consistent with
 *     ResourceOptimizer - avoids java.util.ArrayList
 *
 * See {@link #main(String[])} for a small hand-solvable example used to
 * verify correctness independently of the database/graph.
 */
public class RequestSelectionDP {

    /** Rule 4: fixed daily capacity budget for the knapsack (see class Javadoc). */
    public static final int BUDGET = 240;

    /**
     * Result bundle: the selected requests, the total value achieved, and
     * the FULL tabulation table - kept (not discarded) because Section 10
     * requires the table itself as correctness evidence, not just the
     * final selected set.
     */
    public static class Result {
        public final ServiceRequest[] selected;
        public final int totalValue;
        public final int[][] table;

        public Result(ServiceRequest[] selected, int totalValue, int[][] table) {
            this.selected = selected;
            this.totalValue = totalValue;
            this.table = table;
        }
    }

    // ------------------------------------------------------------------
    // Main entry point (real usage - wired to ShortestPathFinder)
    // ------------------------------------------------------------------

    /**
     * Runs the DP selection over all given pending requests, using the
     * real Rule-4 budget.
     *
     * @param requests   PENDING service requests to consider today
     * @param pathFinder a ShortestPathFinder with the campus graph already
     *                   loaded (pathFinder.loadGraph(conn) called beforehand
     *                   by the caller, same pattern as ResourceOptimizer)
     */
    public static Result select(ServiceRequest[] requests, ShortestPathFinder pathFinder) {
        int n = requests.length;
        int[] value  = new int[n];
        int[] weight = new int[n];

        for (int i = 0; i < n; i++) {
            value[i]  = computeValue(requests[i]);
            weight[i] = computeCost(requests[i], pathFinder);
        }

        return selectByValueWeight(requests, value, weight, BUDGET);
    }

    // ------------------------------------------------------------------
    // Core DP logic (parameterised by explicit value/weight/budget so it
    // can be hand-verified in isolation - see main() below)
    // ------------------------------------------------------------------

    public static Result selectByValueWeight(ServiceRequest[] requests, int[] value,
                                              int[] weight, int budget) {
        int n = requests.length;

        // 1. Bottom-up 0/1 knapsack tabulation.
        //    rows = requests (1..n), columns = budget levels (0..budget)
        int[][] dp = new int[n + 1][budget + 1];
        for (int i = 1; i <= n; i++) {
            int w = weight[i - 1];
            int v = value[i - 1];
            for (int b = 0; b <= budget; b++) {
                if (w <= b) {
                    dp[i][b] = Math.max(dp[i - 1][b], dp[i - 1][b - w] + v);
                } else {
                    dp[i][b] = dp[i - 1][b];
                }
            }
        }

        // 2. Reconstruction: walk the table backward to find exactly which
        //    requests were chosen.
        boolean[] chosenFlag = new boolean[n];
        int b = budget;
        for (int i = n; i >= 1; i--) {
            if (dp[i][b] != dp[i - 1][b]) {
                chosenFlag[i - 1] = true;
                b -= weight[i - 1];
            }
        }

        // 3. Count-then-fill into a plain array (no ArrayList), matching
        //    ResourceOptimizer's loadPendingRequests() style.
        int count = 0;
        for (boolean f : chosenFlag) if (f) count++;
        ServiceRequest[] selected = new ServiceRequest[count];
        int idx = 0;
        for (int i = 0; i < n; i++) {
            if (chosenFlag[i]) selected[idx++] = requests[i];
        }

        return new Result(selected, dp[n][budget], dp);
    }

    // ------------------------------------------------------------------
    // Value / weight derivation
    // ------------------------------------------------------------------

    /**
     * Knapsack value = urgency score (ServiceRequest.getUrgencyScore())
     * plus a deadline-proximity boost, so a request nearing its deadline
     * can outrank one that only just arrived, even at the same urgency.
     */
    private static int computeValue(ServiceRequest r) {
        return r.getUrgencyScore() + deadlineProximityBoost(r);
    }

    private static int deadlineProximityBoost(ServiceRequest r) {
        if (r.getDeadline() == null) return 0;
        long msToDeadline = r.getDeadline().getTime() - System.currentTimeMillis();
        long hoursToDeadline = msToDeadline / (1000L * 60 * 60);

        if (hoursToDeadline <= 0)  return 20; // overdue / due now
        if (hoursToDeadline <= 6)  return 15;
        if (hoursToDeadline <= 24) return 10;
        if (hoursToDeadline <= 72) return 5;
        return 0;
    }

    /**
     * Knapsack weight = estimated service cost, taken as the travel time
     * (rounded to the nearest whole minute) from the request's source to
     * its destination. A request whose route is unreachable is given a
     * cost one unit over budget so it can never be selected.
     */
    private static int computeCost(ServiceRequest r, ShortestPathFinder pathFinder) {
        ShortestPathFinder.PathResult result =
                pathFinder.findShortestPath(r.getSource(), r.getDestination());
        if (result == null) return BUDGET + 1;
        return (int) Math.round(result.totalTime);
    }

    // ------------------------------------------------------------------
    // Reporting helper
    // ------------------------------------------------------------------

    /** Prints the full tabulation table - required as evidence by Section 10. */
    public static void printTable(int[][] table) {
        for (int i = 0; i < table.length; i++) {
            StringBuilder row = new StringBuilder("row " + i + ": ");
            for (int bCol = 0; bCol < table[i].length; bCol++) {
                row.append(table[i][bCol]).append(' ');
            }
            System.out.println(row.toString().trim());
        }
    }

    // ------------------------------------------------------------------
    // Hand-solvable correctness check (Definition of Done requirement)
    // ------------------------------------------------------------------

    /**
     * Small hand-solvable example, deliberately using a SMALL toy budget
     * (50) - NOT the real Rule-4 budget (240) - so the correct answer can
     * be worked out by hand and compared to the program's output.
     *
     * Items (value, weight):
     *   DEMO-1 = (60, 10)   DEMO-2 = (100, 20)   DEMO-3 = (120, 30)
     *   DEMO-4 = (80, 15)   DEMO-5 = (30, 5)
     *
     * By hand: the best combination under budget 50 is
     * {DEMO-1, DEMO-2, DEMO-4, DEMO-5} = weight 10+20+15+5 = 50,
     * value 60+100+80+30 = 270 (DEMO-3 is excluded - adding it would
     * exceed the budget with any combination that beats 270).
     */
    public static void main(String[] args) {
        ServiceRequest[] demo = new ServiceRequest[5];
        int[] demoValue  = {60, 100, 120, 80, 30};
        int[] demoWeight = {10, 20, 30, 15, 5};
        for (int i = 0; i < demo.length; i++) {
            demo[i] = new ServiceRequest();
            demo[i].setRequestId("DEMO-" + (i + 1));
        }

        Result r = selectByValueWeight(demo, demoValue, demoWeight, 50);

        System.out.println("Expected (hand-solved): DEMO-1 DEMO-2 DEMO-4 DEMO-5, total value 270");
        StringBuilder actual = new StringBuilder("Actual:   ");
        for (ServiceRequest sr : r.selected) actual.append(sr.getRequestId()).append(' ');
        System.out.println(actual.toString().trim());
        System.out.println("Total value: " + r.totalValue);
    }
}