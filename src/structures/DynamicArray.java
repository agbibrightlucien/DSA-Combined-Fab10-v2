package structures;

/**
 * Generic Dynamic Array (Array-Backed List) implemented from scratch.
 * Provides a growable array container without using java.util.ArrayList.
 *
 * <p><strong>Manual Resize Trace (Report Evidence):</strong></p>
 * <pre>
 * Scenario: Initial capacity = 4, elements inserted = 5
 * ----------------------------------------------------
 * Initial State:
 *   capacity = 4, size = 0, elements = [null, null, null, null]
 *
 * Insert 1..4 (Array reaches full capacity):
 *   insert("A") -> size = 1, elements = ["A", null, null, null]
 *   insert("B") -> size = 2, elements = ["A", "B", null, null]
 *   insert("C") -> size = 3, elements = ["A", "B", "C", null]
 *   insert("D") -> size = 4, elements = ["A", "B", "C", "D"] (Full!)
 *
 * Trigger Resize on 5th Insert:
 *   insert("E"):
 *     1. Old Capacity = 4
 *     2. New Capacity = 4 * 2 = 8
 *     3. Copy Old Array -> New Array
 *     4. Backing Array BEFORE resize: ["A", "B", "C", "D"] (capacity 4)
 *     5. Backing Array AFTER resize : ["A", "B", "C", "D", "E", null, null, null] (capacity 8)
 *   Final State: capacity = 8, size = 5
 * </pre>
 *
 * @param <T> the type of elements stored in this list
 */
public class DynamicArray<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    /**
     * Constructs a DynamicArray with the default initial capacity (10).
     */
    public DynamicArray() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a DynamicArray with the specified initial capacity.
     *
     * @param initialCapacity initial size of the backing array
     * @throws IllegalArgumentException if initialCapacity is less than or equal to 0
     */
    public DynamicArray(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException("Initial capacity must be greater than 0");
        }
        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Appends the specified value to the end of the array.
     * Automatically doubles capacity if the array is full.
     *
     * @param value element to be inserted
     */
    public void insert(T value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    /**
     * Inserts the specified value at the specified index.
     * Shifts existing elements to the right.
     *
     * @param index position at which to insert the value
     * @param value element to be inserted
     * @throws IndexOutOfBoundsException if index is out of range (index < 0 || index > size)
     */
    public void insert(int index, T value) {
        if (index < 0 || index > size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        ensureCapacity(size + 1);
        System.arraycopy(elements, index, elements, index + 1, size - index);
        elements[index] = value;
        size++;
    }

    /**
     * Convenience alias for {@link #insert(Object)}.
     *
     * @param value element to be added
     */
    public void add(T value) {
        insert(value);
    }

    /**
     * Convenience alias for {@link #insert(int, Object)}.
     *
     * @param index position at which to insert
     * @param value element to be inserted
     */
    public void add(int index, T value) {
        insert(index, value);
    }

    /**
     * Returns the element at the specified index.
     *
     * @param index position of element to return
     * @return element at the given index
     * @throws IndexOutOfBoundsException if index is out of range (index < 0 || index >= size)
     */
    @SuppressWarnings("unchecked")
    public T get(int index) {
        checkBounds(index);
        return (T) elements[index];
    }

    /**
     * Replaces the element at the specified index with the given value.
     *
     * @param index position of element to replace
     * @param value new element value
     * @return old element value that was replaced
     * @throws IndexOutOfBoundsException if index is out of range (index < 0 || index >= size)
     */
    @SuppressWarnings("unchecked")
    public T set(int index, T value) {
        checkBounds(index);
        T oldValue = (T) elements[index];
        elements[index] = value;
        return oldValue;
    }

    /**
     * Removes the element at the specified index.
     * Shifts subsequent elements to the left.
     *
     * @param index position of element to remove
     * @return removed element value
     * @throws IndexOutOfBoundsException if index is out of range (index < 0 || index >= size)
     */
    @SuppressWarnings("unchecked")
    public T remove(int index) {
        checkBounds(index);
        T removedValue = (T) elements[index];
        int numMoved = size - index - 1;
        if (numMoved > 0) {
            System.arraycopy(elements, index + 1, elements, index, numMoved);
        }
        elements[--size] = null; // Clear reference for Garbage Collection
        return removedValue;
    }

    /**
     * Returns the current number of elements in the array.
     *
     * @return number of elements
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the array contains no elements.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the current length of the backing array.
     *
     * @return backing array capacity
     */
    public int capacity() {
        return elements.length;
    }

    /**
     * Clears all elements from the array.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }
        size = 0;
    }

    /**
     * Ensures that the backing array has enough capacity to store the required number of elements.
     * If full, doubles the capacity of the array.
     *
     * @param minCapacity minimum required capacity
     */
    private void ensureCapacity(int minCapacity) {
        if (minCapacity > elements.length) {
            int newCapacity = elements.length * 2;
            if (newCapacity < minCapacity) {
                newCapacity = minCapacity;
            }
            Object[] newArray = new Object[newCapacity];
            System.arraycopy(elements, 0, newArray, 0, size);
            elements = newArray;
        }
    }

    /**
     * Validates that the given index is within valid element bounds [0, size - 1].
     *
     * @param index index to validate
     * @throws IndexOutOfBoundsException if index < 0 or index >= size
     */
    private void checkBounds(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
    }

    @Override
    public String toString() {
        if (size == 0) {
            return "[]";
        }
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < size; i++) {
            sb.append(elements[i]);
            if (i < size - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}
