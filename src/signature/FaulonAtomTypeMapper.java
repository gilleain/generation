package signature;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;

/**
 * Very simple class to get a Faulon-style string 'type' for an atom. 
 * Needs improvement/better CDK integration - e.g. using AtomTypeFactories.
 * 
 * @author maclean
 *
 */
public class FaulonAtomTypeMapper {
    
    public static String getTypeString(IAtom atom) {
        String symbol = atom.getSymbol();
        if (symbol.equals("C")) {
            if (atom.getFlag(CDKConstants.ISAROMATIC)) {
                return "cp";
            } else {
                return "c_";
            }
        }
        return "!";
    }

}
