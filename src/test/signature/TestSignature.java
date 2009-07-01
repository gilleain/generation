package test.signature;

import org.junit.*;
import org.openscience.cdk.Atom;
import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.Signature;

public class TestSignature {
    private static NoNotificationChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
    public static IMolecule makeCage() {
        /*
         * This 'molecule' is the example used to illustrate the
         * algorithm outlined in the 2004 Faulon &ct. paper
         */
        IMolecule cage = builder.newMolecule();
        for (int i = 0; i < 16; i++) {
            cage.addAtom(builder.newAtom("C"));
        }
        cage.addBond(0, 1, IBond.Order.SINGLE);
        cage.addBond(0, 3, IBond.Order.SINGLE);
        cage.addBond(0, 4, IBond.Order.SINGLE);
        cage.addBond(1, 2, IBond.Order.SINGLE);
        cage.addBond(1, 6, IBond.Order.SINGLE);
        cage.addBond(2, 3, IBond.Order.SINGLE);
        cage.addBond(2, 8, IBond.Order.SINGLE);
        cage.addBond(3, 10, IBond.Order.SINGLE);
        cage.addBond(4, 5, IBond.Order.SINGLE);
        cage.addBond(4, 11, IBond.Order.SINGLE);
        cage.addBond(5, 6, IBond.Order.SINGLE);
        cage.addBond(5, 12, IBond.Order.SINGLE);
        cage.addBond(6, 7, IBond.Order.SINGLE);
        cage.addBond(7, 8, IBond.Order.SINGLE);
        cage.addBond(7, 13, IBond.Order.SINGLE);
        cage.addBond(8, 9, IBond.Order.SINGLE);
        cage.addBond(9, 10, IBond.Order.SINGLE);
        cage.addBond(9, 14, IBond.Order.SINGLE);
        cage.addBond(10, 11, IBond.Order.SINGLE);
        cage.addBond(11, 15, IBond.Order.SINGLE);
        cage.addBond(12, 13, IBond.Order.SINGLE);
        cage.addBond(12, 15, IBond.Order.SINGLE);
        cage.addBond(13, 14, IBond.Order.SINGLE);
        cage.addBond(14, 15, IBond.Order.SINGLE);
        return cage;
    }
    
    @Test
    public void testCage() {
        IMolecule cage = TestSignature.makeCage();
        String expected = "[c_]([c_]([c_,2]([c_]([c_,3][c_,4]))"
                        + "[c_]([c_,5][c_,3]([c_,6]([c_,1]))))"
                        + "[c_]([c_]([c_,7][c_]([c_,1][c_,8]))"
                        + "[c_,5]([c_,8]([c_,6])))[c_]([c_,2]"
                        + "[c_,7]([c_,4]([c_,1]))))";
        
//        Signature signature = Signature.forMolecule(cage);
        Signature signature = new Signature(cage.getAtom(0), cage);
        String actual = signature.canonize();
        Assert.assertEquals("cage signature not correct", expected, actual);
    }
    
    @Test
    public void testBenzene() {
        String expected = "[cp]([cp]([cp]([cp,1]))[cp]([cp]([cp,1])))";
        Molecule mol = new Molecule();
        mol.addAtom(new Atom("C")); // 0
        mol.addAtom(new Atom("C")); // 1
        mol.addAtom(new Atom("C")); // 2
        mol.addAtom(new Atom("C")); // 3
        mol.addAtom(new Atom("C")); // 4
        mol.addAtom(new Atom("C")); // 5
        for (IAtom atom : mol.atoms()) {
            atom.setFlag(CDKConstants.ISAROMATIC, true);
        }
        
        mol.addBond(0, 1, IBond.Order.SINGLE); // 1
        mol.addBond(1, 2, IBond.Order.SINGLE); // 2
        mol.addBond(2, 3, IBond.Order.SINGLE); // 3
        mol.addBond(3, 4, IBond.Order.SINGLE); // 4
        mol.addBond(4, 5, IBond.Order.SINGLE); // 5
        mol.addBond(5, 0, IBond.Order.SINGLE); // 6
        for (IBond bond : mol.bonds()) {
            bond.setFlag(CDKConstants.ISAROMATIC, true);
        }
        Signature signature = new Signature(0, mol);
        String actual = signature.toString();
        Assert.assertEquals("benzene signature not correct!", expected, actual);
    }
    
}
