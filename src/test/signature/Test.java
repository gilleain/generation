package test.signature;

import signature.TargetMolecularSignature;

public class Test {
    
    public static void main(String[] args) {
        TargetMolecularSignature sig = 
            TestTargetMolecularSignature.makeMolecularSignature();
        System.out.println(sig);
        
        TestTargetAtomicSignature test = new TestTargetAtomicSignature();
        test.roundtrip();
        test.signatureStrings();
        test.subSignature();
        test.subSignatureFromRootChild();
    }

}
