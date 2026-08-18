import structures.CustomLinkedList;
import operations.AuditLogSorter;
import models.ServiceRequest;

import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

public class MainTest {

    public static void main(String[] args) {
        System.out.println("==========================================");
        System.out.println("  TESTING OPERATION 3: FAST TICKET LOOKUP");
        System.out.println("==========================================");
        testOperation3();

        System.out.println("\n==========================================");
        System.out.println("  TESTING OPERATION 5: AUDIT LOG SORTER");
        System.out.println("==========================================");
        testOperation5();
    }

    private static void testOperation3() {
        CustomLinkedList<String> ticketList = new CustomLinkedList<>();

        ticketList.addLast("TCK-101");
        ticketList.addLast("TCK-102");
        ticketList.addLast("TCK-103");
        ticketList.addLast("TCK-104");

        System.out.println("List Size: " + ticketList.size());

        System.out.print("All tickets in list: ");
        for (String id : ticketList) {
            System.out.print(id + " ");
        }
        System.out.println();

        String target = "TCK-103";
        String found = ticketList.find(id -> id.equalsIgnoreCase(target));

        if (found != null) {
            System.out.println("SUCCESS: Found ticket -> " + found);
        } else {
            System.out.println("FAILED: Ticket " + target + " not found.");
        }
    }

    private static void testOperation5() {
        List<ServiceRequest> requests = new ArrayList<>();

        // Construct dummy timestamps using java.sql.Timestamp
        ServiceRequest req1 = new ServiceRequest();
        req1.setTimeSubmitted(Timestamp.valueOf("2026-08-18 14:30:00"));

        ServiceRequest req2 = new ServiceRequest();
        req2.setTimeSubmitted(Timestamp.valueOf("2026-08-10 00:15:00"));

        ServiceRequest req3 = new ServiceRequest();
        req3.setTimeSubmitted(Timestamp.valueOf("2026-08-18 00:00:00"));

        requests.add(req1);
        requests.add(req2);
        requests.add(req3);

        System.out.println("Unsorted count: " + requests.size());

        List<ServiceRequest> sortedAsc = AuditLogSorter.mergeSort(requests, true);

        System.out.println("\nSorted (Ascending - Oldest to Newest):");
        for (ServiceRequest req : sortedAsc) {
            System.out.println(" - " + req.getTimeSubmitted());
        }

        List<ServiceRequest> sortedDesc = AuditLogSorter.mergeSort(requests, false);

        System.out.println("\nSorted (Descending - Newest to Oldest):");
        for (ServiceRequest req : sortedDesc) {
            System.out.println(" - " + req.getTimeSubmitted());
        }
    }
}