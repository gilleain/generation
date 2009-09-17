package test.signature;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import utilities.AtomContainerAtomPermutor;

public class TestAtomContainerAtomPermutor {
    
    private static NoNotificationChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
    public static void printContainer(IAtomContainer container) {
        for (IAtom atom : container.atoms()) {
            System.out.print(atom.getSymbol());
        }
        System.out.print(" ");
        int i = 0;
        for (IBond bond : container.bonds()) {
            int a1 = container.getAtomNumber(bond.getAtom(0));
            int a2 = container.getAtomNumber(bond.getAtom(1));
            if (a1 < a2) {
                System.out.print(String.format("%s-%s", a1, a2));
            } else {
                System.out.print(String.format("%s-%s", a2, a1));
            }
            if (i < container.getBondCount() - 1) {
                System.out.print(".");
            }
            i++;
        }
        System.out.print("\n");
    }
    
    @Test
    public void testPermute() throws Exception {
        IAtomContainer square = builder.newAtomContainer();
        square.addAtom(builder.newAtom("C"));
        square.addAtom(builder.newAtom("C"));
        square.addAtom(builder.newAtom("C"));
        square.addAtom(builder.newAtom("C"));
        square.addBond(0, 1, IBond.Order.SINGLE);
        square.addBond(1, 2, IBond.Order.SINGLE);
        square.addBond(2, 3, IBond.Order.SINGLE);
        square.addBond(3, 0, IBond.Order.SINGLE);
        
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(square);
        
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.next();
            boolean isomorphic = UniversalIsomorphismTester.isIsomorph(
                    square, permutedContainer);
            printContainer(permutedContainer);
            Assert.assertTrue(isomorphic);
        }
    }

}
