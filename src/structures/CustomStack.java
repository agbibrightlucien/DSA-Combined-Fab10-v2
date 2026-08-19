package structures;

/**
 * Generic Stack implemented from scratch using an array.
 * Provides push, pop, peek and empty-check operations.
 *
 * <p>The backing array automatically doubles in capacity
 * when it becomes full, following the same approach as
 * DynamicArray.</p>
 *
 * @param <T> the type of elements stored in this stack
 */
public class CustomStack<T> {

    private static final int DEFAULT_CAPACITY = 10;

    private Object[] elements;
    private int size;

    /**
     * Constructs a CustomStack with the default initial capacity (10).
     */
    public CustomStack() {
        this(DEFAULT_CAPACITY);
    }

    /**
     * Constructs a CustomStack with the specified initial capacity.
     *
     * @param initialCapacity initial size of the backing array
     * @throws IllegalArgumentException if initialCapacity is less than or equal to 0
     */
    public CustomStack(int initialCapacity) {
        if (initialCapacity <= 0) {
            throw new IllegalArgumentException(
                    "Initial capacity must be greater than 0");
        }

        this.elements = new Object[initialCapacity];
        this.size = 0;
    }

    /**
     * Adds an element to the top of the stack.
     *
     * @param value element to be added
     */
    public void push(T value) {
        ensureCapacity(size + 1);
        elements[size++] = value;
    }

    /**
     * Removes and returns the element at the top of the stack.
     *
     * @return the removed element, or null if the stack is empty
     */
    @SuppressWarnings("unchecked")
    public T pop() {
        if (isEmpty()) {
            return null;
        }

        T removedValue = (T) elements[--size];
        elements[size] = null;

        return removedValue;
    }

    /**
     * Returns the element at the top of the stack without removing it.
     *
     * @return the top element, or null if the stack is empty
     */
    @SuppressWarnings("unchecked")
    public T peek() {
        if (isEmpty()) {
            return null;
        }

        return (T) elements[size - 1];
    }

    /**
     * Returns true if the stack contains no elements.
     *
     * @return true if empty, false otherwise
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Returns the current number of elements in the stack.
     *
     * @return number of elements
     */
    public int size() {
        return size;
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
     * Removes all elements from the stack.
     */
    public void clear() {
        for (int i = 0; i < size; i++) {
            elements[i] = null;
        }

        size = 0;
    }

    /**
     * Ensures that the backing array has enough capacity.
     * If full, doubles the capacity, following the same
     * approach used by DynamicArray.
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

            System.arraycopy(
                    elements,
                    0,
                    newArray,
                    0,
                    size
            );

            elements = newArray;
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