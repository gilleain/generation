package test.signature;

import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.AtomContainerAtomPermutor;
import signature.CanonicalChecker;

public class TestCanonicalChecker {
    
    public static void testIsCanonicalFindFirst(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        System.out.println(CanonicalChecker.isCanonical(container));
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.next();
            if (CanonicalChecker.isCanonical(permutedContainer)) {
                System.out.println("true");
                return;
            }
            System.out.println();
        }
        System.out.println("false");
    }
    
    public static void testIsCanonical(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        System.out.println(CanonicalChecker.isCanonical(container));
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.next();
            System.out.println(CanonicalChecker.isCanonical(permutedContainer));
        }
    }
    
    public static void testIsCanonicalRandomly(IAtomContainer container) {
        AtomContainerAtomPermutor permutor = 
            new AtomContainerAtomPermutor(container);
        System.out.println(CanonicalChecker.isCanonical(container));
        while (permutor.hasNext()) {
            IAtomContainer permutedContainer = permutor.randomNext();
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
        square.addBond(0, 3, IBond.Order.SINGLE);
        square.addBond(1, 2, IBond.Order.SINGLE);
        square.addBond(2, 3, IBond.Order.SINGLE);
        
        TestCanonicalChecker.testIsCanonical(square);
    }
    
    @Test
    public void testCuneane() {
        IAtomContainer cuneane = AbstractSignatureTest.makeCuneane();
        TestCanonicalChecker.testIsCanonicalFindFirst(cuneane);
    }
    
    @Test
    public void testCube() {
        IAtomContainer cube = AbstractSignatureTest.makeCubane();
        TestCanonicalChecker.testIsCanonicalRandomly(cube);
    }
}
