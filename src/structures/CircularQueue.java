package structures;

public class CircularQueue<T> {
    private T[] array;
    private int front;
    private int rear;
    private int size;
    private final int capacity;
    
    @SuppressWarnings("unchecked")
    public CircularQueue(int capacity) {
        if (capacity <= 0) {
            throw new IllegalArgumentException("Capacity must be positive");
        }
        this.capacity = capacity;
        this.array = (T[]) new Object[capacity];
        this.front = 0;
        this.rear = -1; 
        this.size = 0;
    }
    
    public void enqueue(T element) {
        if (isFull()) {
            throw new IllegalStateException("Queue is full");
        }

        rear = (rear + 1) % capacity;
        array[rear] = element;
        size++;
        
        System.out.println("Enqueue: " + element + 
                         " | front=" + front + 
                         ", rear=" + rear + 
                         ", size=" + size +
                         " [" + getTrace() + "]");
    }
    
    public T dequeue() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        T element = array[front];
        array[front] = null; 
        front = (front + 1) % capacity;
        size--;
        
        System.out.println("Dequeue: " + element + 
                         " | front=" + front + 
                         ", rear=" + rear + 
                         ", size=" + size +
                         " [" + getTrace() + "]");
        
        return element;
    }
    
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Queue is empty");
        }
        return array[front];
    }
    
    public boolean isEmpty() {
        return size == 0;
    }
    
    public boolean isFull() {
        return size == capacity;
    }
    
    public int size() {
        return size;
    }

    public int capacity() {
        return capacity;
    }

    public String getTrace() {
        StringBuilder sb = new StringBuilder("[");
        for (int i = 0; i < capacity; i++) {
            if (array[i] == null) {
                sb.append("_");
            } else {
                sb.append(array[i]);
            }
            if (i < capacity - 1) {
                sb.append(", ");
            }
        }
        sb.append("]");
        return sb.toString();
    }
}