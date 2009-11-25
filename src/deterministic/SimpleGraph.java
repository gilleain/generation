package deterministic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
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
     * Wrap an atom container in a graph, to manage the fragments
     * 
     * @param atomContainer the underlying atom container
     */
    public SimpleGraph(IAtomContainer atomContainer) {
        this.atomContainer = atomContainer;
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
           
        } catch (CloneNotSupportedException c) {

        }
    }
    
    public IAtomContainer getAtomContainer() {
        return this.atomContainer;
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

    public boolean check(int x, int y) {
        boolean sSubgraphs = Util.saturatedSubgraph(x, atomContainer);
        if (sSubgraphs) {
            System.out.println("saturated subgraphs");
            return false;
        }
        boolean canon = CanonicalChecker.isCanonicalComplete(atomContainer);
//        boolean canon = CanonicalChecker.isCanonicalTotal(atomContainer);
        if (!canon) {
            System.out.println("!canon");
            //            continue;
            return false;
        }
        return true;
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
    
    public Orbit getUnsaturatedOrbit() {
        Signature signature = new Signature(this.atomContainer);
        List<Orbit> orbits = signature.calculateOrbits();
//        Collections.reverse(orbits);
        sort(orbits);
        for (Orbit o : orbits) {
            if (isSaturated(o)) {
                continue;
            } else {
                return o;
            }
        }
        return null;
    }
    
    private void sort(List<Orbit> orbits) {
        for (Orbit o : orbits) {
            o.sort();
        }
        Collections.sort(orbits, new Comparator<Orbit>() {

            public int compare(Orbit o1, Orbit o2) {
                return new Integer(o1.getFirstAtom()).compareTo(
                        new Integer(o2.getFirstAtom()));
            }
            
        });
    }

    /**
     * Get the list of atoms to be saturated.
     * 
     * @return a list of atom indices
     */
    public List<Integer> unsaturatedAtoms() {
        Signature signature = new Signature(this.atomContainer);
        List<Orbit> orbits = signature.calculateOrbits();

        // XXX : fix this
//        Collections.reverse(orbits);
        sort(orbits);
        
        System.out.println("Orbits : " + orbits);
        List<Integer> unsaturated = new ArrayList<Integer>();
        for (Orbit o : orbits) {
            if (o.isEmpty() || isSaturated(o)) continue;
            unsaturated.add(o.getFirstAtom());
        }
        return unsaturated;
    }
    

    
    private boolean isSaturated(Orbit o) {
        return this.isSaturated(o.getFirstAtom());
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
        IAtom a = atomContainer.getAtom(x);
        IAtom b = atomContainer.getAtom(y);
        IBond existingBond = this.atomContainer.getBond(a, b);
        if (existingBond != null) {
            IBond.Order o = existingBond.getOrder(); 
            if (o == IBond.Order.SINGLE) {
                existingBond.setOrder(IBond.Order.DOUBLE);
            } else if (o == IBond.Order.DOUBLE) {
                existingBond.setOrder(IBond.Order.TRIPLE);
            }
        } else {
            atomContainer.addBond(x, y, IBond.Order.SINGLE);
        }
    }

    public String toString() {
        StringBuffer sb = new StringBuffer();
        int i = 0;
        for (IAtom atom : this.atomContainer.atoms()) {
            sb.append(atom.getSymbol()).append(i);
            i++;
        }
        sb.append(" { ");
        for (IBond bond : this.atomContainer.bonds()) {
            int l = this.atomContainer.getAtomNumber(bond.getAtom(0));
            int r = this.atomContainer.getAtomNumber(bond.getAtom(1));
            if (l < r) {
                sb.append(l).append("-").append(r);
            } else {
                sb.append(r).append("-").append(l);
            }
            int o = bond.getOrder().ordinal() + 1;
            sb.append("(").append(o).append(") ");
        }
        sb.append("} ");
//        for (Orbit o : orbits) {
//            sb.append(o.toString()).append(",");
//        }
        return sb.toString();
    }

    public boolean isFullySaturated() {
        for (int i = 0; i < this.atomContainer.getAtomCount(); i++) {
            if (isSaturated(i)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }



}
