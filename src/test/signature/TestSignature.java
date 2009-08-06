package test.signature;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.templates.MoleculeFactory;

import signature.AtomContainerAtomPermutor;
import signature.OrbitElement;
import signature.Signature;

public class TestSignature {
    
    public static void printMolecule(IMolecule mol) {
        StringWriter stringWriter = new StringWriter();
        MDLWriter writer = new MDLWriter(stringWriter);
        try {
            writer.writeMolecule(mol);
            System.out.println(stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void testIsCanonical(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.next();
            Signature signature = new Signature(permutedContainer);
            System.out.println(signature.isCanonical());
        }
    }
    
    public static void testCanonical(IMolecule mol, String expected) {
        Signature sig = new Signature(mol);
        String actual = sig.toCanonicalSignatureString();
        Assert.assertEquals("not canonical", expected, actual);
    }
    
    public static void testAtoms(IMolecule mol, Map<Integer, String> expected) {
        Signature sig = new Signature(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String expectedSig = expected.get(i);
            String actualSig = sig.forAtom(i);
//            System.out.println(expectedSig);
            System.out.println(actualSig);
//            System.out.println("---------");
            Assert.assertEquals(i + " not correct", expectedSig, actualSig);
        }
    }
    
    public static void testAtom(IMolecule mol, int i, String expected) {
        Signature sig = new Signature(mol);
        String actual = sig.forAtom(i);
        Assert.assertEquals("not correct", expected, actual);
    }
    
    public static void testAllSame(IMolecule mol, String expected) {
        Signature sig = new Signature(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String actual = sig.forAtom(i);
            Assert.assertEquals("not equal for atom " + i, expected, actual);
        }
    }
    
    public static void printSignatures(IMolecule mol) {
        Signature sig = new Signature(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String sigForAtomI = sig.forAtom(i);
            System.out.println(String.format("%3d %s", i, sigForAtomI));
        }
    }
    
    @Test
    public void testCyclohexaneWithHydrogens() {
        IMolecule cyclohexane = MoleculeFactory.makeCyclohexane();
        for (int i = 0; i < 6; i++) {
            TestTargetAtomicSignature.addHydrogens(
                    cyclohexane, cyclohexane.getAtom(i), 2);
        }
        String expected = "[H]([C]([C]([C]([C,1]([H][H])[H][H])[H][H])" +
        		          "[C]([C]([C,1][H][H])[H][H])[H]))";
        Signature signature = new Signature(cyclohexane);
        
        String actual = signature.toCanonicalSignatureString();
        Assert.assertEquals(expected, actual);
    }
    
    @Test
    public void testCage() {
        String expectedA =  "[C]([C]([C,2]([C]([C,3][C,4]))[C]([C,5]" +
                            "[C,3]([C,6]([C,1]))))[C]([C]([C,7][C]" +
                            "([C,1][C,8]))[C,5]([C,8]([C,6])))[C]([C,2]" +
                            "[C,7]([C,4]([C,1]))))";
        String expectedB =  "[C]([C]([C]([C,2][C]([C,1][C,3]))[C]" +
                            "([C,1]([C,4])[C,5]))[C]([C,2]([C,6]" +
                            "([C,3]))[C]([C,7][C,6]))[C]([C,5]([C,4]" +
                            "([C,8]))[C,7]([C,8]([C,3]))))";
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(0, expectedA);
        expected.put(1, expectedA);
        expected.put(2, expectedA);
        expected.put(3, expectedA);
        expected.put(4, expectedB);
        expected.put(5, expectedB);
        expected.put(6, expectedB);
        expected.put(7, expectedB);
        expected.put(8, expectedB);
        expected.put(9, expectedB);
        expected.put(10, expectedB);
        expected.put(11, expectedB);
        expected.put(12, expectedA);
        expected.put(13, expectedA);
        expected.put(14, expectedA);
        expected.put(15, expectedA);
        
        IMolecule mol = AbstractSignatureTest.makeCage();
        TestSignature.testAtoms(mol, expected);
        TestSignature.testCanonical(mol, expectedB);
    }
    
    @Test
    public void testIsCanonicalForSquare() {
        IAtomContainer square = AbstractSignatureTest.builder.newAtomContainer();
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addBond(0, 1, IBond.Order.SINGLE);
        square.addBond(1, 2, IBond.Order.SINGLE);
        square.addBond(2, 3, IBond.Order.SINGLE);
        square.addBond(3, 0, IBond.Order.SINGLE);
        
        TestSignature.testIsCanonical(square);
    }
    
    @Test
    public void testCubane() {
        String expected = "[C]([C]([C,2]([C,3])[C,4]([C,3]))" +
        		          "[C]([C,1]([C,3])[C,4])[C]([C,1][C,2]))";
        IMolecule mol = AbstractSignatureTest.makeCubane();
//        TestSignaturePort.testAllSame(mol, expected);
        TestSignature.testCanonical(mol, expected);
    }
    
    @Test
    public void testCuneane() {
        String expectedA = "[C]([C]([C,2]([C,3])[C,3]([C,1]))" +
                           "[C]([C,2][C,4]([C,1]))[C]([C,1][C,4]))";
        String expectedB = "[C]([C]([C,1][C]([C,2][C,3]))[C]([C,4]" +
        		           "([C,3])[C,2]([C,3]))[C,1]([C,4]))";
        String expectedC = "[C]([C]([C]([C,1][C,2])[C,2]([C,3]))" +
                           "[C]([C,4][C,1]([C,3]))[C,4]([C,3]))";
        IMolecule mol = AbstractSignatureTest.makeCuneane();
//        TestSignaturePort.printSignatures(mol);
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(0, expectedA);
        expected.put(1, expectedB);
        expected.put(2, expectedB);
        expected.put(3, expectedA);
        expected.put(4, expectedB);
        expected.put(5, expectedB);
        expected.put(6, expectedC);
        expected.put(7, expectedC);
        TestSignature.testAtoms(mol, expected);
    }
     
    @Test
    public void testPropellane() {
        String expectedA = "[C]([C]([C,1])[C]([C,1])[C]([C,1])[C,1])";
        String expectedB = "[C]([C]([C,1][C,2][C,3])[C,2]([C,1][C,3]))";
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(0, expectedA);
        expected.put(4, expectedA);
        expected.put(1, expectedB);
        expected.put(2, expectedB);
        expected.put(3, expectedB);
        
        IMolecule mol = AbstractSignatureTest.makePropellane();
//        TestSignaturePort.printMolecule(mol);
        TestSignature.testCanonical(mol, expectedB);
//        TestSignaturePort.testAtoms(mol, expected);
//        TestSignaturePort.testAtom(mol, 1, expectedB);
    }
    
    @Test
    public void testHexaneForOrbitElements() {
        IMolecule mol = AbstractSignatureTest.makeHexane();
        Signature signature = new Signature(mol);
        for (OrbitElement orbitElement : signature.calculateOrbitElements()) {
            System.out.println(orbitElement);
        }
    }
    
    @Test
    public void testCageForOrbitElements() {
        IMolecule mol = AbstractSignatureTest.makeCage();
        Signature signature = new Signature(mol);
        for (OrbitElement orbitElement : signature.calculateOrbitElements()) {
            System.out.println(orbitElement);
        }
    }
    
    @Test
    public void testNapthalene() {
        String expectedA = "[C](p[C](p[C](p[C](p[C](p[C,1]))" +
        		           "p[C,2](p[C](p[C,1]))))p[C](p[C](p[C,2])))";
        String expectedB = "[C](p[C](p[C](p[C,1]))" +
        		           "p[C](p[C](p[C](p[C,2]))p[C](p[C,1]p[C](p[C,2]))))";
        String expectedC = "[C](p[C](p[C](p[C,1]))" +
        		           "p[C](p[C](p[C,2]))p[C](p[C](p[C,2])p[C](p[C,1])))";
        IMolecule mol = AbstractSignatureTest.makeNapthalene();
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(0, expectedA);
        expected.put(1, expectedB);
        expected.put(2, expectedC);
        expected.put(3, expectedB);
        expected.put(4, expectedA);
        expected.put(5, expectedA);
        expected.put(6, expectedB);
        expected.put(7, expectedC);
        expected.put(8, expectedB);
        expected.put(9, expectedA);
        
        TestSignature.testAtoms(mol, expected);
    }
    

}
