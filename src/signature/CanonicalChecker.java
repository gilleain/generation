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
 * identify different members of the set as canonical.</p>
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
                return false;
            }
        }
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
        for (int i = 0; i < atomIndices.size(); i++) {
            for (int j = i + 1; j < atomIndices.size(); j++) {
                if (checkElement(i, j, atomContainer, initialString)) {
                    continue;
                } else {
                    return false;
                }
            }
        }
        return true;
    }
    
    /**
     * Swap the atoms at <code>i</code> and <code>j</code> and check to see if
     * this produces a certificate that is lexicographically smaller than
     * the original.
     * 
     * @param i the index of the first atom
     * @param j the index of the second atom
     * @param atomContainer the container we are checking
     * @param initialString the initial certificate string
     * 
     * @return false if a shorter certificate is found
     */
    private static boolean checkElement(
            int i, int j, IAtomContainer atomContainer, String initialString) {
        
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

}