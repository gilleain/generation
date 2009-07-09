package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SignatureTreePanel extends JPanel {
    
    public int width;
    
    public int height;
    
    public Node root;
    
    public int depth;
    
    public class Node {
        
        public int x;
        
        public int y;
        
        public int depth;
        
        public List<Node> children = new ArrayList<Node>();
        
        public String label;
        
        public Node parent;
        
        public Node(String label, Node parent, int d) {
            this.label = label;
            this.parent = parent;
            this.depth = d;
        }
        
        public int countLeaves() {
            if (this.isLeaf()) {
                return 1;
            } else {
                int c = 0;
                for (Node child : this.children) {
                    c += child.countLeaves();
                }
                return c;
            }
        }
        
        public boolean isLeaf() {
            return this.children.size() == 0;
        }
    }
    
    public SignatureTreePanel(String s) {
        root = parse(s);
        this.width = 400;
        this.height = 400;
    }
    
    public Node parse(String s) {
        Node root = null;
        Node parent = null;
        Node current = null;
        int d = 1;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                parent = current;
                d++;
                if (d > depth) depth = d;
            } else if (c == ')') {
                parent = parent.parent;
                d--;
            } else {
                if (root == null) {
                    root = new Node(c + "", null, d);
                    parent = root;
                    current = root;
                } else {
                    current = new Node(c + "", parent, d);
                    parent.children.add(current);
                    current.parent = parent;
                }
            }
        }
        return root;
    }
    
    private int offset = 0;
    
    public void layout(Node node, int center, int xSep, int ySep) {
        node.y = node.depth * ySep;
        if (node.isLeaf()) {
            node.x = offset + center;
            if (node.x > offset) offset = center;
            return;
        } else {
            int l = node.countLeaves();
            int w = l * xSep;
            node.x = offset + (w / 2);
            int c = 0;
            for (Node child : node.children) {
                if (child.isLeaf()) {
                    layout(child, c * xSep, xSep, ySep);
                    c++;
                } else {
                    int leafCount = child.countLeaves();
                    int childWidth = xSep * leafCount;
                    layout(child, childWidth / 2, xSep, ySep);
                }
            }
        }
    }
    
    public void paint(Graphics g) {
        if (root == null) return;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        int leafCount = root.countLeaves();
        int xSep = width / (leafCount + 1);
        int ySep = height / (depth + 1);
        layout(root, width / 2, xSep, ySep);
        g.setColor(Color.BLACK);
        paint(g, root);
    }

    public void paint(Graphics g, Node node) {
        int x = node.x;
        int y = node.y;
        System.out.println("label at " + x + "," + y);
        g.drawString(node.label, x, y);
        for (Node child : node.children) {
            paint(g, child);
        }
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
//        String s = "C(CC(C(CH)C(H))C(C(C))";
        String s = "A(BC(D(EF)H(I))J(K(L))";
        f.add(new SignatureTreePanel(s));
        f.setPreferredSize(new Dimension(400,400));
        f.pack();
        f.setVisible(true);
    }

}
