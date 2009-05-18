package display;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;
import java.awt.Graphics2D;
import java.awt.RenderingHints;

import javax.swing.JPanel;

import model.Graph;
import model.GraphTree;

public class DiagramTreePanel extends JPanel {

    private DiagramTree tree;
    
    public DiagramTreePanel(int width, int height, int graphWidth, int graphHeight) {
        this.tree = new DiagramTree(width, height, graphWidth, graphHeight);
        this.setPreferredSize(new Dimension(width, height));
        this.setBackground(Color.WHITE);
    }
    
    public void addInitialGraph(Graph graph) {
        this.tree.addInitialGraph(graph);
    }
    
    public void addGraphAsChildTo(Graph child, Graph parent) {
        this.tree.addGraphAsChildOf(child, parent);
    }
    
    public void setTree(GraphTree tree) {
        this.tree.setTree(tree);
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        Graphics2D g2 = (Graphics2D) g;
        g2.setRenderingHint(
                RenderingHints.KEY_ANTIALIASING,
                RenderingHints.VALUE_ANTIALIAS_ON);
        int newWidth = tree.paint(g2);
        this.setPreferredSize(new Dimension(newWidth, this.getHeight()));
        this.revalidate();
    }
    
}
