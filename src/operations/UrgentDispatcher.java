package operations;

import models.ServiceRequest;
import structures.CustomHeap;
import structures.HeapComparator;
import java.sql.Timestamp;

/**
 * Operation 2: Urgent Dispatcher — Max-Heap Priority Queue for service requests.
 *
 * Computes a priority score for each request and builds a max-heap so that
 * the most critical job is always dispatched first, regardless of arrival order.
 *
 * Priority score formula:
 *   score = urgencyWeight + deadlineProximityBoost + categoryBoost
 *
 * Urgency weights   : CRITICAL=40, HIGH=30, MEDIUM=20, LOW=10
 * Deadline boost    : up to +25 (higher as deadline approaches)
 * Category boost    : +10 for IT_SUPPORT (Rule 3 — implementer digit = 1, range 1–4)
 *
 * Custom DSA used   : CustomHeap<ServiceRequest> configured as a max-heap
 *                     via HeapComparator (no java.util.PriorityQueue).
 */
public class UrgentDispatcher {

    // Rule 3: IT_SUPPORT category boost (digit = 1, falls in range 1–4)
    private static final int IT_SUPPORT_BOOST = 10;

    // -------------------------------------------------------------------------
    // Priority score calculation
    // -------------------------------------------------------------------------

    /**
     * Calculates the total priority score for a service request.
     * Higher score = higher priority = extracted from the heap first.
     *
     * @param request the service request to score
     * @return integer priority score
     */
    public int calculatePriorityScore(ServiceRequest request) {
        int base        = getUrgencyWeight(request.getUrgency());
        int catBoost    = "IT_SUPPORT".equalsIgnoreCase(request.getCategory())
                          ? IT_SUPPORT_BOOST : 0;
        int dlBoost     = calculateDeadlineProximityBoost(request.getDeadline());
        return base + catBoost + dlBoost;
    }

    /**
     * Converts urgency string to a base numeric weight.
     */
    private int getUrgencyWeight(String urgency) {
        if (urgency == null) return 10;
        switch (urgency.toUpperCase()) {
            case "CRITICAL": return 40;
            case "HIGH":     return 30;
            case "MEDIUM":   return 20;
            case "LOW":      return 10;
            default:         return 10;
        }
    }

    /**
     * Returns a deadline proximity boost based on how soon the deadline is.
     * A request nearing its deadline earns a higher boost, ensuring it eventually
     * overtakes newer but less urgent requests.
     *
     * Minutes remaining  | Boost
     * already overdue    |  +25
     * ≤ 60  min          |  +20
     * ≤ 180 min          |  +15
     * ≤ 360 min          |  +10
     * ≤ 720 min          |  + 5
     * > 720 min          |    0
     */
    private int calculateDeadlineProximityBoost(Timestamp deadline) {
        if (deadline == null) return 0;
        long minutesRemaining = (deadline.getTime() - System.currentTimeMillis()) / 60_000L;
        if (minutesRemaining <= 0)   return 25;   // overdue
        if (minutesRemaining <= 60)  return 20;
        if (minutesRemaining <= 180) return 15;
        if (minutesRemaining <= 360) return 10;
        if (minutesRemaining <= 720) return 5;
        return 0;
    }

    // -------------------------------------------------------------------------
    // Max-heap construction
    // -------------------------------------------------------------------------

    /**
     * Builds a max-heap from an array of service requests ordered by priority score.
     * The request with the highest score is at the top and is extracted first.
     *
     * The max-heap is created using our custom CustomHeap with a HeapComparator
     * that inverts the natural order: compare(a, b) returns negative when
     * a has a HIGHER score than b, so a rises to the top.
     *
     * @param requests array of service requests to enqueue
     * @return a ready-to-use max-heap CustomHeap
     */
    public CustomHeap<ServiceRequest> buildMaxHeap(ServiceRequest[] requests) {
        int capacity = (requests.length > 0) ? requests.length : 1;

        // Max-heap: comparator returns negative when a has HIGHER priority than b
        CustomHeap<ServiceRequest> maxHeap = new CustomHeap<>(
            capacity + 1,
            (a, b) -> calculatePriorityScore(b) - calculatePriorityScore(a)
        );

        for (ServiceRequest req : requests) {
            if (req != null) maxHeap.insert(req);
        }
        return maxHeap;
    }
}
