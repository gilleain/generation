package signature;

import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;

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
    
    /**
     * Convert or return the molecule corresponding to this signature. If the
     * signature was created from a molecule in the first place, this can just
     * return a reference to that molecule. For signatures read from strings,
     * the molecule has to be built from scratch. It may be a subgraph - that 
     * is to say, a fragment - of a molecule. 
     *  
     * @return an IMolecule
     */
    public IMolecule toMolecule();
    
    /**
     * Construct the molecule using the supplied builder.
     *  
     * @param builder the builder to use
     * @return an IMolecule
     */
    public IMolecule toMolecule(IChemObjectBuilder builder);

}
