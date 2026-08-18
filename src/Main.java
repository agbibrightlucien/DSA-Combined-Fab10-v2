import operations.TicketLookupEngine;

public class Main {
    public static void main(String[] args) {
        System.out.println("=== Testing Operation 3: Fast Ticket Lookup ===");

        // Step 1: Initialize engine (passing sample sum for Rule 2 capacity)
        TicketLookupEngine lookupEngine = new TicketLookupEngine(12);

        // Step 2: Add sample campus tickets
        lookupEngine.addTicket("REQ22044996", "Balme Library - Wi-Fi Router Reset");
        lookupEngine.addTicket("REQ22044997", "JQB - Air Conditioner Repair");
        lookupEngine.addTicket("REQ22044998", "Volta Hall - Plumbing Issue");

        // Step 3: Perform lookups
        System.out.println(lookupEngine.searchTicket("REQ22044996"));
        System.out.println(lookupEngine.searchTicket("REQ22044997"));
        System.out.println(lookupEngine.searchTicket("REQ22044998"));

        // Step 4: Test searching for a non-existent ticket
        System.out.println(lookupEngine.searchTicket("REQ99999999"));

        // Step 5: Check indexed total and collision count
        System.out.println("\nTotal Tickets Indexed: " + lookupEngine.getTotalIndexedTickets());
        System.out.println("Total Collisions Tracked: " + lookupEngine.getCollisionCount());
    }
}