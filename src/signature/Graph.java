package signature;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.graph.PathTools;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * The graph maintains its underlying atom container as well as a list of the
 * atoms that can be saturated and a mapping of atoms to target atomic 
 * signatures.
 * 
 * @author maclean
 *
 */
public class Graph {
    
    /**
     * The actual atom and bond data - may be disconnected fragments.
     */
    private IAtomContainer atomContainer;
    
    /**
     * These are the indices of the TargetAtomicSignatures that have
     * been assigned to each atom.
     */
    private ArrayList<Integer> targets;
    
    /**
     * The 'orbits' are lists of atoms that are equivalent because they have
     * the same target signature and the same signature in the atom container.
     */
    private ArrayList<Orbit> orbits;
    
    public Graph(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
        this.targets = new ArrayList<Integer>();
        this.orbits = new ArrayList<Orbit>();
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
     * Check two atoms to see if a bond can be formed between them, according
     * to the target signatures.
     * 
     * @param x the index of an atom
     * @param y the index of another atom
     * @param hTau the target molecular signature to use
     * @return true if a bond can be formed
     */
    public boolean compatibleBondSignature(
            int x, int y, TargetMolecularSignature hTau) {
        int h = hTau.getHeight();
        String hMinusOneTauY = hTau.getTargetAtomicSubSignature(y, h - 1);
        
        // count the number of (h - 1) target signatures of atoms bonded to x 
        // compatible with the (h - 1) signature of y 
        int n12 = 0;
        for (String subSignature : hTau.getBondedSignatures(x, h - 1)) {
            if (hMinusOneTauY.equals(subSignature)) {
                n12++;
            }
        }
        if (n12 == 0) return false;
        
        // count the number of bonds already used between x and y
        int m12 = 0;
        for (String hMinusOneTauY1 : getSignaturesOfBondedAtoms(x, h - 1)) {
            if (hMinusOneTauY.equals(hMinusOneTauY1)) {
                m12++;
            }
        }
        return n12 - m12 >= 0;
    }
    
    public IAtomContainer getAtomContainer() {
        return this.atomContainer;
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

    public boolean isSaturated(int atomNumber) {
        IAtom atom = this.atomContainer.getAtom(atomNumber);
        try {
            return 
                Util.getInstance().getChecker().isSaturated(
                        atom, atomContainer);
        } catch (CDKException c) {
            return false;
        }
    }
    
    public boolean isConnected() {
        int numberOfAtoms = atomContainer.getAtomCount();
        int numberOfBonds = atomContainer.getBondCount();
        
        // n atoms connected into a simple chain have (n - 1) bonds
        return numberOfBonds >= (numberOfAtoms - 1) 
                && ConnectivityChecker.isConnected(atomContainer);
    }
    
    public ArrayList<Integer> unsaturatedAtoms() {
        return this.targets;
    }

    public void bond(int x, int y) {
        this.atomContainer.addBond(x, y, IBond.Order.SINGLE);
    }

    public List<String> getSignaturesOfBondedAtoms(int x, int h) {
        IAtom atom = atomContainer.getAtom(x);
        List<String> signatures = new ArrayList<String>();
        for (IAtom connected : atomContainer.getConnectedAtomsList(atom)) {
            int atomNumber = atomContainer.getAtomNumber(connected);
            AtomicSignature signature = new AtomicSignature(atomNumber, this, h);
            signatures.add(signature.toString());
        }
        return signatures;
    }
    
    public List<Orbit> getOrbits() {
        return this.orbits;
    }
    
    public int getDiameter() {
        return PathTools.getMolecularGraphDiameter(atomContainer);
    }

    /**
     * Divide the atoms into partitions using signatures.
     */
    public void partition() {
        for (int i = 0; i < atomContainer.getAtomCount(); i++) {
            Orbit o = getOrbitForAtom(i);
            o.addAtom(i);
        }
    }
    
    /**
     * Search the existing orbits for matches
     * 
     * @param atomNumber
     * @return
     */
    public Orbit getOrbitForAtom(int atomNumber) {
        AtomicSignature signature = new AtomicSignature(atomNumber, this);
        for (Orbit orbit : orbits) {
            if (orbit.hasSignature(signature)) {
                return orbit;
            }
        }
        return null;
    }

    /**
     * Get the first unsaturated orbit
     * 
     * @return the orbit (list of atoms) to try and saturate
     */
    public Orbit getUnsaturatedOrbit() {
        return this.orbits.get(0);
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
    
}
