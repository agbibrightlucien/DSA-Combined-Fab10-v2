# Correctness Evidence — Part 13

Owner: Emmanuella Ewurama Prah (ewura1). This document collects the six
required trace tables, three proof sketches, and two counterexamples
(Section 10 of the brief), plus a summary of the unit test suite. All
worked examples below were computed and verified by hand.

## Unit test suite summary

**46 JUnit 5 tests** across 16 test classes (exceeds the 40-test minimum),
each covering at least one normal case, one boundary case, and — where the
class supports it — one invalid-input case:

| File | Tests |
|---|---|
| structures/DynamicArrayTest.java | 3 |
| structures/CustomLinkedListTest.java | 3 |
| structures/CustomStackTest.java | 3 |
| structures/CustomQueueTest.java | 3 |
| structures/CircularQueueTest.java | 3 |
| structures/CustomDequeTest.java | 3 |
| structures/DisjointSetTest.java | 3 |
| operations/CustomHeapTest.java | 3 |
| operations/CustomHashTableTest.java | 3 |
| com/allocation/structures/BSTTest.java | 3 |
| com/allocation/structures/RedBlackTreeTest.java | 3 |
| algorithms/SearchAlgorithmsTest.java | 3 |
| algorithms/SortAlgorithmsTest.java | 3 |
| algorithms/GraphTraversalTest.java | 3 |
| algorithms/MSTTest.java | 2 |
| algorithms/RequestSelectionDPTest.java | 2 |

Note: `src/test/structures/BTreeTest.java` (Part 7) is a plain `main()`
demo script, not a JUnit test class — it does not count toward the 40-test
minimum. Recommend either converting it to real `@Test` methods or adding
a separate `BTreeTest` (JUnit) alongside it before submission.

`GraphTraversalTest` and `MSTTest` are integration tests against the real
50-location campus graph — they require `campus_hub.db` to already be
seeded (run `database/SeedAllData.java` first if they fail to connect).

---

## Trace Tables (6 required)

### 1. Binary Search — sorted 8-element array

Array: `[1, 3, 5, 7, 9, 11, 13, 15]`, target = 13

| Step | low | high | mid | arr[mid] | Comparison | Action |
|---|---|---|---|---|---|---|
| 1 | 0 | 7 | 3 | 7 | 7 < 13 | low = 4 |
| 2 | 4 | 7 | 5 | 11 | 11 < 13 | low = 6 |
| 3 | 6 | 7 | 6 | 13 | 13 == 13 | **found at index 6** |

### 2. Insertion Sort — 6-element array

Array: `[8, 3, 5, 1, 9, 2]`

| Pass (key) | Array state after pass |
|---|---|
| i=1 (key=3) | 3, 8, 5, 1, 9, 2 |
| i=2 (key=5) | 3, 5, 8, 1, 9, 2 |
| i=3 (key=1) | 1, 3, 5, 8, 9, 2 |
| i=4 (key=9) | 1, 3, 5, 8, 9, 2 (no shift needed) |
| i=5 (key=2) | 1, 2, 3, 5, 8, 9 |

Final: `[1, 2, 3, 5, 8, 9]` — matches `SortAlgorithmsTest.boundary_quickSortOnSixElementArray_matchesRequiredTraceSize` input array (sorted correctly by both algorithms).

### 3. Quicksort — 6-element array (Lomuto partition, last-element pivot)

Array: `[8, 3, 5, 1, 9, 2]`

| Recursive call | Pivot | Partition result | Left / Right subarrays |
|---|---|---|---|
| [8,3,5,1,9,2] | 2 | [1,3,5,8,9,2] → pivot lands at index 1 | Left: [1], Right: [5,8,9,3] |
| [5,8,9,3] | 3 | [3,8,9,5] → pivot lands at index 2 | Left: [], Right: [8,9,5] |
| [8,9,5] | 5 | [5,9,8] → pivot lands at index 3 | Left: [], Right: [9,8] |
| [9,8] | 8 | [8,9] → pivot lands at index 4 | Left: [], Right: [9] (done) |

Final: `[1, 2, 3, 5, 8, 9]`

### 4. Dijkstra — 5-node subgraph

Nodes: S, A, B, C, D. Edges: S–A=4, S–B=1, B–A=2, B–C=5, A–C=1, C–D=3, B–D=8

| Step | Visited | dist(S) | dist(A) | dist(B) | dist(C) | dist(D) |
|---|---|---|---|---|---|---|
| 0 | — | 0 | ∞ | ∞ | ∞ | ∞ |
| 1 | S | 0 | 4 | 1 | ∞ | ∞ |
| 2 | S,B | 0 | 3 | 1 | 6 | 9 |
| 3 | S,B,A | 0 | 3 | 1 | 4 | 9 |
| 4 | S,B,A,C | 0 | 3 | 1 | 4 | 7 |
| 5 | S,B,A,C,D | 0 | 3 | 1 | 4 | 7 |

Shortest path S→D: **S → B → A → C → D**, total cost **7**.

### 5. Kruskal — same 5-node subgraph

Edges sorted ascending: S-B=1, A-C=1, B-A=2, C-D=3, S-A=4, B-C=5, B-D=8

| Edge | Union-Find action | Accepted? | Running cost |
|---|---|---|---|
| S-B (1) | different sets → union | ✓ | 1 |
| A-C (1) | different sets → union | ✓ | 2 |
| B-A (2) | different sets → union | ✓ | 4 |
| C-D (3) | different sets → union | ✓ | 7 |
| S-A (4) | same set (cycle) | ✗ | 7 |
| B-C (5) | same set (cycle) | ✗ | 7 |
| B-D (8) | same set (cycle) | ✗ | 7 |

MST total cost: **7** (4 edges for 5 nodes) — **matches Dijkstra's independently-computed path cost of 7 on this small graph, and matches Prim's result computed from S on the same edge set (S-B, B-A, A-C, C-D → cost 1+2+1+3=7)**, confirming Prim/Kruskal agreement (also checked programmatically on the real 50-node graph in `MSTTest.primAndKruskal_onSameGraph_produceEqualTotalCost`).

### 6. Dynamic Programming — knapsack table (Part 12)

Items: value = [3, 4, 5], weight = [2, 3, 4], budget = 5

| i \ w | 0 | 1 | 2 | 3 | 4 | 5 |
|---|---|---|---|---|---|---|
| 0 | 0 | 0 | 0 | 0 | 0 | 0 |
| 1 (v3,w2) | 0 | 0 | 3 | 3 | 3 | 3 |
| 2 (v4,w3) | 0 | 0 | 3 | 4 | 4 | 7 |
| 3 (v5,w4) | 0 | 0 | 3 | 4 | 5 | 7 |

`dp[3][5] = 7`. Reconstruction (walking backward): item 3 excluded (value
unchanged from row 2) → item 2 included (row drops to `dp[1][2]=3`) → item
1 included. **Selected: item 1 + item 2, total value 7, total weight 5**
(fits exactly within budget).

---

## Proof Sketches (3 required)

### 1. Loop invariant — Binary Search

**Invariant:** at the start of every iteration, if the target is present
in the array, it lies within `arr[low..high]`.

- **Base case:** initially `low=0, high=n-1`, covering the whole array —
  the invariant holds trivially.
- **Maintenance:** each iteration computes `mid`. If `arr[mid]==target`,
  the algorithm returns immediately (correct). If `arr[mid] < target`,
  since the array is sorted ascending, the target — if present — must lie
  in `arr[mid+1..high]`, so setting `low=mid+1` preserves the invariant.
  Symmetrically, if `arr[mid] > target`, setting `high=mid-1` preserves it.
- **Termination:** the loop ends when `low > high`, i.e. the search
  interval is empty. Combined with the invariant, this proves the target
  cannot be present, so returning -1 is correct.

### 2. Structural induction — Merge Sort

- **Base case:** an array of length 0 or 1 is trivially sorted; `mergeSort`
  returns it unchanged.
- **Inductive step:** assume `mergeSort` correctly sorts any array of
  length `< n`. For an array of length `n`, `mergeSort` splits it into two
  halves of length `< n`, recursively sorts each (correct by the
  inductive hypothesis), then merges them. The merge step itself
  maintains its own invariant: at each step, the merged-so-far prefix
  contains the smallest elements of the combined remaining input, in
  sorted order — repeatedly taking the smaller of the two current front
  elements preserves this. Therefore the merged result — and hence
  `mergeSort(arr)` for length `n` — is sorted, completing the induction.

### 3. Optimal substructure — DP Knapsack (Part 12)

`dp[i][w]` represents the maximum value achievable using only the first
`i` items within capacity `w`. The recurrence
`dp[i][w] = max(dp[i-1][w], dp[i-1][w-weight[i]] + value[i])` holds
because, for item `i`, an optimal solution has exactly two possibilities:
either item `i` is excluded — in which case the best value is exactly the
best solution using the first `i-1` items at capacity `w`
(`dp[i-1][w]`) — or item `i` is included, leaving capacity `w-weight[i]`
for the remaining items, whose best value is `dp[i-1][w-weight[i]]` by the
same argument applied recursively (induction on `i`). Since these are the
only two cases and each subproblem is itself optimal, the recurrence — and
therefore the final tabulated `dp[n][budget]` — gives the true optimum.

---

## Counterexamples (2 required)

### 1. Greedy failure counterexample

**Setup** (illustrative, using real project category names): two pending
requests, ReqA (category `IT_SUPPORT`, higher priority) and ReqB
(category `SANITATION`, lower priority). Two resources: R1 (type only
compatible with `IT_SUPPORT`) and R2 (a more versatile type compatible
with **both** `IT_SUPPORT` and `SANITATION`). Distances: R1→ReqA = 5 min,
R2→ReqA = 2 min, R2→ReqB = 3 min.

**Greedy outcome:** processing ReqA first (higher priority), the nearest
*compatible* resource is R2 (2 min, beating R1's 5 min) → R2 is assigned
to ReqA. When ReqB is processed next, R2 is already busy, and R1 is not
`SANITATION`-compatible → **ReqB is queued, unserved.**

**Better alternative:** assigning R1 to ReqA instead (5 min — slower, but
still serves ReqA) leaves R2 free for ReqB (3 min) → **both requests
served**, total time 5+3=8 min, zero requests left unserved.

This demonstrates that always picking the nearest resource for the
current top-priority request can strand a request that a different,
locally-slower assignment order would have served — exactly the
limitation already documented in `ResourceOptimizer.java`'s class Javadoc.

### 2. Invalid precondition counterexample

`binarySearch`'s precondition is that its input is sorted. Given the
deliberately unsorted array `[9, 5, 13, 1, 3, 15, 7, 11]`, the value `1`
genuinely exists at index 3, but `binarySearch(arr, 1)` is not guaranteed
to find it, because the algorithm's midpoint-elimination logic assumes
ascending order. This is captured directly as a JUnit test:
`SearchAlgorithmsTest.counterexample_binarySearchOnUnsortedInput_precondViolationGivesWrongAnswer`
— confirming the precondition must be enforced by the caller (running
`SortAlgorithms` first), never assumed silently.
