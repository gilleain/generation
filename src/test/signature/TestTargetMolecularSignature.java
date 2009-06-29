package test.signature;

import signature.TargetMolecularSignature;

public class TestTargetMolecularSignature {
    
    /**
     * Make a simple signature compatible with hexane.
     * 
     * @return a sample molecular signature
     */
    public static TargetMolecularSignature makeSimpleMolecularSignature() {
        TargetMolecularSignature sig = new TargetMolecularSignature(5);
        sig.add("C(C(C(C(C(C)))))", 2);
        sig.add("C(CC(C(C(C))))", 2);
        sig.add("C(C(C(C))C(C))", 2);
        return sig;
    }
    
    /**
     * Make a height-2 signature compatible with (at least) adenine.
     * 
     * @return a height-2 signature compatible with adenine
     */
    public static TargetMolecularSignature makeAdenineExample() {
        TargetMolecularSignature sig = new TargetMolecularSignature(2);
        sig.add("C(NN)",  3);
        sig.add("C(NNC)", 2);
        sig.add("N(CC)",  3);
        sig.add("N(CCC)", 1);
        sig.add("N(C)",   1);
        return sig;
    }
    
    public static TargetMolecularSignature makeCuneaneExample() {
        TargetMolecularSignature sig = new TargetMolecularSignature(4);
        sig.add("C(C(C(C)C(CC))C(C(C)C(C))C(C(C)C(CC)))", 2, "A");
        sig.add("C(C(C(C)C(CC))C(CC(CC))C(C(C)C))", 2, "B");
        sig.add("C(C(C(CC)C)C(C)C(CC)C(C(CC)C(CC)))", 2, "C");
        return sig;
    }
    
    /**
     * Make the example molecular signature given in the signature enumeration
     * paper.
     * 
     * @return a sample molecular signature
     */
    public static TargetMolecularSignature makePaperExampleMolecularSignature() {
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
        TargetMolecularSignature tms = makePaperExampleMolecularSignature();
        System.out.println(tms);
    }
    
}
