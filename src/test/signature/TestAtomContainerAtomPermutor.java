package test.signature;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.isomorphism.UniversalIsomorphismTester;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.AtomContainerAtomPermutor;

public class TestAtomContainerAtomPermutor {
    
    private static NoNotificationChemObjectBuilder builder =
        NoNotificationChemObjectBuilder.getInstance();
    
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
            Assert.assertTrue(isomorphic);
        }
    }

}
