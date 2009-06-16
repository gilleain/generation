package signature;

import java.util.ArrayList;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;

public class Graph {
    
    private IAtomContainer atomContainer;
    
    private ArrayList<Integer> targets;
    
    public Graph(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
        this.targets = new ArrayList<Integer>();
    }
    
    public Graph(Graph g) {
        // For now, clone the whole atom container, to make sure.
        // In theory, it might be possible to just copy over atom references
        // and clone the bonds
        try {
            this.atomContainer = (IAtomContainer) g.atomContainer.clone();
            this.targets = (ArrayList<Integer>) g.targets.clone();
        } catch (CloneNotSupportedException c) {
            
        }
    }
    
    /**
     * Given a target molecular signature composed of target atomic signatures,
     * assign an atomic signature to each atom of the atom container.
     * 
     * @param signature the target molecular signature
     */
    public void assignAtomsToTarget(TargetMolecularSignature signature) {
        int currentTarget = 0;
        int currentCount = signature.getCount(0);
        for (int i = 0; i < this.atomContainer.getAtomCount(); i++) {
            if (currentCount > 0) {
                currentCount -= 1;
                this.targets.add(currentTarget);
            } else {
                currentTarget += 1;
                currentCount = signature.getCount(currentTarget) - 1;
                this.targets.add(currentTarget);
            }
        }
    }


    public void partition() {
        // TODO
    }
    
    public Orbit getUnsaturatedOrbit() {
        // TODO
        return null;
    }
    
    public boolean isSaturated(int atomNumber) {
        // TODO Auto-generated method stub
        return false;
    }
    
    public boolean isConnected() {
        return ConnectivityChecker.isConnected(atomContainer);
    }
    
    public ArrayList<Integer> unsaturatedAtoms() {
        // TODO Auto-generated method stub
        return null;
    }

    public void bond(int x, int y) {
        // TODO Auto-generated method stub
    }

    public boolean noSaturatedSubgraphs() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean isCanonical() {
        // TODO Auto-generated method stub
        return false;
    }

    public boolean signatureMatches(TargetMolecularSignature tau) {
        // TODO Auto-generated method stub
        return false;
    }

    public ArrayList<Integer> getBonded(int x) {
        // TODO Auto-generated method stub
        return null;
    }
    
}
