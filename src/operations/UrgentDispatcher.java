package operations;

import models.ServiceRequest;
import models.Resource;

import java.util.*;

public class UrgentDispatcher {

    private static final int RULE3_IT_SUPPORT_BOOST = 10;

    private static class ScoredRequest {
        ServiceRequest request;
        double priorityScore;

        ScoredRequest(ServiceRequest request, double priorityScore) {
            this.request = request;
            this.priorityScore = priorityScore;
        }
    }

   
    public List<String> dispatchUrgentRequest(List<ServiceRequest> requestQueue,
            List<Resource> resourcePool,
            Object graph) {

        List<String> dispatchLog = new ArrayList<>();

        PriorityQueue<ScoredRequest> priorityQueue = new PriorityQueue<>(
                (a, b) -> Double.compare(b.priorityScore, a.priorityScore));

        for (ServiceRequest request : requestQueue) {
            double baseScore = getUrgencyWeight(request.getUrgency());

            long timeToDeadline = request.getDeadline() - currentTime();
            double urgencyBoost = calculateDeadlineProximityBoost(timeToDeadline);

            double categoryBoost = 0;
            if ("IT_SUPPORT".equals(request.getCategory())) {
                categoryBoost = RULE3_IT_SUPPORT_BOOST;
            }

            double priorityScore = baseScore + urgencyBoost + categoryBoost;
            priorityQueue.add(new ScoredRequest(request, priorityScore));
        }

        while (!priorityQueue.isEmpty()) {
            ScoredRequest top = priorityQueue.poll();
            ServiceRequest topRequest = top.request;

            List<Resource> candidateResources = new ArrayList<>();
            for (Resource r : resourcePool) {
                if ("AVAILABLE".equals(r.getAvailabilityStatus())) {
                    candidateResources.add(r);
                }
            }

            if (candidateResources.isEmpty()) {
                topRequest.setStatus("QUEUED");
                dispatchLog.add(topRequest.getRequestId() + " -> QUEUED (no resources available)");
                continue;
            }

            Resource nearestResource = null;
            double shortestTime = Double.POSITIVE_INFINITY;

            for (Resource resource : candidateResources) {
                double travelTime = findShortestPathTime(graph, resource.getHomeLocation(),
                        topRequest.getDestination());
                if (travelTime < shortestTime) {
                    shortestTime = travelTime;
                    nearestResource = resource;
                }
            }

            nearestResource.setAvailabilityStatus("BUSY");
            topRequest.setStatus("IN_PROGRESS");

            dispatchLog.add(topRequest.getRequestId() + " -> " + nearestResource.getResourceId()
                    + " (score=" + top.priorityScore + ", travelTime=" + shortestTime + ")");
        }

        return dispatchLog;
    }

    private double getUrgencyWeight(String urgency) {
        if (urgency == null)
            return 0;
        switch (urgency.toUpperCase()) {
            case "CRITICAL":
                return 100;
            case "HIGH":
                return 75;
            case "MEDIUM":
                return 50;
            case "LOW":
                return 25;
            default:
                return 0;
        }
    }

    private double calculateDeadlineProximityBoost(long timeToDeadlineMillis) {

        double minutesLeft = timeToDeadlineMillis / (1000.0 * 60);

        if (minutesLeft <= 0) {
            return 100;
        } else if (minutesLeft <= 15) {
            return 60;
        } else if (minutesLeft <= 30) {
            return 40;
        } else if (minutesLeft <= 60) {
            return 20;
        } else if (minutesLeft <= 120) {
            return 10;
        } else {
            return 0;
        }
    }

    private long currentTime() {
        return System.currentTimeMillis();
    }

    private double findShortestPathTime(Object graph, String from, String to) {

        throw new UnsupportedOperationException(
                "Wire this up to Operation 1's findShortestPath(graph, from, to) method");
    }
}