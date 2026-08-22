-- Ghana Smart Service Operations Optimizer -- University of Ghana Campus Service Hub
-- SQLite schema, extracted verbatim from src/database/DatabaseInitializer.java so it
-- can be inspected, versioned, and re-run independently of the Java program (Section 12
-- of the project brief: "Database scripts: SQL files and database seed data -- schema.sql
-- and sample CSV files required").
--
-- This file is the source of truth for the schema; DatabaseInitializer.createTables()
-- executes these exact six CREATE TABLE statements at program startup. If you change one
-- here, change it there too (and vice versa) -- see README.md for how the two stay in sync.
--
-- Sample seed data lives in src/main/resources/seed_data/*.csv and is loaded through
-- database/*Loader.java (see README.md "Running" section).

CREATE TABLE IF NOT EXISTS locations (
    locationId VARCHAR(50) PRIMARY KEY,
    name VARCHAR(100),
    area VARCHAR(50),
    type VARCHAR(50),
    coordinates VARCHAR(100)
);

CREATE TABLE IF NOT EXISTS roads (
    fromLocationId VARCHAR(50),
    toLocationId VARCHAR(50),
    distance FLOAT,
    travelTime INT,
    roadConditionWeight FLOAT,
    PRIMARY KEY (fromLocationId, toLocationId)
);

CREATE TABLE IF NOT EXISTS resources (
    resourceId VARCHAR(50) PRIMARY KEY,
    type VARCHAR(50),
    homeLocation VARCHAR(50),
    capacity INT,
    availabilityStatus VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS service_requests (
    requestId VARCHAR(50) PRIMARY KEY,
    source VARCHAR(50),
    destination VARCHAR(50),
    category VARCHAR(50),
    urgency VARCHAR(20),
    timeSubmitted TIMESTAMP,
    deadline TIMESTAMP,
    status VARCHAR(20)
);

CREATE TABLE IF NOT EXISTS algorithm_runs (
    runId INTEGER PRIMARY KEY,
    algorithmName VARCHAR(100),
    inputSize INTEGER,
    timeNs BIGINT,
    memoryKb BIGINT,
    dateRun TIMESTAMP
);

-- event_type / related_request_id / event_id use snake_case to match what
-- AuditLogger.java and ResourceOptimizer.java actually insert/query against
-- (see the Part 15 fix note in DatabaseInitializer.java) -- the one table
-- here that deliberately breaks from the camelCase style of the other five.
CREATE TABLE IF NOT EXISTS audit_events (
    event_id INTEGER PRIMARY KEY,
    event_type VARCHAR(50),
    description VARCHAR(255),
    related_request_id VARCHAR(50),
    timestamp TIMESTAMP
);
