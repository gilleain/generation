package signature;

/**
 * Basically an 'equivalence class' that stores the information about
 * which atom corresponds to which signature; allowing the atoms to
 * be partitioned by signatures.
 *
 * @author maclean
 */
public class OrbitElement implements Comparable<OrbitElement> {
    public int atomNumber; 
    public int orbitIndex;
    public int label;
    public String signatureString;
    
    public OrbitElement(int atomNumber, String signatureString) {
        this.atomNumber = atomNumber;
        this.signatureString = signatureString;
    }

    public int compareTo(OrbitElement o) {
        return -(this.signatureString.compareTo(o.signatureString));
    }
    
}
