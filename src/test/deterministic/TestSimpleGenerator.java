package test.deterministic;

import java.util.List;

import junit.framework.Assert;

import org.junit.Test;
import org.openscience.cdk.interfaces.IMolecule;

import deterministic.SimpleGenerator;

public class TestSimpleGenerator {
    
    @Test
    public void testMethane() throws Exception {
        List<IMolecule> molecules = new SimpleGenerator().generate("CH4");
        Assert.assertEquals(1, molecules.size());
    }
    
    @Test
    public void testEthane() throws Exception {
        List<IMolecule> molecules = new SimpleGenerator().generate("C2H6");
        Assert.assertEquals(1, molecules.size());
    }

}
