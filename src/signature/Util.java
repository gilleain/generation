package signature;

import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
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
    
    /**
     * Check for saturated subgraphs, using only the connected component that
     * contains the atom x. The reasoning is: if the atom x has just been
     * bonded to another atom, it is the only one that can have contributed
     * to a saturated subgraph.
     * 
     * The alternative is that the most recent bond made a complete (connected)
     * and saturated graph - that is, a solution. In that case, the method also
     * returns true, as this is not really a saturated 'sub' graph.
     * 
     * @param x an atom index
     * @param container IAtomContainer
     * @return true if this atom is not part of a saturated subgraph
     */
    public static boolean saturatedSubgraph(
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

        return saturationCount == atomCount && 
                atomCount < container.getAtomCount();
    }
    
    public static boolean isSaturated(IAtom atom, IAtomContainer container) 
        throws CDKException {
        // TODO (todo properly, that is)
        int totalOrder = 0;
        for (IBond bond : container.getConnectedBondsList(atom)) {
            totalOrder += bond.getOrder().ordinal() + 1;
        }
        if (atom.getSymbol().equals("H") && totalOrder >= 1) {
            return true;
        }
        
        if (atom.getSymbol().equals("C") && totalOrder >= 4) {
            return true;
        }
        
        return false;
    }
    
    public static SaturationChecker getChecker() {
        return instance.checker;
    }

}
