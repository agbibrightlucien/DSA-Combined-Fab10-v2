package structures;

import java.util.List;

/**
 * Custom Set ADT built directly on top of {@link CustomMap} (which is itself
 * built on {@link operations.CustomHashTable}) — Section 6 of the brief:
 * "Set, map: custom set/map on top of hash table or BST". Presence is tracked
 * with a constant sentinel value; no java.util.HashSet involved.
 *
 * @param <T> element type
 */
public class CustomSet<T> {

    private static final Boolean PRESENT = Boolean.TRUE;

    private final CustomMap<T, Boolean> map;

    public CustomSet() {
        this.map = new CustomMap<>();
    }

    public CustomSet(int capacityFactor) {
        this.map = new CustomMap<>(capacityFactor);
    }

    /** Adds an element; returns true if it was newly added (false if already present). */
    public boolean add(T item) {
        boolean isNew = !map.containsKey(item);
        map.put(item, PRESENT);
        return isNew;
    }

    /** Membership use case: is this element present? */
    public boolean contains(T item) {
        return map.containsKey(item);
    }

    /** Removes an element; returns true if it was present. */
    public boolean remove(T item) {
        return map.remove(item);
    }

    /** All elements currently stored, in no particular order. */
    public List<T> elements() {
        return map.keys();
    }

    public int size() {
        return map.size();
    }

    public boolean isEmpty() {
        return map.isEmpty();
    }
}
