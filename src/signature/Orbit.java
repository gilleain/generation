package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * A list of atom indices, and the signature string
 * 
 * @author maclean
 *
 */
public class Orbit implements Iterable<Integer> {
    
    private List<Integer> atomIndices;
    
    private String signatureString;
    
    public Orbit(String signatureString) {
        this.signatureString = signatureString;
        this.atomIndices = new ArrayList<Integer>();
    }
    
    public Iterator<Integer> iterator() {
        return this.atomIndices.iterator();
    }
    
    public List<Integer> getAtomIndices() {
        return this.atomIndices;
    }
    
    public void addAtom(int i) {
        this.atomIndices.add(i);
    }
    
    public boolean hasSignature(AtomicSignature signature) {
        return this.signatureString.equals(signature.toString());
    }
    
    public boolean hasSignatureString(String otherSignatureString) {
        return this.signatureString.equals(otherSignatureString);
    }
    
    public boolean isEmpty() {
        return this.atomIndices.isEmpty();
    }

    public int getFirstAtom() {
        return this.atomIndices.get(0);
    }
    
    public void remove(int i) {
        this.atomIndices.remove(i);
    }
    
    public String toString() {
        return Arrays.deepToString(atomIndices.toArray());
    }

}
