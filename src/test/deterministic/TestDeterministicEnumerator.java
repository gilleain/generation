package test.deterministic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;

import deterministic.DeterministicEnumerator;

public class TestDeterministicEnumerator {
    
    public static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    public static SmilesGenerator smilesGenerator = new SmilesGenerator();
    
    public static String toSmiles(IAtomContainer container) {
        if (ConnectivityChecker.isConnected(container)) {
            return TestDeterministicEnumerator.smilesGenerator.createSMILES(
                    TestDeterministicEnumerator.builder.newMolecule(container));
        } else {
            return "disconnected";
        }
    }
    
    public static void printResults(List<IAtomContainer> results) {
        for (IAtomContainer result : results) {
            System.out.println(TestDeterministicEnumerator.toSmiles(result));
        }
    }
    
    public static void testFormula(String formulaString, int expected) {
        DeterministicEnumerator enumerator = 
            new DeterministicEnumerator(formulaString);
        List<IAtomContainer> results = enumerator.generate();
        int actual = results.size();
        TestDeterministicEnumerator.printResults(results);
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testPartialEthane() {
        IAtomContainer ac = builder.newAtomContainer();
        for (int i = 0; i < 3; i++) { ac.addAtom(builder.newAtom("C")); }
        for (int i = 0; i < 8; i++) { ac.addAtom(builder.newAtom("H")); }
        ac.addBond(0, 3, IBond.Order.SINGLE);
        ac.addBond(0, 4, IBond.Order.SINGLE);
        DeterministicEnumerator enumerator = 
            new DeterministicEnumerator(ac);
        enumerator.generate();
    }
    
    @Test
    public void testMethane() {
        TestDeterministicEnumerator.testFormula("CH4", 1);
    }
    
    @Test
    public void testEthyne() {
        TestDeterministicEnumerator.testFormula("C2H2", 1);
    }
    
    @Test
    public void testEthene() {
        TestDeterministicEnumerator.testFormula("C2H4", 1);
    }
    
    @Test
    public void testEthane() {
        TestDeterministicEnumerator.testFormula("C2H6", 1);
    }
    
    @Test
    public void testPropane() {
        TestDeterministicEnumerator.testFormula("C3H8", 1);
    }

}
