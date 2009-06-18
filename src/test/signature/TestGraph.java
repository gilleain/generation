package test.signature;

import org.openscience.cdk.interfaces.IAtomContainer;
import org.openscience.cdk.interfaces.IChemObjectBuilder;
import org.openscience.cdk.nonotify.NoNotificationChemObjectBuilder;

import signature.Graph;

public class TestGraph {
    
    private static IChemObjectBuilder builder = 
        NoNotificationChemObjectBuilder.getInstance();
    
    public static Graph makeGraph() {
        IAtomContainer ac = builder.newAtomContainer();
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        ac.addAtom(builder.newAtom("C"));
        Graph graph = new Graph(ac);
        return graph;
    }

}
