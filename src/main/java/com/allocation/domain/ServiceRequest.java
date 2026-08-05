package com.allocation.domain;

import java.time.Instant;

public class ServiceRequest {
    private final String requestId;
    private final String source;
    private final String destination;
    private final Priority priority;
    private final Instant timeSubmitted;
    private final Instant deadline;
    private Status status;
    private final String category;

    public ServiceRequest(String requestId, String source, String destination, String category, Priority priority, Instant timeSubmitted, Instant deadline, Status status) {
        this.requestId = requestId;
        this.source = source;
        this.destination = destination;
        this.category = category;
        this.priority = priority;
        this.timeSubmitted = timeSubmitted;
        this.deadline = deadline;
        this.status = status;
    }

    public String getRequestId() {
        return requestId;
    }

    public String getSource() {
        return source;
    }

    public String getDestination() {
        return destination;
    }

    public String getCategory() {
        return category;
    }

    public Priority getPriority() {
        return priority;
    }

    public Instant getTimeSubmitted() {
        return timeSubmitted;
    }

    public Instant getDeadline() {
        return deadline;
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
