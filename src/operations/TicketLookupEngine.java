package operations;

/**
 * Service execution layer for Operation 3: Fast Ticket Lookup.
 *
 * <p>Generic over V so callers can store any record type — including the full
 * {@code ServiceRequest} object once that model is available.  Example usage:
 * <pre>
 *   TicketLookupEngine&lt;ServiceRequest&gt; engine = new TicketLookupEngine&lt;&gt;(indexDigitsSum);
 *   engine.addTicket(req.getRequestId(), req);
 *   ServiceRequest found = engine.searchTicket("REQ-001");
 * </pre>
 *
 * @param <V> The value type stored per ticket key (e.g. {@code ServiceRequest}).
 */
public class TicketLookupEngine<V> {

    private final CustomHashTable<String, V> requestTable;

    /**
     * Initialises the lookup engine using Rule 2 (Hash Table Capacity Rule).
     *
     * @param indexDigitsSum Sum of the last two digits of each Level-300 member's
     *                       index number. The initial table capacity is set to
     *                       {@code indexDigitsSum * 10}.
     */
    public TicketLookupEngine(int indexDigitsSum) {
        this.requestTable = new CustomHashTable<>(indexDigitsSum);
    }

    /**
     * Inserts or updates a ticket record in the Hash Table.
     *
     * @param requestId The unique Request ID key (e.g., "REQ-001").
     * @param record    The full record object to store (e.g., a {@code ServiceRequest}).
     */
    public void addTicket(String requestId, V record) {
        requestTable.put(requestId, record);
    }

    /**
     * Looks up a ticket by its request ID.
     *
     * @param requestId The target lookup key.
     * @return The stored record, or {@code null} if not found.
     */
    public V searchTicket(String requestId) {
        return requestTable.get(requestId);
    }

    /**
     * Returns a formatted lookup result string suitable for console output.
     *
     * @param requestId The target lookup key.
     * @return Human-readable result message.
     */
    public String searchTicketFormatted(String requestId) {
        V result = requestTable.get(requestId);
        if (result == null) {
            return "Lookup Result: Ticket ID '" + requestId + "' NOT FOUND.";
        }
        return "Lookup Result: Ticket ID '" + requestId + "' -> " + result;
    }

    /** Returns the number of records currently indexed. */
    public int getTotalIndexedTickets() {
        return requestTable.getSize();
    }

    /** Returns the total number of hash collisions recorded. */
    public int getCollisionCount() {
        return requestTable.getTotalCollisions();
    }
}