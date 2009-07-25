package signature;

/**
 * A signature is a path-descriptor of some part (or all) of a molecule. If it
 * spans the whole molecule, it can be used for isomorphism - as two structures
 * only have the same canonical signature iff they are isomorphic.
 * 
 * The basic structure of a molecule (atom symbols, bond orders, and 
 * connectivity) can be reconstructed from a complete signature. Subgraphs or
 * fragments are represented by signatures with heights less than the spanning
 * height.
 * 
 * @author maclean
 *
 */
public interface ISignature {
    
    /**
     * Get the canonical string form of this signature.
     * 
     * @return a canonical string 
     */
    public String getCanonicalSignatureString();

}
