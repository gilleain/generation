package tmputil;

import java.io.File;
import java.io.IOException;
import java.io.PrintStream;
import java.util.Arrays;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import signature.AtomContainerAtomPermutor;
import signature.Signature;
import test.signature.AbstractSignatureTest;
import test.signature.TestCanonicalChecker;

public class PermutationPrinter {
    
    /**
     * Print a molfile in the 'molfile' format that faulon's program uses.
     * 
     * @param container
     * @param file
     * @throws IOException
     */
    public static void printContainer(
            IAtomContainer container, File file) throws IOException {
        if (!file.exists()) file.createNewFile();
        PrintStream s = new PrintStream(file);
        s.println(container.getAtomCount() + " " + container.getBondCount());
        for (IAtom atom : container.atoms()) {
            s.println("0.0000 0.0000 0.0000 " + atom.getSymbol() + " 0 0 0 0 0");
        }
        
        for (IBond bond : container.bonds()) {
            int i = container.getAtomNumber(bond.getAtom(0)) + 1;
            int j = container.getAtomNumber(bond.getAtom(1)) + 1;
            int o = bond.getOrder().ordinal() + 1;
            s.println(i + " " + j + " " + o + " 0 0 0");
        }
        s.close();
    }
    
    /**
     * Print all permutations of the smiles.
     * 
     * @param smiles
     * @param directory
     * @throws InvalidSmilesException
     * @throws IOException
     */
    public static void printPermutations(String smiles, File directory) 
    throws InvalidSmilesException, IOException {
        SmilesParser parser = 
            new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
        IAtomContainer container = parser.parseSmiles(smiles);
        PermutationPrinter.printPermutations(container, directory);
    }
    
    public static void printPermutations(
            IAtomContainer container, File directory) throws IOException {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        File original = new File(directory, "0.mol");
        PermutationPrinter.printContainer(container, original);
        int i = 1;
        while (permutor.hasNext()) {
            IAtomContainer permutation = permutor.next();
            File file = new File(directory, i + ".mol");
            PermutationPrinter.printContainer(permutation, file);
            System.out.println(i + " " + 
                    Arrays.toString(permutor.getCurrentPermutation()));
            i++;
        }
    }
    
    public static void check(IAtomContainer container, int i, int[] p) {
        Signature sig = new Signature(container);
        if (sig.isCanonical()) {
            System.out.println("CANONICAL " + i + " " + Arrays.toString(p));
        } else {
            System.out.println(i + " " + Arrays.toString(p));
        }
    }
    
    public static void checkPermutations(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        PermutationPrinter.check(container, 0, permutor.getCurrentPermutation());
        int i = 1;
        while (permutor.hasNext()) {
            IAtomContainer permutation = permutor.next();
            PermutationPrinter.check(
                    permutation, i, permutor.getCurrentPermutation());
            i++;
        }
    }
    
    public static IAtomContainer makeCNO() {
        IAtomContainer container = AbstractSignatureTest.builder.newAtomContainer();
        container.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        container.addAtom(AbstractSignatureTest.builder.newAtom("N"));
        container.addAtom(AbstractSignatureTest.builder.newAtom("O"));
        container.addBond(0, 1, IBond.Order.SINGLE);
        container.addBond(0, 2, IBond.Order.SINGLE);

        return container;
    }
    
    public static void main(String[] args) {
//        String smiles = "c1cccc1";
        File directory = 
            new File(
        "/Users/maclean/development/otherpeoplesstuff/faulon/translator/perms");
        
        try {
//            PermutationPrinter.printPermutations(smiles, directory);
//            IAtomContainer container = TestCanonicalChecker.makeCanonicalEthane();
//            IAtomContainer container = TestCanonicalChecker.makeFaulonCanonicalEthane();
//            IAtomContainer container = AbstractSignatureTest.builder.newAtomContainer();
            IAtomContainer container = PermutationPrinter.makeCNO();
//            container.addAtom(AbstractSignatureTest.builder.newAtom("C"));
//            container.addAtom(AbstractSignatureTest.builder.newAtom("H"));
//            container.addAtom(AbstractSignatureTest.builder.newAtom("H"));
//            container.addAtom(AbstractSignatureTest.builder.newAtom("H"));
//            container.addAtom(AbstractSignatureTest.builder.newAtom("H"));
//            container.addBond(0, 1, IBond.Order.SINGLE);
//            container.addBond(0, 2, IBond.Order.SINGLE);
//            container.addBond(0, 3, IBond.Order.SINGLE);
//            container.addBond(0, 4, IBond.Order.SINGLE);
            PermutationPrinter.printPermutations(container, directory);
//            PermutationPrinter.checkPermutations(container);
//            PermutationPrinter.checkPermutations(ch4);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

}
