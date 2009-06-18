package signature;

import java.util.ArrayList;

/**
 * A target atomic signature records the structural context that an atom must 
 * have - that is, its neighbours, and neighbours of neighbours - in the final
 * structure. It is more abstract than just the AtomicSignature, which is 
 * derived from a molecule.
 * 
 *  
 * @author maclean
 *
 */
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
        
        public void toString(StringBuffer buffer, int h, boolean useParent) {
            buffer.append(this.label);
            if (this.children.size() == 0 || h == 1) return;
            for (Node child : this.children) {
                child.toString(buffer, h - 1, false);
            }
            if (useParent && this.parent != null) {
                buffer.append(this.parent.label);
            }
            buffer.append(")");
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
    
    public ArrayList<String> getSignatureStrings(int height) {
        ArrayList<String> sigStrings = new ArrayList<String>();
        for (Node child : this.root.children) {
            sigStrings.add(this.getSignatureString(child, height));
        }
        return sigStrings;
    }
    
    public String getSignatureString(Node start, int h) {
        StringBuffer buffer = new StringBuffer();
        buffer.append(start.label);
        buffer.append("(");
        return buffer.toString();
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
