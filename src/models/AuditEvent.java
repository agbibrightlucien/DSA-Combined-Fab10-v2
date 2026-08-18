package models;

/**
 * AuditEvent entity model.
 * Represents one audit event recorded in the audit_events table.
 */
public class AuditEvent {

    private int eventId;
    private String eventType;
    private String description;
    private String relatedRequestId;
    private String timestamp;

    // Empty constructor
    public AuditEvent() {}

    // Fully parameterized constructor
    public AuditEvent(int eventId, String eventType, String description,
                      String relatedRequestId, String timestamp) {
        this.eventId = eventId;
        this.eventType = eventType;
        this.description = description;
        this.relatedRequestId = relatedRequestId;
        this.timestamp = timestamp;
    }

    // Getters
    public int getEventId() {
        return eventId;
    }

    public String getEventType() {
        return eventType;
    }

    public String getDescription() {
        return description;
    }

    public String getRelatedRequestId() {
        return relatedRequestId;
    }

    public String getTimestamp() {
        return timestamp;
    }

    // Setters
    public void setEventId(int eventId) {
        this.eventId = eventId;
    }

    public void setEventType(String eventType) {
        this.eventType = eventType;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setRelatedRequestId(String relatedRequestId) {
        this.relatedRequestId = relatedRequestId;
    }

    public void setTimestamp(String timestamp) {
        this.timestamp = timestamp;
    }

    @Override
    public String toString() {
        return "AuditEvent{" +
                "eventId=" + eventId +
                ", eventType='" + eventType + '\'' +
                ", description='" + description + '\'' +
                ", relatedRequestId='" + relatedRequestId + '\'' +
                ", timestamp='" + timestamp + '\'' +
                '}';
    }
}