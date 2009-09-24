package deterministic;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

import signature.Orbit;

/**
 * A structure enumerator that starts from just the elemental formula, and 
 * creates all possible structures.
 *  
 * @author maclean
 *
 */
public class DeterministicEnumerator {
    
    /**
     * Convenience instance of a builder
     */
    private IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    /**
     * The formula that the enumerator was created with
     */
    private IMolecularFormula formula;
    
    /**
     * A class that handles the results that are created
     */
    private EnumeratorResultHandler handler;
    
    /**
     * Listener for debugging/visualisation purposes.
     */
    private BondCreationListener bondCreationListener;
    
    /**
     * Start from just the formula string.
     * 
     * @param formulaString a formula string like "C4H8"
     */
    public DeterministicEnumerator(String formulaString) {
        this.formula = 
            MolecularFormulaManipulator.getMolecularFormula(
                    formulaString, this.builder);
        
        this.handler = new DefaultEnumeratorResultHandler();
    }
    
    public void setBondCreationListener(BondCreationListener listener) {
        this.bondCreationListener = listener;
    }
    
    /**
     * Set the result handler. 
     * 
     * @param handler
     */
    public void setHandler(EnumeratorResultHandler handler) {
        this.handler = handler;
    }
    
    private IAtomContainer makeAtomContainerFromFormula() {
        IAtomContainer atomContainer = this.builder.newAtomContainer();
        
        ArrayList<IAtom> atoms = new ArrayList<IAtom>();
        for (IIsotope isotope : formula.isotopes()) {
            for (int i = 0; i < formula.getIsotopeCount(isotope); i++) {
                atoms.add(this.builder.newAtom(isotope));
                System.out.println("added " + isotope.getSymbol());
            }
        }
        
        // sort by symbol lexicographic order
        Collections.sort(atoms, new Comparator<IAtom>() {

            public int compare(IAtom o1, IAtom o2) {
                return o1.getSymbol().compareTo(o2.getSymbol());
            }
            
        });
        atomContainer.setAtoms(atoms.toArray(new IAtom[]{}));
        return atomContainer;
    }
    
    /**
     * Create the structures, passing each one to the result handler.
     */
    public void generateToHandler() {
        SimpleGraph initialGraph = new SimpleGraph(
                this.makeAtomContainerFromFormula());
        this.enumerate(initialGraph);
    }
    
    /**
     * Generate the structures, and return them in a list.
     * 
     * @return a list of atom containers
     */
    public List<IAtomContainer> generate() {
        final List<IAtomContainer> results = new ArrayList<IAtomContainer>();
        this.handler = new EnumeratorResultHandler() {
            public void handle(IAtomContainer result) {
                results.add(result);
            }
        };
        this.enumerate(new SimpleGraph(this.makeAtomContainerFromFormula()));
        return results;
    }
    
    private void enumerate(SimpleGraph g) {
        if (g.isConnected()) {
            this.handler.handle(g.getAtomContainer());
        } else {
            Orbit o = g.getUnsaturatedOrbit();
            if (o == null) return;
            
            for (SimpleGraph h : saturateOrbit(o, g)) {
                enumerate(h);
            }
        }
    }
    
    private List<SimpleGraph> saturateOrbit(Orbit o, SimpleGraph g) {
        ArrayList<SimpleGraph> orbitSolutions = new ArrayList<SimpleGraph>();
        saturateOrbit(o, g, orbitSolutions);
        return orbitSolutions;
    }
    
    private void saturateOrbit(Orbit o, SimpleGraph g, ArrayList<SimpleGraph> s) {
        System.out.println("Saturating orbit : " + o);
        if (o == null || o.isEmpty()) {
            System.out.println("orbit empty");
            s.add(g);
        } else {
            int x = o.getFirstAtom();
            o.remove(x);
            System.out.println("first atom " + x);
            
            for (SimpleGraph h : saturateAtom(x, g)) {
                saturateOrbit(o, h, s);
            }
        }
    }
    
    private List<SimpleGraph> saturateAtom(int x, SimpleGraph g) {
        ArrayList<SimpleGraph> atomSolutions = new ArrayList<SimpleGraph>();
        saturateAtom(x, g, atomSolutions);
        return atomSolutions;
    }
    
    private void saturateAtom(int x, SimpleGraph g, List<SimpleGraph> s) {
        System.out.println("saturating atom " + x + " in " + g);
        if (g.isSaturated(x)) {
            System.out.println(x + " is already saturated");
            s.add(g);
            return;
        } else {
            List<Integer> unsaturatedAtoms = g.unsaturatedAtoms();
            System.out.println("trying all of " + unsaturatedAtoms);
            for (int y : unsaturatedAtoms) {
                if (x == y) continue;
                SimpleGraph copy = new SimpleGraph(g);
                copy.bond(x, y);
                
                if (copy.check(x, y)) {
                    System.out.println("passed all tests");
                    if (this.bondCreationListener != null) {
                        BondCreationEvent b = new BondCreationEvent();
                        b.parent = g;
                        b.child = copy;
                        this.bondCreationListener.bondAdded(b);
                    }
                    saturateAtom(x, copy, s);
                }
            }
        }
    }
}
