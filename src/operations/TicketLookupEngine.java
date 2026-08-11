package operations;

/**
 * Service execution layer for Operation 3: Fast Ticket Lookup.
 */
public class TicketLookupEngine {
    private CustomHashTable<String, String> requestTable;

    /**
     * Initializes the lookup engine using Rule 2 index number capacity.
     * @param indexDigitsSum Sum of Level 300 index digits.
     */
    public TicketLookupEngine(int indexDigitsSum) {
        this.requestTable = new CustomHashTable<>(indexDigitsSum);
    }

    /**
     * Adds or updates a ticket record in the Hash Table.
     * @param requestId The Request ID key (e.g., "22044996")
     * @param details Details/Record associated with the request.
     */
    public void addTicket(String requestId, String details) {
        requestTable.put(requestId, details);
    }

    /**
     * Searches for a ticket by ID.
     * @param requestId Target lookup key.
     * @return Formatted lookup result string.
     */
    public String searchTicket(String requestId) {
        String result = requestTable.get(requestId);
        if (result == null) {
            return "Lookup Result: Ticket ID '" + requestId + "' NOT FOUND.";
        }
        return "Lookup Result: Ticket ID '" + requestId + "' -> Details: " + result;
    }

    public int getTotalIndexedTickets() {
        return requestTable.getSize();
    }

    public int getCollisionCount() {
        return requestTable.getTotalCollisions();
    }
}