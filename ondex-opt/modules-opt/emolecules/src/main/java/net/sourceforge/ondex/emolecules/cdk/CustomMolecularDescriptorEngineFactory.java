package net.sourceforge.ondex.emolecules.cdk;

import java.util.Arrays;
import java.util.List;
import org.openscience.cdk.qsar.DescriptorEngine;

/**
 *
 * @author grzebyta
 */
public class CustomMolecularDescriptorEngineFactory {
    
    /**
     * return list of all descriptor classes.
     * @return 
     */
    private static List<String> getDescriptors() {
        String[] desc = new String[]{"org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor"};
        
        return Arrays.asList(desc);
    }
    
    
    public static DescriptorEngine instantiate() {
        return new DescriptorEngine(getDescriptors());
    }
}
