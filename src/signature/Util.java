package signature;

import java.io.IOException;

import org.openscience.cdk.tools.SaturationChecker;

public class Util {
    
    private static SaturationChecker checker;
    
    private static Util instance;
    
    private Util() {
        try {
            this.checker = new SaturationChecker();
        } catch (ClassNotFoundException cnfe) {
            
        } catch (IOException ioe) {
            
        }
    }
    
    public static Util getInstance() {
        if (instance == null) {
            instance = new Util();
        }
        return instance;
    }
    
    public static SaturationChecker getChecker() {
        return instance.checker;
    }

}
