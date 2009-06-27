package signature;

import java.util.ArrayList;

public class TargetMolecularSignature {
    
    private ArrayList<TargetAtomicSignature> signatures;
    
    private ArrayList<Integer> counts;
    
    private int height;

    public TargetMolecularSignature(ArrayList<String> signatureStrings, 
                                    ArrayList<Integer> counts,
                                    int height) {
        this.signatures = new ArrayList<TargetAtomicSignature>();
        for (String signatureString : signatureStrings) {
            this.signatures.add(new TargetAtomicSignature(signatureString));
        }
        this.counts = counts;
        this.height = height;
    }
    
    public TargetMolecularSignature(int height) {
        this.signatures = new ArrayList<TargetAtomicSignature>();
        this.counts = new ArrayList<Integer>();
        this.height = height;
    }
    
    /**
     * Count of the number of compatible bonds between target atomic signatures. 
     * 
     * @param targetX the target atomic signature index of atom x
     * @param h the height of the signature
     * @param hMinusOneTauY the h-1 signature to match against
     * @return the count of the number of bonds that can be made
     */
    public int compatibleTargetBonds(int targetX, int h, String hMinusOneTauY) {
        
        // count the number of (h - 1) target signatures of atoms bonded to x 
        // compatible with the (h - 1) signature of y 
        int n12 = 0;
        for (String subSignature : this.getBondedSignatures(targetX, h - 1)) {
            System.out.println(subSignature 
                    + "\t" + hMinusOneTauY 
                    + "\t" + subSignature.equals(hMinusOneTauY));
            if (hMinusOneTauY.equals(subSignature)) {
                n12++;
            }
        }
        return n12;
    }
    
    public int getCount(int i) {
        return this.counts.get(i);
    }
    
    public void add(String signatureString) {
        this.add(signatureString, 1);
    }
    
    public void add(String signatureString, int count) {
        this.signatures.add(new TargetAtomicSignature(signatureString));
        this.counts.add(count);
    }
    
    public void add(String signatureString, int count, String name) {
        this.signatures.add(new TargetAtomicSignature(signatureString, name));
        this.counts.add(count);
    }

    /**
     * Get the signatures strings of height <code>height</code> 
     * of the atoms bonded to atom <code>x</code>.
     * 
     * @param g
     *            the underlying graph
     * @param x
     *            the atom to use
     * @param height
     *            the height of the signatures to return
     * @return a list of signatures of attached atoms
     */
    public ArrayList<String> getBondedSignatures(int x, int height) {
        return this.signatures.get(x).getSignatureStrings(height);
    }

    public int getHeight() {
        return this.height;
    }
   
    public TargetAtomicSignature getTargetAtomicSignature(int i) {
        return this.signatures.get(i);
    }
    
    public String getTargetAtomicSignature(int i, int h) {
        return this.signatures.get(i).getSubSignature(h);
    }

    public String toString() {
        StringBuffer buffer = new StringBuffer();
        int l = this.counts.size();
        for (int i = 0; i < l - 1; i++) {
            int count = this.counts.get(i);
            TargetAtomicSignature signature = this.signatures.get(i);
            if (count > 1) {
                buffer.append(String.valueOf(count) + signature.toString());
            } else {
                buffer.append(signature.toString());
            }
            buffer.append(" + ");
        }
        int count = this.counts.get(l - 1);
        TargetAtomicSignature signature = this.signatures.get(l - 1);
        if (count > 1) {
            buffer.append(String.valueOf(count) + signature.toString());
        } else {
            buffer.append(signature.toString());
        }
        return buffer.toString();
    }

    public String getTargetAtomicSubSignature(int x, int h) {
        return this.signatures.get(x).getSignatureString(0, h);
    }
}
