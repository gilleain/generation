package signature;

import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * <p>Use this tool to determine if an atom container is the <i>canonical</i>
 * example.</p>
 * 
 * <p>Given a complete set of isomorphic graphs, one of them will be the 
 * canonical example. Different algorithms for determining canonicity may
 * identify different members of the set as canonical. However, any one method
 * should consistently identify the same member of the set each time.</p>
 * 
 * <p>It works by permuting the atoms of the container, and checking to see if
 * this makes a 'certificate' that is lexicographically smaller than the 
 * certificate from the initial order. If no shorter certificate is found,
 * then the atom container is canonical.</p>
 * 
 * @author maclean
 *
 */
public class CanonicalChecker {
    
    /**
     * Check an atom container to see if it is canonical.
     * 
     * @param atomContainer
     * @return
     */
    public static boolean isCanonical(IAtomContainer atomContainer) {
        String initialString = CanonicalChecker.asString(atomContainer);
        Signature signature = new Signature(atomContainer);
        for (Orbit orbit : signature.calculateOrbits()) {
            if (CanonicalChecker.checkOrbit(
                    atomContainer, orbit, initialString)) {
                continue;
            } else {
//                System.out.print(initialString);
                return false;
            }
        }
//        System.out.print(initialString);
        return true;
    }
    
    /**
     * Check an orbit by swapping labels within the orbit, generating a
     * certificate each time and comparing this with the original certificate. 
     * If any permutation is found to produce a smaller certificate, then the
     * method returns false, as the initial ordering cannot be canonical. 
     * 
     * @param orbit essentially just a list of symmetry related atoms 
     * @param initialString the initial certificate string
     * 
     * @return false if any permutation produces a smaller certificate
     */
    private static boolean checkOrbit(
            IAtomContainer atomContainer, Orbit orbit, String initialString) {
        List<Integer> atomIndices = orbit.getAtomIndices();
        
        // to make things easier, create a complete permutation of the vertices
        // which will map non-orbit atoms to themselves
        int[] fullPermutation = new int[atomContainer.getAtomCount()];
        for (int i = 0; i < fullPermutation.length; i++) {
            fullPermutation[i] = i;
        }
//        System.out.println(atomIndices);
        Permutor permutor = new Permutor(atomIndices.size());
        while (permutor.hasNext()) {
            int[] permutation = permutor.getNextPermutation();
            for (int j = 0; j < permutation.length; j++) {
                int k = atomIndices.get(permutation[j]);
                fullPermutation[atomIndices.get(j)] = k;
            }
            String permutedString = 
                CanonicalChecker.asString(atomContainer, fullPermutation);
            if (permutedString.compareTo(initialString) > 0) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
    
    /**
     * Convert the atom container into a certificate - that is, a string that 
     * can be compared lexicographically with strings from permuted copies.
     * 
     * @param container
     * @return
     */
    private static String asString(IAtomContainer container) {
        StringBuffer bondString = new StringBuffer();
        for (IBond bond : container.bonds()) {
            int a1 = container.getAtomNumber(bond.getAtom(0));
            int a2 = container.getAtomNumber(bond.getAtom(1));
            if (a1 < a2) {
                bondString.append(a1).append("-").append(a2);
            } else {
                bondString.append(a2).append("-").append(a1);
            }
            bondString.append(".");
        }
        return bondString.toString(); 
    }
    
    /**
     * Produce a certificate under the given permutation.
     * 
     * @param container the atom container to make the certificate for
     * @param permutation the permutation to alter the container with
     * @return a string 'certificate' that can be checked for minimality
     */
    private static String asString(IAtomContainer container, int[] permutation){
        StringBuffer bondString = new StringBuffer();
        for (IBond bond : container.bonds()) {
            int a1 = permutation[container.getAtomNumber(bond.getAtom(0))];
            int a2 = permutation[container.getAtomNumber(bond.getAtom(1))];
            if (a1 < a2) {
                bondString.append(a1).append("-").append(a2);
            } else {
                bondString.append(a2).append("-").append(a1);
            }
            bondString.append(".");
        }
        return bondString.toString();
    }

}
