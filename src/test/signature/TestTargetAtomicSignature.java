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
        String sigString = "C(HC(HHH)C(HHC)C(HCC))";
        TargetAtomicSignature sig = new TargetAtomicSignature(sigString);
        for (int height = 0; height < 2; height++) {
            for (String s : sig.getSignatureStrings(height)) {
                System.out.println(height + " " + s);
            }
        }
    }
    
    public static void main(String[] args) {
        TestTargetAtomicSignature test = new TestTargetAtomicSignature();
        test.roundtrip();
        test.signatureStrings();
    }

}
