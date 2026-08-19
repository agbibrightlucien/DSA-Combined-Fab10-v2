package models;

public class Roads {
    private String fromLocationId = "";
    private String toLocationId = "";
    private float distance = 0;
    private int travelTime = 0;
    private float roadConditionWeight = 0;

    // Empty constructor
    public Roads() {
    }

    // Parameterized constructor
    public Roads(String fromLocationId, String toLocationId, float distance, int travelTime, float roadConditionWeight){
        this.fromLocationId = fromLocationId;
        this.toLocationId = toLocationId;
        this.distance = distance;
        this.travelTime = travelTime;
        this.roadConditionWeight = roadConditionWeight;
    }
    public String getFromLocationId(){
        return fromLocationId;
    }
    public String getToLocationId(){
        return toLocationId;
    }
    public float getDistance(){
        return distance;
    }
    public int getTravelTime(){
        return travelTime;
    }
    public float getRoadConditionWeight(){
        return roadConditionWeight;
    }
    public void setFromLocationId(String fromLocationId){
        this.fromLocationId = fromLocationId;
    }
    public void setToLocationId(String toLocationId){
        this.toLocationId = toLocationId;
    }
    public void setDistance(float distance){
        this.distance = distance;
    }
    public void setTravelTime(int travelTime){
        this.travelTime = travelTime;
    }
    public void setRoadConditionWeight(float roadConditionWeight){
        this.roadConditionWeight = roadConditionWeight;
    }

    @Override
    public String toString() {
        return "Roads{" +
                "fromLocationId='" + fromLocationId + '\'' +
                ", toLocationId='" + toLocationId + '\'' +
                ", distance=" + distance +
                ", travelTime=" + travelTime +
                ", roadConditionWeight=" + roadConditionWeight +
                '}';
    }
}
