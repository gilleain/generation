package test.utilities;

import org.junit.Assert;
import org.junit.Test;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.Util;

public class TestUtil {
    
    public static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance(); 
    
    @Test
    public void testSaturatedSubgraphsWithMethane() {
        IAtomContainer m = builder.newAtomContainer();
        for (int i = 0; i < 1; i++) m.addAtom(builder.newAtom("C"));
        for (int i = 0; i < 5; i++) m.addAtom(builder.newAtom("H"));
        m.addBond(0, 1, IBond.Order.SINGLE);
        m.addBond(0, 2, IBond.Order.SINGLE);
        m.addBond(0, 3, IBond.Order.SINGLE);
        m.addBond(0, 4, IBond.Order.SINGLE);
        Assert.assertTrue(Util.saturatedSubgraph(0, m));
        m.removeAtom(5);
        Assert.assertFalse(Util.saturatedSubgraph(0, m));
    }
    
    @Test
    public void testIsSaturated() {
        IAtomContainer m = builder.newAtomContainer();
        for (int i = 0; i < 1; i++) m.addAtom(builder.newAtom("C"));
        for (int i = 0; i < 4; i++) m.addAtom(builder.newAtom("H"));
        m.addBond(0, 1, IBond.Order.SINGLE);
        m.addBond(0, 2, IBond.Order.SINGLE);
        m.addBond(0, 3, IBond.Order.SINGLE);
        m.addBond(0, 4, IBond.Order.SINGLE);
        try {
            Assert.assertTrue(Util.isSaturated(m.getAtom(0), m));
        } catch (CDKException c) {
            
        }
    }

}
