package signature;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IIsotope;
import org.openscience.cdk.interfaces.IMolecularFormula;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.tools.manipulator.MolecularFormulaManipulator;

/**
 * A structure generator based on the work of J.L.Faulon, that uses the idea of
 * a 'signature' to represent allowed fragments to connect.
 * 
 * @author maclean
 *
 */
public class SignatureEnumerator {
    
    private IAtomContainer atomContainer;
    
    private IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    private TargetMolecularSignature hTau;
    
    private ArrayList<Graph> solutions = new ArrayList<Graph>();
    
    /**
     * A generator for the whole of an isomer space, defined by the formula.
     * 
     * @param formulaString a molecular formula (like C4H10)
     */
    public SignatureEnumerator(String formulaString) {
        IMolecularFormula formula = 
            MolecularFormulaManipulator.getMolecularFormula(
                    formulaString, this.builder);
        this.makeAtomContainerFromFormula(formula);
        this.hTau = new TargetMolecularSignature(formula);
    }
    
    /**
     * A generator for a formula space, but with restrictions defined by a
     * target molecular signature.
     * 
     * @param formulaString a molecular formula (like C4H10)
     * @param targetSignature a target molecular signature
     */
    public SignatureEnumerator(String formulaString, 
            TargetMolecularSignature targetSignature) {
        this.makeAtomContainerFromFormulaString(formulaString);
        this.hTau = targetSignature;
    }
    
    /**
     * Make the generator from only a formula. 
     * 
     * @param formula the molecular formula to use
     */
    public SignatureEnumerator(IMolecularFormula formula) {
        this(formula, new TargetMolecularSignature(formula));
    }
    
    /**
     * Make the generator from only a formula and the target signature.
     *
     * @param formula the molecular formula
     * @param hTau the target molecular signature
     */
    public SignatureEnumerator(
            IMolecularFormula formula, TargetMolecularSignature hTau) {
        this.makeAtomContainerFromFormula(formula);
        this.hTau = hTau;
    }
    
    private void makeAtomContainerFromFormulaString(String formulaString) {
        IMolecularFormula formula = 
            MolecularFormulaManipulator.getMolecularFormula(
                    formulaString, this.builder);
        this.makeAtomContainerFromFormula(formula);
    }
    
    private void makeAtomContainerFromFormula(IMolecularFormula formula) {
        this.atomContainer = this.builder.newAtomContainer();
        for (IIsotope isotope : formula.isotopes()) {
            for (int i = 0; i < formula.getIsotopeCount(isotope); i++) {
                this.atomContainer.addAtom(this.builder.newAtom(isotope));
                System.out.println("added " + isotope.getSymbol());
            }
        }
    }
    
    public IAtomContainer getInitialContainer() {
        return this.atomContainer;
    }
    
    /**
     * Generate all the solutions compatible with the molecular signature.
     * 
     * @return a list of IAtomContainers 
     */
    public List<IAtomContainer> generateSolutions() {
        Graph initialGraph = new Graph(this.atomContainer);
        initialGraph.assignAtomsToTarget(hTau);
        this.enumerateMoleculeSignature(initialGraph);
        List<IAtomContainer> atomContainers = new ArrayList<IAtomContainer>();
        for (Graph solution : this.solutions) {
            atomContainers.add(solution.getAtomContainer());
        }
        return atomContainers;
    }
    
    /**
     * Connect the atoms of the graph <code>g</code> in all ways compatible 
     * with the TargetMolecularSignature given in the constructor.
     * 
     * @param g the graph to saturate
     */
    public void enumerateMoleculeSignature(Graph g) {
        if (g.isConnected() && g.signatureMatches(this.hTau)) {
            this.solutions.add(g);
        } else {
            g.partition();
            g.determineUnsaturated();
            Orbit o = g.getUnsaturatedOrbit();
            ArrayList<Graph> orbitSolutions = new ArrayList<Graph>();
            saturateOrbitSignature(o, g, orbitSolutions);
            for (Graph h : orbitSolutions) {
                enumerateMoleculeSignature(h);
            }
        }
    }

    /**
     * Saturate all the atoms in a single orbit <code>o</code> (a list of atoms,
     * essentially) by calling saturateAtomSignature for each atom, storing the
     * results in the the list <code>s</code>.
     * 
     * @param o
     *            an orbit (list) of atoms
     * @param g
     *            the graph to saturate in
     * @param s
     *            the list of resulting graphs
     */
    public void saturateOrbitSignature(Orbit o, Graph g, List<Graph> s) {
        System.out.println("saturating orbit " + o);
        if (o.isEmpty()) {
            System.out.println("orbit empty");
            s.add(g);
        } else {
            int x = o.getFirstAtom();
            System.out.println("first atom " + x);
            
            // TODO : should this happen before saturation, or after?!
            o.remove(x); 
            g.removeFromUnsaturatedList(x);
            
            ArrayList<Graph> atomSolutions = new ArrayList<Graph>();
            saturateAtomSignature(x, g, atomSolutions);
            
            for (Graph h : atomSolutions) {
                saturateOrbitSignature(o, h, s);
            }
        }
    }
    
    /**
     * Saturate an atom <code>x</code> in the graph <code>g</code> and add all
     * the resulting graphs to the list <code>s</code>.
     * 
     * @param x the index of an atom
     * @param g the graph to use
     * @param s the list of resulting graphs
     */
    public void saturateAtomSignature(int x, Graph g, List<Graph> s) {
        System.out.println("saturating atom " + x + " in " + g);
        if (g.isSaturated(x)) {
            System.out.println(x + " is already saturated");
            s.add(g);
            return;
        } else {
            System.out.println("trying all of " + g.unsaturatedAtoms());
            for (int y : g.unsaturatedAtoms()) {
                Graph copy = new Graph(g);
                
                if (check(copy, x, y)) {
                    System.out.println("passed all tests");
                    if (copy.isSaturated(y)) {
                        System.out.println("removing from unsaturated list");
                        copy.removeFromUnsaturatedList(y);
                    }
                    saturateAtomSignature(x, copy, s);
                }
            }
        }
    }
    
    public boolean check(Graph copy, int x, int y) {
        boolean xy = copy.compatibleBond(x, y, hTau);
        if (!xy) {
            System.out.println("!xy");
//            continue;
        }
        boolean yx = copy.compatibleBond(y, x, hTau);
        if (!yx) {
            System.out.println("!yx");
//            continue;
        }
        
        copy.bond(x, y);
        boolean noSubgraphs = copy.noSaturatedSubgraphs(x);
        if (!noSubgraphs) {
            System.out.println("saturated subgraphs");
            return false;
        }
        boolean canon = copy.isCanonical();
        if (!canon) {
            System.out.println("!canon");
//            continue;
        }
        return true;
    }
}
