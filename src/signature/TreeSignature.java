package signature;

import java.util.ArrayList;
import java.util.Collections;

public class TreeSignature {
    
    public enum Comparison { EQ, NE, LT, GT }

    private class Sigchild implements Comparable<Sigchild> {
        public Node node;
        public String sig;
        
        public Sigchild(Node node, String sig) {
            this.node = node;
            this.sig = sig;
        }
        
        public int compareTo(Sigchild other) {
            return -(this.sig.compareTo(other.sig));
        }
    }
    
    private class Node {
        
        public int atomNumber;
        
        public String element;
        
        public Node up;
        
        public Node down;
        
        public Node next;
        
        public Node(String s, Node parent) {
            this.element = s;
            this.up = parent;
        }
        
        public Node(Node other) {
            this.atomNumber = other.atomNumber;
            this.element = other.element;
        }
        
        public void addChild(Node child) {
            if (this.down == null) this.down = child;
            if (child != null) child.up = this; 
        }
        
        public Node canonize() {
            Node next = this.down;
            if (next == null) return this;
            
            ArrayList<Sigchild> children = new ArrayList<Sigchild>();
            next = this.down;
            while (next != null) {
                next.canonize();
                children.add(new Sigchild(next, next.toString()));
                next = next.next;
            }
            Collections.sort(children);
            
            this.down = children.get(0).node;
            next = this.down;
            for (int i = 1; i < children.size(); i++) {
                next.next = children.get(i).node;
                next = next.next;
            }
            next.next = null;
            return this;
        }
        
        public void truncate(int h, int maxH) {
            if (h > maxH) return;
            Node n = this.down;
            while (n != null) {
                Node p = n.next;
                if (h < maxH) {
                    n.truncate(h + 1, maxH);
                }
                n = p;
            }
            if (h == maxH) this.down = null;
        }
        
        public String toString() {
            StringBuffer sb = new StringBuffer();
            this.toString(sb);
            return sb.toString();
        }
        
        private void toString(StringBuffer sb) {
            sb.append("[").append(this.element).append("]");
            Node r = this.down;
            if (r == null) {
                return;
            } else {
                sb.append("(");
                while (r != null) {
                    r.toString(sb);
                    r = r.next;
                }
                sb.append(")");
            }
        }
        
        public int height() {
            int h = 0;
            int hmax = 0;
            Node r = this.down;
            while (r != null) {
                h = r.height() + 1;
                if (h > hmax) hmax = h;
                r = r.next;
            }
            return hmax;
        }
        
        public Comparison compare(Node other) {
            if (other == null) return Comparison.GT;
            if (this.element.compareTo(other.element) != 0) {
                return Comparison.NE;
            }
            Node n1 = this.down;
            Node n2 = other.down;
            while (true) {
                if (n1 == null && n2 == null) break;
                Comparison r = n1.compare(n2);
                if (r != Comparison.EQ) return r;
                n1 = n1.next;
                n2 = n2.next;
            }
            return Comparison.EQ;
        }
    }
    
    private Node root;
    
    public TreeSignature(String signatureString) {
        this.root = parse(signatureString);
    }
    
    public TreeSignature(TreeSignature other) {
        this.root = copy(other.root);
    }
    
    private Node parse(String s) {
        Node root = null;
        Node parent = null;
        Node current = null;
        Node previous = null;
        int j = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                parent = current;
            } else if (c == ')') {
                parent = parent.up;
                previous = parent;
            } else if (c == '[') {
                j = i + 1;
            } else if (c == ']') {
                String ss = s.substring(j, i); 
                if (root == null) {
                    root = new Node(ss, null);
                    parent = root;
                    current = root;
                } else {
                    current = new Node(ss, parent);
                    if (previous != null) {
                        previous.next = current;
                    }
                    previous = current;
                    parent.addChild(current);
                }
            } else if (c == 'p') {
                // ignore, for now
            } else {
            }
        }
        return root;
    }
    
    private Node copy(Node root) {
        if (root == null) return null;
        
        Node r = new Node(root);
        ArrayList<Node> children = new ArrayList<Node>();
        Node next = root.down;
        while (next != null) {
            children.add(copy(next));
            next = next.next;
        }
        if (children.size() > 0) {
            r.addChild(children.get(0));
            next = r.down;
            for (int i = 0; i < children.size(); i++) {
                next.next = children.get(i);
                if (next.next != null) next.next.up = r;
                next = next.next;
            }
        }
        return r;
    }
    
    public int height() {
        return root.height();
    }
    
    public int countCompatibleBonds(TreeSignature other) {
        int n = 0;
        
        TreeSignature t1 = new TreeSignature(this);
        int h = t1.height();
        t1.truncate(0, h - 1);
        t1.canonize();
        for (int i = 0; i < other.rootNeighbourCount(); i++) {
            TreeSignature t2 = new TreeSignature(other);
            t2.changeRoot(i);
            t2.truncate(0, h - 1);
            t2.canonize();
            if (t1.compare(t2) == Comparison.EQ) n++;
        }
        return n;
    }

    public Comparison compare(TreeSignature other) {
        return compare(this.root, other.root);
    }
    
    private static Comparison compare(Node t1, Node t2) {
        if (t1 == null && t2 == null) return Comparison.EQ;
        if (t1 == null) return Comparison.LT;
        if (t2 == null) return Comparison.GT;
        if (t1.element.compareTo(t2.element) != 0) {
            return Comparison.NE;
        }
        Node n1 = t1.down;
        Node n2 = t2.down;
        while (true) {
            if (n1 == null && n2 == null) break;
            Comparison r = compare(n1, n2);
            if (r != Comparison.EQ) return r;
            n1 = n1.next;
            n2 = n2.next;
        }
        return Comparison.EQ;
    }

    public void changeRoot(int i) {
        
        ArrayList<Node> children = new ArrayList<Node>();
        Node next = root.down;
        while (next != null) {
            children.add(next);
            next = next.next;
        }
        if (i > children.size()) return;
        
        if (i == 0) {
            root.down = children.get(1);
        } else {
            root.down = children.get(0);
            next = root.down;
            for (int j = 1; j < children.size(); j++) {
                if (j != i) {
                    next.next = children.get(j);
                    next = next.next;
                }
            }
            next.next = null;
        }
        Node r = children.get(i);
        if (r.down == null) {
            r.down = root;
            root = r;
            return;
        } else {
            next = r.down;
            while (next.next != null) next = next.next;
            next.next = root;
            
            r.up = null;
            next = r.down;
            while (next != null) {
                next.up = r;
                next = next.next;
            }
            root = r;
            return;
        }
    }

    public int rootNeighbourCount() {
        int i = 0;
        Node next = root.down;
        while (next != null) {
            i++;
            next = next.next;
        }
        return i;
    }

    public void canonize() {
        root = root.canonize(); 
    }

    public void truncate(int h, int H) {
        root.truncate(h, H);
    }
    
    public String toString() {
        return root.toString();
    }
}
