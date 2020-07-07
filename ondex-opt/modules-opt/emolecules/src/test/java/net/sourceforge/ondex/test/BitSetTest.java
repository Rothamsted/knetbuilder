package net.sourceforge.ondex.test;

import java.util.Arrays;
import java.util.BitSet;
import java.util.Collection;
import net.sourceforge.ondex.emolecules.utils.BitSetUtils;
import org.apache.log4j.Logger;
import org.junit.Assert;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class BitSetTest {
    
    private Logger log = Logger.getLogger(getClass());
    private BitSet expected;
    private String bin;
    
    @Parameterized.Parameters
    public static Collection<Object[]> getParameters() {
        Object[][] toReturn = new Object[][]{{"100",new int[]{2}}, {"1", new int[]{0}}, {"100101", new int[]{0,2,5}}, {"1111", new int[]{0,1,2,3}}, {"10001", new int[]{0,4}}};
        
        return Arrays.asList(toReturn);
    }

    public BitSetTest(String bin, int[] var) {
        this.bin = bin;
        this.expected = new BitSet(var.length);
        
        for (int v: var) {
            expected.set(v);
        }
    }
    
    @Test
    public void test() {
        log.info("test bitset");
        log.info("value: " + bin);
        log.info("expected: " + expected);
        
        BitSet bit = BitSetUtils.fromString(bin);
        log.info("bitset: " + bit);
        
        Assert.assertEquals(expected, bit);
    }
    
    @Test
    public void toStringTest() throws Exception {
        log.info("test to String");
        
        log.info("now expected: " + bin);
        String val = BitSetUtils.toString(expected);
        log.info("value: " + val);
        Assert.assertEquals(bin, val);
    }
    
}
