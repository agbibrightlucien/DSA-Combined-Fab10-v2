import structures.BTree;

public class BTreeTest {
    public static void main(String[] args) {
        BTree<String, String> tree = new BTree<>(3); // minDegree 3

        // requestId-style String keys, matching the real service_requests.requestId column
        String[] requestIds = {
            "REQ101", "REQ205", "REQ310", "REQ042", "REQ088", "REQ150", "REQ275", "REQ006",
            "REQ199", "REQ333", "REQ017", "REQ400", "REQ062", "REQ091", "REQ128", "REQ250",
            "REQ305", "REQ055", "REQ180", "REQ220", "REQ009", "REQ500"
        };

        System.out.println("=== Inserting requestIds one at a time ===");
        for (String id : requestIds) {
            tree.insert(id, "Request-" + id);
            if (tree.getLastSplitTriggerKey() != null) {
                System.out.println(">>> SPLIT triggered by inserting requestId " + id
                        + " (split #" + tree.getSplitCount() + ")");
                System.out.println("--- Tree structure after this split ---");
                tree.printStructure();
                System.out.println();
            }
        }

        System.out.println("=== Final tree structure ===");
        tree.printStructure();

        System.out.println("\n=== Total splits: " + tree.getSplitCount() + " ===");

        System.out.println("\n=== Search correctness check ===");
        boolean allFound = true;
        for (String id : requestIds) {
            String val = tree.search(id);
            boolean ok = ("Request-" + id).equals(val);
            allFound &= ok;
            if (!ok) {
                System.out.println("FAIL: search(" + id + ") returned " + val);
            }
        }
        System.out.println("All " + requestIds.length + " inserted keys found correctly: " + allFound);

        String missing = tree.search("REQ9999");
        System.out.println("search(\"REQ9999\") [not inserted] returns null: " + (missing == null));
    }
}
