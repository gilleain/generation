package engine;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * This is an attempt at implementing the full canonizable signature of the 
 * fourth paper in Faulon's signature series.
 * 
 * At the moment, canonization does not fully work.
 * 
 * @author maclean
 *
 */
public class Signature {
    
    private class Edge {
        public TreeNode a;
        public TreeNode b;
        
        public Edge(TreeNode a, TreeNode b) {
            this.a = a;
            this.b = b;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Edge) {
                Edge other = (Edge) o;
                return (   this.a.atomNumber == other.a.atomNumber
                        && this.b.atomNumber == other.b.atomNumber)
                        ||
                        (  this.b.atomNumber == other.a.atomNumber
                        && this.a.atomNumber == other.b.atomNumber);
            } else {
                return false;
            }
        }
        
        public String toString() {
            return this.a + "-" + this.b; 
        }
    }
    
    private class TreeNode {
        public int invariant;
        public int color;
        public int atomNumber;
        public ArrayList<TreeNode> parents;
        public ArrayList<TreeNode> children;
        
        public TreeNode(int atomNumber) {
            this.atomNumber = atomNumber;
            this.parents = new ArrayList<TreeNode>();
            this.children = new ArrayList<TreeNode>();
            this.color = 0;
        }
        
        public String toString() {
            return "" + this.atomNumber;
        }
    }
    
    private TreeNode root;
    
    private ArrayList<ArrayList<TreeNode>> layers;
    
    private IMolecule molecule;
    
    public Signature(int atomNumber, IMolecule molecule) {
        this(molecule.getAtom(atomNumber), molecule);
    }
    
    public Signature(IAtom atom, IMolecule molecule) {
        this.molecule = molecule;
        this.root = new TreeNode(molecule.getAtomNumber(atom));
        
        // create the layers, with a single root layer
        this.layers = new ArrayList<ArrayList<TreeNode>>();
        ArrayList<TreeNode> rootLayer = new ArrayList<TreeNode>();
        rootLayer.add(root);
        this.layers.add(rootLayer);
        
        this.makeNextLayer(rootLayer, new ArrayList<Edge>());
        this.calculateInitialInvariants();
    }
    
    private void makeNextLayer(ArrayList<TreeNode> layer, List<Edge> edges) {
        ArrayList<TreeNode> nextLayer = new ArrayList<TreeNode>();
        
        /*
         *  Bonds can be visited twice in the same layer from different
         *  directions, but not twice in different layers, so record the
         *  edges in a separate list from the one passed in
         */ 
        ArrayList<Edge> layerEdges = new ArrayList<Edge>();
        
        for (TreeNode node : layer) {
            IAtom atom = molecule.getAtom(node.atomNumber);
            for (IAtom neighbour : molecule.getConnectedAtomsList(atom)) {
                int n = molecule.getAtomNumber(neighbour);
                
                // use references to existing nodes within a layer
                TreeNode nextNode = this.findNode(nextLayer, n);
                if (nextNode == null) {
                    nextNode = new TreeNode(n);
                }
                Edge edge = new Edge(node, nextNode);
                if (edges.contains(edge)) {
                    continue;
                } else {
                    layerEdges.add(edge);
                    node.children.add(nextNode);
                    nextNode.parents.add(node);
                    nextLayer.add(nextNode);
                }
            }
        }
        if (nextLayer.size() > 0) {
            this.layers.add(nextLayer);
        }
        if (edges.size() < molecule.getBondCount()) {
            edges.addAll(layerEdges);
            makeNextLayer(nextLayer, edges);
        }
    }
    
    private TreeNode findNode(ArrayList<TreeNode> layer, int atomNumber) {
        for (TreeNode node : layer) {
            if (node.atomNumber == atomNumber) {
                return node;
            }
        }
        return null;
    }
    
    private void calculateInitialInvariants() {
        int n = this.molecule.getAtomCount();
        int[] parentCount = new int[n];
        for (ArrayList<TreeNode> layer : this.layers) {
            for (TreeNode node : layer) {
                
            }
        }
    }
    
    /**
     * Equivalent to the algorithm "invariant-vertex(T(x), relative)" where
     * 'relative' is parent
     */
    private void updateVertexInvariantsUpward() {
        Map<TreeNode, Integer> vInv = new HashMap<TreeNode, Integer>();
        
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            ArrayList<TreeNode> layer = this.layers.get(i);
            for (TreeNode v : layer) {
                v.invariant = v.color + v.invariant; // + parents
                vInv.put(v, v.invariant);
            }
        }
    }
    
    /**
     * Equivalent to the algorithm "invariant-vertex(T(x), relative)" where
     * 'relative' is child
     */
    private void updateVertexInvariantsDownward() {
        Map<TreeNode, Integer> vInv = new HashMap<TreeNode, Integer>();
        for (ArrayList<TreeNode> layer : this.layers) {
            for (TreeNode v : layer) {
                v.invariant = v.color + v.invariant; // + children
                vInv.put(v, v.invariant);
            }
        }
    }
    
    private boolean invariantsConstant() {
        // TODO
        return true;
    }
    
    /**
     * Equivalent to the algorithm "invariant-atom(T(x), G)" as described in
     * the paper.
     */
    private void updateAtomInvariants() {
        while (!this.invariantsConstant()) {
            this.updateVertexInvariantsDownward();
            this.updateVertexInvariantsUpward();
            for (ArrayList<TreeNode> layer : this.layers) {
                for (TreeNode node : layer) {
                    
                }
            }
            List<Integer> atomInvariants = new ArrayList<Integer>();
            for (IAtom atom : this.molecule.atoms()) {
                atomInvariants.add(1);  // TODO
            }
            // sort atom invariants in decreasing order
            for (IAtom atom : this.molecule.atoms()) {
                
            }
        }
    }
    
    private List<IAtom> getMaxOrbit() {
        List<List<IAtom>> orbits = new ArrayList<List<IAtom>>();
        int bestOrbitSize = 0;
        List<IAtom> bestOrbit = null;
        for (List<IAtom> orbit : orbits) {
            int numberOfAtomsWithAtLeastTwoParents = 0;
            for (IAtom atom : orbit) {
                // TODO : count the parents.
            }
            if (orbit.size() > bestOrbitSize) {
                bestOrbitSize = orbit.size();
                bestOrbit = orbit;
            }
        }
        return bestOrbit;
    }
    
    public String canonize() {
        return canonize(0, new String());
    }
    
    private String canonize(int color, String sMax) {
        updateAtomInvariants();
        List<IAtom> maxOrbit = getMaxOrbit();
        if (maxOrbit.size() < 2) {
            
        } else {
            for (IAtom atom : maxOrbit) {
                // TODO color atom with color
                sMax = canonize(color + 1, sMax);
            }
        }
        return sMax;
    }
    
    private void toString(
            TreeNode node, StringBuffer buffer, List<Edge> edges) {
        buffer.append("[");
        buffer.append(this.molecule.getAtom(node.atomNumber).getSymbol());
        if (node.color != 0) buffer.append(",").append(node.color);
        buffer.append("]");
        if (node.children.size() == 0) return;
        buffer.append("(");
        // TODO : sort children by invariants 
        for (TreeNode child : node.children) {
            Edge edge = new Edge(node, child);
            if (edges.contains(edge)) {
                continue;
            } else {
                edges.add(edge);
                toString(child, buffer, edges);
            }
        }
        buffer.append(")");
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        this.toString(root, buffer, new ArrayList<Edge>());
        return buffer.toString();
    }
    
    public static Signature forMolecule(IMolecule molecule) {
        Signature[] atomSignatures = new Signature[molecule.getAtomCount()];
        int i = 0;
        for (IAtom atom : molecule.atoms()) {
            atomSignatures[i] = new Signature(atom, molecule);
            i++;
        }
        
        Arrays.sort(atomSignatures);
        return atomSignatures[0];
    }
}
