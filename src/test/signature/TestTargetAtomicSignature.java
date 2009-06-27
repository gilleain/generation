package test.signature;

import signature.TargetAtomicSignature;

public class TestTargetAtomicSignature {
    
    public void roundtrip() {
        String sigString = "C(HC(HHH)C(HHC)C(HCC))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        System.out.println(sigString);
        System.out.println(sig);
    }
    
    public void signatureStrings() {
//        String sigString = "C(HC(HHH)C(HHC)C(HCC))";
        String sigString = "C(HHHC(HCC))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        for (int height = 0; height < 2; height++) {
            for (String s : sig.getSignatureStrings(height)) {
                System.out.println(height + " " + s);
            }
        }
    }
    
    public void subSignatureFromRootChild() {
        String sigString = "A(B(C(D))E(F(G)H))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        int child = 0;
        int height = 2;
        String expected = "B(C(D)A(E))";
        String actual = sig.getSignatureString(child, height);
        
        System.out.println("signature\t" + sig);
        System.out.println("sub " + child + " / " + height + "\t" + actual);
        System.out.println("expected\t" + expected);
    }
    
    public void subSignature() {
        String sigString = "A(B(C(D))E(F(G)H))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        int height = 2;
        String expected = "A(B(C)E(FH))";
        String actual = sig.getSubSignature(height);
        
        System.out.println("signature\t" + sig);
        System.out.println("sub h = " + height + "\t" + actual);
        System.out.println("expected\t" + expected);
    }

}
