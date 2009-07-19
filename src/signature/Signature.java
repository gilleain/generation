package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.SortedSet;
import java.util.TreeSet;

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
        public Node a;
        public Node b;
        
        public Edge(Node a, Node b) {
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
    
    private class Node {
        
        public int invariant;
        
        public int color;
        
        public int atomNumber;
        
        public ArrayList<Node> parents;
        
        public ArrayList<Node> children;
        
        public boolean visited;
        
        public Node(int atomNumber) {
            this.atomNumber = atomNumber;
            this.parents = new ArrayList<Node>();
            this.children = new ArrayList<Node>();
            this.color = 0;
            this.visited = false;
        }
        
        public String toString() {
            return "" + (this.atomNumber + 1);
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
        
        public String toString() {
            return Arrays.toString(this.invariants);
        }
    }

    private class InvariantPair implements Comparable<InvariantPair> {
        public int color;
        public int inv;
        
        public InvariantPair(int color, int inv) {
            this.color = color;
            this.inv = inv;
        }
        
        public boolean equals(Object o) {
            if (o instanceof InvariantPair) {
                InvariantPair other = (InvariantPair) o;
                return this.color == other.color && this.inv == other.inv;
            } else {
                return false;
            }
        }
        
        public int compareTo(InvariantPair other) {
            if (this.color < other.color) {
                return -1;
            } else if (this.color > other.color) {
                return 1;
            } else {
                if (this.inv < other.inv) {
                    return -1;
                } else if (this.inv > other.inv) {
                    return 1;
                } else {
                    return 0;
                }
            }
        }
        
        public String toString() {
            return "(" + this.color + "," + this.inv + ")";
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
        
        public boolean equals(Object o) {
            if (!(o instanceof PairVector)) return false;
            PairVector other = (PairVector) o;
            if (this.invariantPairs.size() != other.invariantPairs.size()) {
                return false;
            } else {
                return this.invariantPairs.equals(other.invariantPairs);
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
        
        public String toString() {
            return this.invariantPairs.toString();
        }
    }
    
    private class NodeComparator implements Comparator<Node> {

        public int compare(Node a, Node b) {
            if (a.invariant < b.invariant) return -1;
            else if (a.invariant > b.invariant) return 1;
            else return 0;
        }
        
    }
    
    private NodeComparator nodeSorter = new NodeComparator();
    
    private Node root;
    
    private ArrayList<ArrayList<Node>> layers;
    
    private IMolecule molecule;
    
    private int[] atomInvariants;
    
    private int nodeCount;
    
    public Signature(int atomNumber, IMolecule molecule) {
        this(molecule.getAtom(atomNumber), molecule);
    }
    
    public Signature(IAtom atom, IMolecule molecule) {
        this.molecule = molecule;
        this.root = new Node(molecule.getAtomNumber(atom));
        
        // create the layers, with a single root layer
        this.layers = new ArrayList<ArrayList<Node>>();
        ArrayList<Node> rootLayer = new ArrayList<Node>();
        rootLayer.add(root);
        this.layers.add(rootLayer);
        
        this.nodeCount = 0;
        this.makeNextLayer(rootLayer, new ArrayList<Edge>());
        this.calculateInitialInvariants();
    }
    
    /**
     * Recursive method to construct the tree, layer by layer.
     * 
     * @param previousLayer the previous layer
     * @param edges the edges that have already been visited
     */
    private void makeNextLayer(ArrayList<Node> previousLayer, List<Edge> edges) {
        ArrayList<Node> layer = new ArrayList<Node>();
        
        /*
         *  Bonds can be visited twice in the same layer from different
         *  directions, but not twice in different layers, so record the
         *  edges in a separate list from the one passed in
         */ 
        ArrayList<Edge> layerEdges = new ArrayList<Edge>();
        
        for (Node node : previousLayer) {
            IAtom atom = molecule.getAtom(node.atomNumber);
            for (IAtom neighbour : molecule.getConnectedAtomsList(atom)) {
                int n = molecule.getAtomNumber(neighbour);
                
                // use references to existing nodes within a layer
                boolean isNew = false;
                Node nextNode = this.findNode(layer, n);
                if (nextNode == null) {
                    nextNode = new Node(n);
                    isNew = true;
                }
                Edge edge = new Edge(node, nextNode);
                if (edges.contains(edge)) {
                    continue;
                } else {
                    layerEdges.add(edge);
                    node.children.add(nextNode);
                    nextNode.parents.add(node);
                    if (isNew) {
                        this.nodeCount++;
                        layer.add(nextNode);
                    }
                }
            }
        }
        if (layer.size() > 0) {
            this.layers.add(layer);
        }
        if (edges.size() < molecule.getBondCount()) {
            edges.addAll(layerEdges);
            makeNextLayer(layer, edges);
        }
    }

    /**
     * Find existing references to the node in the layer.
     * 
     * @param layer
     * @param atomNumber
     * @return
     */
    private Node findNode(ArrayList<Node> layer, int atomNumber) {
        for (Node node : layer) {
            if (node.atomNumber == atomNumber) {
                return node;
            }
        }
        return null;
    }

    public String canonize() {
        String s = canonize(1, new String());
        System.out.println(s);
        return s;
    }

    private String canonize(int color, String sMax) {
        calculateAtomInvariants();
        Map<Integer, List<Integer>> orbits = this.partitionIntoOrbits();
        System.out.println("orbits " + orbits);
        List<Integer> maxOrbit = getMaxOrbit(orbits);
        System.out.println("max orbit " + maxOrbit);
        if (maxOrbit.size() < 2) {
            // TODO : improve
            current_color = 1;
            colorUncoloredAtoms(root);
            String s = this.toString();
            if (sMax.equals("") || s.compareTo(sMax) == 1) {
                sMax = s;
            }
            return sMax;
        } else {
            for (int i : maxOrbit) {
                colorAtom(i, color);
                sMax = canonize(color + 1, sMax);
                colorAtom(i, 0);
            }
        }
        return sMax;
    }
    
    // TODO : improve
    private int current_color;
    
    private void colorUncoloredAtoms(Node node) {
        if (node.color == 0 && node.parents.size() > 1) {
            node.color = current_color;
            current_color++;
            for (Node child : node.children) {
                colorUncoloredAtoms(child);
            }
        } else {
            for (Node child : node.children) {
                colorUncoloredAtoms(child);
            }
        }
    }
    
    private void colorAtom(int atomNumber, int color) {
        for (List<Node> layer : this.layers) {
            for (Node node : layer) {
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
     * Calculate initial invariants that are of the form (Symbol,
     * Number of Parents) like "C,1" or "C,0" - these are then sorted lexico-
     * graphically (so that, e.g. "C,1" > "H,1" > "H,2") and the position of
     * the invariant in this sorted list is the final invariant. 
     * 
     */
    private void calculateInitialInvariants() {
        // store these values temporarily, to make it easier to look them up
        String[] symbols = new String[this.nodeCount+1];
        int[] parentCounts = new int[this.nodeCount+1];
    
        // create the list of invariant strings (like "C,2" or "H,1"
        ArrayList<String> invariantStrings = new ArrayList<String>();
        int i = 0;
        for (ArrayList<Node> layer : this.layers) {
            for (Node node : layer) {
                String symbol = molecule.getAtom(node.atomNumber).getSymbol();
                symbols[i] = symbol;
                parentCounts[i] = node.parents.size();
                String invariantString = symbol + "," + parentCounts[i];
                if (!invariantStrings.contains(invariantString)) {
                    invariantStrings.add(invariantString);
                }
                i++;
            }
        }
        Collections.sort(invariantStrings);
        
        // store the index of the invariant string in the tree
        i = 0;
        for (ArrayList<Node> layer : this.layers) {
            printLayer(layer);
            for (Node node : layer) {
                String invariantString = symbols[i] + "," + parentCounts[i];
                node.invariant = invariantStrings.indexOf(invariantString) + 1;
            }
        }
        
        System.out.println("parents " + Arrays.toString(parentCounts));
        System.out.println("symbols " + Arrays.toString(symbols));
        System.out.println("invstri " + invariantStrings);
    }
    
    private int numberOfParents(int atomNumber) {
        for (List<Node> layer : this.layers) {
            for (Node node : layer) {
                if (node.atomNumber == atomNumber) {
                    return node.parents.size();
                }
            }
        }
        return 0;
    }

    private void printLayer(ArrayList<Node> layer) {
        System.out.print("Layer ");
        for (Node node : layer) {
            System.out.print(" " + node);
        }
        System.out.print("\n");
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
                ArrayList<Node> layer = this.layers.get(i);
                for (Node node : layer) {
                    int a = node.atomNumber;
                    if (invariantVectors[a] == null) {
                        invariantVectors[a] =  new InvariantVector(l);
                    }
                    invariantVectors[a].put(i, node.invariant);
                }
            }
            ArrayList<InvariantVector> vlist = new ArrayList<InvariantVector>();
            for (InvariantVector vector : invariantVectors) {
                if (vector == null) continue;   // XXX
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
        SortedSet<PairVector> pairVectors = new TreeSet<PairVector>();
        HashMap<Node, PairVector> nodeVectorMap = 
            new HashMap<Node, PairVector>();
        
        for (int i = this.layers.size() - 1; i >= 0; i--) {
            ArrayList<Node> layer = this.layers.get(i);
            for (Node node : layer) {
                PairVector pairVector = new PairVector();
                pairVector.add(node.color, atomInvariants[node.atomNumber]);
                if (direction == Direction.UP) {
                    for (Node parent : node.parents) {
                        pairVector.add(parent.color, parent.invariant);
                    }
                } else if (direction == Direction.DOWN) {
                    for (Node child : node.children) {
                        pairVector.add(child.color, child.invariant);
                    }
                }
                pairVectors.add(pairVector);
                nodeVectorMap.put(node, pairVector);
            }
            List<PairVector> vectorList = new ArrayList<PairVector>();
            vectorList.addAll(pairVectors);
            System.out.println(vectorList);
            for (Node node : layer) {
                node.invariant = vectorList.indexOf(nodeVectorMap.get(node));
                System.out.println("layer " + i 
                                   + " node " + node 
                                   + " invariant " + node.invariant
                                   + " vector " + nodeVectorMap.get(node));
            }
        }
    }
    
    private void toString(
            Node node, StringBuffer buffer, List<Edge> edges) {
        buffer.append("[");
        buffer.append(getLabelForAtom(node.atomNumber));
        if (node.color != 0) buffer.append(",").append(node.color);
        buffer.append("]");
        if (node.children.size() == 0) return;
        boolean addedChildren = false;
        Collections.sort(node.children, nodeSorter);
        for (Node child : node.children) {
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
    
    public String getLabelForAtom(int atomNumber) {
        IAtom atom = this.molecule.getAtom(atomNumber);
        return FaulonAtomTypeMapper.getTypeString(atom);
    }
    
    public String toString() {
        StringBuffer buffer = new StringBuffer();
        this.toString(root, buffer, new ArrayList<Edge>());
        return buffer.toString();
    }
    
    /**
     * Temporary method that can only make canonical strings from
     * simple labelled trees (not DAGs, and with no multiple parent nodes
     * 
     * @return
     */
    public String toSimpleCanonicalString() {
        return this.toSimpleCanonicalString(root);
    }
    
    private String toSimpleCanonicalString(Node node) {
        String nodeLabel = "[" + getLabelForAtom(node.atomNumber) + "]"; 
        if (node.children.size() > 0) {
            String[] childStrings = new String[node.children.size()];
            int i = 0;
            for (Node child : node.children) {
                childStrings[i] = toSimpleCanonicalString(child);
                i++;
            }
            Arrays.sort(childStrings);
            String children = "";
            for (String childString : childStrings) {
                children += childString;
            }
            return nodeLabel + "(" + children + ")";
        } else {
            return nodeLabel;
        }
        
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
