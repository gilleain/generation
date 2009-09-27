package display.signature;

import java.awt.Color;
import java.awt.Dimension;
import java.awt.Graphics;

import javax.swing.JPanel;

import deterministic.SimpleGraph;

public class GraphPanel extends JPanel implements GraphSelectionListener {
    
    private SimpleGraph graph;
    
    public GraphPanel() {
        this.graph = null;
        this.setBackground(Color.GRAY);
        this.setPreferredSize(new Dimension(400, 400));
    }
    
    public void graphSelected(GraphSelectionEvent gse) {
        this.setGraph(gse.selected);
    }
    
    public void setGraph(SimpleGraph graph) {
        this.graph = graph;
        this.repaint();
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        if (this.graph == null) return;
        int width = this.getWidth(); 
        int center = width / 2;
        int axis = this.getHeight() / 2;
//        System.out.println("painting" + width + " " + center + " " + axis);
        GraphRenderer.paintDiagram(graph, g, center, width, axis);
        g.drawString(graph.toString(), 10, axis + 20);
    }

}
