package test.signature;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.AtomContainerAtomPermutor;
import signature.CanonicalChecker;

public class TestCanonicalChecker {
    
    public static void testIsCanonical(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        System.out.println(CanonicalChecker.isCanonical(container));
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.next();
            System.out.println(CanonicalChecker.isCanonical(permutedContainer));
        }
    }
    
    @Test
    public void testSquare() {
        IAtomContainer square = AbstractSignatureTest.builder.newAtomContainer();
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        square.addBond(0, 1, IBond.Order.SINGLE);
        square.addBond(1, 2, IBond.Order.SINGLE);
        square.addBond(2, 3, IBond.Order.SINGLE);
        square.addBond(3, 0, IBond.Order.SINGLE);
        
        TestCanonicalChecker.testIsCanonical(square);
    }
}
