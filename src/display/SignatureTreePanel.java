package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.GridLayout;
import java.awt.geom.Rectangle2D;
import java.util.ArrayList;
import java.util.List;

import javax.swing.JFrame;
import javax.swing.JPanel;

public class SignatureTreePanel extends JPanel {
    
    public int width;
    
    public int height;
    
    public Node root;
    
    public int depth;
    
    public String s;
    
    public class TreeLayout {
        
        public int leafCount = 0;
        
        public int xSep;
        
        public int ySep;
        
        public void layoutTree(Node root) {
            int leafCount = root.countLeaves();
            this.xSep = width / (leafCount + 1);
            this.ySep = height / (depth + 1);
            layout(root);
        }
        
        public int layout(Node node) {
            node.y  = node.depth * ySep;
            if (node.isLeaf()) {
                leafCount += 1;
                node.x = leafCount * xSep;
                return node.x;
            } else {
                int min = 0;
                int max = 0;
                for (Node child : node.children) {
                    int childCenter = layout(child);
                    if (min == 0) {
                        min = childCenter;
                    }
                    max = childCenter;
                }
                if (min == max) {
                    node.x = min;
                } else {
                    node.x = min + (max - min) / 2;
                }
                return node.x;
            }
        }
        
    }
    
    public class Node {
        
        public int x = -1;
        
        public int y = -1;
        
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
    
    public SignatureTreePanel(String s, int width, int height) {
        root = parse(s);
        this.s = s;
        this.width = width;
        this.height = height;
        this.setPreferredSize(new Dimension(width, height));
    }
    
    public Node parse(String s) {
        Node root = null;
        Node parent = null;
        Node current = null;
        int d = 1;
        int j = 0;
        for (int i = 0; i < s.length(); i++) {
            char c = s.charAt(i);
            if (c == '(') {
                parent = current;
                d++;
                if (d > depth) depth = d;
            } else if (c == ')') {
                parent = parent.parent;
                d--;
            } else if (c == '[') {
                j = i + 1;
            } else if (c == ']') {
                String ss = s.substring(j, i); 
                if (root == null) {
                    root = new Node(ss, null, d);
                    parent = root;
                    current = root;
                } else {
                    current = new Node(ss, parent, d);
                    parent.children.add(current);
                    current.parent = parent;
                }
            } else if (c == 'p') {
                // ignore, for now
            } else {
            }
        }
        return root;
    }
    
    public void paint(Graphics g) {
        if (root == null) return;
        g.setColor(Color.WHITE);
        g.fillRect(0, 0, width, height);
        new TreeLayout().layoutTree(root);
        g.setColor(Color.BLACK);
        paint(g, root);
        Rectangle2D r = g.getFontMetrics().getStringBounds(s, g);
        g.drawString(s, width / 2 - (int)(r.getWidth() / 2), 30);
    }

    public void paint(Graphics g, Node node) {
        for (Node child : node.children) {
            g.drawLine(node.x, node.y, child.x, child.y);
            paint(g, child);
        }
        Rectangle2D r = g.getFontMetrics().getStringBounds(node.label, g);
        int rw = (int)r.getWidth();
        int rh = (int)r.getHeight();
        int textX = node.x - (rw / 2);
        int textY = node.y + (rh / 2);
        int border = 3;
        int boundX = textX - border;
        int boundY = node.y - (rh / 2) - border;
        int boundW = rw + (2 * border);
        int boundH = rh + (2 * border);
//        System.out.println(rw + " " + rh + " " + textX + " " + textY);
        g.setColor(Color.WHITE);
        g.fillRect(boundX, boundY, boundW, boundH);
        g.setColor(Color.BLACK);
        g.drawRect(boundX, boundY, boundW, boundH);
        g.drawString(node.label, textX, textY);
    }
    
    public static void main(String[] args) {
        JFrame f = new JFrame();
        String a = "[C]([C]([C,2][C]([C,1]([C,3])[C,4]([C,5])))[C]([C]([C,4][C,3])[C]([C,1][C,5]))[C,2]([C]([C,5][C,3])))"; 
        String b = "[C]([C]([C]([C,1]([C,2])[C,3])[C]([C,4][C,5]))[C]([C,4]([C,2])[C,3]([C,5]))[C]([C]([C,1][C,2])[C,5]))";
        
        int width = 1200;
        int height = 350;
        f.setLayout(new GridLayout(2, 1));
        f.add(new SignatureTreePanel(a, width, height));
        f.add(new SignatureTreePanel(b, width, height));
        f.setPreferredSize(new Dimension(width,2*height));
        f.pack();
        f.setVisible(true);
    }

}
