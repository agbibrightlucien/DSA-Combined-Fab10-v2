package com.allocation.domain;

public class Resource {
    private final String resourceId;
    private final String type;
    private final String homeLocation;
    private int capacity;
    private Status status;

    public Resource(String resourceId, String type, String homeLocation, int capacity, Status status) {
        this.resourceId = resourceId;
        this.type = type;
        this.homeLocation = homeLocation;
        this.capacity = capacity;
        this.status = status;
    }

    public String getResourceId() {
        return resourceId;
    }

    public String getType() {
        return type;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public int getCapacity() {
        return capacity;
    }

    public void decrementCapacity() {
        if (capacity > 0) {
            capacity -= 1;
        }
    }

    public Status getStatus() {
        return status;
    }

    public void setStatus(Status status) {
        this.status = status;
    }
}
