# Development Log - Ghana Smart Service Operations Optimizer

Team: DCIT 204/308 Joint DSA Project - University of Ghana Campus Service Hub
This log covers weekly progress, challenges, and decisions per Section 15 of
the brief (academic integrity / development log requirement). Compiled from
the team's GitHub pull request and issue history, meeting attendance
records, and the Part 15 work log below.

---

## Week 1 - Local context, dataset foundations, false start

- Local context locked in: University of Ghana Campus Service Hub.
- Locations dataset started early (#7, Richard Kojo Djivon - 50 verified
  campus locations, merged).
- One early attempt at a routing utility ported from JavaScript
  (`SmartRoutePlanner.java`, #6) was opened as a draft and ultimately
  closed - the team moved to building the shortest-path logic natively in
  Java against the custom data structures instead of adapting ported code.
- A general onboarding/"tutorial" PR (#8) was opened and closed without
  merging.

## Week 2 - Models, database layer, seed data, problem specification

The heaviest single week of foundational work - most of the domain models,
the database layer, and the CSV seed data landed here:

- Domain models: Location, Road, Resource, ServiceRequest (issues #14-#17,
  assigned to Mohammed Kassim, Almad Marzuk, Sawyerr Elnathan, Richard
  Weir respectively).
- Database layer: `DatabaseConnection` utility (#18/#32, Richard Kojo
  Djivon), table initialization logic (#19/#30, Margaret Narnor).
- CSV loaders: Locations (#20, Jerry Miriam Dzidzor), Roads (#21,
  Emmanuella Prah), Resources (#22/#34, Abigail Adjei), Service Requests
  (#23/#35, **Seth Acheampong** - created `ServiceRequestLoader.java`,
  merged and approved).
- Seed data expanded: Roads.csv reformatted with comprehensive data (#12),
  ServiceRequests.csv expanded to the full dataset (#11), Resources.csv
  added with matching locations (#10).
- `M1_Specification.md` written and merged (#13, #9) - problem statement,
  data dictionary, schema draft, and the three index-derived algorithm
  parameters (Rules 1-3) required by Section 2 of the brief.
- Task Track 2 (#4) and the general Task2 setup (#29) assigned the
  problem-specification and index-rule work across Lucien, Gabriel Twum,
  Crystal Hazekawa, and Anaam Francis.

## Week 3 - Quieter week: hashing and ticket lookup

- Only one major merge this week: Operation 3 - Custom Hash Table and Fast
  Ticket Lookup Engine (#37, Gabriel Twum, approved). This became
  `CustomHashTable.java` and `TicketLookupEngine.java`, later reused
  directly by the Part 15 Ticket Lookup panel.

## Week 4 - Completion sprint (structures, algorithms, GUI, integration)

With the Tuesday deadline set, the remaining Parts 1-14 landed in a
concentrated burst over roughly the last two days, immediately followed by
Part 15 integration:

- Dynamic Array with auto-resize (#40, Lucien).
- FIFO Queue, Circular Queue, Deque for dispatch rules (#38, Almad Marzuk).
- Greedy failure counterexample + `RequestSelectionDP` (#39, Jerry Miriam
  Dzidzor, approved).
- Sorting algorithms (#47, Daniel Dominion) and search algorithms - linear
  and binary (#46, Richard Weir).
- Stack-based audit logging (#48, Mohammed Kassim) - `AuditLogger.java`
  and the `audit_events` table.
- BFS/DFS traversal and Prim/Kruskal MST (#49, Richard Kojo Djivon).
- BST and balanced (red-black) tree (#50, Anaam Francis).
- Part 14 - empirical efficiency lab and performance results (#51,
  Abigail Adjei) - `BenchmarkRunner`, `CsvExporter`, and the six required
  performance graphs.
- B-Tree for simulated database indexing (#44, Sawyerr Elnathan) and
  disjoint set + graph representation (#43, Crystal Hazekawa) landed
  slightly earlier in the same push.

### Part 15 - GUI, integration testing, and bug fixes

**Owner:** Seth Acheampong (Seth-crypto)

Once Parts 1-14 were merged into `master`, Part 15 work began:

- Pulled the latest `master` and created branch `seth/part-15-gui-report`.
- Set up IntelliJ IDEA locally, configured JDK 21 (Eclipse Temurin) as the
  project SDK, and added `sqlite-jdbc.jar` and `slf4j-api-1.7.36.jar` to
  the module classpath.
- Designed and implemented the Part 15 Swing GUI: `MainUI` (tabbed frame)
  plus five feature panels - `PathFinderPanel`, `DispatchPanel`,
  `LookupPanel`, `OptimizerPanel`, `BenchmarkPanel` - each wired directly
  to the existing operations layer (`ShortestPathFinder`,
  `DispatchModeComparer`, `TicketLookupEngine`, `ResourceOptimizer`,
  `AuditLogger`, `BenchmarkRunner`) with no duplicated business logic in
  the UI.
- Replaced the placeholder `Main.java` with a single call to
  `MainUI.launch()`.

**Problems found and fixed during integration:**

- **`audit_events` schema mismatch:** `DatabaseInitializer` created the
  table with camelCase columns (`eventId`, `eventType`,
  `relatedRequestId`), but `AuditLogger` (#48) and `ResourceOptimizer`
  already inserted/queried snake_case columns (`event_id`, `event_type`,
  `related_request_id`). Every audit-log write was silently throwing an
  `SQLException`, so the undo feature never actually persisted or
  reversed anything. Fixed the column names in `DatabaseInitializer` to
  match the code already in use.
- **`AuditLogger` was unreachable from outside `ResourceOptimizer`:** the
  optimizer created its own private `AuditLogger` with its own in-memory
  stack, so a separate `AuditLogger` instance in the UI would have had
  nothing to undo. Added a `getAuditLogger()` accessor (no behaviour
  change) so the undo button operates on the exact stack a given run used.
- **Empty local database on first run:** a fresh `campus_hub.db` only gets
  its six tables created, not seeded - none of the four individual loader
  classes are wired together in one place. Added `database/SeedAllData.java`,
  a small one-time convenience runner that loads all four seed CSVs in one
  pass, for anyone starting from a clean database.

**Verification** - ran the finished GUI end to end and confirmed:
- Path Finder: Dijkstra shortest path returns a correct route and travel
  time between two real, DB-loaded locations (50 locations loaded).
- Resource Optimizer: greedy assignment runs against real PENDING
  requests; Undo Last Assignment correctly reverses the most recent status
  change in the database (confirms the schema fix above works).
- Ticket Lookup: hash index built from 300 service requests, 0 collisions
  at this table size; lookups return the correct record.
- Dispatch Modes: FIFO, circular queue, and priority-heap dispatch orders
  differ as expected for the same input batch.
- Benchmarks: existing performance graphs load and display correctly.

**Decision:** kept `SeedAllData.java` as a small, separate, non-graded
convenience class rather than folding automatic reseeding into `MainUI`
on launch - reseeding on every GUI start risks duplicate-key errors
against an already-populated database.

- Branch pushed, PR opened against `master` (review required per team
  workflow).

### Next steps

- Merge Part 15 PR once reviewed.
- Assemble `docs/Final_Report.docx` from each part's section content
  (Part 15's section already drafted - see
  `Final_Report_Part15_Section.docx`).
- Record the 5-8 minute demonstration video against the merged `master`.

---

## Attendance note

Team meeting attendance and task completion were tracked throughout via a
shared attendance/task tracker (average task completion rate ~78% across
two tracked snapshots). One recorded Zoom session (DSA Combined Fab10 v2,
1 August 2026, 14 participants, ~57 minutes) is on file as supporting
evidence per Section 15's attendance sign-in/out requirement.
