package structures;

import java.util.List;

public class UrgentDispatcher<T> {
    private CustomDeque<T> urgentQueue;
    private CustomHeap<T> heap;
    
    public UrgentDispatcher() {
        this.urgentQueue = new CustomDeque<>();
        this.heap = new CustomHeap<>();
    }

    public void addRequest(T request, int priorityScore, boolean deadlinePassed) {
        if (deadlinePassed) {
            urgentQueue.addFront(request);
            System.out.println("Urgent: Added to front of queue: " + request);
        } else {
            heap.insert(request, priorityScore);
            System.out.println("Normal: Added to heap: " + request + " (score: " + priorityScore + ")");
        }
    }

    public T processNext() {
        if (!urgentQueue.isEmpty()) {
            return urgentQueue.removeFront();
        }
        if (!heap.isEmpty()) {
            return heap.extractMax();
        }
        return null;
    }

    public List<T> processAll() {
        List<T> result = new java.util.ArrayList<>();
        T next;
        while ((next = processNext()) != null) {
            result.add(next);
        }
        return result;
    }

    public boolean hasPending() {
        return !urgentQueue.isEmpty() || !heap.isEmpty();
    }
}