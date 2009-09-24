package test.display.signature;

import javax.swing.JFrame;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import deterministic.BondCreationEvent;
import deterministic.DeterministicEnumerator;
import deterministic.SimpleGraph;
import display.signature.OutlineTreePanel;

public class TestOutlinePanel {
    
    public static void makePanel() {
        JFrame frame = new JFrame();
        IChemObjectBuilder bb = NoNotificationChemObjectBuilder.getInstance();
        IAtomContainer start = bb.newAtomContainer(); 
        SimpleGraph base = new SimpleGraph(start);   
        OutlineTreePanel panel = new OutlineTreePanel(base);
        BondCreationEvent bce = new BondCreationEvent();
        bce.parent = base;
        bce.child = new SimpleGraph(bb.newAtomContainer());
        panel.bondAdded(bce);
        bce = new BondCreationEvent();
        bce.parent = base;
        bce.child = new SimpleGraph(bb.newAtomContainer());
        panel.bondAdded(bce);
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setVisible(true);
    }
    
    public static void testHookup() {
        JFrame frame = new JFrame();
        DeterministicEnumerator enumerator = new DeterministicEnumerator("C2H8");
        OutlineTreePanel panel = new OutlineTreePanel();
        enumerator.setBondCreationListener(panel);
        frame.add(panel);
        frame.setSize(400, 400);
        frame.setVisible(true);
        enumerator.generate();
    }

    public static void main(String[] args) {
//        TestOutlinePanel.makePanel();
        TestOutlinePanel.testHookup();
    }
}
