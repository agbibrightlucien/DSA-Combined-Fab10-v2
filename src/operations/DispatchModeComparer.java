package operations;

import structures.CircularQueue;
import structures.CustomDeque;
import structures.CustomHeap;
import structures.CustomQueue;
import structures.UrgentDispatcher;

import java.util.ArrayList;
import java.util.List;

public class DispatchModeComparer {

    public static class ServiceRequest {
        private String id;
        private int priorityScore;
        private boolean deadlinePassed;
        
        public ServiceRequest(String id, int priorityScore, boolean deadlinePassed) {
            this.id = id;
            this.priorityScore = priorityScore;
            this.deadlinePassed = deadlinePassed;
        }
        
        public String getId() { return id; }
        public int getPriorityScore() { return priorityScore; }
        public boolean isDeadlinePassed() { return deadlinePassed; }
        
        @Override
        public String toString() {
            return id + "(P:" + priorityScore + (deadlinePassed ? ",URGENT" : "") + ")";
        }
    }
    
    public static ComparisonResult compareDispatchModes(List<ServiceRequest> requests) {
        ComparisonResult result = new ComparisonResult();

        result.fifoOrder = runFIFO(new ArrayList<>(requests));

        result.circularOrder = runCircular(new ArrayList<>(requests));

        result.heapOrder = runUrgentDispatcher(new ArrayList<>(requests));
        
        return result;
    }
    
    private static List<String> runFIFO(List<ServiceRequest> requests) {
        CustomQueue<ServiceRequest> queue = new CustomQueue<>();

        for (ServiceRequest req : requests) {
            queue.enqueue(req);
        }

        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            order.add(queue.dequeue().toString());
        }
        
        return order;
    }
    
    private static List<String> runCircular(List<ServiceRequest> requests) {
        int capacity = Math.max(requests.size() + 5, 10);
        CircularQueue<ServiceRequest> queue = new CircularQueue<>(capacity);
        
        System.out.println("\n=== Circular Queue Trace ===");
        
        for (ServiceRequest req : requests) {
            queue.enqueue(req);
        }
        
        List<String> order = new ArrayList<>();
        while (!queue.isEmpty()) {
            order.add(queue.dequeue().toString());
        }
        
        return order;
    }
    
    private static List<String> runUrgentDispatcher(List<ServiceRequest> requests) {
        UrgentDispatcher<ServiceRequest> dispatcher = new UrgentDispatcher<>();
        
        for (ServiceRequest req : requests) {
            dispatcher.addRequest(req, req.getPriorityScore(), req.isDeadlinePassed());
        }
        
        List<ServiceRequest> processed = dispatcher.processAll();
        List<String> order = new ArrayList<>();
        for (ServiceRequest req : processed) {
            order.add(req.toString());
        }
        
        return order;
    }

    public static class ComparisonResult {
        public List<String> fifoOrder;
        public List<String> circularOrder;
        public List<String> heapOrder;
        
        @Override
        public String toString() {
            StringBuilder sb = new StringBuilder();
            sb.append("=== DISPATCH MODE COMPARISON ===\n\n");
            
            sb.append("FIFO Queue Order:\n");
            sb.append("  ").append(String.join(" -> ", fifoOrder)).append("\n\n");
            
            sb.append("Circular Queue Order:\n");
            sb.append("  ").append(String.join(" -> ", circularOrder)).append("\n\n");
            
            sb.append("Urgent Dispatcher (Heap + Deque) Order:\n");
            sb.append("  ").append(String.join(" -> ", heapOrder)).append("\n\n");
            
            sb.append("=== DIFFERENCES ===\n");
            if (!fifoOrder.equals(circularOrder)) {
                sb.append("✓ FIFO and Circular Queue produce different orders\n");
            } else {
                sb.append("  FIFO and Circular Queue produce the same order\n");
            }
            
            if (!fifoOrder.equals(heapOrder)) {
                sb.append("✓ Urgent Dispatcher produces a different order from FIFO\n");
            } else {
                sb.append("  Urgent Dispatcher produces the same order as FIFO\n");
            }
            
            if (!circularOrder.equals(heapOrder)) {
                sb.append("✓ Urgent Dispatcher produces a different order from Circular Queue\n");
            } else {
                sb.append("  Urgent Dispatcher produces the same order as Circular Queue\n");
            }
            
            return sb.toString();
        }
    }

    public static void main(String[] args) {
        // Create a sample batch of service requests
        List<ServiceRequest> requests = new ArrayList<>();
        requests.add(new ServiceRequest("R1", 5, false));
        requests.add(new ServiceRequest("R2", 8, true));  // Urgent - deadline passed
        requests.add(new ServiceRequest("R3", 3, false));
        requests.add(new ServiceRequest("R4", 9, false));
        requests.add(new ServiceRequest("R5", 6, true));  // Urgent - deadline passed
        requests.add(new ServiceRequest("R6", 2, false));
        requests.add(new ServiceRequest("R7", 7, false));
        
        System.out.println("Input Requests: " + requests);
        System.out.println();
        
        ComparisonResult result = compareDispatchModes(requests);
        System.out.println(result);
        
        System.out.println("=== CIRCULAR QUEUE WRAP AROUND DEMONSTRATION ===");
        CircularQueue<String> demoQueue = new CircularQueue<>(5);
        System.out.println("Queue capacity: 5");
        System.out.println("Enqueuing 1, 2, 3, 4, 5 (should fill queue)");
        for (int i = 1; i <= 5; i++) {
            demoQueue.enqueue("Item" + i);
        }
        System.out.println("\nDequeuing 3 items (should show wrap around when re-enqueuing)");
        for (int i = 0; i < 3; i++) {
            demoQueue.dequeue();
        }
        System.out.println("\nEnqueuing 6, 7, 8 (should wrap around to front)");
        for (int i = 6; i <= 8; i++) {
            demoQueue.enqueue("Item" + i);
        }
    }
}