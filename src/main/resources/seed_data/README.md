# Dataset Collection Phase - Ghana Smart Service Operations Optimizer

## Overview
This directory contains the initial seed data files for the local context operational database supporting the **Ghana Smart Service Operations Optimizer** at the University of Ghana, Legon campus. The dataset models campus locations, road networks, available service resources, and operational service requests.

---

## Data Dictionary & Formatting Guide

### 1. Locations (`Locations.csv`)
| Field | Data Type | Description | Formatting Rules |
| :--- | :--- | :--- | :--- |
| `locationId` | String | Unique identifier for a campus location. | Format: `LOC-` followed by 3 digits (e.g., `LOC-001`). Must be unique across all records. |
| `name` | String | Official name of the facility or building on the University of Ghana, Legon campus. | Free-form text (e.g., `Department of Computer Science`, `Balme Library`). |
| `area` | String | Zone or sector within the campus. | Text string describing the campus area (e.g., `Academic Area`, `Central Campus`). |
| `type` | String | Functional classification of the location. | Standard category string (e.g., `Academic`, `Library`, `Administrative`, `Residential`). |
| `coordinates` | String | Exact Google Maps latitude and longitude coordinates. | Must be enclosed in double quotes containing latitude and longitude separated by a comma (e.g., `"5.65460676594273, -0.18386035360020642"`). |

---

### 2. Roads (`Roads.csv`)
| Field | Data Type | Description | Formatting Rules |
| :--- | :--- | :--- | :--- |
| `fromLocationId` | String | Origin location ID for the road segment. | Must reference a valid `locationId` in `Locations.csv` (e.g., `LOC-001`). |
| `toLocationId` | String | Destination location ID for the road segment. | Must reference a valid `locationId` in `Locations.csv` (e.g., `LOC-002`). |
| `distance` | Float | Physical distance between the two locations in kilometers. | Positive decimal value (e.g., `1.2`). |
| `travelTime` | Integer / Float | Estimated normal travel duration in minutes between locations. | Positive numeric value (e.g., `5`). |
| `roadConditionWeight` | Float | Penalty multiplier representing traffic congestion, road surface quality, or terrain difficulty. | Numeric multiplier where `1.0` indicates a clear, smooth road and higher values (e.g., `2.0`) represent heavy traffic, bad terrain, or construction delays. |

---

### 3. Resources (`Resources.csv`)
| Field | Data Type | Description | Formatting Rules |
| :--- | :--- | :--- | :--- |
| `resourceId` | String | Unique identifier for the campus service resource or personnel. | Format: `RES-` followed by 3 digits (e.g., `RES-001`). Must be unique across all records. |
| `type` | String | Type of resource or service unit. | Text string describing the asset or personnel type (e.g., `Campus Shuttle`, `IT Support Technician`). |
| `homeLocation` | String | Base station or starting location ID for the resource. | Must reference a valid `locationId` in `Locations.csv` (e.g., `LOC-002`). |
| `capacity` | Integer | Maximum payload, passenger capacity, or concurrent job limit. | Positive integer value (e.g., `30` for a shuttle, `1` for an individual technician). |
| `availabilityStatus` | String | Current operational state of the resource. | Uppercase status string. Standard values: `AVAILABLE`, `BUSY`, `IN_TRANSIT`, `MAINTENANCE`. |

---

### 4. Service Requests (`ServiceRequests.csv`)
| Field | Data Type | Description | Formatting Rules |
| :--- | :--- | :--- | :--- |
| `requestId` | String | Unique identifier for the service request ticket. | Format: `REQ-` followed by 3 digits (e.g., `REQ-001`). Must be unique across all records. |
| `source` | String | Origin location ID where the service is requested from. | Must reference a valid `locationId` in `Locations.csv` (e.g., `LOC-001`). |
| `destination` | String | Target location ID for delivery, dispatch, or service completion. | Must reference a valid `locationId` in `Locations.csv` (e.g., `LOC-002`). |
| `category` | String | Functional service category of the request. | Uppercase string format (e.g., `IT_SUPPORT`, `TRANSPORT`, `MAINTENANCE`, `EMERGENCY`). |
| `urgency` | String | Priority level assigned to the request. | Uppercase enum values: `LOW`, `MEDIUM`, `HIGH`, `CRITICAL`. |
| `timeSubmitted` | Timestamp | Date and time when the service request was created. | Must strictly follow the ISO 8601 standard format: `YYYY-MM-DDTHH:MM:SS` (e.g., `2026-07-31T08:30:00`). |
| `deadline` | Timestamp | Date and time by which the service request must be resolved. | Must strictly follow the ISO 8601 standard format: `YYYY-MM-DDTHH:MM:SS` (e.g., `2026-07-31T10:30:00`). |
| `status` | String | Current lifecycle state of the service request ticket. | Uppercase status string. Standard values: `PENDING`, `ASSIGNED`, `IN_PROGRESS`, `COMPLETED`, `CANCELLED`. |

---

## Team Instructions & Scaling Requirements

All team members participating in data collection must expand the seed dataset to satisfy the project's minimum scale requirements:
- **Locations**: Minimum **50** entries (`LOC-001` through `LOC-050`+)
- **Roads**: Minimum **100** connections (`fromLocationId` to `toLocationId`)
- **Resources**: Minimum **30** active service units (`RES-001` through `RES-030`+)
- **Service Requests**: Minimum **300** logged service tickets (`REQ-001` through `REQ-300`+)

---

## ⚠️ CRITICAL RULE: CSV HEADER INTEGRITY

> [!CAUTION]
> **DO NOT modify, rename, add, remove, or reorder any CSV column headers!**
> The Java database loader relies on exact column header names and positions to parse and populate the database entities. Any change to header names or column sequence will break the automated ingestion pipeline.
