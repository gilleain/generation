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
public class ValenceFilter implements Iterator<IAtomContainer> {
    
    private int[] valences;
    
//    private IIteratingChemObjectReader stream;
    
    private IAtomContainer cachedAtomContainer;
    
    public ValenceFilter(int[] valences, IIteratingChemObjectReader stream) {
        this.valences = valences;
//        this.stream = stream;
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
//        this.cachedAtomContainer = null;
    }

    public boolean accepts(IAtomContainer atomContainer) {
        BitSet bitSet = new BitSet();
        for (IAtom atom : atomContainer.atoms()) {
            if (atom.getSymbol().equals("H")) continue;
            
            int valence = (int)atomContainer.getBondOrderSum(atom);
            if (contains(bitSet, valence)) {
                continue;
            } else {
                return false;
            }
        }
        return true;
    }
    
    private boolean contains(BitSet bitSet, int valenceToTest) {
        for (int i = 0; i < this.valences.length; i++) {
            if (this.valences[i] == valenceToTest && !bitSet.get(i)) {
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
////        int[] valences = new int[] {0, 0, 1, 1, 1, 2, 2, 3, 3, 3};
////        String inputFile = "C10H16.sdf";
////        String outputFile = "C10H16_filtered.sdf";
//        int[] valences = new int[] {1, 1, 1, 1, 4};
//        String inputFile = "C4H10O1.sdf";
//        String outputFile = "C4H10O1_filtered.sdf";
//
//        
//        try {
//            java.io.FileReader fileReader = new java.io.FileReader(inputFile);
//            org.openscience.cdk.interfaces.IChemObjectBuilder nnBuilder = 
//            org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder.getInstance();
//            org.openscience.cdk.io.iterator.IteratingMDLReader mdlReader = new
//            org.openscience.cdk.io.iterator.IteratingMDLReader(fileReader, nnBuilder);
//            
////            org.openscience.cdk.io.SDFWriter writer = 
////                new org.openscience.cdk.io.SDFWriter(
////                        new java.io.FileWriter(outputFile)); 
////            
////            ValenceFilter filter = new ValenceFilter(valences, mdlReader);
////            while (filter.hasNext()) {
////                writer.write(filter.next());
////            }
////            
//        } catch (Exception e) {
//            
//        }
    }
}
