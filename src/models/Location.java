package models;

/**
 * Location entity model.
 * Represents a node (campus landmark) on the University of Ghana campus map.
 */
public class Location {

    private String locationId;
    private String name;
    private String area;
    private String type;
    private String coordinates;

    // Empty constructor
    public Location() {}

    // Fully parameterized constructor
    public Location(String locationId, String name, String area,
                    String type, String coordinates) {
        this.locationId  = locationId;
        this.name        = name;
        this.area        = area;
        this.type        = type;
        this.coordinates = coordinates;
    }

    // Getters
    public String getLocationId()  { return locationId; }
    public String getName()        { return name; }
    public String getArea()        { return area; }
    public String getType()        { return type; }
    public String getCoordinates() { return coordinates; }

    // Setters
    public void setLocationId(String locationId)   { this.locationId  = locationId; }
    public void setName(String name)               { this.name        = name; }
    public void setArea(String area)               { this.area        = area; }
    public void setType(String type)               { this.type        = type; }
    public void setCoordinates(String coordinates) { this.coordinates = coordinates; }

    @Override
    public String toString() {
        return "Location{" +
                "locationId='" + locationId + '\'' +
                ", name='"     + name        + '\'' +
                ", area='"     + area        + '\'' +
                ", type='"     + type        + '\'' +
                ", coordinates='" + coordinates + '\'' +
                '}';
    }
}
