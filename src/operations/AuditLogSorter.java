package operations;

import models.ServiceRequest;
import database.DatabaseLoader; // Adjust or remove if your DB loader class has a different name

import java.util.ArrayList;
import java.util.List;

/**
 * Operation 5: Audit Log Sorter
 * Implementation of Merge Sort algorithm to sort ServiceRequest audit logs
 * based on the timeSubmitted timestamp with O(n log n) time complexity.
 */
public class AuditLogSorter {

    /**
     * Main entry point to sort a list of ServiceRequests by timeSubmitted.
     *
     * @param records   List of ServiceRequest objects from models
     * @param ascending true for oldest to newest, false for newest to oldest
     * @return Sorted List of ServiceRequest objects
     */
    public static List<ServiceRequest> mergeSort(List<ServiceRequest> records, boolean ascending) {
        if (records == null || records.size() <= 1) {
            return records == null ? new ArrayList<>() : new ArrayList<>(records);
        }

        int mid = records.size() / 2;

        // Divide step
        List<ServiceRequest> left = mergeSort(new ArrayList<>(records.subList(0, mid)), ascending);
        List<ServiceRequest> right = mergeSort(new ArrayList<>(records.subList(mid, records.size())), ascending);

        // Conquer / Merge step
        return merge(left, right, ascending);
    }

    /**
     * Merge helper method that combines two sorted sub-lists.
     */
    private static List<ServiceRequest> merge(List<ServiceRequest> left, List<ServiceRequest> right, boolean ascending) {
        List<ServiceRequest> result = new ArrayList<>();
        int i = 0;
        int j = 0;

        while (i < left.size() && j < right.size()) {
            ServiceRequest leftReq = left.get(i);
            ServiceRequest rightReq = right.get(j);

            int comparison = compareTimestamps(leftReq, rightReq);

            if (ascending) {
                if (comparison <= 0) { // left is earlier or equal
                    result.add(leftReq);
                    i++;
                } else {
                    result.add(rightReq);
                    j++;
                }
            } else { // descending
                if (comparison >= 0) { // left is later or equal
                    result.add(leftReq);
                    i++;
                } else {
                    result.add(rightReq);
                    j++;
                }
            }
        }

        // Collect remaining elements from left
        while (i < left.size()) {
            result.add(left.get(i));
            i++;
        }

        // Collect remaining elements from right
        while (j < right.size()) {
            result.add(right.get(j));
            j++;
        }

        return result;
    }

    /**
     * Safely compares timeSubmitted timestamps of two ServiceRequest objects.
     */
    private static int compareTimestamps(ServiceRequest req1, ServiceRequest req2) {
        if (req1 == null || req1.getTimeSubmitted() == null) return -1;
        if (req2 == null || req2.getTimeSubmitted() == null) return 1;

        return req1.getTimeSubmitted().compareTo(req2.getTimeSubmitted());
    }

    /**
     * Utility method to pull service requests from the database layer and sort them.
     */
    public static List<ServiceRequest> getSortedAuditLogFromDatabase(boolean ascending) {
        List<ServiceRequest> rawRequests = DatabaseLoader.getServiceRequests();
        return mergeSort(rawRequests, ascending);
    }
}


