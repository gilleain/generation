package deterministic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

import signature.Orbit;
import signature.Signature;
import signature.Util;
import utilities.CanonicalChecker;

public class SimpleGraph {


    /**
     * The actual atom and bond data - may be disconnected fragments.
     */
    private IAtomContainer atomContainer;

    /**
     * The 'orbits' are lists of atoms that are equivalent because they have
     * the same target signature and the same signature in the atom container.
     */
    private List<Orbit> orbits;

    private ArrayList<Integer> unsaturatedAtoms;

    private ArrayList<Boolean> orbitUnsaturatedFlags;

    /**
     * Wrap an atom container in a graph, to manage the fragments
     * 
     * @param atomContainer the underlying atom container
     */
    public SimpleGraph(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
        this.orbits = new ArrayList<Orbit>();
        this.unsaturatedAtoms = new ArrayList<Integer>();
        this.orbitUnsaturatedFlags = new ArrayList<Boolean>();
        this.determineUnsaturated();
        this.determineOrbitUnsaturated();
    }

    /**
     * Copy constructor
     * 
     * @param g the graph to copy
     */
    public SimpleGraph(SimpleGraph g) {
        // For now, clone the whole atom container, to make sure.
        // In theory, it might be possible to just copy over atom references
        // and clone the bonds
        try {
            this.atomContainer = (IAtomContainer) g.atomContainer.clone();
            this.orbits = new ArrayList<Orbit>();
            for (Orbit o : g.orbits) {
                this.orbits.add((Orbit)o.clone());
            }
            this.unsaturatedAtoms = (ArrayList<Integer>) g.unsaturatedAtoms.clone();
            this.orbitUnsaturatedFlags = 
                (ArrayList<Boolean>) g.orbitUnsaturatedFlags.clone();
        } catch (CloneNotSupportedException c) {

        }
    }

    public IAtomContainer getAtomContainer() {
        return this.atomContainer;
    }

    /**
     * Divide the atoms into partitions using both the calculated signatures
     * based on connectivity, and the target signatures. So, two atoms are in
     * the same partition (orbit) if and only if they have both signatures
     * equal. 
     */
    public void partition() {
        Signature signature = new Signature(this.atomContainer);
        this.orbits = signature.calculateOrbits();

        // XXX : fix this
        Collections.reverse(orbits);
    }

    /**
     * Remove the atom index <code>i</code> from the list 
     * of atoms to be saturated.
     * 
     * @param i the atom index (not the index of the atom index!) to remove
     */
    public void removeFromUnsaturatedList(int i) {
        this.unsaturatedAtoms.remove(new Integer(i));
    }

    public void removeFromOrbit(int i) {
        for (Orbit o : this.orbits) {
            if (o.contains(i)) {
                o.remove(i);
                return;
            }
        }
    }

    /**
     * Determine which atoms are unsaturated.
     */
    public void determineUnsaturated() {
        for (IAtom atom : atomContainer.atoms()) {
            try {
                if (Util.isSaturated(atom, atomContainer)) {
                    continue;
                } else {
                    unsaturatedAtoms.add(atomContainer.getAtomNumber(atom));
                }
            } catch (CDKException c) {
                c.printStackTrace();
            }
        }
    }

    /**
     * For each orbit, determine if it is an unsaturated one, or not.
     */
    public void determineOrbitUnsaturated() {
        for (Orbit o : this.orbits) {
            if (o.isEmpty()) continue;
            int i = o.getFirstAtom();
            if (isSaturated(i)) {
                this.orbitUnsaturatedFlags.add(true);
            } else {
                this.orbitUnsaturatedFlags.add(false);
            }
        }
    }

    /**
     * Check this atom for saturation.
     * 
     * @param atomNumber the atom to check
     * @return true if this atom is saturated
     */
    public boolean isSaturated(int atomNumber) {
        IAtom atom = this.atomContainer.getAtom(atomNumber);
        try {
            //                return Util.getInstance().getChecker().isSaturated(
            //                            atom, atomContainer);
            return Util.isSaturated(atom, atomContainer);
        } catch (CDKException c) {
            c.printStackTrace();
            return false;
        }
    }

    /**
     * Check that the graph is connected.
     * 
     * @return true if there is a path from any atom to any other atom
     */
    public boolean isConnected() {
        int numberOfAtoms = atomContainer.getAtomCount();
        int numberOfBonds = atomContainer.getBondCount();

        // n atoms connected into a simple chain have (n - 1) bonds
        return numberOfBonds >= (numberOfAtoms - 1) 
        && ConnectivityChecker.isConnected(atomContainer);
    }

    /**
     * Get the list of atoms to be saturated.
     * 
     * @return a list of atom indices
     */
    public List<Integer> unsaturatedAtoms() {
        //            return this.unsaturatedAtoms;
        List<Integer> unsaturated = new ArrayList<Integer>();
        for (Orbit o : this.orbits) {
            if (o.isEmpty()) continue;
            unsaturated.add(o.getFirstAtom());
        }
        return unsaturated;
    }

    /**
     * Add a bond between these two atoms.
     * 
     * @param x the first atom to be bonded
     * @param y the second atom to be bonded
     */
    public void bond(int x, int y) {
        System.out.println(
                String.format("bonding %d and %d (%s-%s)",
                        x, y, 
                        atomContainer.getAtom(x).getSymbol(),
                        atomContainer.getAtom(y).getSymbol()));
        this.atomContainer.addBond(x, y, IBond.Order.SINGLE);
    }

    /**
     * Get the first unsaturated orbit
     * 
     * @return the orbit (list of atoms) to try and saturate
     */
    public Orbit getUnsaturatedOrbit() {
        for (Orbit o : this.orbits) {
            if (this.unsaturatedAtoms.contains(o.getFirstAtom())) {
                return o;
            }
        }
        return null;
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
     * @return true if this atom is not part of a saturated subgraph
     */
    public boolean noSaturatedSubgraphs(int x) {
       return Util.noSaturatedSubgraphs(x, atomContainer);
    }

    public boolean isCanonical() {
        return CanonicalChecker.isCanonicalComplete(atomContainer);
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (IAtom atom : this.atomContainer.atoms()) {
            sb.append(atom.getSymbol()).append(i);
            i++;
        }
        sb.append(" [ ");
        for (IBond bond : this.atomContainer.bonds()) {
            int l = this.atomContainer.getAtomNumber(bond.getAtom(0));
            int r = this.atomContainer.getAtomNumber(bond.getAtom(1));
            if (l < r) {
                sb.append(l).append("-").append(r).append(" ");
            } else {
                sb.append(r).append("-").append(l).append(" ");
            }
        }
        sb.append("] ");
        for (Orbit o : orbits) {
            sb.append(o.toString()).append(",");
        }
        return sb.toString();
    }



}
