package test.signature;

import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.templates.MoleculeFactory;

import signature.TargetAtomicSignature;

public class TestTargetAtomicSignature {
    
    public static IChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
    public static void addHydrogens(IMolecule mol, IAtom atom, int n) {
        for (int i = 0; i < n; i++) {
            IAtom h = builder.newAtom("H");
            mol.addAtom(h);
            mol.addBond(builder.newBond(atom, h));
        }
    }
    
    public static IMolecule makeTreeMolecule() {
        IMolecule propane = MoleculeFactory.makeAlkane(3);
        TestTargetAtomicSignature.addHydrogens(propane, propane.getAtom(0), 3);
        TestTargetAtomicSignature.addHydrogens(propane, propane.getAtom(1), 2);
        TestTargetAtomicSignature.addHydrogens(propane, propane.getAtom(2), 3);
        return propane;
    }
    
    public static IMolecule makeRingMolecule() {
        IMolecule cyclohexane = MoleculeFactory.makeCyclohexane();
        for (int i = 0; i < 6; i++) {
            TestTargetAtomicSignature.addHydrogens(
                    cyclohexane, cyclohexane.getAtom(i), 2);
        }
        return cyclohexane;
    }
    
    public static void print(IMolecule mol) {
        for (int i = 0; i < mol.getAtomCount(); i++) {
            IAtom a = mol.getAtom(i);
            System.out.println(a.getSymbol() + " " + i);
        }
        for (IBond bond : mol.bonds()) {
            IAtom aa = bond.getAtom(0);
            IAtom ab = bond.getAtom(1);
            System.out.println(
                    mol.getAtomNumber(aa) + "-" + mol.getAtomNumber(ab));
        }
    }
    
    public static void checkMolecule(String sigString, IMolecule expected) {
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        IMolecule actual = sig.toMolecule();
        try {
            boolean isIsomorph = 
                UniversalIsomorphismTester.isIsomorph(expected, actual);
            Assert.assertEquals(true, isIsomorph);
        } catch (CDKException c) {
            
        }
    }
    
    @Test
    public void testCageMolecule() {
        IMolecule molecule = TestSignature.makeCage();
        String sigString = "[C]([C]([C,2]([C]([C,3][C,4]))[C]([C,5]" +
                           "[C,3]([C,6]([C,1]))))[C]([C]([C,7][C]" +
                           "([C,1[C,8]))[C,5]([C,8]([C,6])))[C]([C,2]" +
                           "[C,7]([C,4]([C,1]))))";
        TestTargetAtomicSignature.checkMolecule(sigString, molecule);
    }
    
    @Test
    public void testTreeMolecule() {
         IMolecule molecule = TestTargetAtomicSignature.makeTreeMolecule();
         String sigString = "[C]([H][H][H][C]([H][H][C]([H][H][H])))";
         TestTargetAtomicSignature.checkMolecule(sigString, molecule);
    }
    
    @Test
    public void testCyclicMolecule() {
         IMolecule molecule = TestTargetAtomicSignature.makeRingMolecule();
         String sigString = "[H]([C]([C]([C]([C,1]([H][H])[H][H])[H][H])" +
                            "[C]([C]([C,1][H][H])[H][H])[H]))";
         TestTargetAtomicSignature.checkMolecule(sigString, molecule);
    }
    
    @Test
    public void labelledRoundtrip() {
        String expected = "[C]([C,1][C,2][C,3])";
        TargetAtomicSignature sig = new TargetAtomicSignature(expected);
        String actual = sig.toString();
        Assert.assertEquals("labelled roundtrip failed", expected, actual);
    }
    
    @Test
    public void roundtrip() {
        String expected = "[C]([H][C]([H][H][H])[C]([H][H][C])[C]([H][C][C]))";
        TargetAtomicSignature sig = new TargetAtomicSignature(expected);
        String actual = sig.toString();
        Assert.assertEquals("roundtrip failed", expected, actual);
    }
    
    @Test
    public void signatureStrings() {
        String sigString = "[C]([H][H][H][C]([H][C][C]))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        for (int height = 0; height < 2; height++) {
            for (String s : sig.getSignatureStrings(height)) {
                // TODO
                System.out.println(height + " " + s);
            }
        }
    }
    
    @Test
    public void subSignatureFromRootChild() {
        String sigString = "[A]([B]([C]([D]))[E]([F]([G])[H]))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        int child = 0;
        int height = 2;
        String expected = "[B]([C]([D])[A]([E]))";
        String actual = sig.getSignatureString(child, height);

        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void subSignature() {
        String sigString = "[A]([B]([C]([D]))[E]([F]([G])[H]))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        int height = 2;
        String expected = "[A]([B]([C])[E]([F][H]))";
        String actual = sig.getSubSignature(height);
        
        Assert.assertEquals(expected, actual);
    }

}
