package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;
import java.util.List;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * Straight port of Faulon's c implementation.
 * 
 * @author maclean
 *
 */
public class Signature implements ISignature {
    
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
    
    private IAtomContainer container;
    
    private int[] OCCUR;
    private int[] COLOR;
    
    
    /**
     * These two arrays store the labels assigned when printing the string
     */
    private int[] maxLabels;
    private int[] currentLabels;
    
    /**
     * The number of atoms in the molecule - used everywhere!
     */
    private int SIZE;
    private String SMAX;
    
    /**
     * added as a temporary hack to get the most recently created string
     * TODO : refactor this to not be a global!
     */
    private String SCURRENT;
    
    private int MAX_COLOR;

    private int NBCUR;
    
    private static final double VALENCE = 4;
    
    /**
     * Create a signature 'factory' for a molecule - to actually get 
     * signature strings, the canonize method needs to be called.
     * 
     * @param molecule
     */
    public Signature(IAtomContainer container) {
        this.container = container;
        this.SIZE = this.container.getAtomCount();
        this.MAX_COLOR = this.SIZE;
        this.maxLabels = new int[SIZE];
    }
    
    
    /**
     * Get the (lexicographically least) signature for a particular atom
     * of the molecule.
     * 
     * @param atomNumber the index of the atom in the molecule
     * @return the signature of this atom
     */
    public String forAtom(int atomNumber) {
        signatureAtom(atomNumber, SIZE);
        return getBestSignatureString();
    }
    
    /**
     * Get the (lexicographically least) signature for a particular atom
     * of the molecule, of height <code>h</code>.
     * 
     * @param atomNumber the index of the atom in the molecule
     * @param h the height of the signature
     * @return the signature of this atom
     */
    public String forAtom(int atomNumber, int h) {
        signatureAtom(atomNumber, h);
        return getBestSignatureString();
    }
    
    public IMolecule toMolecule() {
        // we don't really care about the builder, since no construction is
        // going on...
        return this.toMolecule(null);
    }


    public IMolecule toMolecule(IChemObjectBuilder builder) {
        // XXX note that if the height is less than the span, it should really
        // return a subgraph, not the whole molecule...
        return builder.newMolecule(container);
    }
    
    private String getBestSignatureString() {
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
     * The basic entry point for producing a canonical signature for a molecule.
     * It creates signatures for each atom, then finds the lexicographically 
     * smallest one, and returns that.
     * 
     * @return the canonical signature for the molecule
     */
    public String toCanonicalSignatureString() {
        return toCanonicalSignatureString(SIZE);
    }
    
    public String toCanonicalSignatureString(int height) {
        // make a signature for each atom
        for (int atomNumber = 0; atomNumber < SIZE; atomNumber++) {
            String s = signatureAtom(atomNumber, height);
            if (SMAX != null && s.compareTo(SMAX) < 0) {
                continue;
            } else {
                SMAX = s;
            }
        }
        return SMAX;
    }
    
    /**
     * Calculate the orbit elements, that contain information about the 
     * signature of each atom (up to maximum height), and what orbit 
     * it belongs to. 
     * 
     * @see calculateOrbits
     * @return an OrbitElement instance for each atom
     */
    public OrbitElement[] calculateOrbitElements() {
        return calculateOrbitElements(SIZE);
    }
    
    /**
     * Calculate the orbit elements, that contain information about the 
     * signature of each atom (up to height <code>height</code>), and what 
     * orbit it belongs to.
     * 
     * @param height the height to calculate each signature to
     * @return an OrbitElement instance for each atom
     */
    public OrbitElement[] calculateOrbitElements(int height) {
        OrbitElement[] orbitElements = new OrbitElement[SIZE];
        
        // make a signature for each atom
        for (int atomNumber = 0; atomNumber < SIZE; atomNumber++) {
            String s = signatureAtom(atomNumber, height);
            s = getBestSignatureString();   // XXX TODO
//            System.out.println(String.format("%3d %s", atomNumber, s));
            if (orbitElements[atomNumber] == null) {
                OrbitElement orbitElement = new OrbitElement(atomNumber, s);
                orbitElements[atomNumber] = orbitElement;
            } else {
                orbitElements[atomNumber].atomNumber = atomNumber;
                orbitElements[atomNumber].signatureString = s;
            }
            orbitElements[atomNumber].height = height;
            if (SMAX != null && s.compareTo(SMAX) < 0) {
                continue;
            } else {
                SMAX = s;
                for (int i = 0; i < SIZE; i++) {
                    if (orbitElements[i] == null) {
                        orbitElements[i] = new OrbitElement(-1, "");
                    }
                    orbitElements[i].label = maxLabels[i];
                }
                orbitElements[atomNumber].signatureString = s;
            }
        }
        rankOrbits(orbitElements);
       
        return orbitElements;
    }
    
    private void rankOrbits(OrbitElement[] orbitElements) {
        // bucket-sort the orbit elements
        Arrays.sort(orbitElements);
        orbitElements[0].orbitIndex = 0;
        for (int i = 1; i < SIZE; i++) {
            OrbitElement a = orbitElements[i];
            OrbitElement b = orbitElements[i - 1];
            if (a.signatureString.equals(b.signatureString)) {
                a.orbitIndex = b.orbitIndex;
            } else {
                a.orbitIndex = b.orbitIndex + 1;
            }
        } 
    }
    
    /**
     * Calculate the 'orbits' of the atom container; that is, each subset of the
     * atoms that share the same signature is in the same orbit.
     * 
     * @return a list of Orbit instances, that partition the atoms
     */
    public List<Orbit> calculateOrbits() {
        OrbitElement[] orbitElements = this.calculateOrbitElements();
        List<Orbit> orbits = new ArrayList<Orbit>();
        int index = -1;
        Orbit currentOrbit = null;
        for (OrbitElement element : orbitElements) {
            if (element.orbitIndex != index || currentOrbit == null) {
                currentOrbit = new Orbit(element.signatureString, element.height);
                orbits.add(currentOrbit);
                index = element.orbitIndex;
            }
            currentOrbit.addAtom(element.atomNumber);
        }
        return orbits;
    }
    
    /**
     * Determine if the atoms in the atom container are in canonical order. To
     * do this, the signatures are computed, and ordered lexicographically -
     * if this order is the same as the original order, then the atom container
     * is canonical, otherwise it is not.
     * 
     * @return true if the atoms are in canonical order
     */
    public boolean isCanonical() {
        OrbitElement[] orbitElements = this.calculateOrbitElements();
        for (OrbitElement o : orbitElements) System.out.println(o);
        for (OrbitElement o : orbitElements) {
            if (o.atomNumber == o.label) {
                continue;
            } else {
                return false;
            }
        }
//        int last = orbitElements[0].label;
//        for (int i = 1; i < orbitElements.length; i++) {
//            if (orbitElements[i].label > last) {
//                return false;
//            }
//            last = orbitElements[i].label;
//        }
//        for (OrbitElement o : orbitElements) System.out.println(o);
        return true;
    }

    /**
     * Start point : create a DAG, make the initial labels, and canonize
     * 
     * @param atomNumber the atom to use as the root
     * @param h the height
     * @return the canonical string
     */
    private String signatureAtom(int atomNumber, int h) {
        if (h > this.SIZE + 1) h = SIZE + 1;
        DAG dag = new DAG(this.container, atomNumber, h);
        
        int[] LABEL = new int[SIZE];
        OCCUR = new int[SIZE];
        COLOR = new int[SIZE];
        double[] INVAR = new double[SIZE];
        maxLabels = new int[SIZE];
        currentLabels = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            OCCUR[i] = COLOR[i] = 0;
            INVAR[i] = 0;
            LABEL[i] = maxLabels[i] = currentLabels[i] = -1;
        }
        
        int[] intInv = initialInvariants(dag, OCCUR, COLOR);
        for (int i = 0; i < intInv.length; i++) { INVAR[i] = intInv[i]; }
        
        computeLabelInvariant(dag, h, LABEL, OCCUR, INVAR, 0);
        return SMAX;
    }

    /**
     * Determine the initial invariants using the parents of each vertex.
     * 
     * @param dag the DAG
     * @param OCC the occurrences
     * @param COL the colors (TODO : difference between colors and labels?)
     * @param INV the invariants
     */
    private int[] initialInvariants(DAG dag, int[] OCC, int[] COL) {
        
        /* (coment copied from c source)
         * vertices with degree 1 have OCC = 1 
         * JLF 10/03 
         * vertices occurring alone with more than one parent have OCC += 1 
         * all other vertices have OCC += (number of parents) each time they occur
         */
        int l = 0;
        for (ArrayList<Vertex> layer : dag) {
            int parent = 0;
            int k = 0;
            for (Vertex vertex : layer) {
                if (vertex.parents.size() > 1) { parent++; }
                k++;
            }
            for (Vertex vertex : layer) {
                int degree = container.getConnectedBondsCount(vertex.atomNumber); 
                if (degree < 2) {
                    OCC[vertex.atomNumber] = COL[vertex.atomNumber] = 1;
                } else {
                    int j = vertex.parents.size();
                    OCC[vertex.atomNumber] += j;
                    if (parent < 2) COL[vertex.atomNumber] += 1;
                    else            COL[vertex.atomNumber] += j;
                }
            }
            l++;
        }
        return OCC;
    }


    /**
     * Make a canonical labelling of the DAG, recursively trying 
     * possible labels.
     * 
     * @param dag the directed acyclic graph
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrances
     * @param INV the invariants
     * @param ITER the current iteration (TODO : remove)
     */
    private void computeLabelInvariant(
            DAG dag, int h, int[] LAB, int[] OCC, double[] INV, int ITER) {
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
            newinv = computeInvariant(dag, h, label, occur, invar, "parent");
            newinv = computeInvariant(dag, h, label, occur, invar, "child");
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
            endLabelInvariant(dag, h, label, occur, invar, L0);
            return;
        }
        
        /* Find the orbit to singularize from leaves to root
         * this orbit has the maximum number of elements and the max invariant
         */
        for (int l = h; l >= 0; l--) {
            if (l >= dag.size()) continue;
            for (Vertex vertex : dag.get(l)) {
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
                    computeLabelInvariant(dag, h, label, occur, invar, ITER + 1);
                    label[i] = l;
                    
                    if (ITER >= MAX_COLOR) break; 
                }
            }
        }
        if (omax == 1) {
            computeLabelInvariant(dag, h, label, occur, invar, ITER + 1);
        }
    }

    /**
     * Compute the vertex invariants, either going UP or DOWN the tree (DAG),
     * depending on the value of the relation parameter. If this is 'parent',
     * go down - otherwise go up.  
     * 
     * @param dag the DAG
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrences
     * @param INV the invariants
     * @param relation 'parent' or 'child'
     * @return the maximum invariant after ranking
     */
    private int computeInvariant(DAG dag, int h,
            int[] LAB, int[] OCC, double[] INV, String relation) {
        int l0;
        int ln;
        int li;
        if (relation.equals("parent")) {
            l0 = 0;
            ln = dag.size();
            li = 1;
        } else {
            l0 = dag.size() - 1;
            ln = -1;
            li = -1;
        }
        
        // compute invariant for all vertices
        for (int l = l0; l != ln; l += li) {
            computeLayerInvariant(dag.get(l), LAB, INV, relation);
        }
        
        // find K, the maximum invariant for nodes and atoms
        double K = 0;
        double n = 0;
        for (ArrayList<Vertex> layer : dag) {
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
        for (ArrayList<Vertex> layer : dag) {
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
     * Finish the labelling and generate the string form.
     * 
     * @param dag the DAG
     * @param h the height
     * @param LAB the labels
     * @param OCC the occurrances
     * @param INV the invariants
     * @param L0 the max label used (?)
     */
    private void endLabelInvariant(
            DAG dag, int h, int[] LAB, int[] OCC, double[] INV, int L0) {
        for (int i = 0; i < SIZE; i++) {
            currentLabels[i] = -1;
        }
        NBCUR = 0;
        String s = layerPrintString(dag, LAB, L0);
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
     * @param dag the DAG
     * @param LAB the labels
     * @param L0 the max (?) label
     * @return the canonical string
     */
    private String layerPrintString(DAG dag, int[] LAB, int L0) {
        Vertex root = dag.getRoot();
        int[] OCC = new int[SIZE];
        occurString(root, new ArrayList<Edge>(), OCC);
        
        /* remove labels occurring only one time JLF 02-05 */
        for (int i = 0; i < SIZE; i++) {
            if (LAB[i] > -1 && OCC[i] < 2) L0--;
        }
        LL = L0 + 1;
        
        StringBuffer sb = new StringBuffer();
        printString(sb, null, root, new ArrayList<Edge>(), LAB, OCC);
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

    // TODO either rename, or pass into the method, or get rid of, this.
    int LL = -1;
    
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
     */
    private void printString(StringBuffer sb, Vertex parent,
            Vertex current, ArrayList<Edge> edges, int[] LAB,
            int[] OCC) {
        if (OCC[current.atomNumber] > 1) {
            // if it SHOULD have a number, but doesn't, add one
            if (!current.element.contains(",")) {
                if (LAB[current.atomNumber] < 0) {
                    LAB[current.atomNumber] = (++LL);
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
            currentLabels[current.atomNumber] = NBCUR++;
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
                printString(sb, current, child, edges, LAB, OCC); 
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
