package algorithms;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

import models.ServiceRequest;

import java.util.HashSet;
import java.util.Set;

/**
 * Part 13 correctness evidence for the 0/1 knapsack DP request selection
 * (Part 12). Reuses the exact hand-solvable example documented in
 * RequestSelectionDP's own class Javadoc and main() method.
 */
class RequestSelectionDPTest {

    private ServiceRequest[] demoRequests() {
        ServiceRequest[] demo = new ServiceRequest[5];
        for (int i = 0; i < demo.length; i++) {
            demo[i] = new ServiceRequest();
            demo[i].setRequestId("DEMO-" + (i + 1));
        }
        return demo;
    }

    @Test
    void normal_knapsackSelection_matchesHandSolvedExample() {
        // Items (value, weight): DEMO-1=(60,10) DEMO-2=(100,20) DEMO-3=(120,30)
        // DEMO-4=(80,15) DEMO-5=(30,5); budget 50.
        // By hand: {DEMO-1, DEMO-2, DEMO-4, DEMO-5} = weight 50, value 270.
        ServiceRequest[] demo = demoRequests();
        int[] value = {60, 100, 120, 80, 30};
        int[] weight = {10, 20, 30, 15, 5};

        RequestSelectionDP.Result result =
                RequestSelectionDP.selectByValueWeight(demo, value, weight, 50);

        assertEquals(270, result.totalValue);
        Set<String> selectedIds = new HashSet<>();
        for (ServiceRequest r : result.selected) selectedIds.add(r.getRequestId());
        assertEquals(Set.of("DEMO-1", "DEMO-2", "DEMO-4", "DEMO-5"), selectedIds);
    }

    @Test
    void boundary_zeroBudget_selectsNothing() {
        ServiceRequest[] demo = demoRequests();
        int[] value = {60, 100, 120, 80, 30};
        int[] weight = {10, 20, 30, 15, 5};

        RequestSelectionDP.Result result =
                RequestSelectionDP.selectByValueWeight(demo, value, weight, 0);

        assertEquals(0, result.totalValue);
        assertEquals(0, result.selected.length);
    }

    @Test
    void boundary_budgetExceedsAllWeights_selectsEverything() {
        ServiceRequest[] demo = demoRequests();
        int[] value = {60, 100, 120, 80, 30};
        int[] weight = {10, 20, 30, 15, 5};

        RequestSelectionDP.Result result =
                RequestSelectionDP.selectByValueWeight(demo, value, weight, 1000);

        assertEquals(60 + 100 + 120 + 80 + 30, result.totalValue);
        assertEquals(5, result.selected.length);
    }

    @Test
    void tableEvidence_tabulationTableFinalCellMatchesReportedTotalValue() {
        ServiceRequest[] demo = demoRequests();
        int[] value = {60, 100, 120, 80, 30};
        int[] weight = {10, 20, 30, 15, 5};

        RequestSelectionDP.Result result =
                RequestSelectionDP.selectByValueWeight(demo, value, weight, 50);

        // Section 10 requires the DP table itself, not just the final answer.
        assertEquals(result.totalValue, result.table[demo.length][50]);
    }
}
