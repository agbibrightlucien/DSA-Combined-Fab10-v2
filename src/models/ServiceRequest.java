package models;

import java.sql.Timestamp;

    public class ServiceRequest {

        private String requestId;
        private String source;
        private String destination;
        private String category;
        private String urgency;
        private Timestamp timeSubmitted;
        private Timestamp deadline;
        private String status;

        // Empty constructor
        public ServiceRequest() {
        }

        // Parameterized constructor
        public ServiceRequest(String requestId, String source, String destination, String category,
                              String urgency, Timestamp timeSubmitted, Timestamp deadline, String status) {
            this.requestId = requestId;
            this.source = source;
            this.destination = destination;
            this.category = category;
            this.urgency = urgency;
            this.timeSubmitted = timeSubmitted;
            this.deadline = deadline;
            this.status = status;
        }

        // Getters
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

        public String getUrgency() {
            return urgency;
        }

        public Timestamp getTimeSubmitted() {
            return timeSubmitted;
        }

        public Timestamp getDeadline() {
            return deadline;
        }

        public String getStatus() {
            return status;
        }

        // Setters
        public void setRequestId(String requestId) {
            this.requestId = requestId;
        }

        public void setSource(String source) {
            this.source = source;
        }

        public void setDestination(String destination) {
            this.destination = destination;
        }

        public void setCategory(String category) {
            this.category = category;
        }

        public void setUrgency(String urgency) {
            this.urgency = urgency;
        }

        public void setTimeSubmitted(Timestamp timeSubmitted) {
            this.timeSubmitted = timeSubmitted;
        }

        public void setDeadline(Timestamp deadline) {
            this.deadline = deadline;
        }

        public void setStatus(String status) {
            this.status = status;
        }

        @Override
        public String toString() {
            return "ServiceRequest{" +
                    "requestId='" + requestId + '\'' +
                    ", source='" + source + '\'' +
                    ", destination='" + destination + '\'' +
                    ", category='" + category + '\'' +
                    ", urgency='" + urgency + '\'' +
                    ", timeSubmitted=" + timeSubmitted +
                    ", deadline=" + deadline +
                    ", status='" + status + '\'' +
                    '}';
        }
    }

