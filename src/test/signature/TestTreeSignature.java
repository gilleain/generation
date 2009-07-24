package test.signature;

import org.junit.Assert;
import org.junit.Test;

import signature.TreeSignature;

public class TestTreeSignature {
    
    @Test
    public void testReadWriteString() {
        String sigString = "[A]([B][C])";
        int h = 1;
        TreeSignature sig = new TreeSignature(sigString);
        Assert.assertEquals("Tree not correct height", sig.height(), h);
        String actual = sig.toString();
        Assert.assertEquals("Roundtrip failed", sigString, actual);
    }
    
    @Test
    public void testCopy() {
        String sigString = "[A]([B][C])";
        TreeSignature sig = new TreeSignature(sigString);
        TreeSignature copy = new TreeSignature(sig);
        String actual = copy.toString();
        Assert.assertEquals("Copy not correct", sigString, actual);
    }
    
    @Test
    public void testChangeRoot() {
        String sigString = "[A]([B][C])";
        TreeSignature sig = new TreeSignature(sigString);
        TreeSignature copyA = new TreeSignature(sig);
        copyA.changeRoot(0);
        System.out.println(copyA.toString());
        TreeSignature copyB = new TreeSignature(sig);
        copyB.changeRoot(1);
        System.out.println(copyB.toString());
    }
    
    @Test
    public void testRootNeighbourCount() {
        String sigString = "[A]([B][C])";
        TreeSignature sig = new TreeSignature(sigString);
        int count = sig.rootNeighbourCount();
        Assert.assertEquals("root count not correct", 2, count);
    }
    
    @Test
    public void testCanonize() {
        TreeSignature sig = new TreeSignature("[A]([B]([C])[D]([E][F]))");
        sig.canonize();
        System.out.println(sig.toString());
    }
    
    @Test
    public void testCountCompatibleBonds() {
        TreeSignature sigA = new TreeSignature("[C]([C][C][C][C][C])");
        TreeSignature sigB = new TreeSignature("[C]([C][C]([C][C][C]))");
        TreeSignature sigC = new TreeSignature("[C]([C]([C])[C]([C][C]))");
        
//        System.out.println(sigA.countCompatibleBonds(sigA));
//        System.out.println(sigA.countCompatibleBonds(sigB));
        System.out.println(sigA.countCompatibleBonds(sigC));
        System.out.println(sigB.countCompatibleBonds(sigB));
        System.out.println(sigB.countCompatibleBonds(sigC));
    }

}
