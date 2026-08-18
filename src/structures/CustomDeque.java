package structures;
 
public class CustomDeque<T> {
    private Node<T> front;
    private Node<T> rear;
    private int size;
    
    private static class Node<T> {
        T data;
        Node<T> prev;
        Node<T> next;
        
        Node(T data) {
            this.data = data;
            this.prev = null;
            this.next = null;
        }
    }
    
    public CustomDeque() {
        front = null;
        rear = null;
        size = 0;
    }

    public void addFront(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            front = rear = newNode;
        } else {
            newNode.next = front;
            front.prev = newNode;
            front = newNode;
        }
        size++;
    }
    

    public void addRear(T element) {
        Node<T> newNode = new Node<>(element);
        if (isEmpty()) {
            front = rear = newNode;
        } else {
            newNode.prev = rear;
            rear.next = newNode;
            rear = newNode;
        }
        size++;
    }

    public T removeFront() {
        if (isEmpty()) {
            throw new IllegalStateException("Deque is empty");
        }
        T data = front.data;
        front = front.next;
        if (front == null) {
            rear = null;
        } else {
            front.prev = null;
        }
        size--;
        return data;
    }

    public T removeRear() {
        if (isEmpty()) {
            throw new IllegalStateException("Deque is empty");
        }
        T data = rear.data;
        rear = rear.prev;
        if (rear == null) {
            front = null;
        } else {
            rear.next = null;
        }
        size--;
        return data;
    }
    
    public T peekFront() {
        if (isEmpty()) {
            throw new IllegalStateException("Deque is empty");
        }
        return front.data;
    }

    public T peekRear() {
        if (isEmpty()) {
            throw new IllegalStateException("Deque is empty");
        }
        return rear.data;
    }

    public boolean isEmpty() {
        return size == 0;
    }

    public int size() {
        return size;
    }
}