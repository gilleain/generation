package signature;

import java.util.ArrayList;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

public class SignatureEnumerator {
    
    private IAtomContainer atomContainer;
    
    private IChemObjectBuilder builder;
    
    private TargetMolecularSignature hTau;
    
    private ArrayList<Graph> solutions;
    
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
    
    public void saturateOrbitSignature(Orbit o, Graph g, ArrayList<Graph> s) {
        if (o.isEmpty()) {
            s.add(g);
        } else {
            int x = o.getFirstAtom();
            ArrayList<Graph> atomSolutions = new ArrayList<Graph>();
            saturateAtomSignature(x, g, atomSolutions);
            o.remove(x);
            for (Graph h : atomSolutions) {
                saturateOrbitSignature(o, h, s);
            }
        }
    }
    
    public void saturateAtomSignature(int x, Graph g, ArrayList<Graph> s) {
        if (g.isSaturated(x)) {
            return;
        } else {
            for (int y : g.unsaturatedAtoms()) {
                Graph copy = new Graph(g);
                copy.bond(x, y);
                if (compatibleBondSignature(x, y, g) &&
                    compatibleBondSignature(y, x, g) &&
                    g.isCanonical() &&
                    g.noSaturatedSubgraphs()) {
                    saturateAtomSignature(x, g, s);
                }
            }
        }
    }

    public boolean compatibleBondSignature(int x, int y, Graph g) {
        int h = this.hTau.getHeight();
        int n12 = 0;
        AtomicSignature sX = new AtomicSignature(x, g);
        AtomicSignature hMinusOneTauY = new AtomicSignature(y, g, h - 1);
        for (int y1 : this.hTau.getBonded(g, x)) {
            if (sX.getSignature(y1, h - 1) == hMinusOneTauY) {
                n12++;
            }
        }
        int m12 = 0;
        for (int y1 : g.getBonded(x)) {
            AtomicSignature hMinusOneTauY1 = new AtomicSignature(y1, g, h - 1);
            if (hMinusOneTauY.equals(hMinusOneTauY1)) {
                m12++;
            }
        }
        return n12 - m12 > 0;
    }
    
}
