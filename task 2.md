# GitHub Issues: Campus Service Hub Optimizer

## 🏗️ Domain Models

### Issue #1: Create `Location` Model
**Assignee Level:** Level 200 Junior
**Description:** Create the `Location.java` POJO in `src/models/` to represent nodes on the campus map.
**Acceptance Criteria:**
- [ ] Fields: `locationId` (String), `name` (String), `area` (String), `type` (String), `coordinates` (String).
- [ ] All fields must be `private`.
- [ ] Include an empty constructor and a fully parameterized constructor.
- [ ] Implement public Getters and Setters for all fields.
- [ ] Override `toString()` for debugging.

### Issue #2: Create `Road` Model
**Assignee Level:** Level 200 Junior
**Description:** Create the `Road.java` POJO in `src/models/` to represent the edges (paths) between locations.
**Acceptance Criteria:**
- [ ] Fields: `fromLocationId` (String), `toLocationId` (String), `distance` (Float), `travelTime` (Float), `roadConditionWeight` (Float).
- [ ] All fields must be `private`.
- [ ] Include constructors, Getters, Setters, and a `toString()` override.

### Issue #3: Create `Resource` Model
**Assignee Level:** Level 200 Junior
**Description:** Create the `Resource.java` POJO in `src/models/` to track deployable campus assets.
**Acceptance Criteria:**
- [ ] Fields: `resourceId` (String), `type` (String), `homeLocation` (String), `capacity` (Integer), `availabilityStatus` (String).
- [ ] All fields must be `private`.
- [ ] Include constructors, Getters, Setters, and a `toString()` override.

### Issue #4: Create `ServiceRequest` Model
**Assignee Level:** Level 200 Junior
**Description:** Create the `ServiceRequest.java` POJO in `src/models/` to track campus incidents.
**Acceptance Criteria:**
- [ ] Fields: `requestId` (String), `source` (String), `destination` (String), `category` (String), `urgency` (Integer), `timeSubmitted` (Timestamp), `deadline` (Timestamp), `status` (String).
- [ ] All fields must be `private`.
- [ ] Include constructors, Getters, Setters, and a `toString()` override.

---

## 💾 Database Initialization

### Issue #5: Implement `DatabaseConnection`
**Assignee Level:** Level 200 Junior
**Description:** Create `DatabaseConnection.java` in `src/database/` to handle the SQLite JDBC connection.
**Acceptance Criteria:**
- [ ] Implement a static `connect()` method returning a `Connection`.
- [ ] Connect to `jdbc:sqlite:campus_hub.db`.
- [ ] Catch `SQLException` and log a clean error message to the console.

### Issue #6: Implement `DatabaseInitializer`
**Assignee Level:** Level 200 Junior
**Description:** Create `DatabaseInitializer.java` in `src/database/` to scaffold the SQLite tables.
**Acceptance Criteria:**
- [ ] Call `DatabaseConnection.connect()`.
- [ ] Execute `CREATE TABLE IF NOT EXISTS` statements for `locations`, `roads`, `resources`, and `service_requests`.
- [ ] Ensure SQL schemas strictly match the Java Model data types.

---

## 📥 Data Ingestion

### Issue #7: Implement `LocationLoader`
**Assignee Level:** Level 200 Junior
**Description:** Create `LocationLoader.java` in `src/database/` to parse `Locations.csv`.
**Acceptance Criteria:**
- [ ] Use `BufferedReader` to read the CSV line-by-line (skip the header).
- [ ] Parse rows and use a `PreparedStatement` to `INSERT INTO locations`.
- [ ] Close all resources in a `finally` block or use try-with-resources.

### Issue #8: Implement `RoadLoader`
**Assignee Level:** Level 200 Junior
**Description:** Create `RoadLoader.java` in `src/database/` to parse `Roads.csv`.
**Acceptance Criteria:**
- [ ] Read and parse the CSV.
- [ ] Insert records into the `roads` table using a `PreparedStatement`.
- [ ] Handle resource closures properly.

### Issue #9: Implement `ResourceLoader`
**Assignee Level:** Level 200 Junior
**Description:** Create `ResourceLoader.java` in `src/database/` to parse `Resources.csv`.
**Acceptance Criteria:**
- [ ] Read and parse the CSV.
- [ ] Insert records into the `resources` table using a `PreparedStatement`.
- [ ] Handle resource closures properly.

### Issue #10: Implement `ServiceRequestLoader`
**Assignee Level:** Level 200 Junior
**Description:** Create `ServiceRequestLoader.java` in `src/database/` to parse `ServiceRequests.csv`.
**Acceptance Criteria:**
- [ ] Read and parse the CSV. Validate Java `Timestamp` parsing.
- [ ] Insert records into the `service_requests` table.
- [ ] Handle resource closures properly.

---

## 🧠 Core Algorithms

### Issue #11: Implement `ShortestPathFinder` (Dijkstra)
**Assignee Level:** Level 300 Senior
**Description:** Create `ShortestPathFinder.java` in `src/operations/` to calculate the fastest route between locations.
**Acceptance Criteria:**
- [ ] Implement Dijkstra’s Algorithm using a `PriorityQueue`.
- [ ] Query the `roads` table to build an Adjacency List dynamically.
- [ ] **Crucial Rule:** Apply a hardcoded `1.5x` travel time multiplier to all roads leading to academic areas.

### Issue #12: Implement `UrgentDispatcher` (Max-Heap)
**Assignee Level:** Level 300 Senior
**Description:** Create `UrgentDispatcher.java` in `src/operations/` to sort incoming service requests by urgency.
**Acceptance Criteria:**
- [ ] Implement a Priority Queue (Max-Heap) using Java's `PriorityQueue` and a custom `Comparator`.
- [ ] Sort `ServiceRequest` objects by their calculated urgency score.
- [ ] **Crucial Rule:** Include logic that detects `IT_SUPPORT` requests and automatically adds `+10` to their priority weight calculation.

### Issue #13: Implement `TicketLookup` (Hash Table)
**Assignee Level:** Level 300 Senior
**Description:** Create `TicketLookup.java` in `src/operations/` for instant lookup of a `ServiceRequest` by `requestId`. 
**Acceptance Criteria:**
- [ ] Implement a custom Hash Table with Chaining (Array of `LinkedList<ServiceRequest>`). Java's `HashMap` is strictly prohibited.
- [ ] Write a custom `hash()` function.
- [ ] **Crucial Rule:** Calculate the sum of the last two digits of the 5 seniors' index numbers, multiply by 10, and hardcode this exact integer as the initial array capacity.

### Issue #14: Implement `ResourceOptimizer` (Greedy)
**Assignee Level:** Level 300 Senior
**Description:** Create `ResourceOptimizer.java` in `src/operations/` to assign available resources to high-priority requests.
**Acceptance Criteria:**
- [ ] Implement a Greedy Algorithm.
- [ ] Query `PENDING` requests and `AVAILABLE` resources.
- [ ] Assign closest capable resources to top-priority tickets (from Issue #12).
- [ ] Execute `UPDATE service_requests SET status = 'ASSIGNED'` in the database. Set unassigned to `QUEUED`.

### Issue #15: Implement `AuditLogSorter` (Merge Sort)
**Assignee Level:** Level 300 Senior
**Description:** Create `AuditLogSorter.java` in `src/operations/` to sort historical service requests chronologically.
**Acceptance Criteria:**
- [ ] Query all records from the database into a List and convert to an Array.
- [ ] Implement a custom, recursive Merge Sort algorithm.
- [ ] Sort objects by the `timeSubmitted` Timestamp using `.before()` / `.after()`.