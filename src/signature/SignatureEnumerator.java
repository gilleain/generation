package signature;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

/**
 * A structure generator based on the work of J.L.Faulon, that uses the idea of
 * a 'signature' to represent allowed fragments to connect.
 * 
 * @author maclean
 *
 */
public class SignatureEnumerator {
    
    private IAtomContainer atomContainer;
    
    private IChemObjectBuilder builder;
    
    private TargetMolecularSignature hTau;
    
    private ArrayList<Graph> solutions;
    
    /**
     * Give the generator only the chemical element symbols, and the count of
     * each, as well as a target signature.
     * 
     * The number of elements, counts, and atomic signatures must match - in 
     * other words, there must be a count and atomic signature for each element
     * symbol. Furthermore, the counts of atomic signatures must match those for
     * the elements. Precisely, if there are N elements of type X, there must be
     * N atomic signatures with a root of type X - even if these atomic 
     * signatures are different. 
     * 
     * @param elements a set of element string symbols like ['C', 'H']
     * @param counts the count of each of the element symbols
     * @param hTau the target molecular signature
     */
    public SignatureEnumerator(ArrayList<String> elements, 
                               ArrayList<Integer> counts, 
                               TargetMolecularSignature hTau) {
        this.builder = NoNotificationChemObjectBuilder.getInstance();
        this.atomContainer = this.builder.newAtomContainer();
        for (int i = 0; i < elements.size(); i++) {
            String element = elements.get(i);
            for (int j = 0; j < counts.get(i); j++) {
                this.atomContainer.addAtom(this.builder.newAtom(element));
            }
        }
        this.hTau = hTau;
        this.solutions = new ArrayList<Graph>();
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
            Orbit o = g.getUnsaturatedOrbit();
            ArrayList<Graph> orbitSolutions = new ArrayList<Graph>();
            saturateOrbitSignature(o, g, orbitSolutions);
            for (Graph h : orbitSolutions) {
                enumerateMoleculeSignature(h);
            }
        }
    }
    
    /**
     * Saturate all the atoms in a single orbit (a list of atoms, essentially)
     * <code>o</code> by calling saturateAtomSignature for each atom, storing
     * the results in the the list <code>s</code>.
     * 
     * @param o an orbit (list) of atoms
     * @param g the graph to saturate in
     * @param s the list of resulting graphs
     */
    public void saturateOrbitSignature(Orbit o, Graph g, List<Graph> s) {
        if (o.isEmpty()) {
            s.add(g);
        } else {
            int x = o.getFirstAtom();
            
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
        if (g.isSaturated(x)) {
            return;
        } else {
            for (int y : g.unsaturatedAtoms()) {
                Graph copy = new Graph(g);
                copy.bond(x, y);
                boolean xy = copy.compatibleBondSignature(x, y, hTau);
                boolean yx = copy.compatibleBondSignature(y, x, hTau);
                boolean canon = copy.isCanonical();
                boolean noSubgraphs = copy.noSaturatedSubgraphs(x);
                
                if (xy && yx && canon && noSubgraphs) {
                    if (copy.isSaturated(y)) {
                        copy.removeFromUnsaturatedList(y);
                    }
                    saturateAtomSignature(x, copy, s);
                }
            }
        }
    }
}
