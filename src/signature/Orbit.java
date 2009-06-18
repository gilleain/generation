package signature;

import java.util.ArrayList;
import java.util.List;


public class Orbit {
    
    private List<Integer> atomIndices;
    
    private Graph graph;
    
    public Orbit(Graph graph) {
        this.graph = graph;
        this.atomIndices = new ArrayList<Integer>();
    }
    
    public boolean isEmpty() {
        return this.atomIndices.isEmpty();
    }

    public int getFirstAtom() {
        return this.atomIndices.get(0);
    }
    
    public void remove(int i) {
        this.atomIndices.remove(i);
    }

}
