package signature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
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
    
    public static boolean noSaturatedSubgraphs(
            int atomNumber, IAtomContainer container) {
        IMolecule subGraph = 
            NoNotificationChemObjectBuilder.getInstance().newMolecule();
        List<IAtom> sphere = new ArrayList<IAtom>(); 
        IAtom atomX = container.getAtom(atomNumber);
        sphere.add(atomX);
        atomX.setFlag(CDKConstants.VISITED, true);
        PathTools.breadthFirstSearch(container, sphere, subGraph);
        int saturationCount = 0;
        for (IAtom atom : subGraph.atoms()) {
            atom.setFlag(CDKConstants.VISITED, false);
            try {
                if (Util.isSaturated(atom, subGraph)) {
                    saturationCount++;
                }
            } catch (CDKException c) {
                c.printStackTrace();
            }
        }
        int atomCount = subGraph.getAtomCount();

        return saturationCount < atomCount || 
               atomCount == container.getAtomCount();
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
