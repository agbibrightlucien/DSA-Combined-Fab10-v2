# M1 Problem Specification

## Project Overview

This document defines the problem scope, requirements, and deliverables for the M1 assignment of the DSA Combined Fab10 project.

## Problem Statement

The system must ingest, process, and analyze city infrastructure data from CSV seed files, including service requests, locations, roads, and resources. The key goal is to support efficient data access and provide clear outputs for the assignment tasks.

## Operation 4: Resource Allocation Optimization

### Problem Definition

Assign available resources to service requests using a greedy resource matcher that maximizes immediate suitability. The algorithm should match the best available resource to each request based on priority, resource type compatibility, and proximity, while respecting availability constraints.

### Inputs

- List of service requests, each with:
  - request ID
  - priority level
  - required resource type(s)
  - location or area
  - requested service time window
- List of available resources, each with:
  - resource ID
  - resource type or skill set
  - current status (available / busy / offline)
  - current or home location
  - capacity or quantity available
- Optional: distance metric or travel time between request location and resource location.

### Outputs

- Assignment list mapping service request IDs to chosen resource IDs.
- Unassigned requests list when no compatible resource is available.
- Summary report showing matches, unfulfilled requests, and allocation decisions.

### Assumptions

- Each request needs at most one primary resource assignment.
- Resources are either available or unavailable at the allocation time; partial availability is not considered.
- Priority is the primary driver when selecting which request to satisfy first.
- Resource types are compatible using a predefined compatibility matrix or direct type match.
- Travel distance or estimated response time can be approximated using location data.

### Constraints

- A resource can be assigned to only one request at the same time.
- A request cannot be assigned to an incompatible resource type.
- Requests with higher priority should be matched before lower priority requests.
- The algorithm should run efficiently for the dataset sizes expected in the assignment.
- The matcher should not reassign a resource already committed to a request in the current allocation pass.

### Greedy Algorithm Pseudocode

```
function allocateResources(requests, resources):
    assignments = []
    unassigned = []

    sort requests by descending priority, then by earliest requested time
    mark all resources as available

    for each request in requests:
        bestResource = null
        bestScore = -infinity

        for each resource in resources:
            if resource is not available:
                continue
            if not isCompatible(resource, request):
                continue

            score = computeMatchScore(resource, request)
            if score > bestScore:
                bestScore = score
                bestResource = resource

        if bestResource is not null:
            assign bestResource to request
            bestResource.available = false
            assignments.append({requestId: request.id, resourceId: bestResource.id})
        else:
            unassigned.append(request.id)

    return {assignments: assignments, unassigned: unassigned}

function computeMatchScore(resource, request):
    score = 0
    if resource.type == request.requiredType:
        score += 100
    if isPreferredSkill(resource, request):
        score += 50
    score -= distance(resource.location, request.location)
    score -= travelTime(resource.location, request.location) / 10
    score += resource.capacity
    return score
```

### Notes

- The greedy matcher chooses a locally optimal resource for each request without backtracking.
- If multiple resources score equally, tie-break on proximity or availability time.
- The algorithm can be extended later to support batching, multi-resource requests, or a cost-minimization objective.

## Scope

- Load seed data from `src/main/resources/seed_data`
- Model the domain entities for locations, roads, resources, and service requests
- Implement search, filtering, and reporting capabilities
- Ensure correct handling of CSV parsing and data relationships

## Functional Requirements

1. Read and validate input data from provided CSV files.
2. Build in-memory or persisted data structures to represent entities.
3. Support queries for service requests by location, status, priority, and resource assignment.
4. Generate summary reports for infrastructure metrics.
5. Include documentation for how the M1 solution is designed and validated.

## Non-Functional Requirements

- Code should be clean, modular, and easy to maintain.
- The solution should handle malformed CSV rows gracefully.
- The repository must include a clear team sign-off process.

## Deliverables

- `M1_Specification.md`
- Updated source code implementing the required features
- Test cases or validation steps demonstrating correct behavior

## Team Sign-off

- [ ] Team review completed
- [ ] Requirements validated against assignment expectations
- [ ] Pull request opened for final approval
