package display.signature;

import java.awt.Graphics;
import java.util.ArrayList;

import deterministic.SimpleGraph;

/**
 * Stores the outline detail of the graph creation process.  
 * 
 * @author maclean
 *
 */
public class OutlineTree {
    
    public int xOffset = 0;
    
    public int yOffset = 20;
    
    public int width;
    
    public int height;
    
    private int maxDepth;
    
    public class TreeLayout {
        
        public int totalLeafCount = 0;
        
        public int xSep;
        
        public int ySep;
        
        public void layoutTree(GraphicalNode root) {
            int leafCount = root.countLeaves();
            this.xSep = width / (leafCount + 1);
            this.ySep = height / (maxDepth + 1);
            layout(root);
        }
        
        public int layout(GraphicalNode node) {
            node.y  = node.depth * ySep;
            if (node.isLeaf()) {
                totalLeafCount += 1;
                node.x = totalLeafCount * xSep;
                return node.x;
            } else {
                int min = 0;
                int max = 0;
                for (GraphicalNode child : node.children) {
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
    
    private class GraphicalNode {
        
        public SimpleGraph graph;
        
        public int x;
        
        public int y;
        
        public int depth;
        
        public ArrayList<GraphicalNode> children;
        
        public GraphicalNode(SimpleGraph graph) {
            this.graph = graph; 
            this.children = new ArrayList<GraphicalNode>();
        }
        
        public void addChild(SimpleGraph child) {
            GraphicalNode childNode = new GraphicalNode(child);
            childNode.depth = this.depth + 1;
            if (childNode.depth > maxDepth) maxDepth = childNode.depth;
            this.children.add(childNode);
        }
        
        public int countLeaves() {
            if (this.isLeaf()) {
                return 1;
            } else {
                int c = 0;
                for (GraphicalNode child : this.children) {
                    c += child.countLeaves();
                }
                return c;
            }
        }
        
        public boolean isLeaf() {
            return this.children.size() == 0;
        }
        
        public String toString() {
            return graph.toString() + "(" + x + ", " + y + ")";
        }
    }
    
    private GraphicalNode root;
    
    private ArrayList<GraphicalNode> nodeList;
    
    public int nodeWidth = 10;
    
    public int nodeHeight = 10;
    
    private boolean isDirty = false;
    
    public OutlineTree() {
        nodeList = new ArrayList<GraphicalNode>();
    }
    
    public OutlineTree(SimpleGraph rootGraph) {
        this.root = new GraphicalNode(rootGraph);
        this.root.depth = 0;
        nodeList = new ArrayList<GraphicalNode>();
        nodeList.add(this.root);
    }

    public void addNode(SimpleGraph parent, SimpleGraph child) {
        if (this.root == null) {
            this.root = new GraphicalNode(parent);
            nodeList.add(this.root);
            this.root.addChild(child);
            nodeList.add(root.children.get(root.children.size() - 1));
        } else {
            System.out.println("adding " + parent + " " + child);
            System.out.println("searching " + nodeList);
            GraphicalNode parentNode = searchForNode(parent);
            parentNode.addChild(child);
            nodeList.add(parentNode.children.get(parentNode.children.size() - 1));
        }
        this.isDirty = true;
    }
    
    private GraphicalNode searchForNode(SimpleGraph graph) {
//        return searchForNode(graph, root);
        for (GraphicalNode n : nodeList) {
            if (n.graph == graph) return n;
        }
        return null;
    }
    
//    private GraphicalNode searchForNode(SimpleGraph graph, GraphicalNode node) {
//        if (node.graph == graph) {
//            return node;
//        } else {
//            for (GraphicalNode child : node.children) {
//                GraphicalNode result = searchForNode(graph, child);
//                if (result == null) {
//                    continue;
//                } else {
//                    return result;
//                }
//            }
//        }
//        return null;
//    }
    
    public void layout() {
        new TreeLayout().layoutTree(this.root);
        this.isDirty = false;
    }
    
    public void draw(Graphics g) {
        if (this.root == null) return;
        if (this.isDirty) {
            layout();
        }
        draw(root, g);
    }
    
    public void draw(GraphicalNode current, Graphics g) {
        for (GraphicalNode child : current.children) {
            g.drawLine(current.x + xOffset, current.y + yOffset,
                    child.x + xOffset, child.y + yOffset);
            draw(child, g);
        }
        g.drawOval((current.x - nodeWidth/2) + xOffset, 
                   (current.y - nodeWidth/2) + yOffset,
                   nodeWidth, nodeHeight);
    }

    public SimpleGraph getGraphAt(int x, int y) {
        for (GraphicalNode node : this.nodeList) {
            if (within(node, x, y)) {
                return node.graph;
            }
        }
        return null;
    }
    
    private boolean within(GraphicalNode node, int x, int y) {
        int deltaX = nodeWidth;
        int deltaY = nodeHeight;
        return Math.abs(node.x - x) < deltaX || Math.abs(node.y - y) < deltaY;
    }
    
}
