package test.signature;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.Graph;
import signature.TargetMolecularSignature;

public class TestGraph {
    
    private static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
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
    
    public void getDiameter() {
        Graph g = makeConnectedGraph();
        int d = g.getDiameter();
        System.out.println("diameter = " + d);
    }
    
    public void connectivity() {
        Graph connected = makeConnectedGraph();
        Graph disconnected = makeDisconnectedAtomGraph();
        System.out.println("connected " + connected.isConnected());
        System.out.println("disconnected " + disconnected.isConnected());
    }
    
    public void compatibleBondsInDisconnected() {
        Graph disconnected = makeDisconnectedAtomGraph();
        TargetMolecularSignature hTau = 
            TestTargetMolecularSignature.makeSimpleMolecularSignature();
        disconnected.assignAtomsToTarget(hTau);
        int l = disconnected.getAtomContainer().getAtomCount();
        List<Integer> targets = disconnected.getAtomTargetMap(); 
        int h = hTau.getHeight();
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
