package test.signature;

import java.io.StringWriter;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.MDLWriter;

import signature.SignaturePort;

public class TestSignaturePort {
    
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
    
    public static void testCanonical(IMolecule mol, String expected) {
        SignaturePort sig = new SignaturePort(mol);
        String actual = sig.sisc_canonize();
        Assert.assertEquals("not canonical", expected, actual);
    }
    
    public static void testAtoms(IMolecule mol, Map<Integer, String> expected) {
        SignaturePort sig = new SignaturePort(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String expectedSig = expected.get(i);
            String actualSig = sig.forAtom(i);
//            System.out.println(expectedSig);
            System.out.println(actualSig);
//            System.out.println("---------");
//            Assert.assertEquals("not correct", expectedSig, actualSig);
        }
    }
    
    public static void testAtom(IMolecule mol, int i, String expected) {
        SignaturePort sig = new SignaturePort(mol);
        String actual = sig.forAtom(i);
        Assert.assertEquals("not correct", expected, actual);
    }
    
    public static void testAllSame(IMolecule mol, String expected) {
        SignaturePort sig = new SignaturePort(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String actual = sig.forAtom(i);
            Assert.assertEquals("not equal for atom " + i, expected, actual);
        }
    }
    
    public static void printSignatures(IMolecule mol) {
        SignaturePort sig = new SignaturePort(mol);
        for (int i = 0; i < mol.getAtomCount(); i++) {
            String sigForAtomI = sig.forAtom(i);
            System.out.println(i + " " + sigForAtomI);
        }
    }
    
    @Test
    public void testCage() {
        String expectedA =  "[C]([C]([C,2]([C]([C,3][C,4]))[C]([C,5]" +
                            "[C,3]([C,6]([C,1]))))[C]([C]([C,7][C]" +
                            "([C,1[C,8]))[C,5]([C,8]([C,6])))[C]([C,2]" +
                            "[C,7]([C,4]([C,1]))))";
        String expectedB =  "[C]([C]([C]([C,2][C]([C,1][C,3]))[C]" +
                            "([C,1]([C,4])[C,5]))[C]([C,2]([C,6]" +
                            "([C,3]))[C]([C,7][C,6]))[C]([C,5]([C,4]" +
                            "([C,8]))[C,7]([C,8]([C,3]))))";
        Map<Integer, String> expected = new HashMap<Integer, String>();
        expected.put(0, expectedA);
        IMolecule mol = TestSignature.makeCage();
        TestSignaturePort.printSignatures(mol);
    }
    
    @Test
    public void testCubane() {
        String expected = "[C]([C]([C](C,1])[C]([C,2])" +
                          "[C]([C,3]))[C]([C,2][C,2]))";
        IMolecule mol = TestSignature.makeCubane();
        TestSignaturePort.testAllSame(mol, expected);
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
        
        IMolecule mol = TestSignature.makePropellane();
//        TestSignaturePort.printMolecule(mol);
        TestSignaturePort.testCanonical(mol, expectedB);
//        TestSignaturePort.testAtoms(mol, expected);
//        TestSignaturePort.testAtom(mol, 1, expectedB);
    }
    

}
