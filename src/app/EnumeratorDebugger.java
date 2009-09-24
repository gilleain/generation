package app;

import java.awt.GridLayout;

import javax.swing.JFrame;

import deterministic.DeterministicEnumerator;
import display.signature.GraphPanel;
import display.signature.OutlineTreePanel;

public class EnumeratorDebugger {
    
    private OutlineTreePanel outlineTreePanel;
    
    private GraphPanel graphPanel;
    
    public EnumeratorDebugger(String formula) {
        JFrame frame = new JFrame();
        frame.setLayout(new GridLayout(2, 1));
        DeterministicEnumerator enumerator = 
            new DeterministicEnumerator(formula);
        
        outlineTreePanel = new OutlineTreePanel();
        enumerator.setBondCreationListener(outlineTreePanel);
        frame.add(outlineTreePanel);
        
        graphPanel = new GraphPanel();
        outlineTreePanel.setGraphSelectionListener(graphPanel);
        frame.add(graphPanel);
        
        outlineTreePanel.addMouseListener(outlineTreePanel);
        
        frame.setSize(800, 800);
        frame.setVisible(true);
        enumerator.generate();
    }
    
    public static void main(String[] args) {
        String formula = "C2H8";
        
        new EnumeratorDebugger(formula);
    }

}
