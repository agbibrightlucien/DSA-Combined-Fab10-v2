package structures;

import java.util.Iterator;
import java.util.NoSuchElementException;

public class CustomIterator<T> implements Iterator<T> {
    private CustomLinkedList.Node<T> current;

    // Constructs iterator starting at the head of the list
    public CustomIterator(CustomLinkedList.Node<T> head) {
        this.current = head;
    }

    // Checks if there are more elements to traverse
    @Override
    public boolean hasNext() {
        return current != null;
    }

    // Returns current data and advances the pointer
    @Override
    public T next() {
        if (!hasNext()) {
            throw new NoSuchElementException("No elements remaining in list.");
        }
        T data = current.data;
        current = current.next;
        return data;
    }
}