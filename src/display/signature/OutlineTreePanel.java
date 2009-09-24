package display.signature;

import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.MouseEvent;
import java.awt.event.MouseListener;

import javax.swing.JPanel;

import deterministic.BondCreationEvent;
import deterministic.BondCreationListener;
import deterministic.SimpleGraph;

public class OutlineTreePanel extends JPanel 
    implements BondCreationListener, MouseListener {

    private OutlineTree tree;
    
    private GraphSelectionListener graphSelectionListener;
    
    public OutlineTreePanel() {
        this.tree = new OutlineTree();
        this.setBackground(Color.BLUE);
    }
    
    public OutlineTreePanel(SimpleGraph rootGraph) {
        this.tree = new OutlineTree(rootGraph);
    }
    
    public void setGraphSelectionListener(GraphSelectionListener listener) {
        this.graphSelectionListener = listener;
    }
    
    public void paint(Graphics g) {
        super.paint(g);
        this.tree.width = this.getWidth();
        this.tree.height = this.getHeight();
//        System.out.println("tree painting " + this.tree.width + " " + this.tree.height);
        this.tree.draw(g);
    }
    
    public SimpleGraph getGraphAt(int x, int y) {
        return this.tree.getGraphAt(x, y);
    }
    
    public void bondAdded(BondCreationEvent bondCreationEvent) {
        this.tree.addNode(bondCreationEvent.parent, bondCreationEvent.child);
    }
    
    public void mouseClicked(MouseEvent e) {
        SimpleGraph g = this.getGraphAt(e.getX(), e.getY());
        if (g != null) {
            graphSelectionListener.graphSelected(new GraphSelectionEvent(g));
            this.repaint();
        }
        System.out.println(this.tree);
    }

    public void mouseEntered(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseExited(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mousePressed(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }

    public void mouseReleased(MouseEvent e) {
        // TODO Auto-generated method stub
        
    }
}
