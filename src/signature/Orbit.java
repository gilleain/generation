package signature;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Iterator;
import java.util.List;


/**
 * A list of atom indices, and the label of the orbit.
 * 
 * @author maclean
 *
 */
public class Orbit implements Iterable<Integer>, Cloneable {
    
    private List<Integer> atomIndices;
    
    private String label;
    
    private int height;
    
    public Orbit(String label, int height) {
        this.label = label;
        this.atomIndices = new ArrayList<Integer>();
        this.height = height;
    }
    
    public Iterator<Integer> iterator() {
        return this.atomIndices.iterator();
    }
    
    public Object clone() {
        Orbit o = new Orbit(this.label, this.height);
        for (Integer i : this.atomIndices) {
            o.atomIndices.add(new Integer(i));
        }
        return o;
    }
    
    public int getHeight() {
        return this.height;
    }
    
    public List<Integer> getAtomIndices() {
        return this.atomIndices;
    }
    
    public void addAtom(int i) {
        this.atomIndices.add(i);
    }
    
    public boolean hasLabel(String otherLabel) {
        return this.label.equals(otherLabel);
    }
    
    public boolean isEmpty() {
        return this.atomIndices.isEmpty();
    }

    public int getFirstAtom() {
        return this.atomIndices.get(0);
    }
    
    public void remove(int i) {
        this.atomIndices.remove(this.atomIndices.indexOf(i));
    }
    
    public String toString() {
        return label + " " +
                Arrays.deepToString(atomIndices.toArray()); 
    }

    public String getLabel() {
        return this.label;
    }

    public boolean contains(int i) {
        return this.atomIndices.contains(i);
    }

}
