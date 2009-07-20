package test.signature;

import java.util.HashMap;
import java.util.Map;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IMolecule;

import signature.SignaturePort;

public class TestSignaturePort {
    
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
//        TestSignaturePort.testCanonical(mol, expectedB);
        TestSignaturePort.testAtoms(mol, expected);
    }

}
