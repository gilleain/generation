package signature;

import java.util.ArrayList;
import java.util.List;


/**
 * A list of atom indices, and the signature string
 * 
 * @author maclean
 *
 */
public class Orbit {
    
    private List<Integer> atomIndices;
    
    private String signatureString;
    
    public Orbit(String signatureString) {
        this.signatureString = signatureString;
        this.atomIndices = new ArrayList<Integer>();
    }
    
    public void addAtom(int i) {
        this.atomIndices.add(i);
    }
    
    public boolean hasSignature(AtomicSignature signature) {
        return true;    // TODO
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

}
