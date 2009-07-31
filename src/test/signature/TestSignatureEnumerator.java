package test.signature;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;

import signature.Graph;
import signature.Orbit;
import signature.SignatureEnumerator;
import signature.TargetMolecularSignature;

public class TestSignatureEnumerator {
    
    private IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    public void create() {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makePaperExampleMolecularSignature();
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        SignatureEnumerator enumerator = new SignatureEnumerator(e, c, sig);
        IAtomContainer initialContainer = enumerator.getInitialContainer();
        System.out.println(initialContainer);
    }
    
    public void hexaneExample() {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makeHexane();
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        e.add("C");
        c.add(6);
        SignatureEnumerator enumerator = new SignatureEnumerator(e, c, sig);
        SmilesGenerator smilesGenerator = new SmilesGenerator();
        for (IAtomContainer solution : enumerator.generateSolutions()) {
            String smiles = smilesGenerator.createSMILES((IMolecule)solution);
            System.out.println("solution " + smiles);
        }
    }
    
    public void adenineExample() {
        TargetMolecularSignature sig =
            TestTargetMolecularSignature.makeAdenineExample();
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        e.add("C");
        e.add("N");
        c.add(5);
        c.add(5);
        SignatureEnumerator enumerator = new SignatureEnumerator(e, c, sig);
        SmilesGenerator smilesGenerator = new SmilesGenerator();
        for (IAtomContainer solution : enumerator.generateSolutions()) {
            String smiles = smilesGenerator.createSMILES((IMolecule)solution);
            System.out.println("solution " + smiles);
        }
    }
    
    public void paperExample() {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makePaperExampleMolecularSignature();
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        e.add("H"); e.add("C");
        c.add(22);  c.add(10);
        SignatureEnumerator enumerator = new SignatureEnumerator(e, c, sig);
        Graph gA = new Graph(enumerator.getInitialContainer());
        gA.assignAtomsToTarget(sig);
        gA.determineUnsaturated();
        gA.partition();
        Orbit o = gA.getUnsaturatedOrbit();
        List<Graph> resultsOfStep1 = new ArrayList<Graph>();
        enumerator.saturateOrbitSignature(o, gA, resultsOfStep1);
        boolean match1 = matchStep1(resultsOfStep1);
        if (!match1) {
            System.out.println("failed step 1"); return;
        }
    }
    
    private IAtomContainer makePaperExampleContainer() {
        IAtomContainer ac = builder.newAtomContainer();
        for (int i = 0; i < 22; i++) {
            ac.addAtom(builder.newAtom("H"));
        }
        for (int i = 0; i < 10; i++) {
            ac.addAtom(builder.newAtom("C"));
        }
        return ac;
    }
    
    private boolean graphsMatch(Graph gA, Graph gB) {
        IAtomContainer a = gA.getAtomContainer();
        IAtomContainer b = gB.getAtomContainer();
        return a.getBondCount() == b.getBondCount();
    }
    
    private void makeStep1Bonds(IAtomContainer ac) {
        ac.addBond(0, 22, IBond.Order.SINGLE);
        ac.addBond(1, 22, IBond.Order.SINGLE);
        ac.addBond(2, 22, IBond.Order.SINGLE);
        
        ac.addBond(3, 23, IBond.Order.SINGLE);
        ac.addBond(4, 23, IBond.Order.SINGLE);
        ac.addBond(5, 23, IBond.Order.SINGLE);
        
        ac.addBond(6, 24, IBond.Order.SINGLE);
        ac.addBond(7, 24, IBond.Order.SINGLE);
        ac.addBond(8, 24, IBond.Order.SINGLE);
    }
    
    private boolean matchStep1(List<Graph> resultsOfStep1) {
        IAtomContainer ac = makePaperExampleContainer();
        makeStep1Bonds(ac);
        Graph expectedGraph = new Graph(ac);
        if (resultsOfStep1.size() != 1) {
            return false;
        }
        Graph observedGraph = resultsOfStep1.get(0);
        return graphsMatch(expectedGraph, observedGraph);
    }


}
