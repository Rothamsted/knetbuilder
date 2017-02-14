package net.sourceforge.ondex.emolecules.cdk;

import java.util.BitSet;
import java.util.Map;
import org.apache.log4j.Logger;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.Molecule;
import org.openscience.cdk.exception.CDKException;
import org.openscience.cdk.exception.InvalidSmilesException;
import org.openscience.cdk.fingerprint.Fingerprinter;
import org.openscience.cdk.qsar.DescriptorEngine;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;
import org.openscience.cdk.smiles.SmilesParser;

/**
 * Calculate basic chemical characteristic based on smile
 *
 * @author grzebyta
 */
public class CdkCalculator {
    private Logger log = Logger.getLogger(getClass());
    private DescriptorEngine de = CustomMolecularDescriptorEngineFactory.instantiate();
    private Fingerprinter fp = new Fingerprinter();
    
    

    /**
     * Calculate basic description
     *
     * @param smiles
     * @return
     */
    public Map<Object, Object> describe(String smiles)
            throws InvalidSmilesException, CDKException {
        log.debug("describe molecule: " + smiles);
        Molecule m = getMolecule(smiles);
        
        log.debug(m.getClass());

        // process calculations
        de.process(m);

        return m.getProperties();
    }
    
    public double getMolecularMass(String str) throws RuntimeException {
        try{
            Map<Object, Object> out = describe(str);
            
            for (Object k: out.keySet()) {
                DescriptorSpecification spec = (DescriptorSpecification) k;
                if (spec.getImplementationTitle()
                        .equals("org.openscience.cdk.qsar.descriptors.molecular.WeightDescriptor")) {
                    DescriptorValue v = (DescriptorValue) out.get(k);
                    
                    return Double.valueOf(v.getValue().toString());
                }
                
            }
        } catch (Exception e) {
            throw new RuntimeException(e);
        } 
        
        return 0;
    }
    
    private Molecule getMolecule(String smiles) throws InvalidSmilesException {
        SmilesParser sp = new SmilesParser(DefaultChemObjectBuilder.getInstance());
        
        return (Molecule) sp.parseSmiles(smiles);
    }
    
    public BitSet getFingerprint(String smiles) throws CDKException {
        return fp.getFingerprint(getMolecule(smiles));
    }
}
