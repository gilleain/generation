package deterministic;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

import signature.Util;
import utilities.CanonicalChecker;

public class SimpleGenerator {
    
    public List<IMolecule> generate(String formula) throws CDKException {
        List<IMolecule> results = new ArrayList<IMolecule>();
        IAtomContainer initialContainer = 
            Util.makeAtomContainerFromFormulaString(formula);
        extend(initialContainer, results);
        return results;
    }
    
    public void extend(IAtomContainer current, List<IMolecule> results) 
        throws CDKException {
        if (current == null) return;
        
        if (isComplete(current)) {
            results.add(new Molecule(current));
        } else {
            int n = current.getAtomCount();
            for (int i = 0; i < n - 1; i++) {
                if (Util.isSaturated(current.getAtom(i), current)) {
                    continue;
                }
                for (int j = i + 1; j < n; j++) {
                    if (canBond(current, i, j)) {
                        IAtomContainer copy = makeCopy(current);
                        copy.addBond(i, j, IBond.Order.SINGLE);
                        if (CanonicalChecker.isCanonical(copy)) {
                            extend(copy, results);
                        }
                    }
                }
            }
        }
    }
    
    public boolean canBond(IAtomContainer container, int i, int j) {
        IAtom aI = container.getAtom(i);
        IAtom aJ = container.getAtom(j);
        if (container.getBond(aI, aJ) != null) return false;
        String aIs =  aI.getSymbol();
        String aJs =  aJ.getSymbol();
        return (aIs.equals("C") && (aJs.equals("C") || aJs.equals("H")))
            || (aIs.equals("H") && aJs.equals("C"));
    }
    
    public IAtomContainer makeCopy(IAtomContainer original) {
        try {
            return (IAtomContainer) original.clone();
        } catch (CloneNotSupportedException c ){
            return null;
        }
    }
    
    public boolean isComplete(IAtomContainer container) {
        return Util.isConnected(container);
    }

}
