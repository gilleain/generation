package tmputil;

import java.util.BitSet;
import java.util.Iterator;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.io.iterator.IIteratingChemObjectReader;

/**
 * Filter a stream of structures according to some valence pattern 
 * 
 * @author maclean
 *
 */
public class HydrogenCountFilter implements Iterator<IAtomContainer> {
    
    private int[] hydrogenCounts;
    
    private IIteratingChemObjectReader stream;
    
    private IAtomContainer cachedAtomContainer;
    
    public HydrogenCountFilter(int[] hydrogenCounts, IIteratingChemObjectReader stream) {
        this.hydrogenCounts = hydrogenCounts;
        this.stream = stream;
        this.cachedAtomContainer = null;
    }
   
    public void remove() {
        throw new UnsupportedOperationException();
    }

    public boolean hasNext() {
        this.cachedAtomContainer = null;
        this.seek();
        if (this.cachedAtomContainer == null) {
            return false;
        } else {
            return true;
        }
    }

    public IAtomContainer next() {
        return this.cachedAtomContainer;
    }
    
    private void seek() {
//        while (this.stream.hasNext()) {
//            IAtomContainer next = (IAtomContainer) this.stream.next();
//            if (this.accepts(next)) {
//                this.cachedAtomContainer = next;
//                return;
//            }
//        }
        this.cachedAtomContainer = null;
    }

    public boolean accepts(IAtomContainer atomContainer) {
        BitSet bitSet = new BitSet();
        for (IAtom atom : atomContainer.atoms()) {
            if (atom.getSymbol().equals("H")) continue;
            
            int hcount = 0;
            for (IAtom attached : atomContainer.getConnectedAtomsList(atom)) {
                if (attached.getSymbol().equals("H")) hcount++;
            }
            if (contains(bitSet, hcount)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
    
    private boolean contains(BitSet bitSet, int hCountToTest) {
        for (int i = 0; i < this.hydrogenCounts.length; i++) {
            if (this.hydrogenCounts[i] == hCountToTest && !bitSet.get(i)) {
                bitSet.set(i);
                return true;
            }
        }
        return false;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        int[] hcounts = new int[] {0, 0, 1, 1, 1, 2, 2, 3, 3, 3};
        String inputFile = "C10H16.sdf";
        String outputFile = "C10H16_filtered.sdf";
//        int[] hcounts = new int[] {3, 3, 3, 0, 1};
//        String inputFile = "C4H10O1.sdf";
//        String outputFile = "C4H10O1_filtered.sdf";

        
        try {
            java.io.FileReader fileReader = new java.io.FileReader(inputFile);
            org.openscience.cdk.interfaces.IChemObjectBuilder nnBuilder = 
            org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder.getInstance();
            org.openscience.cdk.io.iterator.IteratingMDLReader mdlReader = new
            org.openscience.cdk.io.iterator.IteratingMDLReader(fileReader, nnBuilder);
            
            java.io.FileWriter fileWriter = new java.io.FileWriter(outputFile); 
//            org.openscience.cdk.io.SDFWriter writer = 
//                new org.openscience.cdk.io.SDFWriter(fileWriter); 
//            
//            HydrogenCountFilter filter = new HydrogenCountFilter(hcounts, mdlReader);
//            while (filter.hasNext()) {
//                writer.write(filter.next());
//            }
//            writer.close();
            
        } catch (Exception e) {
            
        }
    }
}
