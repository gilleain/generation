package signature;

import java.util.ArrayList;

/**
 * A vertex in a signature DAG, that holds a reference to an atom number.
 * 
 * @author maclean
 *
 */
public class Vertex {
    
    public int atomNumber;
    
    public String element;
    
    public int invariant;
    
    public ArrayList<Vertex> parents;
    
    public ArrayList<Vertex> children;
    
    public Vertex(int atomNumber, String element, int invariant) {
        this.atomNumber = atomNumber;
        this.element = element;
        this.invariant = invariant;
        this.parents = new ArrayList<Vertex>();
        this.children = new ArrayList<Vertex>();
    }
    
    public String toString() {
        return String.format("%s%d(%d)", element, atomNumber, invariant);
    }
    
}
