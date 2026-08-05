package com.allocation.domain;

public class Location {
    private final String locationId;
    private final String name;
    private final String area;
    private final String type;
    private final String coordinates;

    public Location(String locationId, String name, String area, String type, String coordinates) {
        this.locationId = locationId;
        this.name = name;
        this.area = area;
        this.type = type;
        this.coordinates = coordinates;
    }

    public String getLocationId() {
        return locationId;
    }

    public String getName() {
        return name;
    }

    public String getArea() {
        return area;
    }

    public String getType() {
        return type;
    }

    public String getCoordinates() {
        return coordinates;
    }
}
