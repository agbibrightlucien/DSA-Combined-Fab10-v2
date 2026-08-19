# Performance Results (Part 14)

Owner: Abigailnanama

## Machine specification

All runs in `results.csv` were collected on one machine:

| Field | Value |
| --- | --- |
| CPU | Intel(R) Core(TM) i5-1035G1 CPU @ 1.00GHz |
| RAM | 7.7 GB |
| OS | Microsoft Windows 11 Pro (10.0.26200) |
| Java version | Java SE 25.0.1 LTS (HotSpot 64-Bit Server VM) |

## How to regenerate results

1. Run `performance.BenchmarkRunner` to refresh `results.csv`.
2. Open `results.csv` in Excel (or open `performance_charts.xlsx`).
3. For each experiment, chart `inputSize` against `timeNs` as a line chart with markers.
4. Save each chart as PNG into `graphs/`.

## Charts

Charts were made in Excel from `results.csv` and exported as PNG.  
The editable workbook is `performance_charts.xlsx`.

| File | Content |
| --- | --- |
| `graphs/01_search_comparison.png` | Linear vs binary search |
| `graphs/02_sorting_comparison.png` | Selection, insertion, and quicksort |
| `graphs/03a_hash_collisions.png` | Hash collisions by capacity |
| `graphs/03b_hash_insert_time.png` | Hash insert time by capacity |
| `graphs/04a_tree_insert.png` | BST vs Red-Black insert time |
| `graphs/04b_tree_search.png` | BST vs Red-Black search time |
| `graphs/04c_tree_height.png` | BST vs Red-Black height |
| `graphs/05_heap_dispatch.png` | Heap insert and extract |
| `graphs/06_graph_algorithms.png` | BFS, DFS, Dijkstra, Prim, Kruskal |

Each plotted point is the average of three timed repeats.

## Implementation mapping

| Experiment | Classes used |
| --- | --- |
| Search comparison | `algorithms.SearchAlgorithms` |
| Sorting comparison | `algorithms.SortAlgorithms` |
| Hash table load factor | `operations.CustomHashTable` |
| BST vs balanced tree | `com.allocation.structures.BST`, `com.allocation.structures.RedBlackTree` |
| Heap priority dispatch | `operations.CustomHeap` |
| Graph algorithms | `algorithms.GraphTraversal`, `algorithms.MST`, `operations.ShortestPathFinder` |

Campus-sized graph timings use the seeded University of Ghana road network (50 locations, 272 roads). Larger sizes in the growth curves use synthetic graphs where the campus dataset is too small.

## Theory versus observed runtime

### 1. Search comparison

Linear search is O(n). Binary search is O(log n) on sorted input.

Measured times (ns):

| n | linearSearch | binarySearch |
| ---: | ---: | ---: |
| 1000 | 25,133 | 93,633 |
| 5000 | 121,900 | 286,966 |
| 10000 | 257,433 | 660,833 |

Binary search is slower than linear search here because `binarySearch` first scans the array to confirm it is sorted. That O(n) check dominates the O(log n) comparisons, so the measured curve does not look like classic binary search.

### 2. Sorting comparison

Selection sort and insertion sort are O(n²). Quicksort is O(n log n) on average.

Measured times (ns):

| n | selectionSort | insertionSort | quickSort |
| ---: | ---: | ---: | ---: |
| 1000 | 10,411,066 | 5,337,833 | 218,266 |
| 5000 | 55,103,800 | 17,806,966 | 530,766 |
| 10000 | 208,186,800 | 74,544,233 | 1,238,133 |

From n = 5000 to n = 10000, selection and insertion grow roughly four times, while quicksort grows much more slowly. This matches the expected gap between quadratic and n-log-n sorting.

### 3. Hash table load factor

Average-case hash insert is O(1). Collision counts should rise as the table gets fuller or starts with a smaller capacity.

The collision series in `results.csv` increase with n, and smaller initial capacities produce more collisions than larger ones for the same n. Insert time also rises gently because of resizing and longer chains. That behaviour matches separate chaining.

### 4. BST versus balanced tree

An unbalanced BST can reach O(n) height. A red-black tree keeps height O(log n).

Measured heights:

| n | bstHeight | rbtHeight |
| ---: | ---: | ---: |
| 100 | 15 | 8 |
| 1000 | 20 | 13 |
| 10000 | 29 | 16 |

At n = 10000 the red-black tree height is 16, close to log₂(10000) ≈ 13.3, while the plain BST reaches height 29 on the same key set. Search times favour the red-black tree at larger n as well.

### 5. Heap priority dispatch

Binary-heap insert and extract-top are O(log n). Draining n items is O(n log n).

After small-n warm-up noise, both insert and extract grow with n from 1000 to 20000, but far more slowly than the O(n²) sorts. That is consistent with logarithmic per-operation cost.

### 6. Graph algorithms

BFS and DFS are O(V + E). Dijkstra with a binary heap is about O((V + E) log V). Prim and Kruskal are dominated by priority-queue or edge-sorting work.

Campus graph timings (50 locations, 272 roads):

| algorithm | average ns |
| --- | ---: |
| bfs | 435,666 |
| dfs | 34,400 |
| dijkstra | 593,566 |
| prim | 1,617,533 |
| kruskal | 844,133 |

Absolute nanosecond values include JVM overhead, so comparisons are most meaningful within this same machine and run set.
