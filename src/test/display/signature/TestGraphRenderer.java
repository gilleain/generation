package test.display.signature;

import java.awt.Graphics;

import javax.swing.JFrame;
import javax.swing.JPanel;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import deterministic.SimpleGraph;
import display.signature.GraphRenderer;

public class TestGraphRenderer {
    
    @Test
    public void testSimpleGraph() {
        JFrame frame = new JFrame();
        IChemObjectBuilder b = NoNotificationChemObjectBuilder.getInstance();
        IAtomContainer container = b.newAtomContainer();
        container.addAtom(b.newAtom("H"));
        container.addAtom(b.newAtom("H"));
        container.addAtom(b.newAtom("H"));
        container.addAtom(b.newAtom("H"));
        container.addAtom(b.newAtom("C"));
        container.addBond(0, 4, IBond.Order.SINGLE);
        container.addBond(1, 4, IBond.Order.SINGLE);
        container.addBond(2, 4, IBond.Order.SINGLE);
        container.addBond(3, 4, IBond.Order.SINGLE);
        final SimpleGraph graph = new SimpleGraph(container);
        frame.add(new JPanel() {
            public void paint(Graphics g) {
                GraphRenderer.paintDiagram(graph, g, 150, 300, 100);
            }
        });
        frame.pack();
        frame.setSize(300, 200);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        TestGraphRenderer r = new TestGraphRenderer();
        r.testSimpleGraph();
    }

}
