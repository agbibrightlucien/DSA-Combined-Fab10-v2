package com.allocation.structures;

import java.util.ArrayList;
import java.util.List;

/**
 * Simple Binary Search Tree keyed by a Comparable key.
 * Stores values alongside keys. Provides insert, search and inorder traversal.
 */
public class BST<K extends Comparable<K>, V> {
    private class Node {
        K key;
        V value;
        Node left, right;

        Node(K key, V value) {
            this.key = key;
            this.value = value;
        }
    }

    private Node root;

    public void insert(K key, V value) {
        root = insertRec(root, key, value);
    }

    private Node insertRec(Node node, K key, V value) {
        if (node == null) return new Node(key, value);
        int cmp = key.compareTo(node.key);
        if (cmp < 0) node.left = insertRec(node.left, key, value);
        else if (cmp > 0) node.right = insertRec(node.right, key, value);
        else node.value = value; // replace
        return node;
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

    public List<V> inorderTraversal() {
        List<V> list = new ArrayList<>();
        inorderRec(root, list);
        return list;
    }

    private void inorderRec(Node node, List<V> out) {
        if (node == null) return;
        inorderRec(node.left, out);
        out.add(node.value);
        inorderRec(node.right, out);
    }

    // For height comparison
    public int height() {
        return heightRec(root);
    }

    private int heightRec(Node node) {
        if (node == null) return 0;
        return 1 + Math.max(heightRec(node.left), heightRec(node.right));
    }
}
