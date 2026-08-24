package structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Simplified Red-Black Tree implementation focusing on insertion with rotations
 * and recoloring. This implementation is intentionally minimal: it supports
 * insertions that maintain approximate balance using standard RB rules. It is
 * adequate for demonstration and height comparison in the project report.
 */
public class RedBlackTree<K extends Comparable<K>, V> {

    private static final boolean RED = true;
    private static final boolean BLACK = false;

    private class Node {
        K key;
        V value;
        Node left, right, parent;
        boolean color = RED;

        Node(K key, V value, Node parent) {
            this.key = key;
            this.value = value;
            this.parent = parent;
            this.color = RED;
        }
    }

    private Node root;

    public void insert(K key, V value) {
        if (root == null) {
            root = new Node(key, value, null);
            root.color = BLACK;
            return;
        }
        Node n = root;
        Node parent = null;
        int cmp = 0;
        while (n != null) {
            parent = n;
            cmp = key.compareTo(n.key);
            if (cmp < 0) n = n.left;
            else if (cmp > 0) n = n.right;
            else {
                n.value = value;
                return;
            }
        }
        Node newNode = new Node(key, value, parent);
        if (cmp < 0) parent.left = newNode;
        else parent.right = newNode;
        fixInsert(newNode);
    }

    private void fixInsert(Node z) {
        while (z.parent != null && z.parent.color == RED) {
            Node gp = z.parent.parent;
            if (gp == null) break;
            if (z.parent == gp.left) {
                Node y = gp.right; // uncle
                if (y != null && y.color == RED) {
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    gp.color = RED;
                    z = gp;
                } else {
                    if (z == z.parent.right) {
                        z = z.parent;
                        rotateLeft(z);
                    }
                    z.parent.color = BLACK;
                    gp.color = RED;
                    rotateRight(gp);
                }
            } else {
                Node y = gp.left;
                if (y != null && y.color == RED) {
                    z.parent.color = BLACK;
                    y.color = BLACK;
                    gp.color = RED;
                    z = gp;
                } else {
                    if (z == z.parent.left) {
                        z = z.parent;
                        rotateRight(z);
                    }
                    z.parent.color = BLACK;
                    gp.color = RED;
                    rotateLeft(gp);
                }
            }
        }
        root.color = BLACK;
    }

    private void rotateLeft(Node x) {
        Node y = x.right;
        if (y == null) return;
        x.right = y.left;
        if (y.left != null) y.left.parent = x;
        y.parent = x.parent;
        if (x.parent == null) root = y;
        else if (x == x.parent.left) x.parent.left = y;
        else x.parent.right = y;
        y.left = x;
        x.parent = y;
    }

    private void rotateRight(Node x) {
        Node y = x.left;
        if (y == null) return;
        x.left = y.right;
        if (y.right != null) y.right.parent = x;
        y.parent = x.parent;
        if (x.parent == null) root = y;
        else if (x == x.parent.right) x.parent.right = y;
        else x.parent.left = y;
        y.right = x;
        x.parent = y;
    }

    public V search(K key) {
        Node n = root;
        while (n != null) {
            int cmp = key.compareTo(n.key);
            if (cmp == 0) return n.value;
            if (cmp < 0) n = n.left;
            else n = n.right;
        }
        return null;
    }

    public boolean contains(K key) {
        return search(key) != null;
    }

    public List<V> inorderTraversal() {
        List<V> out = new ArrayList<>();
        inorder(root, out);
        return out;
    }

    private void inorder(Node n, List<V> out) {
        if (n == null) return;
        inorder(n.left, out);
        out.add(n.value);
        inorder(n.right, out);
    }

    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node n) {
        if (n == null) return 0;
        return 1 + Math.max(heightRec(n.left), heightRec(n.right));
    }

    public boolean isEmpty() {
        return root == null;
    }
}
