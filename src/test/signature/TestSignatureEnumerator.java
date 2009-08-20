package test.signature;

import java.util.ArrayList;
import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import signature.Graph;
import signature.Orbit;
import signature.SignatureEnumerator;
import signature.TargetMolecularSignature;

public class TestSignatureEnumerator {
    
    public static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    public static SmilesGenerator smilesGenerator = new SmilesGenerator();
    
    public static IMolecularFormula makeFormula(String formulaString) {
        return MolecularFormulaManipulator.getMolecularFormula(
                    formulaString, 
                    NoNotificationChemObjectBuilder.getInstance());
    }
    
    public static String toSmiles(IAtomContainer container) {
        if (ConnectivityChecker.isConnected(container)) {
            return TestSignatureEnumerator.smilesGenerator.createSMILES(
                    TestSignatureEnumerator.builder.newMolecule(container));
        } else {
            return "disconnected";
        }
    }
    
    @Test
    public void methaneExampleWithExplicitTargets() {
        TargetMolecularSignature sig = new TargetMolecularSignature(1);
        sig.add("[H]([C])", 4);
        sig.add("[C]([H][H][H][H])", 1);
        SignatureEnumerator enumerator = new SignatureEnumerator("CH4", sig);
        List<IAtomContainer> solutions = enumerator.generateSolutions();
        Assert.assertEquals(1, solutions.size());
        System.out.println(TestSignatureEnumerator.toSmiles(solutions.get(0)));
    }
    
    @Test
    public void methaneExample() {
        SignatureEnumerator enumerator = new SignatureEnumerator("CH4");
        List<IAtomContainer> solutions = enumerator.generateSolutions();
        Assert.assertEquals(1, solutions.size());
    }
    
    @Test
    public void ethaneExample() {
        SignatureEnumerator enumerator = new SignatureEnumerator("C2H6");
        List<IAtomContainer> solutions = enumerator.generateSolutions();
        Assert.assertEquals(1, solutions.size());
    }
    
    @Test
    public void ethaneExampleWithExplicitTargets() {
        TargetMolecularSignature sig = new TargetMolecularSignature(1);
        sig.add("[H]([C])", 6);
        sig.add("[C]([C][H][H][H])", 2);
        SignatureEnumerator enumerator = new SignatureEnumerator("C2H6", sig);
        List<IAtomContainer> solutions = enumerator.generateSolutions();
//        Assert.assertEquals(1, solutions.size());
        System.out.println(TestSignatureEnumerator.toSmiles(solutions.get(0)));
        for (IAtomContainer c : solutions) { 
            System.out.println(TestSignatureEnumerator.toSmiles(c));
        }
    }
    
    @Test
    public void c4H8CyclobutaneExample() {
        TargetMolecularSignature sig = new TargetMolecularSignature(1);
        sig.add("[H]([C])", 8);
        sig.add("[C]([C][C][H][H])", 4);
        SignatureEnumerator enumerator = new SignatureEnumerator("C4H8", sig);
        List<IAtomContainer> solutions = enumerator.generateSolutions();
//        Assert.assertEquals(1, solutions.size());
        for (IAtomContainer c : solutions) { 
            System.out.println(TestSignatureEnumerator.toSmiles(c));
        }
    }
    
    @Test
    public void c4H8Example() {
        SignatureEnumerator enumerator = new SignatureEnumerator("C4H8");
        List<IAtomContainer> solutions = enumerator.generateSolutions();
        System.out.println(solutions.size());
    }
    
    @Test
    public void hexaneExample() {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makeHexane();
        IMolecularFormula formula = TestSignatureEnumerator.makeFormula("C6");
        SignatureEnumerator enumerator = new SignatureEnumerator(formula, sig);
        
        for (IAtomContainer solution : enumerator.generateSolutions()) {
            String smiles = smilesGenerator.createSMILES((IMolecule)solution);
            System.out.println("solution " + smiles);
        }
    }
    
    @Test
    public void adenineExample() {
        TargetMolecularSignature sig =
            TestTargetMolecularSignature.makeAdenineExample();
        IMolecularFormula formula = TestSignatureEnumerator.makeFormula("C5N5");
        SignatureEnumerator enumerator = new SignatureEnumerator(formula, sig);
        SmilesGenerator smilesGenerator = new SmilesGenerator();
        for (IAtomContainer solution : enumerator.generateSolutions()) {
            String smiles = smilesGenerator.createSMILES((IMolecule)solution);
            System.out.println("solution " + smiles);
        }
    }
    
    @Test
    public void paperExample() {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makePaperExampleMolecularSignature();
        
        IMolecularFormula formula = 
            TestSignatureEnumerator.makeFormula("C10H22");
        SignatureEnumerator enumerator = new SignatureEnumerator(formula, sig);
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
