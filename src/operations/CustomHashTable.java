package operations;

/**
 * Custom Separate-Chaining Hash Table.
 * Satisfies Module M3 and Operation 3 requirements.
 */
public class CustomHashTable<K, V> {
    private HashNode<K, V>[] table;
    private int capacity;
    private int size;
    private final double LOAD_FACTOR_THRESHOLD = 0.75;
    private int totalCollisions;

    /**
     * Constructor implementing Rule 2 (Hash Table Capacity Rule).
     * @param memberLastTwoDigitsSum Sum of last two digits of Level 300 members.
     */
    @SuppressWarnings("unchecked")
    public CustomHashTable(int memberLastTwoDigitsSum) {
        // Rule 2: initial capacity = sum * 10
        this.capacity = Math.max(11, memberLastTwoDigitsSum * 10);
        this.table = new HashNode[this.capacity];
        this.size = 0;
        this.totalCollisions = 0;
    }

    /**
     * Custom Hash Function mapping keys to array indices.
     */
    private int hash(K key) {
        if (key == null) return 0;
        return Math.abs(key.hashCode()) % capacity;
    }

    /**
     * Inserts or updates a key-value pair.
     */
    public void put(K key, V value) {
        if (key == null) return;

        // Auto-resize if load factor is exceeded
        if ((double) size / capacity >= LOAD_FACTOR_THRESHOLD) {
            resize();
        }

        int index = hash(key);
        HashNode<K, V> head = table[index];

        // Track collisions for empirical metrics
        if (head != null) {
            totalCollisions++;
        }

        // Update value if key already exists
        HashNode<K, V> current = head;
        while (current != null) {
            if (current.getKey().equals(key)) {
                current.setValue(value);
                return;
            }
            current = current.getNext();
        }

        // Insert new node at head (O(1) insertion)
        HashNode<K, V> newNode = new HashNode<>(key, value);
        newNode.setNext(head);
        table[index] = newNode;
        size++;
    }

    /**
     * Operation 3 Core Logic: Fast Ticket Lookup.
     * Average Time Complexity: O(1)
     */
    public V get(K key) {
        if (key == null) return null;

        int index = hash(key);
        HashNode<K, V> current = table[index];

        // Traverse linked list chain in bucket
        while (current != null) {
            if (current.getKey().equals(key)) {
                return current.getValue(); // Match found
            }
            current = current.getNext();
        }

        return null; // Key "not found"
    }

    /**
     * Removes an entry by key.
     */
    public boolean remove(K key) {
        if (key == null) return false;

        int index = hash(key);
        HashNode<K, V> current = table[index];
        HashNode<K, V> prev = null;

        while (current != null) {
            if (current.getKey().equals(key)) {
                if (prev != null) {
                    prev.setNext(current.getNext());
                } else {
                    table[index] = current.getNext();
                }
                size--;
                return true;
            }
            prev = current;
            current = current.getNext();
        }

        return false;
    }

    /**
     * Resizes and re-hashes table when load factor threshold is reached.
     */
    @SuppressWarnings("unchecked")
    private void resize() {
        int newCapacity = capacity * 2;
        HashNode<K, V>[] oldTable = table;

        table = new HashNode[newCapacity];
        capacity = newCapacity;
        size = 0;

        for (HashNode<K, V> head : oldTable) {
            HashNode<K, V> current = head;
            while (current != null) {
                put(current.getKey(), current.getValue());
                current = current.getNext();
            }
        }
    }

    public int getSize() { 
        return size; 
    }

    public int getCapacity() { 
        return capacity; 
    }

    public int getTotalCollisions() { 
        return totalCollisions; 
    }
}