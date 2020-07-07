package net.sourceforge.ondex.test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import java.util.Map;
import net.sourceforge.ondex.emolecules.cdk.CdkCalculator;
import net.sourceforge.ondex.emolecules.utils.BitSetUtils;
import org.apache.log4j.Logger;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openscience.cdk.qsar.DescriptorSpecification;
import org.openscience.cdk.qsar.DescriptorValue;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class CdkCalculatorTest {
    private CdkCalculator calc = new CdkCalculator();
    private Logger log = Logger.getLogger(getClass());
    
    private String smile;
    
    @Parameterized.Parameters
    public static Collection<String[]> parameters() {
        String[][] parms = new String[][] {
            {"OC[C@H]1OC(O)[C@H](O)[C@@H](O)[C@@H]1O"},
            {"C(C(C(C(C(=O)CO)O)O)O)O"},
            {"OC[C@H]1O[C@@H](O[C@H]2[C@H](O)[C@@H](O)[C@H](O)O[C@@H]2CO)[C@H](O)[C@@H](O)[C@H]1O"},
            {"[H][C@]1(CCCN1C)c1cccnc1"},
            {"CN1CCCC1c1ccc(O)nc1"}
        };
        
        return Arrays.asList(parms);
    }

    public CdkCalculatorTest(String smile) {
        this.smile = smile;
    }
    
    
    
    @Test
    public void simple() throws Exception {
        log.info("simple test");
        
        Map<Object, Object> props = calc.describe(smile);
        BitSet fp = calc.getFingerprint(smile);
        
        log.info("print description " + props.size());
        for (Object k: props.keySet()) {
            DescriptorSpecification kob = (DescriptorSpecification) k;
            DescriptorValue vob = (DescriptorValue) props.get(k);
            log.info("description: " + kob.getImplementationTitle());
            log.info("\tvalue: " + vob.getValue().toString());
            log.info("fingerprint: "+BitSetUtils.toString(fp));
        }
    }
}
