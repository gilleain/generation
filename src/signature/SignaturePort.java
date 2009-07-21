package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collections;
import java.util.Comparator;

import org.openscience.cdk.CDKConstants;
import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IBond;
import org.openscience.cdk.interfaces.IMolecule;

/**
 * Straight port of Faulon's c implementation.
 * 
 * @author maclean
 *
 */
public class SignaturePort {
    
    private class Vertex {
        public int atomNumber;
        public String element;
        public int invariant;
        public ArrayList<Vertex> parent;
        public ArrayList<Vertex> child;
        
        public Vertex(int atomNumber, String element, int invariant) {
            this.atomNumber = atomNumber;
            this.element = element;
            this.invariant = invariant;
            this.parent = new ArrayList<Vertex>();
            this.child = new ArrayList<Vertex>();
        }
        
        public String toString() {
            return String.format("%s%d(%d)", element, atomNumber, invariant);
        }
        
    }
    
    private class Edge {
        public int a;
        public int b;
        public Edge(int a, int b) { this.a = a; this.b = b; }
        public boolean equals(Object other) {
            if (other instanceof Edge) {
                Edge o = (Edge) other;
                return (this.a == o.a && this.b == o.b) 
                        || (this.a == o.b && this.b == o.a);
            } else {
                return false;
            }
        }
        public String toString() {
            return String.format("%s-%s", a, b);
        }
    }
    
    private class Bucket {
        public double x;
        public int y;
        public int z;
        public String toString() { 
            return String.format("(%6.0f, %d, %d)", x, y, z);
        }
    }
    
    private class cmp_invariant implements Comparator<Double> {
        public int compare(Double a, Double b) {
            if (a.doubleValue() + EPS6 < b.doubleValue()) return 1;
            if (a.doubleValue() - EPS6 > b.doubleValue()) return -1;
            return 0;
        }
    }
    private cmp_invariant cmp_invariant_instance = new cmp_invariant();
    
    private class cmp_invar implements Comparator<Bucket> {

        public int compare(Bucket a, Bucket b) {
            if (a == null || b == null) return 0;
            if (a.x + EPS6 < b.x) return 1;
            if (a.x - EPS6 > b.x) return -1;
            return 0;
        }
        
    }
    private cmp_invar cmp_invar_instance = new cmp_invar();
    
    private class cmp_vertex_invariant implements Comparator<Vertex> {
        public int compare(Vertex a, Vertex b) {
           return cmp_invariant_instance.
           compare(new Double(a.invariant), new Double(b.invariant));
        }
    }
    private cmp_vertex_invariant 
        cmp_vertex_invariant_instance = new cmp_vertex_invariant();
    
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
    
    private class Klass {
        public int n, c, l;
        public String s;
    }
    
    private class klasscmp implements Comparator<Klass> {

        public int compare(Klass a, Klass b) {
            if (a == null && b == null) return 0;
            if (b == null) return -1;
            if (a == null) return 1;
            if (b.s == null) return -1;
            if (a.s == null) return 1;
            return (-a.s.compareTo(b.s));
        }
    }
    private klasscmp klasscmp_instance = new klasscmp();
    
    private static final double EPS6 = 1E-5;

    private static final double POWMAX = 10E30;
    
    private IMolecule molecule;
    
    private int[] OCCUR;
    private int[] COLOR;
    private int[] LACAN;
    private int[] LACUR;
    
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
    
    public SignaturePort(IMolecule molecule) {
        this.molecule = molecule;
        this.SIZE = this.molecule.getAtomCount();
        this.MAX_COLOR = this.SIZE;
        this.LACAN = new int[SIZE];
    }
    
    
    public String forAtom(int atomNumber) {
        sicd_signature_atom(atomNumber, SIZE);
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
    
    public String sisc_canonize() {
        Klass[] klasses = new Klass[SIZE];
        int height = Integer.MAX_VALUE;
        
        // make a signature for each atom
        for (int i = 0; i < SIZE; i++) {
            print_atom_signature(i, height, klasses);
        }
        
        // bucket-sort the classes
        Arrays.sort(klasses, klasscmp_instance);
        klasses[0].c = 0;
        for (int i = 1; i < SIZE; i++) {
            Klass a = klasses[i];
            Klass b = klasses[i - 1];
            if (klasscmp_instance.compare(a, b) == 0) {
                a.c = b.c;
            } else {
                a.c = b.c + 1;
            }
        }
        return SMAX;
    }
    
    private void print_atom_signature(int atomNumber, int h, Klass[] klasses) {
        int[] label = new int[SIZE];
        String s = sicd_signature_atom_label(atomNumber, h, label);
        klasses[atomNumber] = new Klass();
        klasses[atomNumber].n = atomNumber;
        klasses[atomNumber].s = s;
        if (SMAX != null) {
            if (s.compareTo(SMAX) < 0) return;
        } 
        SMAX = s;
        for (int i = 0; i < SIZE; i++) {
            if (klasses[i] == null) {
                klasses[i] = new Klass();
            }
            klasses[i].l = label[i];
        }
    }

    private String sicd_signature_atom_label(int atomNumber, int h, int[] label) {
        String sMax = sicd_signature_atom(atomNumber, h);
        for (int i = 0; i < SIZE; i++) label[i] = LACAN[i];
        return sMax;
    }

    private String sicd_signature_atom(int atomNumber, int h) {
        if (h > this.SIZE + 1) h = SIZE + 1;
        ArrayList<ArrayList<Vertex>> L = new ArrayList<ArrayList<Vertex>>();  
        build_dag(atomNumber, L, h);
        
        int[] LABEL = new int[SIZE];
        OCCUR = new int[SIZE];
        COLOR = new int[SIZE];
        double[] INVAR = new double[SIZE];
        LACAN = new int[SIZE];
        LACUR = new int[SIZE];
        for (int i = 0; i < SIZE; i++) {
            OCCUR[i] = COLOR[i] = 0;
            INVAR[i] = 0;
            LABEL[i] = LACAN[i] = LACUR[i] = -1;
        }
        
        init_label_invariant(L, h, LABEL, OCCUR, COLOR, INVAR);
        
        compute_label_invariant(L, h, LABEL, OCCUR, INVAR, 0);
        return SMAX;
    }

    private void compute_label_invariant(ArrayList<ArrayList<Vertex>> L, int h,
            int[] LAB, int[] OCC, double[] INV, int ITER) {
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
            newinv = compute_invariant(L, h, label, occur, invar, "parent");
            newinv = compute_invariant(L, h, label, occur, invar, "child");
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
            end_label_invariant(L, h, label, occur, invar, L0);
            return;
        }
        
        /* Find the orbit to singularize from leaves to root
         * this orbit has the maximum number of elements and the max invariant
         */
        for (int l = h; l >= 0; l--) {
            if (l >= L.size()) continue;
            for (Vertex vertex : L.get(l)) {
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
                    compute_label_invariant(L, h, label, occur, invar, ITER + 1);
                    label[i] = l;
                    
                    if (ITER >= MAX_COLOR) break; 
                }
            }
        }
        if (omax == 1) {
            compute_label_invariant(L, h, label, occur, invar, ITER + 1);
        }
    }

    private void end_label_invariant(ArrayList<ArrayList<Vertex>> L, int h,
            int[] LAB, int[] OCC, double[] INV, int L0) {
        for (int i = 0; i < SIZE; i++) {
            LACUR[i] = -1;
        }
        NBCUR = 0;
        String s = layer_print_string(L, h, LAB, L0);
        System.out.println("FRESH " + s);
        this.SCURRENT = s;
        if (SMAX != null) {
            if (s.compareTo(SMAX) < 0) return;
        }
        SMAX = s;
        for (int i = 0; i < SIZE; i++) {
            LACAN[i] = LACUR[i];
        }
    }

    private String layer_print_string(ArrayList<ArrayList<Vertex>> L, int h,
            int[] LAB, int L0) {
        Vertex root = L.get(0).get(0);
        int[] OCC = new int[SIZE];
        occur_string(root, new ArrayList<Edge>(), OCC);
        
        /* remove labels occurring only one time JLF 02-05 */
        for (int i = 0; i < SIZE; i++) {
            if (LAB[i] > -1 && OCC[i] < 2) L0--;
        }
        LL = L0 + 1;
        
        StringBuffer sb = new StringBuffer();
        print_string(sb, null, root, new ArrayList<Edge>(), LAB, OCC);
        return sb.toString();
    }
    
    private void occur_string(Vertex v, ArrayList<Edge> edges, int[] OCC) {
        if (OCCUR[v.atomNumber] > 1) {
            OCC[v.atomNumber] += 1;
        }
        
        // sort children by invariant
        if (v.child.size() == 0) return;
        Collections.sort(v.child, this.cmp_vertex_invariant_instance);
        
        // recursion
        for (Vertex child : v.child) {
            Edge e = new Edge(v.atomNumber, child.atomNumber);
            if (edges.contains(e)) {
                continue;
            } else {
                edges.add(e);
                occur_string(child, edges, OCC);
            }
        }
    }

    // TODO either rename, or pass into the method, or get rid of, this.
    int LL = -1;
    
    private void print_string(StringBuffer sb, Vertex parent,
            Vertex current, ArrayList<Edge> edges, int[] LAB,
            int[] OCC) {
        if (OCC[current.atomNumber] > 1) {
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
        
        if (LACUR[current.atomNumber] < 0) LACUR[current.atomNumber] = NBCUR++;
        if (current.child.size() == 0) {
            return;
        }
        
        // recursion
        boolean addedBracket = false;
        for (Vertex child : current.child) {
            Edge e = new Edge(current.atomNumber, child.atomNumber);
            if (edges.contains(e)) {
                continue;
            } else {
                if (!addedBracket) {
                    sb.append("(");
                    addedBracket = true;
                }
                edges.add(e);
                print_string(sb, current, child, edges, LAB, OCC); 
            }
        }
        if (addedBracket) {
            sb.append(")");
        }
    }
   

    private int compute_invariant(ArrayList<ArrayList<Vertex>> L, int h,
            int[] LAB, int[] OCC, double[] INV, String relation) {
        int l0;
        int ln;
        int li;
        if (relation.equals("parent")) {
            l0 = 0;
            ln = L.size();
            li = 1;
        } else {
            l0 = L.size() - 1;
            ln = -1;
            li = -1;
        }
        
        // compute invariant for all vertices
        for (int l = l0; l != ln; l += li) {
            compute_layer_invariant(L.get(l), LAB, INV, relation);
        }
        
        // find K, the maximum invariant for nodes and atoms
        double K = 0;
        double n = 0;
        for (ArrayList<Vertex> layer : L) {
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
        for (ArrayList<Vertex> layer : L) {
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

    private void compute_layer_invariant(
            ArrayList<Vertex> layer, int[] LAB, double[] INV, String relation) {
        for (Vertex vertex : layer) {
            compute_vertex_invariant(vertex, LAB, INV, relation);
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
//            System.out.println("end run i = " + i + " inv = " + inv + " a " + a + " b " + b + " " + cmp_vertex_invariant_element_instance.compare(a, b));
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

    private void compute_vertex_invariant(Vertex vertex, int[] LAB,
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
            if (vertex.child.size() == 0) {
                return;
            } else {
                invar = new Double[2 * vertex.child.size()];
                for (Vertex child : vertex.child) {
                    invar[n++] = new Double(child.invariant);
                    invar[n++] = order(vertex, child) + K;
                }
            }
        } else if (relation.equals("parent")) {
            if (vertex.parent.size() == 0) {
                return;
            } else {
                invar = new Double[2 * vertex.parent.size()];
                for (Vertex parent : vertex.parent) {
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
    
    private double order(Vertex vertex, Vertex parent) {
        double order = -1;
        if (vertex == null || parent == null) return order;
        
        IAtom a = molecule.getAtom(vertex.atomNumber);
        IAtom b = molecule.getAtom(parent.atomNumber);
        IBond bond = molecule.getBond(a, b);
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
    
    private boolean isAromatic(Vertex v) {
        return molecule.getAtom(v.atomNumber).getFlag(CDKConstants.ISAROMATIC);
    }

    private String getType(Vertex vertex) {
        return this.molecule.getAtom(vertex.atomNumber).getSymbol();
    }

    private void init_label_invariant(ArrayList<ArrayList<Vertex>> L, int h,
            int[] LAB, int[] OCC, int[] COL, double[] INV) {
        
        /* (coment copied from c source)
         * vertices with degree 1 have OCC = 1 
         * JLF 10/03 
         * vertices occurring alone with more than one parent have OCC += 1 
         * all other vertices have OCC += (number of parents) each time they occur
         */
        int l = 0;
        for (ArrayList<Vertex> N : L) {
            int parent = 0;
            int k = 0;
            for (Vertex vertex : N) {
                if (vertex.parent.size() > 1) { parent++; }
                k++;
            }
            for (Vertex vertex : N) {
                int degree = molecule.getConnectedBondsCount(vertex.atomNumber); 
                if (degree < 2) {
                    OCC[vertex.atomNumber] = COL[vertex.atomNumber] = 1;
                } else {
                    int j = vertex.parent.size();
                    OCC[vertex.atomNumber] += j;
                    if (parent < 2) COL[vertex.atomNumber] += 1;
                    else            COL[vertex.atomNumber] += j;
                }
            }
            l++;
        }
        for (int i = 0; i < SIZE; i++) { INV[i] = OCC[i]; }
    }

    private void build_dag(int atomNumber, ArrayList<ArrayList<Vertex>> L, int h) {
        assert atomNumber <= this.molecule.getAtomCount();
        assert h >= 0;
        
        ArrayList<Edge> E = new ArrayList<Edge>();
        Vertex root = new Vertex(atomNumber, "", 1);
        ArrayList<Vertex> rootLayer = new ArrayList<Vertex>();
        rootLayer.add(root);
        L.add(rootLayer);
        if (h < 1) return;
        
        build_layer(rootLayer, E, L, h - 1);
    }

    private void build_layer(ArrayList<Vertex> N, 
                             ArrayList<Edge> E, 
                             ArrayList<ArrayList<Vertex>> L, 
                             int h) {
        if (h < 0) return;
        ArrayList<Vertex> NN = new ArrayList<Vertex>();
        ArrayList<Edge> layerE = new ArrayList<Edge>();
        for (Vertex n : N) {
            IAtom atom = this.molecule.getAtom(n.atomNumber);
            for (IAtom aa : this.molecule.getConnectedAtomsList(atom)) {
                add_vertex(n, this.molecule.getAtomNumber(aa), layerE, E, h, NN);
            }
        }
        if (NN.size() != 0) {
            L.add(NN);
        }
        E.addAll(layerE);
        build_layer(NN, E, L, h - 1);
    }
    
    private void add_vertex(
            Vertex n, int aa, ArrayList<Edge> layerE, ArrayList<Edge> E, int h, 
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
        n.child.add(v);
        v.parent.add(n);
        layerE.add(e);
    }

}
