package display.signature;

import java.awt.Color;
import java.awt.Graphics;
import java.util.HashMap;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import deterministic.SimpleGraph;

public class GraphRenderer {

    private static int nodeRadius = 10;

    public static void paintDiagram(SimpleGraph graph, Graphics g, int center, int width, int axis) {
        IAtomContainer container = graph.getAtomContainer();
        int separation = width / container.getAtomCount();
        int height = width / 2;

        HashMap<Integer, Integer> nodePositions = new HashMap<Integer, Integer>();
        int leftEdge = center - (width / 2); 
        int i = leftEdge + (separation / 2);
        g.drawRect(leftEdge, axis - (height / 2), width, height);

        int d = nodeRadius * 2;
        int n = 0;
        for (IAtom atom : container.atoms()) {
            nodePositions.put(container.getAtomNumber(atom), i);
            int x = i - nodeRadius;
            int y = axis - nodeRadius;
            g.drawOval(x, y, d, d);
            g.drawString(atom.getSymbol() + "" + n, x, axis + nodeRadius);
            i += separation;
            n++;
        }

        for (IBond bond : container.bonds()) {
            int a = container.getAtomNumber(bond.getAtom(0));
            int b = container.getAtomNumber(bond.getAtom(1));
            int leftEnd, rightEnd;
            if (a < b) {
                leftEnd = nodePositions.get(a);
                rightEnd = nodePositions.get(b);
            } else {
                leftEnd = nodePositions.get(b);
                rightEnd = nodePositions.get(a);
            }
            int arcWidth = rightEnd - leftEnd;
            int arcHeight = arcWidth / 2;
            int y = axis - (arcHeight / 2);
            g.drawArc(leftEnd, y, arcWidth, arcHeight, 180, -180);
            g.setColor(Color.BLACK);
        }
    }
}
