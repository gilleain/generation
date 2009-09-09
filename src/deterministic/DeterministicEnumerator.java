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

import signature.Graph;
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
        Graph initialGraph = new Graph(this.makeAtomContainerFromFormula());
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
        this.enumerate(new Graph(this.makeAtomContainerFromFormula()));
        return results;
    }
    
    private void enumerate(Graph g) {
        if (g.isConnected()) {
            System.out.println("ADDING " + g + " is canon "+ g.isCanonical());
            this.handler.handle(g.getAtomContainer());
        } else {
            g.partition();
            Orbit o = g.getUnsaturatedOrbit();
            if (o == null) return;
            
            for (Graph h : saturateOrbit(o, g)) {
                enumerate(h);
            }
        }
    }
    
    private List<Graph> saturateOrbit(Orbit o, Graph g) {
        ArrayList<Graph> orbitSolutions = new ArrayList<Graph>();
        saturateOrbit(o, g, orbitSolutions);
        return orbitSolutions;
    }
    
    private void saturateOrbit(Orbit o, Graph g, ArrayList<Graph> s) {
        if (o == null || o.isEmpty()) {
            System.out.println("orbit empty");
            s.add(g);
        } else {
            int x = o.getFirstAtom();
            System.out.println("first atom " + x);
            
            // TODO : should this happen before saturation, or after?!
            o.remove(x); 
            g.removeFromUnsaturatedList(x);
            
            for (Graph h : saturateAtom(x, g)) {
                saturateOrbit(o, h, s);
            }
        }
    }
    
    private List<Graph> saturateAtom(int x, Graph g) {
        ArrayList<Graph> atomSolutions = new ArrayList<Graph>();
        saturateAtom(x, g, atomSolutions);
        return atomSolutions;
    }
    
    private void saturateAtom(int x, Graph g, List<Graph> s) {
        System.out.println("saturating atom " + x + " in " + g);
        if (g.isSaturated(x)) {
            System.out.println(x + " is already saturated");
            s.add(g);
            return;
        } else {
            System.out.println("trying all of " + g.unsaturatedAtoms());
            for (int y : g.unsaturatedAtoms()) {
                Graph copy = new Graph(g);
                copy.bond(x, y);
                
                if (check(copy, x, y)) {
                    System.out.println("passed all tests");
                    if (copy.isSaturated(y)) {
                        System.out.println("removing from unsaturated list");
                        copy.removeFromUnsaturatedList(y);
                        copy.removeFromOrbit(y);
                    }
                    saturateAtom(x, copy, s);
                }
            }
        }
    }
    
    private boolean check(Graph copy, int x, int y) {
        boolean noSubgraphs = copy.noSaturatedSubgraphs(x);
        if (!noSubgraphs) {
            System.out.println("saturated subgraphs");
            return false;
        }
        boolean canon = copy.isCanonical();
        if (!canon) {
            System.out.println("!canon");
//            continue;
            return false;
        }
        return true;
    }
    
}
