package deterministic;

import org.openscience.cdk.graph.ConnectivityChecker;
import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IMolecule;
import org.openscience.cdk.smiles.SmilesGenerator;

/**
 * Default implementation of the {@link EnumeratorResultHandler} interface for
 * handling results from the enumerator. Simply prints out results to standard
 * out, so long as they are fully connected. 
 * 
 * @author maclean
 *
 */
public class DefaultEnumeratorResultHandler implements EnumeratorResultHandler {
    
    private SmilesGenerator smilesGenerator;
    
    private int count;
    
    public DefaultEnumeratorResultHandler() {
        this.smilesGenerator = new SmilesGenerator();
        this.count = 0;
    }

    public void handle(IAtomContainer result) {
        if (ConnectivityChecker.isConnected(result)) {
            IMolecule molecule = result.getBuilder().newMolecule(result);
            String smiles = this.smilesGenerator.createSMILES(molecule);
            System.out.println(smiles);
        } else {
            System.out.println("Result " + count + " not connected!");
        }
        count++;
    }

}
