package structures;

import java.util.NoSuchElementException;

/**
 * Custom Generic Doubly Linked List with custom iterator support.
 * Implemented completely from scratch without using java.util.LinkedList.
 *
 * <p><strong>Node Chain Diagram (Report Evidence):</strong></p>
 * <pre>
 * +------+    +--------------------------+    +--------------------------+    +------+
 * | head | -> | Node 1                   | -> | Node 2                   | -> | tail |
 * |      |    | data: "RES-001 -> REQ-1" |    | data: "RES-002 -> REQ-2" |    |      |
 * |      | <- | prev: null, next: Node 2 | <- | prev: Node 1, next: null | <- |      |
 * +------+    +--------------------------+    +--------------------------+    +------+
 * </pre>
 *
 * @param <T> element type stored in the list
 */
public class CustomLinkedList<T> implements Iterable<T> {

    /**
     * Inner Node class for doubly linked list.
     *
     * @param <T> data type stored in node
     */
    public static class Node<T> {
        public T data;
        public Node<T> prev;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }

        public T getData() {
            return data;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    /**
     * Constructs an empty CustomLinkedList.
     */
    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    /**
     * Adds an element to the front (head) of the linked list.
     *
     * @param value element to add
     */
    public void addFirst(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            newNode.next = head;
            head.prev = newNode;
            head = newNode;
        }
        size++;
    }

    /**
     * Adds an element to the end (tail) of the linked list.
     *
     * @param value element to add
     */
    public void addLast(T value) {
        Node<T> newNode = new Node<>(value);
        if (isEmpty()) {
            head = tail = newNode;
        } else {
            tail.next = newNode;
            newNode.prev = tail;
            tail = newNode;
        }
        size++;
    }

    /**
     * Convenience alias for {@link #addLast(Object)}.
     *
     * @param value element to add
     */
    public void add(T value) {
        addLast(value);
    }

    /**
     * Inserts a new value immediately after an existing node.
     *
     * @param existingNode target node after which newValue is inserted
     * @param newValue     new element to insert
     * @return inserted Node reference
     */
    public Node<T> insertAfter(Node<T> existingNode, T newValue) {
        if (existingNode == null) {
            throw new IllegalArgumentException("Existing node cannot be null");
        }
        Node<T> newNode = new Node<>(newValue);
        newNode.prev = existingNode;
        newNode.next = existingNode.next;

        if (existingNode.next != null) {
            existingNode.next.prev = newNode;
        } else {
            tail = newNode; // existingNode was tail
        }
        existingNode.next = newNode;
        size++;
        return newNode;
    }

    /**
     * Inserts a new value immediately after the first occurrence of an existing value.
     *
     * @param existingValue target value to find
     * @param newValue      new value to insert
     * @return true if target value was found and inserted after, false otherwise
     */
    public boolean insertAfter(T existingValue, T newValue) {
        Node<T> current = head;
        while (current != null) {
            if ((existingValue == null && current.data == null) ||
                (existingValue != null && existingValue.equals(current.data))) {
                insertAfter(current, newValue);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Removes the first occurrence of the specified value from the list.
     *
     * @param value element value to remove
     * @return true if an element was removed, false if not found
     */
    public boolean remove(T value) {
        Node<T> current = head;
        while (current != null) {
            if ((value == null && current.data == null) ||
                (value != null && value.equals(current.data))) {
                removeNode(current);
                return true;
            }
            current = current.next;
        }
        return false;
    }

    /**
     * Removes the first element from the list.
     *
     * @return removed element data
     * @throws NoSuchElementException if the list is empty
     */
    public T removeFirst() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = head.data;
        removeNode(head);
        return data;
    }

    /**
     * Removes the last element from the list.
     *
     * @return removed element data
     * @throws NoSuchElementException if the list is empty
     */
    public T removeLast() {
        if (isEmpty()) {
            throw new NoSuchElementException("List is empty");
        }
        T data = tail.data;
        removeNode(tail);
        return data;
    }

    /**
     * Returns the element at the specified 0-based index.
     *
     * @param index index of element to retrieve
     * @return element at the given index
     * @throws IndexOutOfBoundsException if index is out of bounds
     */
    public T get(int index) {
        if (index < 0 || index >= size) {
            throw new IndexOutOfBoundsException("Index: " + index + ", Size: " + size);
        }
        Node<T> current = head;
        for (int i = 0; i < index; i++) {
            current = current.next;
        }
        return current.data;
    }

    /**
     * Returns the head node of the list.
     *
     * @return head node
     */
    public Node<T> getHead() {
        return head;
    }

    /**
     * Returns the tail node of the list.
     *
     * @return tail node
     */
    public Node<T> getTail() {
        return tail;
    }

    /**
     * Returns the number of elements in the list.
     *
     * @return element count
     */
    public int size() {
        return size;
    }

    /**
     * Returns true if the list is empty.
     *
     * @return true if size == 0
     */
    public boolean isEmpty() {
        return size == 0;
    }

    /**
     * Clears all elements from the list.
     */
    public void clear() {
        head = null;
        tail = null;
        size = 0;
    }

    /**
     * Helper method to un-link a node from the doubly linked list.
     */
    private void removeNode(Node<T> node) {
        if (node.prev != null) {
            node.prev.next = node.next;
        } else {
            head = node.next; // node was head
        }

        if (node.next != null) {
            node.next.prev = node.prev;
        } else {
            tail = node.prev; // node was tail
        }
        size--;
    }

    /**
     * Returns a {@link CustomIterator} over the elements in this list.
     *
     * @return CustomIterator starting at the head
     */
    public CustomIterator<T> customIterator() {
        return new CustomLinkedListIterator();
    }

    /**
     * Returns a standard java.util.Iterator delegating to CustomIterator
     * to satisfy the {@link Iterable} interface for for-each loop support.
     */
    @Override
    public java.util.Iterator<T> iterator() {
        CustomIterator<T> custom = customIterator();
        return new java.util.Iterator<T>() {
            @Override
            public boolean hasNext() {
                return custom.hasNext();
            }

            @Override
            public T next() {
                return custom.next();
            }
        };
    }

    /**
     * Inner CustomIterator implementation for CustomLinkedList.
     */
    private class CustomLinkedListIterator implements CustomIterator<T> {
        private Node<T> current = head;

        @Override
        public boolean hasNext() {
            return current != null;
        }

        @Override
        public T next() {
            if (!hasNext()) {
                throw new NoSuchElementException("No more elements in list");
            }
            T data = current.data;
            current = current.next;
            return data;
        }
    }

    @Override
    public String toString() {
        StringBuilder sb = new StringBuilder("[");
        Node<T> curr = head;
        while (curr != null) {
            sb.append(curr.data);
            if (curr.next != null) {
                sb.append(" <-> ");
            }
            curr = curr.next;
        }
        sb.append("]");
        return sb.toString();
    }
}
