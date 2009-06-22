package signature;

import java.io.IOException;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.tools.SaturationChecker;

public class Util {
    
    private static SaturationChecker checker;
    
    private static Util instance;
    
    private Util() {
        try {
            this.checker = new SaturationChecker();
        } catch (ClassNotFoundException cnfe) {
            
        } catch (IOException ioe) {
            
        }
    }
    
    public static Util getInstance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }
    
    public static boolean isSaturated(IAtom atom, IAtomContainer container) 
        throws CDKException {
        // TODO (todo properly, that is)
        if (atom.getSymbol().equals("H") 
                && container.getConnectedBondsCount(atom) == 1) {
            return true;
        }
        
        if (atom.getSymbol().equals("C") 
                && container.getConnectedBondsCount(atom) == 4) {
            return true;
        }
        
        return false;
    }
    
    public static SaturationChecker getChecker() {
        return instance.checker;
    }

}
