package engine;

import java.util.ArrayList;
import java.util.Arrays;

import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.interfaces.IBond.Order;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesGenerator;

/**
 * A specialised adjacency matrix class that can be constructed using integer
 * partitions 
 * 
 * @author maclean
 *
 */
public class Matrix {
    

    
    /**
     * The integer values of the rows interpreted as binary numbers,
     * so [1, 0, 0, 1] = 9 and [0, 0, 1, 1] = 3 (note they are left-packed)
     */
    private ArrayList<Integer> rowNumbers;
    
    /**
     * A reference to the partition that is used to constrain the column sums
     */
    private Partition partition;
    
    public Matrix(Partition partition) {
        this.partition = partition;
        this.rowNumbers = new ArrayList<Integer>();
    }
    
    public int rowSum() {
        int sum = 0;
        for (int rowNumber : this.rowNumbers) {
            sum += rowNumber;
        }
        return sum;
    }
    
    public boolean[] toBitSet(int i, int n) {
        boolean[] bits = new boolean[n];
        for (int j = n - 1; j > -1; j--) {
            if (((i >> j) & 1) == 1) {
                bits[j] = true;
            } else {
                bits[j] = false;
            }
        }
        return bits;
    }
    
    public boolean check(int i, int l) {
        int n = this.partition.size();
        boolean[] bits = toBitSet(i, n);
        // check that the row, when summed as a column, equals the part l.
//        System.out.println("i = " + i 
//                        + " bit count = " + Integer.bitCount(i) 
//                        + " part = " + this.partition.getPart(l)
//                        + " l = " + l
//                        + " bits[l] " + bits[n - l - 1]
//                        + " bits = " + toProperString(bits));
        if (Integer.bitCount(i) != this.partition.getPart(l)) {
            return false;
        }
        if (bits[n - l - 1]) return false;
        for (int k = 0; k < this.rowNumbers.size(); k++) {
            int rowNum = this.rowNumbers.get(k);
            boolean[] rowBits = toBitSet(rowNum, n);
            if (rowBits[n - l - 1] != bits[ n - k - 1]) {
                return false;
            }
        }
        return true;
    }
    
    public ArrayList<Integer> nextNumbers(int l, int m) {
        int min;
        int max;
        if (l == 0) {
            min = 1;
            max = m;
        } else {
            min = this.rowNumbers.get(l - 1);
            max = m;
        }
        
        ArrayList<Integer> next = new ArrayList<Integer>();
        for (int i = min; i < max; i++) {
            if (check(i, l)) {
                next.add(i);
            }
        }
//        System.out.println(
//                "next = " + next + " min = " + min + " max = " + max + " this = " + this);
        return next;
    }
    
    public String toString() {
        return this.rowNumbers.toString();
    }
    
    public Matrix extendBy(int i) {
        Matrix clone = new Matrix(this.partition);
        clone.rowNumbers.addAll(this.rowNumbers);
        clone.rowNumbers.add(i);
        return clone;
    }
    
    public static void backtrack(Matrix matrix, int l, int n, int m) {
        if (l == n) {
//            System.out.println(matrix);
            System.out.println(matrix.toSmiles() 
                            + "\t\t" + matrix.rowSum() 
                            + "\t\t" + matrix
                            + "\t\t" + Arrays.deepToString(
                                    new Object[] {matrix.getHalfMatrixNumber()})
                            + "\t\t" + matrix.getHalfMatrixSum()
                                    
            );
        } else {
            for (int i : matrix.nextNumbers(l, m)) {
                backtrack(matrix.extendBy(i), l + 1, n, m);
            }
        }
    }
    
    public static String toProperString(boolean[] bits) {
        String s = "";
        for (int i = bits.length - 1; i > -1; i--) {
            if (bits[i]) {
                s += "1";
            } else {
                s += "0";
            }
        }
        return s;
    }
    
    public final SmilesGenerator generator = new SmilesGenerator();
    public final NoNotificationChemObjectBuilder nnb 
                = NoNotificationChemObjectBuilder.getInstance(); 
    public String toSmiles() {
//        System.out.println(this);
        IMolecule molecule = nnb.newMolecule();
        int n = this.rowNumbers.size();
        for (int i = 0; i < n; i++ ){
            molecule.addAtom(nnb.newAtom("C"));
        }
        for (int i = 0; i < n; i++) {
            int r = this.rowNumbers.get(i);
            boolean[] bits = this.toBitSet(r, n);
//            System.out.println(Arrays.deepToString(new Object[] {bits}));
            for (int j = n - i - 1; j > -1; j--) {
                if (bits[j] == true) {
//                    System.out.println("bonding " + i +" " + (n - j - 1) + " bits[j]" + bits[j]);
                    molecule.addBond(i, n - j - 1, Order.SINGLE);
                }
            }
        }
        return generator.createSMILES(molecule);
    }
    
    public int[] getHalfMatrixNumber() {
        int n = this.rowNumbers.size();
        int[] bitSequence = new int[(n * (n - 1)) / 2];
        int bitIndex = 0;
        for (int col = n - 1; col > -1; col--) {
            for (int row = 0; row < ((n - 1) - col); row++) {
                int rowNumber = this.rowNumbers.get(row);
                bitSequence[bitIndex] = (rowNumber >> col) & 1;
                bitIndex++;
            }
        }
        return bitSequence;
    }
    
    public int getHalfMatrixSum() {
        int n = this.rowNumbers.size();
        int sum = 0;
        int bitIndex = ((n * (n - 1)) / 2) - 1;
        for (int col = n - 1; col > -1; col--) {
            for (int row = 0; row < ((n - 1) - col); row++) {
                int rowNumber = this.rowNumbers.get(row);
                if (((rowNumber >> col) & 1) == 1){
                    sum += (int) Math.pow(2, bitIndex);
                }
                bitIndex--;
            }
        }
        return sum;
    }
    
    public static void main(String[] args) {
//        Partition p = new Partition(new int[] {1, 2, 2, 3}); // C4
//        Partition p = new Partition(new int[] {2, 2, 2, 2, 2, 3, 3}); // C7
//        Partition p = new Partition(new int[] {2, 2, 2, 2, 2, 2, 2, 2, 3, 3}); // C10
        int n = 8;
        int vmin = 2 * (n - 1);
        int vmax = (n * 4) - n;
        for (int v = vmin; v <= vmax; v += 2) {
            for (Partition p : PartitionCalculator.partition(v, n)){
                if (p.getPart(0) > 4) continue;
                p.reverse();
                Matrix m = new Matrix(p);
                System.err.println(p);
                backtrack(m, 0, n, (int)Math.pow(2, n));
            }
        }
    }
}
