package signature;

import java.util.Iterator;
import java.util.NoSuchElementException;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * NOTE This is meant to replace the CDK's existing ACAP...
 * 
 * <p>
 * An atom container atom permutor that uses ranking and unranking to calculate
 * the next permutation in the series.</p>
 * 
 * <p>Typical use is like:<pre>
 * AtomContainerAtomPermutor permutor = new AtomContainerAtomPermutor(container);
 * while (permutor.hasNext()) {
 *   IAtomContainer permutedContainer = permutor.next();
 *   ...
 * }</pre>
 * 
 * @author maclean
 *
 */
public class AtomContainerAtomPermutor 
                extends Permutor implements Iterator<IAtomContainer> {
    
    /**
     * The atom container that is permuted at each step
     */
    private IAtomContainer atomContainer;
    
    /**
     * A permutor wraps the original atom container, and produces cloned
     * (and permuted!) copies on demand.
     * 
     * @param atomContainer
     */
    public AtomContainerAtomPermutor(IAtomContainer atomContainer) {
        super(atomContainer.getAtomCount());
        this.atomContainer = atomContainer;
    }

    public IAtomContainer next() {
        if (this.hasNext()) {
            throw new NoSuchElementException();
        } else {
            return this.containerFromPermutation(this.getNextPermutation());
        }
    }

    public void remove() { }
    
     
    /**
     * Generate the atom container with this permutation of the atoms.
     * 
     * @param p the permutation to use
     * @return the 
     */
    private IAtomContainer containerFromPermutation(int[] p) {
        IAtomContainer permutedContainer = 
            this.atomContainer.getBuilder().newAtomContainer();
        try {
            for (int i = 0; i < p.length; i++) {
                IAtom atom = this.atomContainer.getAtom(p[i]);
                permutedContainer.addAtom((IAtom) atom.clone());
            }
            for (IBond bond : this.atomContainer.bonds()) {
                IBond clonedBond = (IBond) bond.clone();
                clonedBond.setAtoms(new IAtom[clonedBond.getAtomCount()]);
                int i = 0;
                for (IAtom atom : bond.atoms()) {
                    int index = this.atomContainer.getAtomNumber(atom);
                    IAtom permutedAtom = permutedContainer.getAtom(p[index]);
                    clonedBond.setAtom(permutedAtom, i++);
                }
                permutedContainer.addBond(clonedBond);
            }
            
            // TODO : other parts of IAtomContainer
        } catch (CloneNotSupportedException c) {
            
        }
        return permutedContainer;
    }
}
