package engine;

import java.util.Arrays;
import java.util.Stack;

/**
 * Slightly mis-named class that stores a list of sub-partitions like 
 * 
 *  [[3, 1], [3], [1, 1], [1]]
 * 
 * which is one 'Solution' for the partition [4, 3, 2, 1].
 * 
 * @author maclean
 *
 */
public class Solution {
    
    private int l;
    private int currentSize;
    private Stack<Integer>[] subPartitions;
    
    public Solution(int l) {
        this.l = l; 
        this.currentSize = 0;
        this.subPartitions = new Stack[l];
        for (int i = 0; i < l; i++) {
            this.subPartitions[i] = new Stack<Integer>();
        }
    }
    
    public Solution(Solution other) {
        this.l = other.l;
        this.currentSize = other.currentSize;
        this.subPartitions = new Stack[l];
        for (int i = 0; i < l; i++) {
            this.subPartitions[i] = (Stack<Integer>) other.subPartitions[i].clone();
        }
    }
    
    public int size() {
        return this.currentSize;
    }
    
    public boolean hasSubPartsAt(int i) {
        return !this.subPartitions[i].isEmpty();
    }
    
    public int pop(int i) {
        return this.subPartitions[i].pop();
    }
    
    public int sizeAt(int i) {
        return this.subPartitions[i].size();
    }
    
    public int subPartAt(int i, int j) {
        return this.subPartitions[i].get(j);
    }
    
    public int remove(int i, int j) {
        return this.subPartitions[i].remove(j);
    }
    
    public int indexOf(int subPart, int i) {
        for (int j = 0; j < this.subPartitions[i].size(); j++) {
            if (this.subPartitions[i].get(j) == subPart) {
                return j;
            }
        }
        return -1;
    }
    
    public Solution add(int[] subPartitionToAdd) {
        Solution copy = new Solution(this.l);
        for (int i = 0; i < currentSize; i++) {
            copy.subPartitions[i].addAll(this.subPartitions[i]);
        }
        for (int i = 0; i < subPartitionToAdd.length; i++) {
            copy.subPartitions[currentSize].add(subPartitionToAdd[i]);
        }
        copy.currentSize = this.currentSize + 1;
        return copy;
    }
    
    public String toString() {
        return Arrays.deepToString(subPartitions);
    }

}
