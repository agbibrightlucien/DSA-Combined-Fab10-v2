package operations;

import database.DatabaseConnection;
import models.ServiceRequest;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;

/**
 * Operation 5: Audit Log Sorter — custom recursive Merge Sort.
 *
 * Loads all service_request records from the database, converts them to
 * a plain array, and sorts them chronologically by timeSubmitted using
 * Timestamp.before() / Timestamp.after() for comparison, exactly as
 * required by the specification.
 *
 * Algorithm: Merge Sort
 *   Time complexity : O(n log n) in all cases (best, average, worst)
 *   Space complexity: O(n) additional for the temporary merge arrays
 *
 * Custom DSA used:
 *   - Recursive divide-and-conquer Merge Sort (no java.util.Arrays.sort)
 *   - Count-then-load DB pattern (no java.util.ArrayList)
 *   - ServiceRequest[] plain array as the data store
 */
public class AuditLogSorter {

    // -------------------------------------------------------------------------
    // Public entry point
    // -------------------------------------------------------------------------

    /**
     * Loads all service requests from the database, sorts them chronologically
     * by timeSubmitted using a custom Merge Sort, and returns the sorted array.
     *
     * @return sorted ServiceRequest[] in ascending timeSubmitted order
     */
    public ServiceRequest[] getSortedAuditLog() {
        ServiceRequest[] records = loadAllRequests();
        if (records.length > 1) {
            mergeSort(records, 0, records.length - 1);
        }
        System.out.println("AuditLogSorter: sorted " + records.length + " record(s).");
        return records;
    }

    // -------------------------------------------------------------------------
    // Merge Sort — recursive implementation from scratch
    // -------------------------------------------------------------------------

    /**
     * Recursively sorts the sub-array arr[left..right] in place.
     * Divides the range in half, sorts each half, then merges them.
     *
     * Base case: a sub-array of length 0 or 1 is already sorted.
     */
    private void mergeSort(ServiceRequest[] arr, int left, int right) {
        if (left >= right) return;              // base case: 0 or 1 elements
        int mid = left + (right - left) / 2;   // midpoint (avoids integer overflow)
        mergeSort(arr, left, mid);              // sort left half
        mergeSort(arr, mid + 1, right);         // sort right half
        merge(arr, left, mid, right);           // merge the two sorted halves
    }

    /**
     * Merges two adjacent sorted sub-arrays arr[left..mid] and arr[mid+1..right]
     * into a single sorted sequence in arr[left..right].
     *
     * Comparison uses Timestamp.before() / .after() as required by the spec.
     * Null timestamps are treated as larger than any non-null timestamp (sorted last).
     */
    private void merge(ServiceRequest[] arr, int left, int mid, int right) {
        int n1 = mid - left + 1;
        int n2 = right - mid;

        // Temporary arrays for the two halves (O(n) additional space)
        ServiceRequest[] leftArr  = new ServiceRequest[n1];
        ServiceRequest[] rightArr = new ServiceRequest[n2];

        System.arraycopy(arr, left,     leftArr,  0, n1);
        System.arraycopy(arr, mid + 1,  rightArr, 0, n2);

        int i = 0, j = 0, k = left;

        while (i < n1 && j < n2) {
            Timestamp t1 = leftArr[i].getTimeSubmitted();
            Timestamp t2 = rightArr[j].getTimeSubmitted();

            // Determine which element goes next using .before() / .after()
            boolean leftGoesFirst;
            if (t1 == null && t2 == null) {
                leftGoesFirst = true;               // both null — keep left
            } else if (t1 == null) {
                leftGoesFirst = false;              // null sorts after a real timestamp
            } else if (t2 == null) {
                leftGoesFirst = true;               // t1 is real, t2 is null → t1 first
            } else {
                // Both non-null: use Timestamp.before() for ascending order
                leftGoesFirst = t1.before(t2) || t1.equals(t2);
            }

            if (leftGoesFirst) {
                arr[k++] = leftArr[i++];
            } else {
                arr[k++] = rightArr[j++];
            }
        }

        // Append any remaining elements
        while (i < n1) arr[k++] = leftArr[i++];
        while (j < n2) arr[k++] = rightArr[j++];
    }

    // -------------------------------------------------------------------------
    // Database loading — count-then-load pattern (no java.util.ArrayList)
    // -------------------------------------------------------------------------

    /**
     * Loads all records from the service_requests table into a plain array.
     * Uses a two-pass approach (COUNT then SELECT) to allocate the exact array size.
     */
    private ServiceRequest[] loadAllRequests() {
        // Pass 1: get the record count
        int count = 0;
        try (Connection conn = DatabaseConnection.connect();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery("SELECT COUNT(*) FROM service_requests")) {
            if (rs.next()) count = rs.getInt(1);
        } catch (SQLException e) {
            System.err.println("AuditLogSorter: error counting records — " + e.getMessage());
            return new ServiceRequest[0];
        }

        if (count == 0) return new ServiceRequest[0];

        // Pass 2: load all records into the array
        ServiceRequest[] records = new ServiceRequest[count];
        int i = 0;
        try (Connection conn = DatabaseConnection.connect();
             Statement  stmt = conn.createStatement();
             ResultSet  rs   = stmt.executeQuery("SELECT * FROM service_requests")) {
            while (rs.next() && i < count) {
                records[i++] = new ServiceRequest(
                        rs.getString("requestId"),    rs.getString("source"),
                        rs.getString("destination"),  rs.getString("category"),
                        rs.getString("urgency"),
                        rs.getTimestamp("timeSubmitted"),
                        rs.getTimestamp("deadline"),
                        rs.getString("status"));
            }
        } catch (SQLException e) {
            System.err.println("AuditLogSorter: error loading records — " + e.getMessage());
        }
        return records;
    }
}
