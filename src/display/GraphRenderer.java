package display;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import model.Arc;
import model.Graph;
import model.Group;
import model.Node;

public class GraphRenderer {
    
  private static int nodeRadius = 3;
    
    public static void paintDiagram(Graph graph, Graphics g, int center, int width, int axis) {
        int separation = width / graph.nodeCount();
        int height = width / 2;
        
        HashMap<Node, Integer> nodePositions = new HashMap<Node, Integer>();
        int leftEdge = center - (width / 2); 
        int i = leftEdge + (separation / 2);
        g.drawRect(leftEdge, axis - (height / 2), width, height);
        
        int d = nodeRadius * 2;
        for (Node node : graph.nodes()) {
            nodePositions.put(node, i);
            g.fillOval(i - nodeRadius, axis - nodeRadius, d, d);
            i += separation;
        }
        
        for (Arc arc : graph.arcs()) {
            int leftEnd = nodePositions.get(arc.getLeft());
            int rightEnd = nodePositions.get(arc.getRight());
            int arcWidth = rightEnd - leftEnd;
            int arcHeight = arcWidth / 2;
            int y = axis - (arcHeight / 2);
            if (arc.isNew) {
                g.setColor(Color.RED);
            }
            g.drawArc(leftEnd, y, arcWidth, arcHeight, 180, -180);
            g.setColor(Color.BLACK);
        }
        
        for (Group group : graph.getGroups()) {
            for (int index : group.memberIndices) {
                Node node = graph.getNode(index);
                int pos = nodePositions.get(node);
                g.drawString(String.valueOf(group.degree), pos - 5, axis + 15);
            }
        }
        
    }
}
