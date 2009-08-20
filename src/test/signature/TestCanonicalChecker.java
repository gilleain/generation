package test.signature;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.AtomContainerAtomPermutor;
import signature.CanonicalChecker;

public class TestCanonicalChecker {
    
    public static IAtomContainer makeCanonicalEthane() {
        IAtomContainer ethane = AbstractSignatureTest.builder.newAtomContainer();
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addBond(0, 1, IBond.Order.SINGLE);
        ethane.addBond(0, 2, IBond.Order.SINGLE);
        ethane.addBond(0, 3, IBond.Order.SINGLE);
        ethane.addBond(0, 4, IBond.Order.SINGLE);
        ethane.addBond(1, 5, IBond.Order.SINGLE);
        ethane.addBond(1, 6, IBond.Order.SINGLE);
        ethane.addBond(1, 7, IBond.Order.SINGLE);
        return ethane;
    }
    
    public static IAtomContainer makeNonCanonicalEthane() {
        IAtomContainer ethane = AbstractSignatureTest.builder.newAtomContainer();
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("C"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addAtom(AbstractSignatureTest.builder.newAtom("H"));
        ethane.addBond(0, 1, IBond.Order.SINGLE);
        ethane.addBond(0, 2, IBond.Order.SINGLE);
        ethane.addBond(0, 3, IBond.Order.SINGLE);
        ethane.addBond(0, 7, IBond.Order.SINGLE);
        ethane.addBond(1, 4, IBond.Order.SINGLE);
        ethane.addBond(1, 5, IBond.Order.SINGLE);
        ethane.addBond(1, 6, IBond.Order.SINGLE);
        return ethane;
    }
    
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
    public void testEthane() {
        Assert.assertTrue(
                CanonicalChecker.isCanonical(
                        TestCanonicalChecker.makeCanonicalEthane()));
        Assert.assertFalse(
                CanonicalChecker.isCanonical(
                        TestCanonicalChecker.makeNonCanonicalEthane()));
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
