package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

import org.openscience.cdk.interfaces.IAtomContainer;
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
     * The container the signature comes from
     */
    private IAtomContainer container;
   
    /**
     * The number of atoms in the molecule - used everywhere!
     */
    private int SIZE;
    
    /**
     * Create a signature 'factory' for a molecule - to actually get 
     * signature strings, the canonize method needs to be called.
     * 
     * @param molecule
     */
    public Signature(IAtomContainer container) {
        this.container = container;
        this.SIZE = this.container.getAtomCount();
    }
    
    /**
     * Get the (lexicographically least) signature for a particular atom
     * of the molecule.
     * 
     * @param atomNumber the index of the atom in the molecule
     * @return the signature of this atom
     */
    public String forAtom(int atomNumber) {
        return this.forAtom(atomNumber, this.SIZE);
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
        DAG dag = signatureAtom(atomNumber, h);
        return dag.getBestSignatureString();
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
        String SMAX = null;
        for (int atomNumber = 0; atomNumber < SIZE; atomNumber++) {
            DAG dag = signatureAtom(atomNumber, height);
            String s = dag.getBestSignatureString();
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
        return calculateOrbitElements(SIZE, true);
    }
    
    public OrbitElement[] calculateOrbitElementsUnsorted() {
        return calculateOrbitElements(SIZE, false);
    }
    
    /**
     * Calculate the orbit elements, that contain information about the 
     * signature of each atom (up to height <code>height</code>), and what 
     * orbit it belongs to.
     * 
     * @param height the height to calculate each signature to
     * @return an OrbitElement instance for each atom
     */
    public OrbitElement[] calculateOrbitElements(int height, boolean sorted) {
        OrbitElement[] orbitElements = new OrbitElement[SIZE];
        
        // make a signature for each atom
        String SMAX = null;
        for (int atomNumber = 0; atomNumber < SIZE; atomNumber++) {
            DAG dag = signatureAtom(atomNumber, height);
            String s = dag.getBestSignatureString();   // XXX TODO
//            System.out.println(String.format("%3d %s", atomNumber, s));
            if (orbitElements[atomNumber] == null) {
                orbitElements[atomNumber] = new OrbitElement(atomNumber, s);
            } else {
                orbitElements[atomNumber].atomNumber = atomNumber;
                orbitElements[atomNumber].signatureString = s;
            }
//            System.out.println("setting height to " + maxHeight);
            orbitElements[atomNumber].height = dag.getMaxHeight();
            if (SMAX != null && s.compareTo(SMAX) < 0) {
                continue;
            } else {
                SMAX = s;
                for (int i = 0; i < SIZE; i++) {
                    if (orbitElements[i] == null) {
                        orbitElements[i] = new OrbitElement(-1, "");
                    }
                    orbitElements[i].label = dag.getMaxLabel(i);
                }
                orbitElements[atomNumber].signatureString = s;
            }
        }
        if (sorted) {
            rankOrbits(orbitElements);
        }
       
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
        int index = -1;
        Orbit currentOrbit = null;
        List<Orbit> orbits = new ArrayList<Orbit>();
        for (OrbitElement element : this.calculateOrbitElements()) {
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
     * Create the signature for a particular atom.
     * 
     * @param atomNumber the atom to use as the root
     * @param h the height
     * @return the DAG
     */
    private DAG signatureAtom(int atomNumber, int h) {
        if (h > this.SIZE + 1) {
            return new DAG(this.container, atomNumber, SIZE + 1);
        } else {
            return new DAG(this.container, atomNumber, h);
        }
    }
}
