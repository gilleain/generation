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
     * Get the atom numbers of all the atoms in the target
     * @param g
     * @param x
     * @return
     */
    public ArrayList<Integer> getBonded(Graph g, int x) {
        // TODO Auto-generated method stub
        return null;
    }

    public int getHeight() {
        return this.height;
    }

    public TargetAtomicSignature getTargetAtomicSignature(int i) {
        return this.signatures.get(i);
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
}
