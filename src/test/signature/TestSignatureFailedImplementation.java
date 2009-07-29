package test.signature;

import org.junit.*;
import org.openscience.cdk.graph.AtomContainerAtomPermutor;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.SignatureFailedImplementation;

public class TestSignatureFailedImplementation {
  
    private static NoNotificationChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
    /**
     * CAREFUL : tries all possible permutations of the atoms, and checks that
     * the canonical strings are the same.
     * 
     * @param container
     * @param expected
     */
    public static void testCanonicalPermutations(
            int atomNumber, IAtomContainer container, String expected) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(
                    (org.openscience.cdk.AtomContainer)container);  // XXX!
        while (permutor.hasNext()) {
            System.out.println(".");
            IAtomContainer permutation = (IAtomContainer) permutor.next();
            IMolecule mol = builder.newMolecule(permutation);
            SignatureFailedImplementation signatureFailedImplementation = new SignatureFailedImplementation(atomNumber, mol);
            String actual = signatureFailedImplementation.canonize();
            Assert.assertEquals("signature not correct!", expected, actual);
        }
    }
    
    public static void testSignatureFromAtom(
            int atomNumber, IMolecule mol, String expected) {
        SignatureFailedImplementation sig = new SignatureFailedImplementation(atomNumber, mol);
        String actual = sig.canonize();
//        String actual = sig.toSimpleCanonicalString();
        Assert.assertEquals("signature not canonical", expected, actual);
    }
    
    @Test
    public void testCage() {
        IMolecule cage = AbstractSignatureTest.makeCage();
        String expectedA = "[c_]([c_]([c_,2]([c_]([c_,3][c_,4]))[c_]([c_,5]" +
        		           "[c_,3]([c_,6]([c_,1]))))[c_]([c_]([c_,7][c_]" +
        		           "([c_,1[c_,8]))[c_,5]([c_,8]([c_,6])))[c_]([c_,2]" +
        		           "[c_,7]([c_,4]([c_,1]))))";
        String expectedB = "[c_]([c_]([c_]([c_,2][c_]([c_,1][c_,3]))[c_]" +
        		           "([c_,1]([c_,4])[c_,5]))[c_]([c_,2]([c_,6]" +
        		           "([c_,3]))[c_]([c_,7][c_,6]))[c_]([c_,5]([c_,4]" +
        		           "([c_,8]))[c_,7]([c_,8]([c_,3]))))";
        TestSignatureFailedImplementation.testSignatureFromAtom(0, cage, expectedA);
        
        // TODO : check which atom(s) has this signature!
        TestSignatureFailedImplementation.testSignatureFromAtom(1, cage, expectedB);
    }
    
    @Test
    public void testCubane() {
        String expected = "[c_]([c_]([c_](c_,1])[c_]([c_,2])" +
        		          "[c_]([c_,3]))[c_]([c_,2][c_,2]))";
      
        IMolecule mol = AbstractSignatureTest.makeCubane();
        TestSignatureFailedImplementation.testCanonicalPermutations(0, mol, expected);
    }
    
    @Test
    public void testPropellane() {
        String expectedA = "[c_]([c_]([c_,1])[c_]([c_,1])[c_]([c_,1])[c_,1]))";
        String expectedB = "[c_]([c_]([c_,1][c_,2][c_,3])[c_,2]([c_,1][c_,3]))";
        
        IMolecule mol = AbstractSignatureTest.makePropellane();
        TestSignatureFailedImplementation.testSignatureFromAtom(0, mol, expectedA);
        TestSignatureFailedImplementation.testSignatureFromAtom(1, mol, expectedB);
    }
    
    @Test
    public void testPseudoPropellane() {
        String expectedA = "[c_]([c_]([c_,1])[c_]([c_,1])[c_]([c_,1]))";
        String expectedB = "[c_]([c_]([c_,1][c_,2])[c_]([c_,1][c_,2]))";
        IMolecule mol = AbstractSignatureTest.makePseudoPropellane();
        TestSignatureFailedImplementation.testSignatureFromAtom(0, mol, expectedA);
        TestSignatureFailedImplementation.testSignatureFromAtom(1, mol, expectedB);
    }
    
    @Test
    public void testHexane() {
        String expectedA = "[c_]([c_]([c_]([c_]([c_]([c_])))))";
        String expectedB = "[c_]([c_][c_]([c_]([c_]([c_]))))";
        String expectedC = "[c_]([c_]([c_]([c_]))[c_]([c_]))";
        IMolecule mol = AbstractSignatureTest.makeHexane();
        
        TestSignatureFailedImplementation.testSignatureFromAtom(0, mol, expectedA);
        TestSignatureFailedImplementation.testSignatureFromAtom(1, mol, expectedB);
        TestSignatureFailedImplementation.testSignatureFromAtom(2, mol, expectedC);
    }
    
    @Test
    public void testNapthalene() {
        String expectedA = "[cp]([cp]([cp]([cp]([cp]([cp,1]))" +
        		           "[cp,2]([cp]([cp,1]))))[cp]([cp]([cp,2])))";
        String expectedB = "[cp]([cp]([cp]([cp,1]))" +
        		           "[cp]([cp]([cp]([cp,2]))[cp]([cp,1][cp]([cp,2]))))";
        String expectedC = "[cp]([cp]([cp]([cp,1]))[cp]([cp]([cp,2]))" +
        		           "[cp]([cp]([cp,2])[cp]([cp,1])))";
        
        IMolecule mol = AbstractSignatureTest.makeNapthalene();
        
        // XXX if the atom numbers are changed, the atom-to-sig map changes!
        TestSignatureFailedImplementation.testSignatureFromAtom(0, mol, expectedA);
        TestSignatureFailedImplementation.testSignatureFromAtom(1, mol, expectedB);
        TestSignatureFailedImplementation.testSignatureFromAtom(2, mol, expectedC);
    }
    
    @Test
    public void testBenzene() {
        String expected = "[cp]([cp]([cp]([cp,1]))[cp]([cp]([cp,1])))";
       
        IMolecule mol = AbstractSignatureTest.makeBenzene();
//        Signature signature = new Signature(0, mol);
//        String actual = signature.canonize();
//        Assert.assertEquals("benzene signature not correct!", expected, actual);
        TestSignatureFailedImplementation.testCanonicalPermutations(0, mol, expected); 
    }
    
}
