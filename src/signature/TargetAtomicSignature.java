package signature;

import java.util.ArrayList;

public class TargetAtomicSignature {
    
    private class Node {
        public char label;
        public Node parent;
        public ArrayList<Node> children;
        public Node(char label, Node parent) {
            this.label = label;
            this.parent = parent;
            this.children = new ArrayList<Node>();
        }
        
        public void toString(StringBuffer buffer) {
            buffer.append(this.label);
            if (this.children.size() == 0) return;
            buffer.append("(");
            for (Node child : this.children) {
                child.toString(buffer);
            }
            buffer.append(")");
        }
    }
    
    private Node root;
    
    private String name;
    
    public TargetAtomicSignature(String signatureString, String name) {
        this(signatureString);
        this.name = name;
    }
    
    public TargetAtomicSignature(String signatureString) {
        this.root = this.parse(signatureString);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        if (this.name != null) {
            buffer.append(" ");
            buffer.append(this.name);
            buffer.append(" ");
        }
        this.root.toString(buffer);
        return buffer.toString();
    }
    
    private Node parse(String s) {
        Node root = null;
        Node parent = null;
        Node current = null;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            switch (c) {
                case '(':
                    parent = current;
                    break;
                case ')':
                    parent = parent.parent;
                    break;
                default:
                    Node node = new Node(c, parent);
                    if (root == null) {
                        root = node;
                        parent = node;
                    } else {
                        parent.children.add(node);
                    }
                    current = node;
                    break;
            }
        }
        return root;
    }
}
