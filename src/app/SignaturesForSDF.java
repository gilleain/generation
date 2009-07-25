package app;

import java.io.FileInputStream;
import java.io.IOException;
import java.io.PrintStream;

import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.io.iterator.IteratingMDLReader;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.ISignature;
import signature.SignaturePort;

/**
 * Process an SDF file, and create canonical signatures for each molecule.
 * 
 * @author maclean
 *
 */
public class SignaturesForSDF {
    
    private String filePath;
    
    public SignaturesForSDF(String filePath) {
        this.filePath = filePath;
    }
    
    public void setFilePath(String filePath) {
        this.filePath = filePath;
    }
    
    /**
     * Calculate canonical signature strings for each structure, and print these
     * to the PrintStream <code>out</code>.
     * 
     * @param out the PrintStream to use (e.g. System.out)
     * @throws IOException if there is a problem with input or output
     */
    public void output(PrintStream out) throws IOException {
        IteratingMDLReader reader = 
            new IteratingMDLReader(
                    new FileInputStream(this.filePath),
                    NoNotificationChemObjectBuilder.getInstance()
            );
        while (reader.hasNext()) {
            IMolecule next = (IMolecule) reader.next();
            ISignature signature = new SignaturePort(next);
            String canonicalString = signature.getCanonicalSignatureString();
            out.println(canonicalString);
        }
        reader.close();
        out.flush();
    }
    
    public static void main(String[] args) {
        String className = SignaturesForSDF.class.getSimpleName();
        if (args.length == 0) {
            String message = 
                String.format("Usage : java %s <SDF File>", className);
            System.err.println(message);
        }
        
        String sdfFile = args[0];
        SignaturesForSDF processor = new SignaturesForSDF(sdfFile);
        try {
            processor.output(System.out);
        } catch (IOException ioe) {
            ioe.printStackTrace();
        }
    }

}
