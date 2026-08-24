# Ghana Smart Service Operations Optimizer — Campus Service Hub

DCIT 204/308 Joint DSA Semester Project. A University of Ghana campus
service-operations platform: custom data structures and algorithms over a
SQLite-backed dataset of campus locations, roads, resources and service
requests. See `M1_Specification.md` for the problem statement and
`docs/Final_Report.docx` for the full report.

There is no Maven/Gradle build — this is a plain `javac`/`java` project, run
straight from the `src/` tree. All commands below assume the repository
root as the working directory and Java 17+ (developed against Java 21).

## Project layout

```
src/
  Main.java                 entry point -> launches ui.MainUI (Swing GUI)
  models/                   plain data classes (Location, Resource, ServiceRequest, ...)
  structures/                required custom data structures (Section 6 of the brief)
  operations/                business logic built on top of structures/ (dispatch, hashing, ...)
  algorithms/                search, sort, graph, greedy, DP algorithms (Section 7)
  database/                  JDBC connection, schema creation, CSV loaders
  performance/                BenchmarkRunner + CsvExporter (empirical efficiency lab)
  ui/                         Swing panels wired together by MainUI
  main/resources/seed_data/   the four required CSV datasets
  test/                       JUnit 5 tests (see src/*Test.java for a couple of same-package ones too)
database/schema.sql          standalone copy of the schema DatabaseInitializer creates
lib/                          vendored jars (sqlite-jdbc, slf4j, JUnit 5 console standalone)
docs/                         Final_Report.docx, Development_Log.md, performance_results/
```

## Building

Compile the whole application (everything under `src/` except the test
sources) in one shot:

```bash
mkdir -p out/main
javac -d out/main -cp "lib/*" $(find src -name "*.java" -not -path "src/test/*" -not -name "*Test.java")
```

This should produce no errors or warnings. If you see `package X does not
exist`, first check `lib/` actually contains `sqlite-jdbc.jar` and
`slf4j-api-1.7.36.jar` (both are committed to the repo).

## Running the GUI

```bash
java -cp "lib/*:out/main" Main
```

This opens the Swing window (`ui.MainUI`), which on startup calls
`DatabaseInitializer.createTables()` against `campus_hub.db` in the current
directory (created automatically if it doesn't exist yet) and then loads
five tabs: Path Finder, Dispatch Modes, Ticket Lookup, Resource Optimizer,
and Benchmarks. A display is required — this will not run in a headless
environment.

## Seeding the database

The GUI creates empty tables on first launch but does not seed them. To
load all four CSV datasets (50 locations, 272 roads, 30 resources, 300
service requests) into `campus_hub.db`:

```bash
java -cp "lib/*:out/main" database.SeedAllData
```

Re-running it is safe (loaders skip rows that already exist). The schema
itself is also available standalone at `database/schema.sql` if you want to
inspect or recreate it without running any Java code.

## Running the empirical efficiency lab

```bash
java -cp "lib/*:out/main" performance.BenchmarkRunner
```

Seeds `campus_hub.db` automatically if it's empty, runs all six required
experiments (search, sorting, hash load factor, BST vs. balanced tree, heap
dispatch, graph algorithms), writes every run into the `algorithm_runs`
table, and exports `docs/performance_results/results.csv`.

## Running the tests

Tests use JUnit 5 (Jupiter). The console-standalone launcher jar is
vendored at `lib/junit-platform-console-standalone-1.10.2.jar` — no
internet access or separate JUnit install is needed.

```bash
# 1. Compile the application (see "Building" above) into out/main

# 2. Compile every test source against it
mkdir -p out/test
javac -d out/test -cp "lib/*:out/main" $(find src/test -name "*.java") $(find src -maxdepth 1 -name "*Test.java")

# 3. Run the whole suite
java -cp "lib/*:out/main:out/test" org.junit.platform.console.ConsoleLauncher --scan-classpath out/test
```

You should see `78 tests successful, 0 tests failed`. `src/test/structures/BTreeTest.java`
and `src/database/DatabaseInitializerTest.java` are separate, non-JUnit
`main()` demo scripts kept for trace/report evidence — run them directly
with `java -cp "lib/*:out/main:out/test" structures.BTreeTest` /
`database.DatabaseInitializerTest` if you want their printed output.

`GraphTraversalTest` and `MSTTest` open their own private in-memory SQLite
connections and don't touch `campus_hub.db`, so the test suite never
depends on the database having been seeded first.

## Notes for examiners

- No `java.util.HashMap`/`TreeMap`/`PriorityQueue`/`Stack`/`ArrayDeque` is
  used anywhere in `structures/`, `operations/`, or `algorithms/` — every
  required data structure in Section 6 of the brief is implemented from
  scratch in `structures/`.
- `structures/CustomHeap.java` is the single canonical heap implementation
  used everywhere a heap is needed (Dijkstra's min-heap, the resource
  max-heap, Prim's candidate-edge heap) — see its class Javadoc for the two
  equivalent ways it can be used.
- Correctness evidence (trace tables, proof sketches, counterexamples, and
  the full unit-test breakdown) is in
  `docs/performance_results/CorrectnessEvidence.md`.
