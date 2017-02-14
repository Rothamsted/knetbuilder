package net.sourceforge.ondex.cdk.test;

import java.util.Arrays;
import java.util.Collection;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.openscience.cdk.DefaultChemObjectBuilder;
import org.openscience.cdk.config.IsotopeFactory;
import org.openscience.cdk.interfaces.IIsotope;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class IsotopeTest {
    
    private static Logger log = Logger.getLogger(IsotopeTest.class);
    private double expected;
    private String atom;
    
    @Parameterized.Parameters
    public static Collection<String[]> init() {
        String[][] prm = new String[][]{{"H:1.01"}, {"O:16"}, {"C:12.01"}};
        
        return Arrays.asList(prm);
    }

    public IsotopeTest(String param) {
        String[] sptd = param.split(":");
        this.atom = sptd[0];
        this.expected = Double.valueOf(sptd[1]);
    }
    
    @Test
    public void hydrogenMassTest() throws Exception {
        log.info("hudrogen mass test");
        IsotopeFactory factory = IsotopeFactory
                .getInstance(DefaultChemObjectBuilder.getInstance());
        
        IIsotope major = factory.getMajorIsotope(this.atom);
        double calc = major.getExactMass().doubleValue();
        log.info("expected mass: " + this.expected);
        log.info("calculated mass: " + calc);
        
        Assert.assertEquals("numbers should be the same", this.expected, calc, 0.01);
    }
}
