package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
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
    
    private enum Direction { UP, DOWN };
    
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
        
        public boolean visited;
        
        public TreeNode(int atomNumber) {
            this.atomNumber = atomNumber;
            this.parents = new ArrayList<TreeNode>();
            this.children = new ArrayList<TreeNode>();
            this.color = 0;
            this.visited = false;
        }
        
        public String toString() {
            return "" + this.atomNumber;
        }
    }
    
    private class InvariantVector implements Comparable<InvariantVector> {
        
        public int[] invariants;
        
        public InvariantVector(int n) {
            this.invariants = new int[n];
        }
        
        public void put(int i, int v) {
            this.invariants[i] = v;
        }
        
        public boolean equals(InvariantVector other) {
           return Arrays.equals(this.invariants, other.invariants);
        }
        
        public int compareTo(InvariantVector other) {
            for (int i = 0; i < this.invariants.length; i++) {
                if (this.invariants[i] < other.invariants[i]) {
                    return -1;
                } else if (this.invariants[i] > other.invariants[i]) {
                    return 1;
                } else {
                    continue;
                }
            }
            return 0;
        }
    }

    private class InvariantPair implements Comparable<InvariantPair> {
        public int color;
        public int inv;
        
        public InvariantPair(int color, int inv) {
            this.color = color;
            this.inv = inv;
        }
        
        public boolean equals(InvariantPair other) {
            return this.color == other.color && this.inv == other.inv;
        }
        
        public int compareTo(InvariantPair other) {
            if (this.color < other.color) {
                return 1;
            } else if (this.color > other.color) {
                return -1;
            } else {
                if (this.inv < other.inv) {
                    return 1;
                } else if (this.inv > other.inv) {
                    return -1;
                } else {
                    return 0;
                }
            }
        }
    }

    private class PairVector implements Comparable<PairVector> {
        public ArrayList<InvariantPair> invariantPairs;
        
        public PairVector() {
            this.invariantPairs = new ArrayList<InvariantPair>();
        }
        
        public void add(int color, int inv) {
            this.invariantPairs.add(new InvariantPair(color, inv));
        }
        
        public boolean equals(PairVector other) {
            if (this.invariantPairs.size() != other.invariantPairs.size()) {
                return false;
            } else{
                for (int i = 0; i < this.invariantPairs.size(); i++) {
                    InvariantPair a = this.invariantPairs.get(i);
                    InvariantPair b = other.invariantPairs.get(i); 
                    if (!a.equals(b)) {
                        return false;
                    }
                }
                return true;
            }
        }
        
        public int compareTo(PairVector other) {
            if (this.invariantPairs.size() > other.invariantPairs.size()) {
                return -1;
            }
            for (int i = 0; i < this.invariantPairs.size(); i++) {
                InvariantPair a = this.invariantPairs.get(i);
                InvariantPair b = other.invariantPairs.get(i);
                int c = a.compareTo(b);
                if (c != 0) {
                    return c;
                }
            }
            return 0;
        }
    }
    
    private class NodeComparator implements Comparator<TreeNode> {

        public int compare(TreeNode a, TreeNode b) {
            if (a.invariant < b.invariant) return -1;
            else if (a.invariant > b.invariant) return 1;
            else return 0;
        }
        
    }
    
    private NodeComparator nodeSorter = new NodeComparator();
    
    private TreeNode root;
    
    private ArrayList<ArrayList<TreeNode>> layers;
    
    private IMolecule molecule;
    
    private int[] atomInvariants;
    
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
        this.atomInvariants = this.calculateInitialInvariants();
    }
    
    public String canonize() {
        return canonize(1, new String());
    }

    private String canonize(int color, String sMax) {
        calculateAtomInvariants();
        Map<Integer, List<Integer>> orbits = this.partitionIntoOrbits();
        List<Integer> maxOrbit = getMaxOrbit(orbits);
        if (maxOrbit.size() < 2) {
            colorUncoloredAtoms(maxOrbit.get(0), color);
            String s = this.toString();
            if (sMax.equals("") || s.compareTo(sMax) == 1) {
                sMax = s;
            }
            return sMax;
        } else {
            for (int i : maxOrbit) {
                colorUncoloredAtoms(i, color);
                sMax = canonize(color + 1, sMax);
                colorUncoloredAtoms(i, 0);
            }
        }
        return sMax;
    }
    
    private void colorUncoloredAtoms(int atomNumber, int color) {
        for (List<TreeNode> layer : this.layers) {
            for (TreeNode node : layer) {
                if (node.atomNumber == atomNumber) {
                    node.color = color;
                }
            }
        }
    }
    
    private Map<Integer, List<Integer>> partitionIntoOrbits() {
        Map<Integer, List<Integer>> orbits = new HashMap<Integer, List<Integer>>();
        for (int i = 0; i < this.molecule.getAtomCount(); i++) {
            int invar = this.atomInvariants[i];
            if (orbits.containsKey(invar)) {
                orbits.get(invar).add(i);
            } else {
                List<Integer> orbit = new ArrayList<Integer>();
                orbit.add(i);
                orbits.put(invar, orbit);
            }
        }
        return orbits;
    }
    
    private int numberOfParents(int atomNumber) {
        for (List<TreeNode> layer : this.layers) {
            for (TreeNode node : layer) {
                if (node.atomNumber == atomNumber) {
                    return node.parents.size();
                }
            }
        }
        return 0;
    }
    
    private boolean allTwoParents(List<Integer> orbit) {
        for (int i : orbit) {
            if (numberOfParents(i) < 2) {
                return false;
            }
        }
        return true;
    }
    
    private List<Integer> getMaxOrbit(Map<Integer, List<Integer>> orbits) {
        List<Integer> bestOrbit = new ArrayList<Integer>();
        int minInvar = Integer.MAX_VALUE;
        for (int invar : orbits.keySet()) {
            List<Integer> orbit = orbits.get(invar);
           
            if (allTwoParents(orbit) 
                    && orbit.size() > bestOrbit.size()
                        && invar < minInvar) {
                minInvar = invar;
                bestOrbit = orbit;
            }
        }
        return bestOrbit;
    }

    /**
     * Recursive method to construct the tree, layer by layer.
     * 
     * @param layer the previous layer
     * @param edges the edges that have already been visited
     */
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
    
    /**
     * Find existing references to the node in the layer.
     * 
     * @param layer
     * @param atomNumber
     * @return
     */
    private TreeNode findNode(ArrayList<TreeNode> layer, int atomNumber) {
        for (TreeNode node : layer) {
            if (node.atomNumber == atomNumber) {
                return node;
            }
        }
        return null;
    }
    
    /**
     * Calculate initial invariants that are of the form (Symbol,
     * Number of Parents) like "C,1" or "C,0" - these are then sorted lexico-
     * graphically (so that, e.g. "C,1" > "H,1" > "H,2") and the position of
     * the invariant in this sorted list is the final invariant. 
     * 
     * @return the initial invariants as a list of numbers
     */
    private int[] calculateInitialInvariants() {
        int n = this.molecule.getAtomCount();
        int[] parentCounts = new int[n];
        String[] symbols = new String[n];
        ArrayList<String> invariantStrings = new ArrayList<String>();
        
        for (ArrayList<TreeNode> layer : this.layers) {
            for (TreeNode node : layer) {
                int i = node.atomNumber;
                parentCounts[i] = 0;
                for (TreeNode p : node.parents) {
                    if (!p.visited) {
                        parentCounts[i] += 1;
                    }
                    p.visited = true;
                }
                for (TreeNode p : node.parents) {
                    p.visited = false;
                }
                
                String symbol = molecule.getAtom(node.atomNumber).getSymbol();
                symbols[i] = symbol;
                String invariantString = symbol + "," + parentCounts[i];
                if (!invariantStrings.contains(invariantString)) {
                    invariantStrings.add(invariantString);
                }
            }
        }
        Collections.sort(invariantStrings);
        int[] initialInvariants = new int[n];
        for (int i = 0; i < n; i ++) {
            String invariantString = symbols[i] + "," + parentCounts[i];
            initialInvariants[i] = invariantStrings.indexOf(invariantString);
        }
        
        for (ArrayList<TreeNode> layer : this.layers) {
            for (TreeNode node : layer) {
                node.invariant = initialInvariants[node.atomNumber];
            }
        }
        return initialInvariants;
    }
    
    private int max(int[] arr) {
        int maxValue = Integer.MIN_VALUE;
        for (int i : arr) {
            if (i > maxValue) {
                maxValue = i;
            }
        }
        return maxValue;
    }
    
    /**
     * Equivalent to the algorithm "invariant-atom(T(x), G)" as described in
     * the paper.
     */
    private void calculateAtomInvariants() {
        int maxInvariant = max(this.atomInvariants);
        int previousMaxI = 0;
        int n = this.molecule.getAtomCount();
        int l = this.layers.size();
        while (maxInvariant != previousMaxI) {
            this.updateVertexInvariants(Direction.UP);
            this.updateVertexInvariants(Direction.DOWN);
            InvariantVector[] invariantVectors = new InvariantVector[n];
            for (int i = 0; i < this.layers.size(); i++) {
                ArrayList<TreeNode> layer = this.layers.get(i);
                for (TreeNode node : layer) {
                    int a = node.atomNumber;
                    if (invariantVectors[a] == null) {
                        invariantVectors[a] =  new InvariantVector(l);
                    }
                    invariantVectors[a].put(i, node.invariant);
                }
            }
            ArrayList<InvariantVector> vlist = new ArrayList<InvariantVector>();
            for (InvariantVector vector : invariantVectors) {
                if (!vlist.contains(vector)) {
                    vlist.add(vector);
                }
            }
            Collections.sort(vlist);
            Collections.reverse(vlist);
            for (int i = 0; i < n; i++) {
                this.atomInvariants[i] = vlist.indexOf(invariantVectors[i]);
            }
            previousMaxI = maxInvariant;
            maxInvariant = max(this.atomInvariants);
        }
    }
    
    /**
     * Equivalent to the algorithm "invariant-vertex(T(x), relative)" 
     */
    private void updateVertexInvariants(Direction direction) {
        ArrayList<PairVector> pairVectors = new ArrayList<PairVector>();
        HashMap<TreeNode, PairVector> nodeVectorMap = 
            new HashMap<TreeNode, PairVector>();
        
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            ArrayList<TreeNode> layer = this.layers.get(i);
            for (TreeNode node : layer) {
                PairVector pairVector = new PairVector();
                pairVector.add(node.color, atomInvariants[node.atomNumber]);
                if (direction == Direction.UP) {
                    for (TreeNode parent : node.parents) {
                        pairVector.add(parent.color, parent.invariant);
                    }
                } else if (direction == Direction.DOWN) {
                    for (TreeNode child : node.children) {
                        pairVector.add(child.color, child.invariant);
                    }
                }
                pairVectors.add(pairVector);
                nodeVectorMap.put(node, pairVector);
            }
            Collections.sort(pairVectors);
            Collections.reverse(pairVectors);
            for (TreeNode node : layer) {
                node.invariant = pairVectors.indexOf(nodeVectorMap.get(node));
            }
        }
    }
    
    private void toString(
            TreeNode node, StringBuffer buffer, List<Edge> edges) {
        buffer.append("[");
        IAtom atom = this.molecule.getAtom(node.atomNumber); 
        buffer.append(FaulonAtomTypeMapper.getTypeString(atom));
        if (node.color != 0) buffer.append(",").append(node.color);
        buffer.append("]");
        if (node.children.size() == 0) return;
        boolean addedChildren = false;
        Collections.sort(node.children, nodeSorter);
        for (TreeNode child : node.children) {
            Edge edge = new Edge(node, child);
            if (edges.contains(edge)) {
                continue;
            } else {
                if (!addedChildren) {
                    addedChildren = true;
                    buffer.append("(");
                }
                edges.add(edge);
                toString(child, buffer, edges);
            }
        }
        if (addedChildren) {
            buffer.append(")");
        }
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
