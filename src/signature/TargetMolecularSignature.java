package signature;

import java.util.ArrayList;

import org.openscience.cdk.interfaces.IMolecule;

public class TargetMolecularSignature {
    
    private ArrayList<TargetAtomicSignature> signatures;
    
    private ArrayList<Integer> counts;
    
    private int height;
    
    private int[][] lookupTable;

    public TargetMolecularSignature(ArrayList<String> signatureStrings, 
                                    ArrayList<Integer> counts,
                                    int height) {
        this.signatures = new ArrayList<TargetAtomicSignature>();
        for (String signatureString : signatureStrings) {
            this.signatures.add(new TargetAtomicSignature(signatureString));
        }
        this.lookupTable = createLookupTable();
        this.counts = counts;
        this.height = height;
    }
    
    public TargetMolecularSignature(int height) {
        this.signatures = new ArrayList<TargetAtomicSignature>();
        this.counts = new ArrayList<Integer>();
        this.height = height;
    }
    
    public int size() {
        return this.signatures.size();
    }
    
    /**
     * Do an all-v-all comparison of the signatures that make up this molecular
     * signature so that they are not re-computed each time.
     * 
     * @return a square N*N array of bond compatibility counts
     */
    private int[][] createLookupTable() {
        // allocate space
        int n = this.signatures.size();
        int[][] table = new int[n][];
        for (int i = 0; i < n; i++) { 
            table[i] = new int[n]; 
        }

        // first, generate and store the reconstructed fragments
        ArrayList<IMolecule> molecules = new ArrayList<IMolecule>();
        for (TargetAtomicSignature signature : this.signatures) {
            molecules.add(signature.toMolecule());
        }
        
        // now, use these to do an all-v-all comparison, and store the results
        for (int i = 0; i < n; i++) {
            TargetAtomicSignature signatureA = this.signatures.get(i);
            IMolecule moleculeA = molecules.get(i);
            for (int j = 0; j < n; j++) {
                TargetAtomicSignature signatureB = this.signatures.get(j);
                IMolecule moleculeB = molecules.get(j);
//                System.out.println("A-B " + i + " " + j + " " + signatureA.getHeight());
//                System.out.println(signatureA.toString());
                int cAB = compatibleCount(moleculeA, signatureB);
//                System.out.println("B-A " + i + " " + j + " " + signatureB.getHeight());
//                System.out.println(signatureB.toString());
                int cBA = compatibleCount(moleculeB, signatureA);
                table[i][j] = cAB;
                table[j][i] = cBA;
            }
        }
        return table;
    }
    
    private int compatibleCount(IMolecule molecule, TargetAtomicSignature target) {
        Signature sigFromMolecule = new Signature(molecule);
        int height = target.getHeight() - 1;
        String a = sigFromMolecule.forAtom(0, height);
        int count = 0;
        for (String b : target.getSignatureStringsFromRootChildren(height)) {
//            System.out.println(a);
//            System.out.println(b);
//            System.out.println("---------------------------------");
            if (a.equals(b)) count++;
        }
        return count;
    }
    
    /**
     * Lookup the number of compatible bonds between target atomic signatures.
     * 
     * @param i the index of one signature
     * @param j the index of the other signature
     * @return the count of the number of bonds that can be made
     */
    public int compatibleTargetBonds(int i, int j) {
        if (this.lookupTable == null) {
            this.lookupTable = this.createLookupTable();
        }
        return this.lookupTable[i][j];
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
        return this.signatures.get(x).getSignatureStringsFromRootChildren(height);
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
