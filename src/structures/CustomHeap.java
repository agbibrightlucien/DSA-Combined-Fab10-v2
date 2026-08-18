package structures;

import java.util.ArrayList;
import java.util.List;

public class CustomHeap<T> {
    private List<HeapNode<T>> heap;
    
    private static class HeapNode<T> {
        T data;
        int priority;
        
        HeapNode(T data, int priority) {
            this.data = data;
            this.priority = priority;
        }
    }
    
    public CustomHeap() {
        heap = new ArrayList<>();
    }
    
    public void insert(T data, int priority) {
        HeapNode<T> node = new HeapNode<>(data, priority);
        heap.add(node);
        heapifyUp(heap.size() - 1);
    }
    
    public T extractMax() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }
        T max = heap.get(0).data;
        if (heap.size() > 1) {
            heap.set(0, heap.remove(heap.size() - 1));
            heapifyDown(0);
        } else {
            heap.remove(0);
        }
        return max;
    }
    
    public T peek() {
        if (isEmpty()) {
            throw new IllegalStateException("Heap is empty");
        }
        return heap.get(0).data;
    }
    
    public boolean isEmpty() {
        return heap.isEmpty();
    }
    
    public int size() {
        return heap.size();
    }
    
    private void heapifyUp(int index) {
        while (index > 0) {
            int parent = (index - 1) / 2;
            if (heap.get(index).priority <= heap.get(parent).priority) {
                break;
            }
            swap(index, parent);
            index = parent;
        }
    }
    
    private void heapifyDown(int index) {
        int largest = index;
        int left = 2 * index + 1;
        int right = 2 * index + 2;
        
        if (left < heap.size() && heap.get(left).priority > heap.get(largest).priority) {
            largest = left;
        }
        if (right < heap.size() && heap.get(right).priority > heap.get(largest).priority) {
            largest = right;
        }
        
        if (largest != index) {
            swap(index, largest);
            heapifyDown(largest);
        }
    }
    
    private void swap(int i, int j) {
        HeapNode<T> temp = heap.get(i);
        heap.set(i, heap.get(j));
        heap.set(j, temp);
    }
}
