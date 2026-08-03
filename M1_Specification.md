# M1 Specification Document: Ghana Smart Service Operations Optimizer

**Local Context:** University of Ghana Campus Service Hub  
**Group Leader:** Bright Lucien Jnr Agbi  
**Department:** Department of Computer Science, University of Ghana  

---

## 1. Problem Statement and System Boundaries
The **Ghana Smart Service Operations Optimizer** is designed to manage and resolve operational bottlenecks across the University of Ghana campus. The system ingests service requests, prioritizes urgent jobs, allocates campus resources, and calculates the most efficient travel routes between campus locations. The boundaries of this system include local operational data processing via custom data structures, temporary memory management of requests, and persistent storage in a PostgreSQL database. It does not include a complex graphical user interface.

---

## 2. Dataset and Database Schema Draft
The system operates on a localized dataset representing the university environment.

### Data Dictionary
- **Locations**: Campus landmarks (minimum 50 records). Fields include `locationId` (String), `name` (String), `area` (String), `type` (String), and `coordinates` (String).
- **Roads**: Weighted edges connecting locations (minimum 100 records). Fields include `fromLocationId` (String), `toLocationId` (String), `distance` (Float), `travelTime` (Integer), and `roadConditionWeight` (Float).
- **Resources**: Available personnel and assets (minimum 30 records). Fields include `resourceId` (String), `type` (String), `homeLocation` (String), `capacity` (Integer), and `availabilityStatus` (String).
- **Service Requests**: Logged campus issues (minimum 300 records). Fields include `requestId` (String), `source` (String), `destination` (String), `category` (String), `urgency` (String), `timeSubmitted` (Timestamp), `deadline` (Timestamp), and `status` (String).

### PostgreSQL Schema Draft
```sql
CREATE TABLE locations (
    locationId VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    area VARCHAR(50),
    type VARCHAR(50),
    coordinates VARCHAR(100)
);

CREATE TABLE roads (
    fromLocationId VARCHAR(50),
    toLocationId VARCHAR(50),
    distance FLOAT,
    travelTime INT,
    roadConditionWeight FLOAT,
    PRIMARY KEY (fromLocationId, toLocationId)
);

CREATE TABLE resources (
    resourceId VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50),
    homeLocation VARCHAR(50),
    capacity INT,
    availabilityStatus VARCHAR(20)
);

CREATE TABLE service_requests (
    requestId VARCHAR(50) PRIMARY KEY,
    source VARCHAR(50),
    destination VARCHAR(50),
    category VARCHAR(50),
    urgency VARCHAR(20),
    timeSubmitted TIMESTAMP,
    deadline TIMESTAMP,
    status VARCHAR(20)
);
```

---

## 3. Index Number Algorithm Rules
To ensure AI resistance and precise localization, the following three parameters are derived directly from the index numbers of our team members.

- **Rule 1 (The Traffic Penalty Rule):** Using the last digit of the Group Leader's (Bright Lucien Jnr Agbi) index number. If the digit is even, the `travelTime` of all roads leading to residential areas (e.g., Volta Hall) is multiplied by 1.5 during the Graph traversal to simulate peak student traffic. If odd, the 1.5 multiplier is applied to academic areas (e.g., JQB) instead.
- **Rule 2 (The Hash Table Capacity Rule):** The initial capacity of the custom Hash Table is determined by summing the last two digits of the five Level 300 members' index numbers, then multiplying that sum by 10.
- **Rule 3 (The Priority Shift Rule):** Using the index number of the team member implementing the Priority Queue. The digits of their index number are added together until a single digit remains. If this digit is between 1 and 4, `IT_SUPPORT` requests receive an automatic priority weight increase of 10. If the digit is between 5 and 9, `SANITATION` requests receive the weight increase.

---

## 4. Major System Operations
The system executes five core computational problems.

### Operation 1: The Shortest Path Finder
- **Problem Definition:** A campus shuttle or IT technician requires the fastest route from a starting location to an endpoint on the University of Ghana campus.
- **Inputs:** `startLocationId` (String), `endLocationId` (String).
- **Outputs:** An ordered list of `locationId` nodes representing the optimal route and the total accumulated travel time.
- **Assumptions:** All road distances and travel times are non-negative values. The graph is connected for all requested paths.
- **Constraints:** The algorithm must factor in the `roadConditionWeight`, penalizing routes with heavy traffic or poor road conditions.

#### Pseudocode
```text
function findShortestPath(graph, startNode, endNode)
    create distanceMap and set all distances to infinity
    set distanceMap[startNode] to 0
    create priorityQueue and add startNode with distance 0
    create previousNodeMap to track the path

    while priorityQueue is not empty
        currentNode = priorityQueue.extractMin()

        if currentNode equals endNode
            break

        for each neighbor of currentNode in graph
            calculate weight = neighbor.travelTime * neighbor.roadConditionWeight
            newDistance = distanceMap[currentNode] + weight

            if newDistance is less than distanceMap[neighbor]
                set distanceMap[neighbor] to newDistance
                set previousNodeMap[neighbor] to currentNode
                add neighbor to priorityQueue with newDistance

    return reconstructPath(previousNodeMap, startNode, endNode)
```

---

### Operation 2: The Urgent Dispatcher
- **Problem Definition:** When a new service request enters the system, it must be evaluated against all currently pending requests and assigned to the nearest available resource in order of true priority not just submission time. Urgency level, approaching deadlines, and the Rule 3 category weight boost must all be factored into a single priority score so that the most critical jobs are dispatched first, even if they arrived later than less urgent ones.
- **Inputs:**
  - `newRequest` (`ServiceRequest` object: `requestId`, `source`, `destination`, `category`, `urgency`, `timeSubmitted`, `deadline`)
  - `resourcePool` (list of available `Resource` objects)
- **Outputs:**
  - `assignedResourceId` (String); the resource dispatched to handle the request
  - `updatedRequestStatus` (String); e.g., `"IN_PROGRESS"` or `"QUEUED"` if no resource is free
- **Assumptions:**
  - Every request eventually receives a priority score, even low-urgency ones (they simply sink to the bottom of the queue rather than being discarded).
  - Resource availability is checked in real time; a resource marked `"BUSY"` cannot be double-assigned.
  - Deadlines are always set after `timeSubmitted` (no already-expired requests enter the queue).
- **Constraints:**
  - The priority score must incorporate the Rule 3 weight increase: since the Priority Queue implementer's index number (22244923) reduces to a single digit of 1 ($2+2+2+4+4+9+2+3 = 28 \rightarrow 2+8 = 10 \rightarrow 1+0 = 1$), and 1 falls in the 1–4 range, `IT_SUPPORT` requests automatically receive a +10 priority weight increase.
  - Requests approaching their deadline must gain urgency dynamically over time (a low-urgency request nearing its deadline should eventually outrank a newer high-urgency one).
  - Dispatch must select the nearest available resource to the request's destination, not simply the first free one; this ties Operation 2 to the shortest-path logic in Operation 1.

#### Pseudocode
```text
function dispatchUrgentRequest(requestQueue, resourcePool, graph)
    create priorityQueue (max-heap) ordered by priorityScore

    for each request in requestQueue
        baseScore = getUrgencyWeight(request.urgency)
        timeToDeadline = request.deadline - currentTime()
        urgencyBoost = calculateDeadlineProximityBoost(timeToDeadline)

        categoryBoost = 0
        if request.category equals "IT_SUPPORT"
            categoryBoost = 10   // Rule 3: digit = 1, falls in 1-4 range

        priorityScore = baseScore + urgencyBoost + categoryBoost
        insert request into priorityQueue with priorityScore

    while priorityQueue is not empty
        topRequest = priorityQueue.extractMax()

        candidateResources = filter resourcePool where availabilityStatus equals "AVAILABLE"
        if candidateResources is empty
            set topRequest.status to "QUEUED"
            continue

        nearestResource = null
        shortestTime = infinity
        for each resource in candidateResources
            travelTime = findShortestPath(graph, resource.homeLocation, topRequest.destination).totalTime
            if travelTime < shortestTime
                shortestTime = travelTime
                nearestResource = resource

        assign topRequest to nearestResource
        set nearestResource.availabilityStatus to "BUSY"
        set topRequest.status to "IN_PROGRESS"

    return dispatchLog
```

---

### Operation 3: Fast Ticket Lookup
- **Problem Definition:** Instead of scanning every record one at a time, it uses a hash table to help the system locate a specific record, such as the `requestId` or `locationName` key. The hash table uses a hash function to convert a key into an integer index so that direct access to the matching item in an underlying array of buckets or slots is possible.
- **Inputs:**
  - Custom hash table populated with service requests and location records.
  - `SearchKey` (String: `requestId` e.g., `22044996` or `locationName` e.g., `"Balme Library"`).
- **Outputs:**
  - All the fields of the matching record if the key exists in the table.
  - If no record matches the key, a "not found" result shows.
- **Assumptions:**
  - All keys in the hash table are unique (no two records share the same `requestId`).
  - The hash table has already been built and populated by the database loader (M2) before any search runs.
  - A hash function and a collision-handling method (chaining) have already been defined for the table.
- **Constraints:**
  - Average search time must be $O(1)$; performance should not depend on how many records are in the table.
  - The operation must correctly handle collisions (two keys hashing to the same bucket) without losing or overwriting data.
  - The table must maintain an acceptable load factor, resizing when needed to avoid excessive collisions.

#### Pseudocode
```text
function searchHashTable(hashTable, key)
    index = hashFunction(key) mod hashTable.size

    if hashTable.buckets[index] is empty
        return "not found"
    for each entry in hashTable.buckets[index]
        if entry.key equals key
            return entry.record
    return "not found"
```

---

### Operation 4: Resource Allocation Optimization
- **Problem Definition:** When multiple service requests are pending, the system must assign each request to an available resource that best matches the request's category, capacity requirements, and location proximity. Using a Greedy heuristic approach, the algorithm processes requests in decreasing order of priority score and matches each request with the nearest compatible available resource to maximize resource utilization and minimize response delay.
- **Inputs:**
  - `pendingRequests`: Priority-ordered list of `ServiceRequest` objects.
  - `resourcePool`: List of `Resource` objects (each with `resourceId`, `type`, `homeLocation`, `capacity`, and `availabilityStatus`).
  - `graph`: The campus road network graph.
- **Outputs:**
  - `allocationMap`: Mapping of `requestId` to `assignedResourceId`.
  - `unassignedRequests`: List of requests queued when no compatible resource is available.
- **Assumptions:**
  - Resources marked `"AVAILABLE"` have non-zero capacity and can accept assignments immediately.
  - A greedy selection strategy (matching highest priority requests to nearest available resources) produces an efficient operational allocation.
  - Once assigned, a resource's status updates to `"BUSY"`.
- **Constraints:**
  - The matching must enforce compatibility between resource type and request category.
  - A resource cannot be assigned to multiple concurrent active requests if its capacity is exceeded.
  - If no compatible available resource exists, the request's status is set to `"QUEUED"`.

#### Pseudocode
```text
function optimizeResourceAllocation(pendingRequests, resourcePool, graph)
    create allocationMap as empty map
    create unassignedRequests as empty list

    for each request in pendingRequests
        bestResource = null
        minTravelTime = infinity

        for each resource in resourcePool
            if resource.availabilityStatus equals "AVAILABLE" and isCompatible(resource.type, request.category)
                travelTime = findShortestPath(graph, resource.homeLocation, request.destination).totalTime
                if travelTime < minTravelTime
                    minTravelTime = travelTime
                    bestResource = resource

        if bestResource is not null
            allocationMap[request.requestId] = bestResource.resourceId
            bestResource.availabilityStatus = "BUSY"
            request.status = "ASSIGNED"
        else
            request.status = "QUEUED"
            append request to unassignedRequests

    return allocationMap, unassignedRequests
```

---

### Operation 5: Audit Log Sorting
- **Problem Definition:** The system needs to sort a large collection of records like service requests or algorithm performance log in order based on a specific field so that staff and reports can view them in order instead of in whatever order they were inserted.
- **Inputs:** The same collection of records, returned in ascending or descending order by the chosen key.
- **Outputs:** Sorted collection of records by the specified key.
- **Assumptions:**
  - All records have a valid, comparable value for the sort key (no nulls/missing timestamps).
  - The array fits in memory (no need for external/disk-based sorting).
  - Records with equal keys can appear in any relative order (or, if stability matters, Merge Sort preserves original order for ties).
- **Constraints:**
  - Must achieve $O(n \log n)$ time complexity in the average and worst case.
  - Merge Sort requires $O(n)$ additional space for the merge step — this should be noted since it's a trade-off against in-place alternatives like Quick Sort.
  - Must remain correct on edge cases: empty array, single element, already-sorted input, all-duplicate keys.

#### Pseudocode
```text
function mergeSort(records, sortKey)
    if length(records) <= 1
        return records

    mid = length(records) / 2 
    left = mergeSort(records[0…mid], sortKey)
    right = mergeSort(records[mid…end], sortKey)
    return merge(left, right, sortKey)

function merge(left, right, sortKey)
    result = empty list
    while left is not empty and right is not empty
        if left[0][sortKey] <= right[0][sortKey]
            append left[0] to result; remove left[0]
        else 
            append right[0] to result; remove right[0]
    append remaining elements of left to result
    append remaining elements of right to result
    return result
```
