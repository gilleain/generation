package signature;

import java.util.Arrays;
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
 * <p>The algorithms used are from the book "Combinatorial Generation : 
 * Algorithms, Generation, and Search" or C.A.G.E.S. by D.L. Kreher and D.R.
 * Stinson</p>
 * 
 * @author maclean
 *
 */
public class AtomContainerAtomPermutor implements Iterator<IAtomContainer> {
    
    /**
     * The atom container that is permuted at each step
     */
    private IAtomContainer atomContainer;
    
    /**
     * The current rank of the permutation to use 
     */
    private int currentRank;
    
    /**
     * The maximum rank possible, given the number of atoms in the container
     */
    private int maxRank;
    
    /**
     * The number of atoms in the atom container, as a convenience 
     */
    private int size;

    public AtomContainerAtomPermutor(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
        this.currentRank = 0;
        this.size = this.atomContainer.getAtomCount();
        this.maxRank = this.calculateMaxRank();
    }

    public boolean hasNext() {
        return this.currentRank < this.maxRank;
    }

    public IAtomContainer next() {
        if (this.currentRank == this.maxRank) {
            throw new NoSuchElementException();
        } else {
            this.currentRank++;
            
            return this.containerFromPermutation(
                    this.unrankPermutationLexicographically(
                          this.currentRank, this.atomContainer.getAtomCount()));
        }
    }

    public void remove() { }
    
    /**
     * Set the permutation to use, given its rank.
     * 
     * @param rank the order of the permutation in the list
     */
    public void setRank(int rank) {
        this.currentRank = rank;
    }
    
    /**
     * Set the currently used permutation.
     * 
     * @param permutation the permutation to use, as an int array
     */
    public void setPermutation(int[] permutation) {
        this.currentRank = this.rankPermutationLexicographically(permutation);
    }
    
    /**
     * Get the permutation that is currently being used.
     * 
     * @return the permutation as an int array
     */
    public int[] getCurrentPermutation() {
        return this.unrankPermutationLexicographically(currentRank, size);
    }
     
    /**
     * Generate the atom container with this permutation of the atoms.
     * 
     * @param p the permutation to use
     * @return the 
     */
    private IAtomContainer containerFromPermutation(int[] p) {
        IAtomContainer permutedContainer = 
            this.atomContainer.getBuilder().newAtomContainer();
        System.out.println(Arrays.toString(p));
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
    
    /**
     * Calculate the max possible rank for permutations of N numbers.
     *  
     * @return the maximum number of permutations
     */
    private int calculateMaxRank() {
        return factorial(size) - 1;
    }
    
    // much much more efficient to pre-calculate this (or lazily calculate)
    // and store in an array, at the cost of memory.
    private int factorial(int i) {
        if (i > 0) {
            return i * factorial(i - 1);
        } else {
            return 1;
        }
    }
    
    /**
     * Convert a permutation (in the form of an int array) into a 'rank' - which
     * is just a single number that is the order of the permutation in a lexico-
     * graphically ordered list.
     * 
     * @param permutation the permutation to use
     * @return the rank as a number
     */
    private int rankPermutationLexicographically(int[] permutation) {
        int rank = 0;
        int n = permutation.length;
        int[] counter = new int[n + 1];
        System.arraycopy(permutation, 0, counter, 1, n);
        for (int j = 1; j <= n; j++) {
            rank = rank + ((counter[j] - 1) * factorial(n - j));
            for (int i = j + 1; i < n; i++) {
                if (counter[i] > counter[j]) {
                    counter[i]--;
                }
            }
        }
        return rank;
    }
    
    /**
     * Performs the opposite to the rank method, producing the permutation that
     * has the order <code>rank</code> in the lexicographically ordered list.
     * 
     * As an implementation note, the algorithm assumes that the permutation is
     * in the form [1,...n] not the more usual [0,...n-1] for a list of size n.
     * This is why there is the final step of 'shifting' the permutation.
     * 
     * @param rank the order of the permutation to generate
     * @param size the length/size of the permutation
     * @return a permutation as an int array
     */
    private int[] unrankPermutationLexicographically(int rank, int size) {
        int[] permutation = new int[size + 1];
        permutation[size] = 1;
        for (int j = 1; j < size; j++) {
            int d = (rank % factorial(j + 1)) / factorial(j);
            rank = rank - d * factorial(j);
            permutation[size - j] = d + 1;
            for (int i = size - j + 1; i <= size; i++) {
                if (permutation[i] > d) {
                    permutation[i]++;
                }
            }
        }
        
        // convert an array of numbers like [1...n] to [0...n-1]
        int[] shiftedPermutation = new int[size];
        for (int i = 1; i < permutation.length; i++) {
            shiftedPermutation[i - 1] = permutation[i] - 1;
        }
        return shiftedPermutation;
    }
}
