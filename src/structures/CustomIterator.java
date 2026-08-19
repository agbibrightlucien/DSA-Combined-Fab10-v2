package structures;

/**
 * Custom Iterator interface implemented from scratch for Module M3 / Part 3.
 * Replaces java.util.Iterator without relying on java.util packages.
 *
 * @param <T> the type of elements returned by this iterator
 */
public interface CustomIterator<T> {
    /**
     * Returns true if the iteration has more elements.
     *
     * @return true if there are more elements to iterate over
     */
    boolean hasNext();

    /**
     * Returns the next element in the iteration.
     *
     * @return the next element
     * @throws java.util.NoSuchElementException if the iteration has no more elements
     */
    T next();
}
