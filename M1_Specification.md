# M1 Problem Specification

## Project Overview

This document defines the problem scope, requirements, and deliverables for the M1 assignment of the DSA Combined Fab10 project.

## Problem Statement

The system must ingest, process, and analyze city infrastructure data from CSV seed files, including service requests, locations, roads, and resources. The key goal is to support efficient data access and provide clear outputs for the assignment tasks.

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
