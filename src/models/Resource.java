package models;

/**
 * Resource entity model.
 * Represents a dispatchable resource (e.g. staff, vehicle, equipment)
 * tracked by the Ghana Smart Service Operations Optimizer.
 */
public class Resource {

    private String resourceId;
    private String type;
    private String homeLocation;
    private Integer capacity;
    private String availabilityStatus;

    // Empty constructor
    public Resource() {
    }

    // Parameterized constructor
    public Resource(String resourceId, String type, String homeLocation,
                     Integer capacity, String availabilityStatus) {
        this.resourceId = resourceId;
        this.type = type;
        this.homeLocation = homeLocation;
        this.capacity = capacity;
        this.availabilityStatus = availabilityStatus;
    }

    // Getters
    public String getResourceId() {
        return resourceId;
    }

    public String getType() {
        return type;
    }

    public String getHomeLocation() {
        return homeLocation;
    }

    public Integer getCapacity() {
        return capacity;
    }

    public String getAvailabilityStatus() {
        return availabilityStatus;
    }

    // Setters
    public void setResourceId(String resourceId) {
        this.resourceId = resourceId;
    }

    public void setType(String type) {
        this.type = type;
    }

    public void setHomeLocation(String homeLocation) {
        this.homeLocation = homeLocation;
    }

    public void setCapacity(Integer capacity) {
        this.capacity = capacity;
    }

    public void setAvailabilityStatus(String availabilityStatus) {
        this.availabilityStatus = availabilityStatus;
    }

    @Override
    public String toString() {
        return "Resource{" +
                "resourceId='" + resourceId + '\'' +
                ", type='" + type + '\'' +
                ", homeLocation='" + homeLocation + '\'' +
                ", capacity=" + capacity +
                ", availabilityStatus='" + availabilityStatus + '\'' +
                '}';
    }
}