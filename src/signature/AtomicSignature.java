package signature;

import java.util.ArrayList;
import java.util.List;

import org.openscience.cdk.interfaces.IAtom;
import org.openscience.cdk.interfaces.IAtomContainer;

/**
 * A signature created from a molecular graph 
 * 
 * @author maclean
 *
 */
public class AtomicSignature {
    
    private class Edge {
        
        public TreeNode a;
        
        public TreeNode b;
        
        public Edge(TreeNode a, TreeNode b) {
            this.a = a;
            this.b = b;
        }
        
        public boolean equals(Object o) {
            if (o instanceof Edge) {
                Edge other = (Edge) o;
                return (   this.a.atomNumber == other.a.atomNumber
                        && this.b.atomNumber == other.b.atomNumber)
                        ||
                        (  this.b.atomNumber == other.a.atomNumber
                        && this.a.atomNumber == other.b.atomNumber);
            } else {
                return false;
            }
        }
        
        public String toString() {
            return this.a + "-" + this.b; 
        }
    }
    
    private class TreeNode {
        
        public int color;
        
        public int atomNumber;
        
        public ArrayList<TreeNode> parents;
        
        public ArrayList<TreeNode> children;
        
        public TreeNode(int atomNumber) {
            this.atomNumber = atomNumber;
            this.parents = new ArrayList<TreeNode>();
            this.children = new ArrayList<TreeNode>();
            this.color = 0;
        }
        
        public String toString() {
            return "" + this.atomNumber;
        }
    }
    
    private TreeNode root;
    
    private ArrayList<ArrayList<TreeNode>> layers;
    
    private IAtomContainer atomContainer;
    
    private int height;

    public AtomicSignature(int i, Graph g) {
        this(i, g, -1);
    }
    
    public AtomicSignature(int i, Graph g, int height) {
        this.height = height;
        this.layers = new ArrayList<ArrayList<TreeNode>>();
        this.root = new TreeNode(i);
        
        ArrayList<TreeNode> rootLayer = new ArrayList<TreeNode>();
        rootLayer.add(root);
        this.layers.add(rootLayer);
        
        this.makeNextLayer(rootLayer, new ArrayList<Edge>());
    }

    /**
     * Make the next layer of the tree, given an existing layer, and a list
     * of those edges already created.
     * 
     * @param parentlayer
     * @param edges
     * @param maxHeight
     */
    private void makeNextLayer(ArrayList<TreeNode> layer, List<Edge> edges) {
        ArrayList<TreeNode> nextLayer = new ArrayList<TreeNode>();
        
        /*
         *  Bonds can be visited twice in the same layer from different
         *  directions, but not twice in different layers, so record the
         *  edges in a separate list from the one passed in
         */ 
        ArrayList<Edge> layerEdges = new ArrayList<Edge>();
        
        for (TreeNode node : layer) {
            IAtom atom = atomContainer.getAtom(node.atomNumber);
            for (IAtom neighbour : atomContainer.getConnectedAtomsList(atom)) {
                int n = atomContainer.getAtomNumber(neighbour);
                
                // use references to existing nodes within a layer
                TreeNode nextNode = this.findNode(nextLayer, n);
                if (nextNode == null) {
                    nextNode = new TreeNode(n);
                }
                Edge edge = new Edge(node, nextNode);
                if (edges.contains(edge)) {
                    continue;
                } else {
                    layerEdges.add(edge);
                    node.children.add(nextNode);
                    nextNode.parents.add(node);
                    nextLayer.add(nextNode);
                }
            }
        }
        if (nextLayer.size() > 0) {
            this.layers.add(nextLayer);
        }
        if (edges.size() < atomContainer.getBondCount()
                && (this.height != -1 && this.layers.size() < height)) {
            edges.addAll(layerEdges);
            makeNextLayer(nextLayer, edges);
        }
    }
    
    /**
     * Check for the presence of an existing node in this layer
     * 
     * @param layer
     * @param atomNumber
     * @return
     */
    private TreeNode findNode(ArrayList<TreeNode> layer, int atomNumber) {
        for (TreeNode node : layer) {
            if (node.atomNumber == atomNumber) {
                return node;
            }
        }
        return null;
    }
    
    public boolean equals(AtomicSignature other) {
        // TODO
        return true;
    }

    public boolean equalsString(String sig) {
        // TODO Auto-generated method stub
        return false;
    }

}
