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
        
        public boolean visited;
        
        public Node(char label, Node parent) {
            this.label = label;
            this.parent = parent;
            this.children = new ArrayList<Node>();
            this.visited = false;
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
    
    /**
     * Starting from the root of this signature, return a sub-signature of
     * height h.
     * 
     * @param h the height to go out to
     * @return a string representation of the sub-signature
     */
    public String getSubSignature(int h) {
        return getSignatureString(root, h);
    }
    
    /**
     * Get a signature string starting at a child of the root - in other words
     * the signature string in the subgraph made from the neighbours of the
     * root. This will include the root for signatures of height greater than
     * zero.
     * 
     * @param startNodeIndex the index of the child of the root
     * @param h the height to go out to
     * @return
     */
    public String getSignatureString(int startNodeIndex, int h) {
        return getSignatureString(this.root.children.get(startNodeIndex), h);
    }
    
    private String getSignatureString(Node start, int h) {
        StringBuffer buffer = new StringBuffer();
        traverse(start, 0, h, buffer);
        clearVisited(root);
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

    private void traverse(Node current, int h, int maxH, StringBuffer buffer) {
        if (current.visited) return;
        buffer.append(current.label);
        current.visited = true;
        if (h < maxH) {
            buffer.append("(");
            for (Node child : current.children) {
                traverse(child, h + 1, maxH, buffer);
            }
            if (current.parent != null) {
                traverse(current.parent, h + 1, maxH, buffer);
            }
            buffer.append(")");
        }
    }
    
    private void clearVisited(Node n) {
        n.visited = false;
        for (Node child : n.children) {
            clearVisited(child);
        }
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
