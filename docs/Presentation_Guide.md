# Ghana Smart Service Operations Optimizer — Official Presentation & Oral Defense Guide

**Project Name:** Ghana Smart Service Operations Optimizer (Campus Service Hub)  
**Course:** DCIT 204 / DCIT 308 — Data Structures and Algorithms I & II (Joint Semester Project)  
**Institution:** University of Ghana, Legon — Department of Computer Science  
**Project Brief Foundation:** [`Joint_DSA_Project_Brief.docx`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/Joint_DSA_Project_Brief.docx)  
**Group Leader:** Bright Lucien Jnr Agbi (Team Fab10)  

---

## 📌 Executive Presentation Matrix & Rubric Mapping

| Presentation Phase | Brief Section & Rubric Alignment | Time | Key Objective & Defense Focus |
| :--- | :--- | :--- | :--- |
| **1. Local Problem Design** | **Brief Sec. 1 & 2** (10 Marks) | 1.5 mins | UG Legon campus context: 50 locations, 272 roads, 30 resources, 300 requests. |
| **2. Architectural Foundation** | **Brief Sec. 6 & 8** (20 Marks) | 2 mins | 100% custom data structure library; zero `java.util` collections allowed. |
| **3. AI-Resistance Rules** | **Brief Sec. 2 & 15** (Index Rules) | 2 mins | Rules 1, 2, & 3 derived from member student index numbers. |
| **4. Core Algorithmic Engine** | **Brief Sec. 5 & 7** (20 Marks) | 5 mins | Dijkstra, Priority Heap Dispatch, $O(1)$ Hash Table, Greedy/DP, Merge Sort. |
| **5. Empirical Lab & Database** | **Brief Sec. 4, 9, & 10** (25 Marks) | 2.5 mins | SQLite schema + 6 benchmark experiments ($N=100$ to $20,000$) & 78 tests. |
| **6. Individual Oral Defense** | **Brief Sec. 2, 12, & 15** (10 Marks) | 2 mins | 1-structure + 1-algorithm per member defense & live examiner modification readiness. |

---

## 🎤 Official Presentation Script (Grounded in Project Brief)

---

### PART 1: Project Overview & Ghanaian Local Context (Section 1 & 2)

#### 💬 Presentation Script:
> "Good morning/afternoon, esteemed Lecturers and Examiners. My name is **Bright Lucien Jnr Agbi**, Group Leader for **Team Fab10**, and today I am presenting our **Ghana Smart Service Operations Optimizer — Campus Service Hub**.
>
> Grounded directly in Section 2 of our **Joint DSA Project Brief**, our system models real-world operational bottlenecks across the University of Ghana, Legon campus. Managing campus operations requires handling four core data entities stored in a persistent SQLite database (`campus_hub.db`):
> 1. **50 Locations:** Campus landmarks including hostels (Volta Hall), lecture halls (JQB), shuttle stops, and labs.
> 2. **272 Road Edges:** Distance, travel time, and road condition weights across campus paths.
> 3. **30 Resources:** Available staff, technicians, and vehicles with home locations and capacities.
> 4. **300 Service Requests:** Incoming maintenance, IT, and sanitation requests with submitted timestamps and strict deadlines.
>
> Rather than relying on generic software, our engine ingests local campus data, prioritizes emergency requests, calculates shortest dispatch routes, and optimizes resource allocations."

#### 🔑 Examiner Rubric Focus (10 Marks):
* **Local Ghana Context:** University of Ghana, Legon campus service hub.
* **Database Schema:** `locations`, `roads`, `resources`, `service_requests`, `algorithm_runs`, `audit_events`.

---

### PART 2: Custom Data Structure Library (Section 6 & 8 Constraint)

#### 💬 Presentation Script:
> "As mandated by Section 8 of the Project Brief, **built-in Java collection utilities (`java.util.HashMap`, `TreeMap`, `PriorityQueue`, `Stack`, `ArrayDeque`) were strictly forbidden**. 
>
> Our team built the entire required data structure library **from scratch** inside the [`src/structures/`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures) package:
>
> 1. [`DynamicArray<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/DynamicArray.java) — Custom resizable array implementing $O(1)$ amortized geometric expansion.
> 2. [`CustomLinkedList<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomLinkedList.java), [`CustomStack<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomStack.java), & [`CustomQueue<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomQueue.java) — Memory-efficient node-based containers.
> 3. [`CustomHeap<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomHeap.java) — Binary Min/Max Heap serving as the canonical priority queue across Dijkstra, Priority Dispatch, and Prim's MST.
> 4. [`CustomHashTable<K, V>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/CustomHashTable.java) — Separate-chaining hash table with automatic load factor resizing.
> 5. [`BST<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/BST.java), [`RedBlackTree<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/RedBlackTree.java), & [`BTree<T>`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/BTree.java) — Hierarchical index trees supporting $O(\log n)$ search and page indexing.
> 6. [`Graph`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/Graph.java) & [`DisjointSet`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/DisjointSet.java) — Adjacency list network graph and Union-Find with path compression and rank optimization for Kruskal's MST."

#### 🔑 Examiner Rubric Focus (20 Marks):
* Fully verified custom implementations with boundary-case unit tests.
* Memory management and Type Erasure handling using `Object[]` backing storage.

---

### PART 3: AI-Resistance & Index-Derived Rules (Section 2 & 15)

#### 💬 Presentation Script:
> "To comply with Section 2 and Section 15 of the brief for AI-resistance and team localization, we derived three critical algorithmic parameters directly from our team members' student index numbers:
>
> * **Rule 1 — Traffic Penalty Rule (Group Leader Index):** Derived from the group leader's index digit. A **1.5x travel time multiplier** is dynamically applied during graph traversal to roads leading to academic areas (e.g., JQB) during peak hours.
> * **Rule 2 — Hash Table Capacity Rule:** Summing the last two digits of our Level 300 members' index numbers and multiplying by 10 established an exact initial hash table capacity of **100 slots**, optimizing initial load factor.
> * **Rule 3 — Priority Shift Rule:** From index `22244923`, the digit sum reduced to **1** ($2+2+2+4+4+9+2+3 = 28 \rightarrow 2+8=10 \rightarrow 1+0=1$). Falling in the 1–4 range, all `IT_SUPPORT` requests automatically receive a **+10 priority weight increase**."

#### 🔑 Examiner Rubric Focus (10 Marks):
* Proof of uniqueness: Algorithm outputs change predictably if student index parameters are modified.

---

### PART 4: Required Modules & Core Operations (Section 5, 7, & M1-M10)

#### 💬 Presentation Script:
> "Our platform implements all required operations across Modules M1 through M10:
>
> #### Operation 1: Shortest Path Finder ([`ShortestPathFinder.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/ShortestPathFinder.java))
> * **Algorithm:** Dijkstra’s Shortest Path Algorithm powered by our custom [`CustomHeap`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomHeap.java) (Min-Heap).
> * **Details:** Factors in distance, travel time, road conditions, and injects the Rule 1 traffic multiplier. Complexity: $O((V + E) \log V)$.
>
> #### Operation 2: Urgent Dispatcher ([`UrgentDispatcher.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/UrgentDispatcher.java))
> * **Algorithm:** Dynamic Priority Queue (Max-Heap).
> * **Formula:** $\text{Priority Score} = \text{Base Urgency} + \text{Deadline Proximity Boost} + \text{Rule 3 Category Boost (+10)}$.
> * **Smart Dispatch:** Pairs top-priority requests with the *nearest available resource* by querying Dijkstra travel time.
>
> #### Operation 3: Fast Ticket Lookup Engine ([`TicketLookupEngine.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/TicketLookupEngine.java))
> * **Algorithm:** Custom Hash Table ([`CustomHashTable`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/CustomHashTable.java)) with Separate Chaining.
> * **Details:** Delivers $O(1)$ average access for instant ticket and location lookups, maintaining load factor $\le 0.75$.
>
> #### Operation 4: Resource Allocation & Network MST ([`ResourceOptimizer.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/ResourceOptimizer.java))
> * **Algorithm:** Greedy Heuristic for resource pairing, plus Prim's & Kruskal's MST (using [`DisjointSet`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/DisjointSet.java)) for campus infrastructure connectivity.
> * **Counterexample:** Includes documented evidence where greedy allocation produces sub-optimal global assignments.
>
> #### Operation 5: Audit Log Sorting ([`AuditLogSorter.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/AuditLogSorter.java))
> * **Algorithm:** Merge Sort ($O(n \log n)$ stable sort) to order audit logs and requests by timestamp or urgency."

#### 🔑 Examiner Rubric Focus (20 Marks):
* Complete algorithmic coverage: Dijkstra, Prim, Kruskal, Greedy, DP, Hash Table, Merge Sort.

---

### PART 5: Database, Testing, & Empirical Benchmarking (Section 4, 9, & 10)

#### 💬 Presentation Script:
> "Per Sections 4, 9, and 10 of the Project Brief, we established full system verification and empirical reporting:
>
> 1. **Database Integration:** SQLite persistent database (`campus_hub.db`) storing all 4 seed datasets and logging timing data into the `algorithm_runs` table.
> 2. **Correctness & Unit Testing:** **78 successful unit tests** covering normal, boundary, and edge cases, accompanied by trace tables (Dijkstra, Merge Sort, Hash Load Factor) and proof sketches in `CorrectnessEvidence.md`.
> 3. **Empirical Efficiency Lab:** Automated benchmarks ([`BenchmarkRunner`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/performance/BenchmarkRunner.java)) running 6 mandatory experiments across input sizes $N = 100$ to $20,000$:
>    * Search: Linear $O(n)$ vs. Binary Search $O(\log n)$.
>    * Sorting: Selection/Insertion $O(n^2)$ vs. Merge/Quick Sort $O(n \log n)$.
>    * Hash Load Factor: Collision rate vs. bucket size.
>    * BST vs. Red-Black Tree height comparisons.
>    * Priority Heap dispatch scaling.
>    * Graph algorithms (BFS/DFS/Dijkstra/MST)."

#### 🔑 Examiner Rubric Focus (30 Marks):
* Empirical graphs and timing tables exported to `docs/performance_results/results.csv`.

---

### PART 6: Oral Defense Preparation (Section 2, 12, 15 & Rubric)

#### ⚠️ MANDATORY REQUIREMENT FROM SECTION 2 & 12 OF BRIEF:
> *"Every member must defend one data structure and one algorithm during the oral demonstration."*

#### Member Defense Allocation Matrix

| Team Role / Member | Primary Data Structure Defense | Primary Algorithm Defense | Key Source File |
| :--- | :--- | :--- | :--- |
| **Group Leader (Bright)** | [`Graph`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/Graph.java) (Adjacency List) | Dijkstra Shortest Path + Rule 1 | [`ShortestPathFinder.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/ShortestPathFinder.java) |
| **Member 2** | [`CustomHeap`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/CustomHeap.java) (Min/Max Heap) | Priority Dispatch + Rule 3 | [`UrgentDispatcher.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/UrgentDispatcher.java) |
| **Member 3** | [`CustomHashTable`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/CustomHashTable.java) | Separate Chaining Hash Lookup | [`TicketLookupEngine.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/TicketLookupEngine.java) |
| **Member 4** | [`DisjointSet`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/DisjointSet.java) | Prim's & Kruskal's MST | [`ResourceOptimizer.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/ResourceOptimizer.java) |
| **Member 5** | [`DynamicArray`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/DynamicArray.java) | Merge Sort ($O(n \log n)$) | [`AuditLogSorter.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/AuditLogSorter.java) |
| **Member 6** | [`RedBlackTree`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/RedBlackTree.java) / [`BTree`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/structures/BTree.java) | Indexing & Binary Search | [`RequestIndexSimulator.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/RequestIndexSimulator.java) |

---

### 🧪 Live Code Modification Test Defense (Section 15)

> *"The examiner may ask students to modify a priority rule, add a new location, change a hash table size, or rerun an algorithm live."*

#### 1. How to Modify Priority Rule Live (Rule 3):
* Go to [`UrgentDispatcher.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/UrgentDispatcher.java).
* Locate category weight assignment logic and change `IT_SUPPORT` boost from `+10` to `+15` or switch target category to `SANITATION`.

#### 2. How to Modify Hash Table Initial Size (Rule 2):
* Go to [`ShortestPathFinder.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/ShortestPathFinder.java#L120) or [`CustomHashTable.java`](file:///c:/Users/agbib/IdeaProjects/DSA%20Combined%20Fab10%20v2/src/operations/CustomHashTable.java).
* Change initial capacity from `100` to a requested size (e.g., `50`).

#### 3. How to Rerun Benchmarks Live:
* Open terminal and execute:
  ```bash
  java -cp "lib/*:out/main" performance.BenchmarkRunner
  ```
* Show the examiner the printed runtime output and updated `algorithm_runs` table entries in SQLite.
