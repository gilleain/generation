package theoretical;

import java.io.StringWriter;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.io.MDLWriter;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.OrbitElement;
import signature.Signature;

public class Snarks {
    
    public static NoNotificationChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
    public static void printAtomSigs(IAtomContainer ac) {
        Signature sig = new Signature(ac);
        for (OrbitElement o : sig.calculateOrbitElements()) {
            System.out.println(o);
        }
    }
    
    public static void printMolecule(IAtomContainer mol) {
        StringWriter stringWriter = new StringWriter();
        MDLWriter writer = new MDLWriter(stringWriter);
        try {
            writer.writeMolecule(mol.getBuilder().newMolecule(mol));
            System.out.println(stringWriter.toString());
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    public static void printDegreeSequence(IAtomContainer ac) {
        int i = 0;
        for (IAtom a : ac.atoms()) {
            int n = ac.getConnectedAtomsCount(a);
            System.out.println(i + " " + n);
            i++;
        }
    }
    
    public static IAtomContainer makeTietzesGraph() {
        IAtomContainer ac = builder.newAtomContainer();
        for (int i = 0; i < 12; i++) { ac.addAtom(builder.newAtom("C")); }
        ac.addBond(0, 1, IBond.Order.SINGLE);
        ac.addBond(0, 4, IBond.Order.SINGLE);
        ac.addBond(0, 8, IBond.Order.SINGLE);
        ac.addBond(1, 2, IBond.Order.SINGLE);
        ac.addBond(1, 6, IBond.Order.SINGLE);
        ac.addBond(2, 3, IBond.Order.SINGLE);
        ac.addBond(2, 10, IBond.Order.SINGLE);
        ac.addBond(3, 4, IBond.Order.SINGLE);
        ac.addBond(3, 7, IBond.Order.SINGLE);
        ac.addBond(4, 5, IBond.Order.SINGLE);
        ac.addBond(5, 6, IBond.Order.SINGLE);
        ac.addBond(5, 11, IBond.Order.SINGLE);
        ac.addBond(6, 7, IBond.Order.SINGLE);
        ac.addBond(7, 8, IBond.Order.SINGLE);
        ac.addBond(8, 9, IBond.Order.SINGLE);
        ac.addBond(9, 10, IBond.Order.SINGLE);
        ac.addBond(9, 11, IBond.Order.SINGLE);
        ac.addBond(10, 11, IBond.Order.SINGLE);
        return ac;
    }
    
    public static IAtomContainer makeUnnamedNearlyRegularGraph() {
        // a bridged, symmetric, 2/3 degree graph a little like cuneane
        IAtomContainer ac = builder.newAtomContainer();
        for (int i = 0; i < 10; i++) { ac.addAtom(builder.newAtom("C")); }
        ac.addBond(0, 1, IBond.Order.SINGLE);
        ac.addBond(0, 4, IBond.Order.SINGLE);   // XXX
        ac.addBond(0, 7, IBond.Order.SINGLE);
        ac.addBond(1, 2, IBond.Order.SINGLE);
        ac.addBond(1, 8, IBond.Order.SINGLE);
        ac.addBond(2, 3, IBond.Order.SINGLE);
        ac.addBond(2, 6, IBond.Order.SINGLE);
        ac.addBond(3, 4, IBond.Order.SINGLE);
        ac.addBond(3, 9, IBond.Order.SINGLE);
        ac.addBond(4, 5, IBond.Order.SINGLE);
        ac.addBond(5, 6, IBond.Order.SINGLE);
        ac.addBond(5, 9, IBond.Order.SINGLE);
        ac.addBond(6, 7, IBond.Order.SINGLE);
        ac.addBond(7, 8, IBond.Order.SINGLE);
        ac.addBond(8, 9, IBond.Order.SINGLE);
        return ac;
    }
    
    public static IAtomContainer makePetersenGraph() {
        IAtomContainer petersen = builder.newAtomContainer();
        for (int i = 0; i < 10; i++) {
            petersen.addAtom(builder.newAtom("C"));
        }
//        petersen.addBond(0, 1, IBond.Order.SINGLE);
//        petersen.addBond(0, 4, IBond.Order.SINGLE);
//        petersen.addBond(0, 6, IBond.Order.SINGLE);
//        petersen.addBond(1, 2, IBond.Order.SINGLE);
//        petersen.addBond(1, 7, IBond.Order.SINGLE);
//        petersen.addBond(2, 3, IBond.Order.SINGLE);
//        petersen.addBond(2, 8, IBond.Order.SINGLE);
//        petersen.addBond(3, 4, IBond.Order.SINGLE);
//        petersen.addBond(3, 9, IBond.Order.SINGLE);
//        petersen.addBond(4, 5, IBond.Order.SINGLE);
//        petersen.addBond(5, 7, IBond.Order.SINGLE);
//        petersen.addBond(5, 8, IBond.Order.SINGLE);
//        petersen.addBond(6, 8, IBond.Order.SINGLE);
//        petersen.addBond(6, 9, IBond.Order.SINGLE);
//        petersen.addBond(7, 9, IBond.Order.SINGLE);
        petersen.addBond(0, 1, IBond.Order.SINGLE);
        petersen.addBond(0, 8, IBond.Order.SINGLE);
        petersen.addBond(0, 9, IBond.Order.SINGLE);
        petersen.addBond(1, 2, IBond.Order.SINGLE);
        petersen.addBond(1, 5, IBond.Order.SINGLE);
        petersen.addBond(2, 3, IBond.Order.SINGLE);
        petersen.addBond(2, 4, IBond.Order.SINGLE);
        petersen.addBond(3, 6, IBond.Order.SINGLE);
        petersen.addBond(3, 9, IBond.Order.SINGLE);
        petersen.addBond(4, 7, IBond.Order.SINGLE);
        petersen.addBond(4, 8, IBond.Order.SINGLE);
        petersen.addBond(5, 6, IBond.Order.SINGLE);
        petersen.addBond(5, 7, IBond.Order.SINGLE);
        petersen.addBond(6, 8, IBond.Order.SINGLE);
        petersen.addBond(7, 9, IBond.Order.SINGLE);
        return petersen;
    }
    
    public static void main(String[] args) {
//        IAtomContainer ac = Snarks.makePetersenGraph();
//        IAtomContainer ac = Snarks.makeTietzesGraph();
        IAtomContainer ac = Snarks.makeUnnamedNearlyRegularGraph();
//        Snarks.printMolecule(ac);
        Snarks.printAtomSigs(ac);
        Snarks.printDegreeSequence(ac);
    }

}
