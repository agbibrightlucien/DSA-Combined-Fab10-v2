package structures;

import java.util.Iterator;
import java.util.function.Predicate;

public class CustomLinkedList<T> implements Iterable<T> {

    // Doubly linked node structure
    public static class Node<T> {
        public T data;
        public Node<T> prev;
        public Node<T> next;

        public Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }

    private Node<T> head;
    private Node<T> tail;
    private int size;

    public CustomLinkedList() {
        this.head = null;
        this.tail = null;
        this.size = 0;
    }

    // Add element at the front
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

    // Add element at the end
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

    // Insert value after a specific existing node
    public void insertAfter(Node<T> existingNode, T value) {
        if (existingNode == null) return;

        if (existingNode == tail) {
            addLast(value);
            return;
        }

        Node<T> newNode = new Node<>(value);
        newNode.next = existingNode.next;
        newNode.prev = existingNode;
        
        if (existingNode.next != null) {
            existingNode.next.prev = newNode;
        }
        existingNode.next = newNode;
        size++;
    }

    // Remove first occurrence of a value
    public boolean remove(T value) {
        Node<T> current = head;
        while (current != null) {
            if ((value == null && current.data == null) || (value != null && value.equals(current.data))) {
                if (current == head) {
                    head = head.next;
                    if (head != null) head.prev = null;
                    else tail = null;
                } else if (current == tail) {
                    tail = tail.prev;
                    if (tail != null) tail.next = null;
                    else head = null;
                } else {
                    current.prev.next = current.next;
                    current.next.prev = current.prev;
                }
                size--;
                return true;
            }
            current = current.next;
        }
        return false;
    }

    // Generic search method using Predicate (Prevents coupling with specific domain classes like Ticket)
    public T find(Predicate<T> predicate) {
        if (predicate == null) return null;
        for (T item : this) {
            if (predicate.test(item)) {
                return item;
            }
        }
        return null;
    }

    public int size() {
        return size;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public Node<T> getHead() {
        return head;
    }

    // Connects to our custom iterator class
    @Override
    public Iterator<T> iterator() {
        return new CustomIterator<>(head);
    }
}