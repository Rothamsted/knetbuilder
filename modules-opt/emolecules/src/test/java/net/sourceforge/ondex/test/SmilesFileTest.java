package net.sourceforge.ondex.test;

import java.io.File;
import java.util.Iterator;
import net.sourceforge.ondex.emolecules.io.Smile;
import net.sourceforge.ondex.emolecules.io.SmilesIteratorFactory;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author grzebyta
 */
@RunWith(JUnit4.class)
public class SmilesFileTest {
    private Logger log = LoggerFactory.getLogger(getClass());
    private Iterator<Smile> smiterator;
    //public String TEST_DATA_FILE = "target/test-classes/version.smi.gz";
    public String TEST_DATA_FILE = "D:/Downloads/version.smi.gz";
    
    @Before
    public void prepareFactory() throws Exception {
        log.info("prepare data factory");
        
        SmilesIteratorFactory fact = 
                new SmilesIteratorFactory(new File(TEST_DATA_FILE));
        Assert.assertNotNull("data factory must not be null", fact);
        
        smiterator = fact.iterator();
        Assert.assertNotNull("iterator cant be null", smiterator);
    }
    
    @Test
    public void testIteration() throws Exception {
        log.info("test iteration");
        log.info("*****"+ log.isDebugEnabled());
        
        log.debug("\tsmiterator: " + smiterator.hasNext());
        Assert.assertTrue("there are data in the file", smiterator.hasNext());
        
        int i=0;
        while(smiterator.hasNext() && i <= 100) {
            Smile smi = smiterator.next();
            log.debug("found: "+smi);
            log.debug(String.format("\tid: %d smile: %s", smi.getId(), smi.getSmile()));
            i++;
        }
    }
    
    
}
