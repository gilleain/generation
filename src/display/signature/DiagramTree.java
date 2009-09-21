package display.signature;

import java.awt.Graphics;
import java.util.ArrayList;

import model.Graph;
import model.GraphTree;

public class DiagramTree {
    
    private GraphTree graphTree;
    
    private int width;
    
    private int height;
    
    private int graphWidth;
    
    private int graphHeight;
    
    private int graphVerticalSeparation = 15;
    
    private int graphHorizontalSeparation = 15;
    
    public DiagramTree(int width, int height, int graphWidth, int graphHeight) {
        this.width = width;
        this.height = height;
        
        this.graphWidth = graphWidth;
        this.graphHeight = graphHeight;
        
        this.graphTree = new GraphTree();
    }
    
    public void setTree(GraphTree tree) {
        this.graphTree = tree;
    }
    
    public void addInitialGraph(Graph graph) {
        graphTree.addGraphToParent(graph, null);
    }
    
    public void addGraphAsChildOf(Graph child, Graph parent) {
        graphTree.addGraphToParent(child, parent);
    }
    
    public int paint(Graphics g) {
//        topDown(g, graphTree.getRoot(), width / 2, graphHeight / 2, width);
        ArrayList<GraphTree.TreeNode> leaves = graphTree.getLeafNodes();
        int totalWidth = leaves.size() * graphWidth;
        width = totalWidth;
        bottomUp(g, leaves, height - graphHeight);
        return totalWidth;
    }
    
    public void bottomUp(Graphics g, ArrayList<GraphTree.TreeNode> nodes, int axis) {
        int totalWidth = nodes.size() * (graphWidth + graphHorizontalSeparation);
        int center = (width / 2) - (totalWidth / 2) 
        + (graphWidth / 2) + (4 * graphHorizontalSeparation);
        ArrayList<GraphTree.TreeNode> parents = new ArrayList<GraphTree.TreeNode>();
        for (GraphTree.TreeNode node : nodes) {
//            GraphRenderer.paintDiagram(node.graph, g, center, graphWidth, axis);
            center += graphWidth + graphHorizontalSeparation;
            if (node.parent != null && !parents.contains(node.parent)) {
                parents.add(node.parent);
            }
        }
        if (parents.size() > 0) {
            bottomUp(g, parents, axis - graphHeight - graphVerticalSeparation);
        }
    }
    
    public void topDown(Graphics g, GraphTree.TreeNode node, int center, int axis, int availableWidth) {
//        GraphRenderer.paintDiagram(node.graph, g, center, graphWidth, axis);
        
        int numberOfChildren = node.children.size(); 
        if (numberOfChildren > 0) {
            int separation = availableWidth / numberOfChildren;
            
            int childCenter;
            if (numberOfChildren == 1) {
              childCenter = center;
            } else {
                int leftEnd = center - (separation * (numberOfChildren / 2));
                childCenter = leftEnd + (graphWidth / 2);
            }
            for (GraphTree.TreeNode treeNode : node.children) {
                topDown(g, treeNode, childCenter, axis + this.graphHeight, availableWidth / 2);
                childCenter += separation;
            }
        }
    }

}
