package test.signature;

import signature.TargetMolecularSignature;

public class TestTargetMolecularSignature {
    
    public static TargetMolecularSignature makeMolecularSignature() {
        TargetMolecularSignature sig = new TargetMolecularSignature(2);
        sig.add("H(C(HHC))", 9, "h3");
        sig.add("H(C(HCC))", 12, "h2");
        sig.add("H(C(CCC))", 1, "h1");
        sig.add("C(HHHC(HCC))", 1, "c31");
        sig.add("C(HHHC(HHC))", 2, "c32");
        sig.add("C(HHC(HHH)C(HHC))", 2, "c232");
        sig.add("C(HHC(HHC)C(HHC))", 2, "c222");
        sig.add("C(HHC(HHC)C(HCC))", 2, "c221");
        
        // NOTE : why is this called '1322' in the paper, when all the
        // others follow the naming pattern of number of hydrogens? 
        sig.add("C(HC(HHH)C(HHC)C(HCC))", 1, "c1322");

        return sig;
    }
    
    public void roundtrip() {
        TargetMolecularSignature tms = makeMolecularSignature();
        System.out.println(tms);
    }
    
}
