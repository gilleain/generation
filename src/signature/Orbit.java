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
public class Orbit implements Iterable<Integer>, Cloneable {
    
    private List<Integer> atomIndices;
    
    private String signatureString;
    
    private int height;
    
    public Orbit(String signatureString, int height) {
        this.signatureString = signatureString;
        this.atomIndices = new ArrayList<Integer>();
        this.height = height;
    }
    
    public Iterator<Integer> iterator() {
        return this.atomIndices.iterator();
    }
    
    public Object clone() {
        Orbit o = new Orbit(this.signatureString, this.height);
        for (Integer i : this.atomIndices) {
            o.atomIndices.add(new Integer(i));
        }
        return o;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public List<Integer> getAtomIndices() {
        return this.atomIndices;
    }
    
    public void addAtom(int i) {
        this.atomIndices.add(i);
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
        this.atomIndices.remove(this.atomIndices.indexOf(i));
    }
    
    public String toString() {
        return Arrays.deepToString(atomIndices.toArray()) 
                + " " + signatureString;
    }

    public String getSignatureString() {
        return this.signatureString;
    }

}
