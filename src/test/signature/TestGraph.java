package test.signature;

import java.io.StringWriter;

import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.Graph;
import signature.TargetMolecularSignature;

public class TestGraph {
    
    private static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    public static void addHydrogens(IAtomContainer ac, IAtom atom, int n) {
        for (int i = 0; i < n; i++) {
            IAtom h = builder.newAtom("H");
            ac.addAtom(h);
            ac.addBond(builder.newBond(atom, h));
        }
    }
    
    public static Graph makeCanonicalExample() {
        // this is an example from figure 3 of the enumeration paper
        IAtomContainer ac = builder.newAtomContainer();
        
        IAtom c1 = builder.newAtom("C");
        ac.addAtom(c1);
        TestGraph.addHydrogens(ac, c1, 3);
        
        IAtom c2 = builder.newAtom("C");
        ac.addAtom(c2);
        ac.addBond(builder.newBond(c1, c2));
        TestGraph.addHydrogens(ac, c2, 2);
        
        IAtom c3 = builder.newAtom("C");
        ac.addAtom(c3);
        TestGraph.addHydrogens(ac, c3, 2);
        
        return new Graph(ac);
    }
        
    
    public static Graph makeNonCanonicalExample() {
        // this is an example from figure 3 of the enumeration paper
        IAtomContainer ac = builder.newAtomContainer();
        
        IAtom c1 = builder.newAtom("C");
        ac.addAtom(c1);
        TestGraph.addHydrogens(ac, c1, 3);
        
        IAtom c2 = builder.newAtom("C");
        ac.addAtom(c2);
        TestGraph.addHydrogens(ac, c2, 2);
        ac.addBond(builder.newBond(c1, c2));
        
        IAtom c3 = builder.newAtom("C");
        ac.addAtom(c3);
        TestGraph.addHydrogens(ac, c3, 2);
        ac.addBond(builder.newBond(c2, c3));
        
        IAtom c4 = builder.newAtom("C");
        ac.addAtom(c4);
        TestGraph.addHydrogens(ac, c4, 2);
        
        IAtom c5 = builder.newAtom("C");
        ac.addAtom(c5);
        TestGraph.addHydrogens(ac, c5, 3);
        
        IAtom c6 = builder.newAtom("C");
        ac.addAtom(c6);
        TestGraph.addHydrogens(ac, c6, 2);
        ac.addBond(builder.newBond(c5, c6));
        
        IAtom c7 = builder.newAtom("C");
        ac.addAtom(c7);
        TestGraph.addHydrogens(ac, c7, 2);
        ac.addBond(builder.newBond(c6, c7));
        
        IAtom c8 = builder.newAtom("C");
        ac.addAtom(c8);
        TestGraph.addHydrogens(ac, c8, 2);
        
        IAtom c9 = builder.newAtom("C");
        ac.addAtom(c9);
        TestGraph.addHydrogens(ac, c9, 1);
        
        IAtom c10 = builder.newAtom("C");
        ac.addAtom(c10);
        TestGraph.addHydrogens(ac, c10, 3);
        ac.addBond(builder.newBond(c9, c10));
        
        return new Graph(ac);
    }
    
    public static Graph makeDisconnectedAtomGraph() {
        IAtomContainer ac = builder.newAtomContainer();
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        return new Graph(ac);
    }
    
    public static Graph makeConnectedGraph() {
        IAtomContainer ac = builder.newAtomContainer();
        ac.addAtom(builder.newAtom("A"));
        ac.addAtom(builder.newAtom("B"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("D"));
        ac.addAtom(builder.newAtom("E"));
        
        ac.addBond(0, 1, IBond.Order.SINGLE);
        ac.addBond(1, 2, IBond.Order.SINGLE);
        ac.addBond(1, 3, IBond.Order.SINGLE);
        ac.addBond(3, 4, IBond.Order.SINGLE);
        return new Graph(ac);
    }
    
    public void bondedAtomSignatures() {
        Graph g = makeConnectedGraph();
        for (int i = 0; i < 5; i++) {
            for (String s : g.getSignaturesOfBondedAtoms(i, 3)) {
                System.out.println(i + " " + s);
            }
        }
    }
    
    public static void printAtomContainer(IAtomContainer ac) {
        StringWriter stringWriter = new StringWriter();
        MDLWriter writer = new MDLWriter(stringWriter);
        IMolecule mol = builder.newMolecule(ac);
        try {
            writer.writeMolecule(mol);
            System.out.println(stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Test
    public void testNonCanonical() {
        Graph g = TestGraph.makeNonCanonicalExample();
//        TestGraph.printAtomContainer(g.getAtomContainer());
        Assert.assertFalse(g.isCanonical());
    }
    
    @Test
    public void getDiameter() {
        Graph g = makeConnectedGraph();
        int actual = g.getDiameter();
        int expected = 3;
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void connectivity() {
        Graph connected = makeConnectedGraph();
        Graph disconnected = makeDisconnectedAtomGraph();
        Assert.assertTrue(connected.isConnected());
        Assert.assertFalse(disconnected.isConnected());
    }
    
    @Test
    public void compatibleBondsInDisconnected() {
        Graph disconnected = makeDisconnectedAtomGraph();
        TargetMolecularSignature hTau = 
            TestTargetMolecularSignature.makeHexane();
        disconnected.assignAtomsToTarget(hTau);
        int l = disconnected.getAtomContainer().getAtomCount();
//        List<Integer> targets = disconnected.getAtomTargetMap(); 
//        int h = hTau.getHeight();
        for (int i = 0; i < l - 1; i++) {
            for (int j = i + 1; j < l; j++) {
                boolean compatibleXY = disconnected.compatibleBond(i, j, hTau);
                boolean compatibleYX = disconnected.compatibleBond(j, i, hTau);
//                int targetX = targets.get(i);
//                int targetY = targets.get(j);
//                String hMinusOneTauX = 
//                    hTau.getTargetAtomicSubSignature(targetX, h - 1);
//                String hMinusOneTauY = 
//                    hTau.getTargetAtomicSubSignature(targetY, h - 1);
//                int n12 = hTau.compatibleTargetBonds(targetX, h, hMinusOneTauY);
//                int n21 = hTau.compatibleTargetBonds(targetY, h, hMinusOneTauX);
//                int m12 = disconnected.countExistingBondsOfType(
//                        i, h, hMinusOneTauY);
//                int m21 = disconnected.countExistingBondsOfType(
//                        i, h, hMinusOneTauX);
                boolean compatible = compatibleXY && compatibleYX;
                System.out.print(i + "\t" + j + "\t" + compatibleXY + "\t");
//                System.out.print("n12 " + n12 + " m12 " + m12 + "\t");
//                System.out.print("n21 " + n21 + " m21 " + m21 + "\t");
                System.out.print(j + "\t" + i + "\t" + compatibleYX);
                System.out.println("\t" + compatible);
            }
        }
        int x = 0;
        for (int i : disconnected.getAtomTargetMap()) {
            System.out.println(x + " -> " + i);
            x++;
        }
    }

}
