package signature;

import java.util.ArrayList;
import java.util.Iterator;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * A directed acyclic graph - each vertex of the graph refers to an atom in a
 * molecule that is passed to the constructor.
 * 
 * @author maclean
 *
 */
public class DAG implements Iterable<ArrayList<Vertex>> {
    
    private IMolecule molecule;
    private ArrayList<ArrayList<Vertex>> layers;

    /**
     * Construct the DAG (directed acyclic graph) rooted at this atom number.
     *
     * @param molecule the molecule to refer to
     * @param atomNumber
     * @param h the height to build it to
     */
    public DAG(IMolecule molecule, int atomNumber, int h) {
        this.molecule = molecule;
        assert atomNumber <= this.molecule.getAtomCount();
        assert h >= 0;
        
        layers = new ArrayList<ArrayList<Vertex>>();
        
        ArrayList<Edge> E = new ArrayList<Edge>();
        Vertex root = new Vertex(atomNumber, "", 1);
        ArrayList<Vertex> rootLayer = new ArrayList<Vertex>();
        rootLayer.add(root);
        layers.add(rootLayer);
        if (h < 1) return;
        
        buildLayer(rootLayer, E, h - 1);
    }
    
    public Iterator<ArrayList<Vertex>> iterator() {
        return this.layers.iterator();
    }

    public Vertex getRoot() {
        return this.layers.get(0).get(0);
    }
    
    public ArrayList<Vertex> get(int l) {
        return this.layers.get(l);
    }
    
    public int size() {
        return this.layers.size();
    }

    /**
     * Build a layer of the DAG
     * 
     * @param N the previous layer
     * @param E the edges seen so far
     * @param h the height to build to
     */
    private void buildLayer(ArrayList<Vertex> N, 
                             ArrayList<Edge> E, 
                             int h) {
        if (h < 0) return;
        ArrayList<Vertex> NN = new ArrayList<Vertex>();
        ArrayList<Edge> layerE = new ArrayList<Edge>();
        for (Vertex n : N) {
            IAtom atom = this.molecule.getAtom(n.atomNumber);
            for (IAtom aa : this.molecule.getConnectedAtomsList(atom)) {
                addVertex(n, this.molecule.getAtomNumber(aa), layerE, E, NN);
            }
        }
        if (NN.size() != 0) {
            layers.add(NN);
        }
        E.addAll(layerE);
        buildLayer(NN, E, h - 1);
    }
    
    /**
     * Add a vertex to this layer.
     * 
     * @param n the parent vertex
     * @param aa the atom number we are adding
     * @param layerE the edges seen in this layer
     * @param E all the edges seen so far (except the ones in this layer)
     * @param NN the new layer
     */
    private void addVertex(
            Vertex n, int aa, ArrayList<Edge> layerE, ArrayList<Edge> E, 
            ArrayList<Vertex> NN) {
        // check to see if this edge (this bond) has been traversed before
        Edge e = new Edge(n.atomNumber, aa);
        if (E.contains(e)) { return; }
        
        // check for an existing vertex referring to this atom
        Vertex v = null;
        for (Vertex x : NN) {
            if (x.atomNumber == aa) {
                v = x;
                break;
            }
        }
        
        // make a new vertex if no existing one is found
        if (v == null) {
            v = new Vertex(aa, "", 1);
            NN.add(v);
        }
        n.children.add(v);
        v.parents.add(n);
        layerE.add(e);
    }

}
