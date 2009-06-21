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
                boolean compatible = 
                    disconnected.compatibleBondSignature(i, j, hTau);
                int targetX = targets.get(i);
                int targetY = targets.get(j);
                String hMinusOneTauY = 
                    hTau.getTargetAtomicSubSignature(targetY, h - 1);
                int n12 = disconnected.countCompatibleTargetBonds(
                        targetX, h, hMinusOneTauY, hTau);
                int m12 = disconnected.countExistingBondsOfType(
                        i, h, hMinusOneTauY);
                System.out.println("n12 " + n12 + " m12 " + m12);
                System.out.println(i + " " + j + " " + compatible);
                System.out.println(j + " " + i + " " + compatible);
            }
        }
        int x = 0;
        for (int i : disconnected.getAtomTargetMap()) {
            System.out.println(x + " -> " + i);
            x++;
        }
    }

}
