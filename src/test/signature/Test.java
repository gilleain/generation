package test.signature;

public class Test {
    
    public static void main(String[] args) {
//        TestTargetMolecularSignature test = new TestTargetMolecularSignature();
//        test.roundtrip();
        
//        TestTargetAtomicSignature test = new TestTargetAtomicSignature();
//        test.roundtrip();
//        test.signatureStrings();
//        test.subSignature();
//        test.subSignatureFromRootChild();
        
        TestGraph test = new TestGraph();
//        test.connectivity();
//        test.bondedAtomSignatures();
//        test.getDiameter();
        test.compatibleBondsInDisconnected();
    }

}
