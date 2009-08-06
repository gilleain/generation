package signature;

/**
 * General permutation generator, that uses orderly generation by ranking and
 * unranking. The basic idea is that all permutations of length N can be ordered
 * (lexicographically) like:
 * <pre>
 * 0 [0, 1, 2]
 * 1 [0, 2, 1]
 * 2 [1, 0, 2]
 * ...
 * </pre>
 * where the number to the left of each permutation is the <i>rank</i> - really
 * just the index in this ordered list. The list is created on demand, by a
 * process called <i>unranking</i> where the rank is converted to the 
 * permutation that appears at that point in the list.
 * 
 * <p>The algorithms used are from the book "Combinatorial Generation : 
 * Algorithms, Generation, and Search" (or C.A.G.E.S.) by D.L. Kreher and D.R.
 * Stinson</p>
 * 
 * @author maclean
 *
 */
public class Permutor {
    
    /**
     * The current rank of the permutation to use 
     */
    private int currentRank;
    
    /**
     * The maximum rank possible, given the number of atoms in the container
     */
    private int maxRank;
    
    /**
     * The number of objects to permute
     */
    private int size;
    
    /**
     * Create a permutor that will generate permutations of numbers up to
     * <code>size</code>.
     * 
     * @param size the size of the permutations to generate
     */
    public Permutor(int size) {
        this.currentRank = 0;
        this.size = size;
        this.maxRank = this.calculateMaxRank();
    }
    
    public boolean hasNext() {
        return this.currentRank < this.maxRank;
    }
    
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
    
    public int[] getNextPermutation() {
        this.currentRank++;
        return this.getCurrentPermutation();
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
     * Calculate the max possible rank for permutations of N numbers.
     *  
     * @return the maximum number of permutations
     */
    public int calculateMaxRank() {
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