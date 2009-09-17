package app;

import java.awt.Dimension;
import java.awt.GridLayout;
import java.io.BufferedReader;
import java.io.FileReader;
import java.io.IOException;
import java.util.ArrayList;

import javax.swing.JFrame;
import javax.swing.JPanel;
import javax.swing.JScrollPane;

import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;
import org.openscience.cdk.smiles.SmilesParser;

import display.DisplayPanel;
import display.signature.SignatureTreePanel;

public class DisplaySmilesAndSignatures extends JFrame {
    
    private ArrayList<DisplayPanel> structureDisplays;
    
    private ArrayList<SignatureTreePanel> signatureDisplays;
    
    private int WIDTH = 200;
    
    private int HEIGHT = 200;
    
    private int MAX_COLS = 6;
    
    public DisplaySmilesAndSignatures(String path) {
        super(path);
        this.structureDisplays = new ArrayList<DisplayPanel>();
        this.signatureDisplays = new ArrayList<SignatureTreePanel>();
        read(path);
    }
    
    public void read(String smilesAndSignatureTextFilePath) {
        JPanel panel = new JPanel();
        int rowCount = 0;
        SmilesParser parser = 
            new SmilesParser(NoNotificationChemObjectBuilder.getInstance());
        try {
            BufferedReader reader = new BufferedReader(
                    new FileReader(smilesAndSignatureTextFilePath));
            String line;
            while ((line = reader.readLine()) != null) {
                String[] parts = line.split("\t+");
                String smi = parts[0];
                String sig = parts[1];
                DisplayPanel structureDisplay = new DisplayPanel(WIDTH, HEIGHT);
                structureDisplay.moleculeWidth = WIDTH;
                structureDisplay.moleculeHeight = HEIGHT;
                SignatureTreePanel treePanel = 
                    new SignatureTreePanel(sig, HEIGHT, WIDTH);
                this.structureDisplays.add(structureDisplay);
                try {
                    IMolecule molecule = parser.parseSmiles(smi);
                    structureDisplay.setMolecule(molecule);
                } catch (InvalidSmilesException ise) {
                    
                }
                this.signatureDisplays.add(treePanel);
                rowCount++;
            }
        } catch (IOException ioe) {
            
        }
        int rows = rowCount;
        panel.setPreferredSize(new Dimension(WIDTH * 2, HEIGHT * rows));
        panel.setLayout(new GridLayout(rows, MAX_COLS));
        for (int i = 0; i < rowCount; i++) {
            panel.add(this.structureDisplays.get(i));
            panel.add(this.signatureDisplays.get(i));
        }
        this.add((new JScrollPane(panel)));
        this.setPreferredSize(new Dimension(700, 600));
    }

    public static void main(String[] args) {
        String className = DisplaySmilesAndSignatures.class.getSimpleName();
        if (args.length == 0) {
            String message = 
                String.format(
                        "Usage : java %s <smiles/signature file>", className);
            System.err.println(message);
        }
        String smilesAndSignatureTextFilePath = args[0];
        
        DisplaySmilesAndSignatures d = 
            new DisplaySmilesAndSignatures(smilesAndSignatureTextFilePath);
        d.pack();
        d.setVisible(true);
    }


}
