package test.signature;

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
     
    @Test
    public void testPropellane() {
        String expected = "[C]([C]([C,1][C,2][C,3])[C,2]([C,1][C,3]))";
        
        IMolecule mol = TestSignature.makePropellane();
        TestSignaturePort.testCanonical(mol, expected);
    }

}
