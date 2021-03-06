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
        
        enumerator.generate();
        frame.setSize(1200, 800);
        frame.setVisible(true);
    }
    
    public static void main(String[] args) {
        String formula = "C4H10";
        
        new EnumeratorDebugger(formula);
    }

}
