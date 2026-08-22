package structures;

import operations.CustomHashTable;

import java.util.List;

/**
 * Custom Map ADT built directly on top of {@link CustomHashTable} (Section 6
 * of the brief: "Set, map: custom set/map on top of hash table or BST").
 * No java.util.HashMap/TreeMap involved anywhere in this class.
 *
 * @param <K> key type
 * @param <V> value type
 */
public class CustomMap<K, V> {

    private final CustomHashTable<K, V> table;

    public CustomMap() {
        this(10); // -> CustomHashTable capacity 100, matching its default sizing rule
    }

    public CustomMap(int capacityFactor) {
        this.table = new CustomHashTable<>(capacityFactor);
    }

    /** Inserts or updates the value stored under {@code key}. */
    public void put(K key, V value) {
        table.put(key, value);
    }

    /** Returns the value stored under {@code key}, or null if absent (lookup use case). */
    public V get(K key) {
        return table.get(key);
    }

    /** Membership use case: is this key present? */
    public boolean containsKey(K key) {
        return table.containsKey(key);
    }

    /** Removes the entry for {@code key}; returns true if one was present. */
    public boolean remove(K key) {
        return table.remove(key);
    }

    /** All keys currently stored, in no particular order. */
    public List<K> keys() {
        return table.keys();
    }

    public int size() {
        return table.getSize();
    }

    public boolean isEmpty() {
        return table.getSize() == 0;
    }
}
