package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.Iterator;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;

/**
 * A directed acyclic graph - each vertex of the graph refers to an atom in a
 * molecule that is passed to the constructor.
 * 
 * @author maclean
 *
 */
public class DAG implements Iterable<ArrayList<Vertex>> {
    
    /**
     * Small class used to bucket-sort (sometimes called radix sort)
     * values in a layer
     */
    private class Bucket {
        public double x;
        public int y;
        public int z;
        public String toString() { 
            return String.format("(%6.0f, %d, %d)", x, y, z);
        }
    }
    
    /**
     * Used for sorting the double-valued invariants
     *
     */
    private class cmp_invariant implements Comparator<Double> {
        public int compare(Double a, Double b) {
            if (a.doubleValue() + EPS6 < b.doubleValue()) return 1;
            if (a.doubleValue() - EPS6 > b.doubleValue()) return -1;
            return 0;
        }
    }
    private cmp_invariant cmp_invariant_instance = new cmp_invariant();
    
    /**
     * Used for bucket-sorting the vertices by their invariants
     *
     */
    private class cmp_invar implements Comparator<Bucket> {

        public int compare(Bucket a, Bucket b) {
            if (a == null || b == null) return 0;
            if (a.x + EPS6 < b.x) return 1;
            if (a.x - EPS6 > b.x) return -1;
            return 0;
        }
        
    }
    private cmp_invar cmp_invar_instance = new cmp_invar();
    
    /**
     * Used for sorting the vertices by their invariants
     *
     */
    private class cmp_vertex_invariant implements Comparator<Vertex> {
        public int compare(Vertex a, Vertex b) {
           return cmp_invariant_instance.
           compare(new Double(a.invariant), new Double(b.invariant));
        }
    }
    private cmp_vertex_invariant 
        cmp_vertex_invariant_instance = new cmp_vertex_invariant();
    
    /**
     * Used for sorting vertices first by their string form, THEN
     * by thier invariants
     */
    private class cmp_vertex_invariant_element implements Comparator<Vertex> {

        public int compare(Vertex a, Vertex b) {
            if (a == null && b == null) return 0;
            if (a == null) return 1;
            if (b == null) return -1;
            int r = -(a.element.compareTo(b.element));
            if (r != 0) { 
                return r;
            } else {
                return cmp_vertex_invariant_instance.compare(a, b);
            }
        }
    }
    private cmp_vertex_invariant_element cmp_vertex_invariant_element_instance 
        = new cmp_vertex_invariant_element();
    
    /**
     * A tolerance factor used when sorting double values
     */
    private static final double EPS6 = 1E-5;

    /**
     * Used to re-scale for certain unusual graphs, such as those
     * with a high vertex degree.
     */
    private static final double POWMAX = 10E30;
    
    private ArrayList<ArrayList<Vertex>> layers;
    
    /**
     * These two arrays store the labels assigned when printing the string
     */
    private int[] maxLabels;
    
    private int[] currentLabels;
    
    private int[] OCCUR;
    
    private int[] COLOR;
    
    private int SIZE;
    
    private int MAX_COLOR;
    
    /**
     * added as a temporary hack to get the most recently created string
     * TODO : refactor this to not be a global!
     */
    private String SCURRENT;
    
    private String SMAX;
    
    /**
     * When canonically labelling, this the current value of the label
     */
    private int CURRENT_LABEL;
    
    private int LARGEST_LABEL;
    
    private static final double VALENCE = 4;
    
    /**
     * The maximum height of the most recently calculated atom signature
     */
    private int maxHeight;
    
    private IAtomContainer container;

    /**
     * Construct the DAG (directed acyclic graph) rooted at this atom number.
     *
     * @param molecule the molecule to refer to
     * @param atomNumber
     * @param h the height to build it to
     */
    public DAG(IAtomContainer container, int atomNumber, int h) {
        this.container = container;
        this.SIZE = container.getAtomCount();
        this.MAX_COLOR = this.SIZE;
        assert atomNumber <= this.container.getAtomCount();
        assert h >= 0;
        
        layers = new ArrayList<ArrayList<Vertex>>();
        
        ArrayList<Edge> E = new ArrayList<Edge>();
        Vertex root = new Vertex(atomNumber, "", 1);
        ArrayList<Vertex> rootLayer = new ArrayList<Vertex>();
        rootLayer.add(root);
        layers.add(rootLayer);
        if (h < 1) return;
        
        buildLayer(rootLayer, E, h - 1);
        this.OCCUR = new int[SIZE];
        this.COLOR = new int[SIZE];
        int[] LABEL = new int[SIZE];
        this.maxLabels = new int[SIZE];
        this.currentLabels = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            OCCUR[i] = COLOR[i] = 0;
            LABEL[i] = maxLabels[i] = currentLabels[i] = -1;
        }
        double[] invariants = this.initialInvariants();
        computeLabelInvariant(h, LABEL, OCCUR, invariants, 0);
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
    
    public int getLargestLabel() {
        return this.LARGEST_LABEL;
    }

    public String getSMAX() {
        return this.SMAX;
    }
    
    public int getMaxHeight() {
        return this.maxHeight;
    }
    
    public int getMaxLabel(int i) {
        return this.maxLabels[i];
    }
    
    public String getBestSignatureString() {
        if (SMAX != null) {
            if (SMAX.compareTo(SCURRENT) < 1) {
                return SMAX;
            } else {
                return SCURRENT;
            }
        } else {
            return SCURRENT;    // :(
        }    
    }
    
    /**
     * Determine the initial invariants using the parents of each vertex.
     * 
     */
    private double[] initialInvariants() {
        
        /* (coment copied from c source)
         * vertices with degree 1 have OCC = 1 
         * JLF 10/03 
         * vertices occurring alone with more than one parent have OCC += 1 
         * all other vertices have OCC += (number of parents) each time they occur
         */
        for (ArrayList<Vertex> layer : this) {
            int parent = 0;
            int k = 0;
            for (Vertex vertex : layer) {
                if (vertex.parents.size() > 1) { parent++; }
                k++;
            }
            for (Vertex vertex : layer) {
                int degree = container.getConnectedBondsCount(vertex.atomNumber); 
                if (degree < 2) {
                    OCCUR[vertex.atomNumber] = COLOR[vertex.atomNumber] = 1;
                } else {
                    int j = vertex.parents.size();
                    OCCUR[vertex.atomNumber] += j;
                    if (parent < 2) COLOR[vertex.atomNumber] += 1;
                    else            COLOR[vertex.atomNumber] += j;
                }
            }
        }
        double[] invariants = new double[this.SIZE];
        for (int i = 0; i < this.SIZE; i++) {
            invariants[i] = (double) OCCUR[i];
        }
        return invariants;
    }
    
    /**
     * Make a canonical labelling of the DAG, recursively trying 
     * possible labels.
     * 
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrences
     * @param INV the invariants
     * @param ITER the current iteration (TODO : remove)
     */
    private void computeLabelInvariant(
            int h, int[] LAB, int[] OCC, double[] INV, int ITER) {
        int L0 = -1;
        int omax = 1;
        int imax = -1;
        int inv = -1;
        
        int[] label = new int[SIZE];
        int[] occur = new int[SIZE];
        double[] invar = new double[SIZE];
        
        for (int i = 0; i < SIZE; i++) {
            label[i] = LAB[i]; invar[i] = INV[i]; occur[i] = OCC[i];
            if (label[i] > L0) { L0 = label[i]; }
        }
        
        // compute invariant for all vertices
        int newinv;
        while (true) {
            newinv = computeInvariant(h, label, occur, invar, "parent");
            newinv = computeInvariant(h, label, occur, invar, "child");
            if (newinv == inv) break;
            inv = newinv;
        }
        inv = -1;
        
        int j;
        for (j = 0; j < SIZE; j++) {
            if (occur[j] > 1) {
                break;
            }
        }
        if (j == SIZE) {
            endLabelInvariant(h, label, occur, invar, L0);
            return;
        }
        
        /* Find the orbit to singularize from leaves to root
         * this orbit has the maximum number of elements and the max invariant
         */
        for (int l = h; l >= 0; l--) {
            if (l >= this.size()) continue;
            for (Vertex vertex : this.get(l)) {
                if (COLOR[vertex.atomNumber] > 1 
                        && ((occur[vertex.atomNumber] > omax)
                                && invar[vertex.atomNumber] > inv)) {
                    imax = vertex.atomNumber;
                    inv = (int)invar[imax];
                    omax = occur[imax];
                }
            }
        }
        
        // recursion
        if (omax > 1) {
            for (int i = 0; i < SIZE; i++) {
                if (invar[i] == invar[imax]) {
                    int l = label[i];
                    label[i] = L0 + 1;
                    computeLabelInvariant(h, label, occur, invar, ITER + 1);
                    label[i] = l;
                    
                    if (ITER >= MAX_COLOR) break; 
                }
            }
        }
        if (omax == 1) {
            computeLabelInvariant(h, label, occur, invar, ITER + 1);
        }
    }
    
    /**
     * Compute the vertex invariants, either going UP or DOWN the tree (DAG),
     * depending on the value of the relation parameter. If this is 'parent',
     * go down - otherwise go up.  
     * 
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrences
     * @param INV the invariants
     * @param relation 'parent' or 'child'
     * @return the maximum invariant after ranking
     */
    private int computeInvariant(int h,
            int[] LAB, int[] OCC, double[] INV, String relation) {
        int l0;
        int ln;
        int li;
        if (relation.equals("parent")) {
            l0 = 0;
            ln = this.size();
            li = 1;
        } else {
            l0 = this.size() - 1;
            ln = -1;
            li = -1;
        }
        
        // compute invariant for all vertices
        for (int l = l0; l != ln; l += li) {
            computeLayerInvariant(this.get(l), LAB, INV, relation);
        }
        
        // find K, the maximum invariant for nodes and atoms
        double K = 0;
        double n = 0;
        for (ArrayList<Vertex> layer : this) {
            n++;
            for (Vertex vertex : layer) {
                if (vertex.invariant > K) {
                    K = (double)vertex.invariant;
                }
            }
        }
        for (int i = 0; i < SIZE; i++) {
            if (INV[i] > K) {
                K = INV[i];
            }
        }
        K = K + 1;
        
        // rescale if necessary
        double KK;
        if (Math.pow(K, n) > POWMAX) {
            KK = Math.pow(10.0, (10.0 / n));
            for (int i = 0; i < SIZE; i++) {
                INV[i] = INV[i] * KK / K;
            }
        } else {
            KK = K;
        }
        
        // compute new invariant for all atoms
        n = 1;
        for (ArrayList<Vertex> layer : this) {
            n++;
            for (Vertex vertex : layer) {
                double invariant = vertex.invariant * (KK / K);
                invariant *= Math.pow(KK, n);
                INV[vertex.atomNumber] += invariant;
            }
        }
        
        /*
         * sort and compute arry INV
         * x is the inital invariant
         * y is the new invariant
         * z in the location in the new array
         */
        Bucket[] invar = new Bucket[SIZE];
        for (int i = 0; i < SIZE; i++) {
            invar[i] = new Bucket();
            invar[i].x = INV[i];
            invar[i].y = invar[i].z = i;
        }
        Arrays.sort(invar, this.cmp_invar_instance);
        
        // rank the sorted buckets
        invar[0].y = 1;
        int inv = 1;
        for (int i = 1; i < SIZE; i++) {
            Bucket a = invar[i];
            Bucket b = invar[i - 1];
            if (this.cmp_invar_instance.compare(a, b) == 0) {
                a.y = b.y;
            } else {
                a.y = (++inv);
            }
        }
        for (int i = 0; i < SIZE; i++) { INV[invar[i].z] = invar[i].y; }
        
        // compute OCC
        int nb = 1;
        int i0 = 0;
        for (int i = 1; i < SIZE; i++) {
            if (invar[i].y == invar[i - 1].y) {
                nb++;
            } else {
                for (int k = i0; k < i0 + nb; k++) {
                    invar[k].x = nb;
                }
                nb = 1;
                i0 = i;
            }
        }
        for (int i = i0; i < SIZE; i++) {
            invar[i].x = nb;
        }
        
        for (int i = 0; i < SIZE; i++) {
            if (OCC[invar[i].z] > 1) {
                OCC[invar[i].z] = (int)invar[i].x;
            }
        }
        
        return inv;
    }
    
    /**
     * Compute invariants for a layer of the DAG.
     * 
     * @param layer the list of vertices at this layer
     * @param LAB the labels
     * @param INV the (atom?) invariants
     * @param relation 'parent' or 'child'
     */
    private void computeLayerInvariant(
            ArrayList<Vertex> layer, int[] LAB, double[] INV, String relation) {
        for (Vertex vertex : layer) {
            computeVertexInvariant(vertex, LAB, INV, relation);
        }
        
        Collections.sort(layer, this.cmp_vertex_invariant_element_instance);
        
        double[] invar = new double[layer.size() + 1];
        invar[0] = 1;
        
        int inv = 1;
        int i = 1;
        int n = layer.size();
        while (i < n) {
            while (cmp_vertex_invariant_element_instance.compare(
                    layer.get(i), layer.get(i - 1)) == 0) {
                invar[i] = inv;
                i++;
                if (i >= n) {
                    break;
                }
            }
            inv++; 
            invar[i] = inv;
            i++;
        }
        i = 0;
        for (Vertex vertex : layer) {
            vertex.invariant = (int)invar[i];
            i++;
        }
        
    }
    

    /**
     * Compute the invariant for a single vertex, relative either to its 
     * children or its parents.
     * 
     * @param vertex the vertex to compute for
     * @param LAB the labels
     * @param INV the invariants
     * @param relation 'parent' or 'child'
     */
    private void computeVertexInvariant(Vertex vertex, int[] LAB,
            double[] INV, String relation) {
        vertex.element = "[";
        vertex.element += getType(vertex);
        if (LAB[vertex.atomNumber] >= 0) {
            vertex.element += ",";
            vertex.element += String.valueOf(LAB[vertex.atomNumber] + 1);
        }
        vertex.element += "]";
        vertex.invariant = (int)INV[vertex.atomNumber];
        
        double K = SIZE + 1;
        Double[] invar;
        int n = 0;
        if (relation.equals("child")) {
            if (vertex.children.size() == 0) {
                return;
            } else {
                invar = new Double[2 * vertex.children.size()];
                for (Vertex child : vertex.children) {
                    invar[n++] = new Double(child.invariant);
                    invar[n++] = order(vertex, child) + K;
                }
            }
        } else if (relation.equals("parent")) {
            if (vertex.parents.size() == 0) {
                return;
            } else {
                invar = new Double[2 * vertex.parents.size()];
                for (Vertex parent : vertex.parents) {
                    invar[n++] = new Double(parent.invariant);
                    invar[n++] = order(vertex, parent) + K;
                }
            }
        } else {
            return;
        }
        Arrays.sort(invar, this.cmp_invariant_instance);
        
        // rescale if necessary
        K += VALENCE;
        double KK;
        if (Math.pow(K, n) > POWMAX) {
            KK = Math.pow(10.0, (10.0 / n));
        } else {
            KK = K;
        }
        
        // compute and scale invariant
        for (int i = 0; i < n; i++) {
            double z = Math.pow(KK, (double)(i + 1)) * (KK / K);
            vertex.invariant += ((double)invar[i]) * z;
        }
    }
    
    /**
     * Build a layer of the DAG
     * 
     * @param N the previous layer
     * @param E the edges seen so far
     * @param h the height to build to
     */
    private void buildLayer(ArrayList<Vertex> N, ArrayList<Edge> E, int h) {
        if (h < 0) return;
        ArrayList<Vertex> NN = new ArrayList<Vertex>();
        ArrayList<Edge> layerE = new ArrayList<Edge>();
        for (Vertex n : N) {
            IAtom atom = this.container.getAtom(n.atomNumber);
            for (IAtom aa : this.container.getConnectedAtomsList(atom)) {
                addVertex(n, this.container.getAtomNumber(aa), layerE, E, NN);
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
    
    /**
     * Finish the labelling and generate the string form.
     * 
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrences
     * @param INV the invariants
     * @param L0 the max label used (?)
     */
    private void endLabelInvariant(
            int h, int[] LAB, int[] OCC, double[] INV, int L0) {
        for (int i = 0; i < SIZE; i++) {
            currentLabels[i] = -1;
        }
        String s = layerPrintString(LAB, L0);
//        System.out.println("FRESH " + s);
        this.SCURRENT = s;
        
        // store s only if it is larger than SMAXs
        if (SMAX != null && s.compareTo(SMAX) < 0) {
            return;
        } else {
            SMAX = s;
            for (int i = 0; i < SIZE; i++) {
                maxLabels[i] = currentLabels[i];
            }
        }
    }

    /**
     * Convert the DAG into a string
     * 
     * @param LAB the labels
     * @param L0 the max (?) label
     * @return the canonical string
     */
    private String layerPrintString(int[] LAB, int L0) {
        Vertex root = this.getRoot();
        int[] OCC = new int[SIZE];
        occurString(root, new ArrayList<Edge>(), OCC);
        
        /* remove labels occurring only one time JLF 02-05 */
        for (int i = 0; i < SIZE; i++) {
            if (LAB[i] > -1 && OCC[i] < 2) L0--;
        }
        LARGEST_LABEL = L0 + 1;
        
        StringBuffer sb = new StringBuffer();
        this.CURRENT_LABEL = 0;
        printString(sb, null, root, new ArrayList<Edge>(), LAB, OCC, 0);
        return sb.toString();
    }
    
    /**
     * Count the occurrences of atom numbers in the tree? (NOTE : why do this
     * now?) Also, as a side-effect (!), sort the children of each vertex...
     *  
     * @param v the current vertex
     * @param edges the edges seen so far
     * @param OCC the recorded occurrences
     */
    private void occurString(Vertex v, ArrayList<Edge> edges, int[] OCC) {
        if (OCCUR[v.atomNumber] > 1) {
            OCC[v.atomNumber] += 1;
        }
        
        // sort children by invariant
        if (v.children.size() == 0) return;
        Collections.sort(v.children, this.cmp_vertex_invariant_instance);
        
        // recursion
        for (Vertex child : v.children) {
            Edge e = new Edge(v.atomNumber, child.atomNumber);
            if (edges.contains(e)) {
                continue;
            } else {
                edges.add(e);
                occurString(child, edges, OCC);
            }
        }
    }
    
    /**
     * Recursively convert the DAG into a string, putting the contents into
     * the string buffer sb.
     * 
     * @param sb the string buffer that is being filled
     * @param parent the parent of the current vertex
     * @param current the current vertex
     * @param edges the edges seen so far
     * @param LAB the labels
     * @param OCC the occurrences
     * @param height the current height of the string
     */
    private void printString(StringBuffer sb, Vertex parent,
            Vertex current, ArrayList<Edge> edges, int[] LAB,
            int[] OCC, int height) {
        if (height > this.maxHeight) {
            this.maxHeight = height;
        }
        if (OCC[current.atomNumber] > 1) {
            // if it SHOULD have a number, but doesn't, add one
            if (!current.element.contains(",")) {
                if (LAB[current.atomNumber] < 0) {
                    this.LARGEST_LABEL++;
                    LAB[current.atomNumber] = this.LARGEST_LABEL;
                }
                current.element = "[";
                current.element += getType(current);
                current.element += ",";
                current.element += String.valueOf(LAB[current.atomNumber]);
                current.element += "]";
            }
        } else {
            // if it SHOULDN'T have a number, but does, remove it
            int commaIndex = current.element.indexOf(",");
            if (commaIndex != -1) {
                current.element = current.element.substring(0, commaIndex) + "]";
            }
        }
        
        if (parent != null) {
            double o = order(parent, current);
            if      (o <= 1) sb.append(current.element);
            else if (o == 2) sb.append("=").append(current.element);
            else if (o == 3) sb.append("t").append(current.element);
            else if (o == 4) sb.append("p").append(current.element);
            else sb.append(String.format("%d-%s", o, current.element));
        } else {
            sb.append(current.element);
        }
        
        if (currentLabels[current.atomNumber] < 0) {
            currentLabels[current.atomNumber] = CURRENT_LABEL;
            this.CURRENT_LABEL++;
        }
        
        if (current.children.size() == 0) {
            return;
        }
        
        // recursion
        boolean addedBracket = false;
        for (Vertex child : current.children) {
            Edge e = new Edge(current.atomNumber, child.atomNumber);
            if (edges.contains(e)) {
                continue;
            } else {
                if (!addedBracket) {
                    sb.append("(");
                    addedBracket = true;
                }
                edges.add(e);
                printString(sb, current, child, edges, LAB, OCC, height + 1); 
            }
        }
        if (addedBracket) {
            sb.append(")");
        }
    }

    /**
     * Determine the bond order between the atoms referred to by these two
     * vertices. Single = 1, Double = 2, Triple = 3, Aromatic = 4.
     * 
     * @param vertex a vertex
     * @param parent its parent
     * @return the bond order as a double
     */
    private double order(Vertex vertex, Vertex parent) {
        double order = -1;
        if (vertex == null || parent == null) return order;
        
        IAtom a = container.getAtom(vertex.atomNumber);
        IAtom b = container.getAtom(parent.atomNumber);
        IBond bond = container.getBond(a, b);
        if (bond == null) return order;
        if (isAromatic(vertex) && isAromatic(parent)) {
            return 4;
        } else {
            switch (bond.getOrder()) {
                case SINGLE: return 1;
                case DOUBLE: return 2;
                case TRIPLE: return 3;
                default: return order;
            }
        }
    }
    
    /**
     * Check for aromaticity of the atom referred to by this vertex.
     * @param v a vertex
     * @return true if the referred atom has the correct flag
     */
    private boolean isAromatic(Vertex v) {
        return container.getAtom(v.atomNumber).getFlag(CDKConstants.ISAROMATIC);
    }

    /**
     * Get the 'type' of the vertex - can be any string, so long as it is used
     * consistently (and does not contain a "," character!).
     * 
     * @param vertex a vertex
     * @return the string for this vertex
     */
    private String getType(Vertex vertex) {
        return container.getAtom(vertex.atomNumber).getSymbol();
    }

}
