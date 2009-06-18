package test.signature;

import java.util.ArrayList;

import signature.SignatureEnumerator;
import signature.TargetMolecularSignature;

public class TestSignatureEnumerator {
    
    public TargetMolecularSignature makeMolecularSignature() {
        TargetMolecularSignature sig = new TargetMolecularSignature(2);
        sig.add("H(C(HHC))", 9, "h3");
        sig.add("H(C(HCC))", 12, "h2");
        sig.add("H(C(CCC))", 1, "h1");
        sig.add("C(HHHC(HCC))", 1, "c31");
        sig.add("C(HHHC(HHC))", 2, "c32");
        sig.add("C(HHC(HHH)C(HHC))", 2, "c232");
        sig.add("C(HHC(HHC)C(HHC))", 2, "c222");
        sig.add("C(HHC(HHC)C(HCC))", 2, "c221");
        return sig;
    }
    
    public void testCreate() {
        TargetMolecularSignature sig = makeMolecularSignature();
        ArrayList<String> e = new ArrayList<String>();
        ArrayList<Integer> c = new ArrayList<Integer>();
        SignatureEnumerator enumerator = new SignatureEnumerator(e, c, sig);
    }


}
