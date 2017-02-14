package net.sourceforge.ondex.test;

import java.util.List;
import net.sourceforge.ondex.emolecules.graph.Configuration;
import net.sourceforge.ondex.emolecules.graph.GraphLoadingService;
import net.sourceforge.ondex.test.utils.DefaultConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.JUnit4;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author grzebyta
 */
@RunWith(JUnit4.class)
public class GraphServiceTest {
    
    private static Logger log = LoggerFactory.getLogger(GraphServiceTest.class);
    private static Configuration conf;
    
    @BeforeClass
    public static void init() throws Exception {
        log.info("build configuration");
        conf = DefaultConfiguration.instantiate();
        conf.setInputFilePath("D:/Downloads/version.smi.gz");
    }
    
    @Test
    public void isConfigValid() throws Exception {
        log.info("is configValid test");
        
        log.debug("is configuration valid: " + conf.isValid());
        log.info("\tindex path: " + conf.getIndexDirectoryPath().getCanonicalPath());
        log.info("\tindex path: " + conf.getInputFilePath().getCanonicalPath());
        Assert.assertTrue("configuration should be valid", conf.isValid());
    }
    
    @Test
    public void testGraph() throws Exception {
        log.info("build test graph");
        
        GraphLoadingService gs = new GraphLoadingService(conf);
        gs.run();
        GraphDatabaseService gdb = gs.getGraphManager().getDatabase();
        Assert.assertNotNull("database service musn't be null", gdb);
        
        List<Node> allNodes = gs.getGraphManager().getAllNodes();
        Assert.assertTrue("empty list is wrong result. found: " + allNodes.size()
                ,allNodes.size() > 0);
        log.info("number of nodes: " + allNodes.size());
        
        gdb.shutdown();
    }
    
    //@AfterClass
    public static void finish() throws Exception {
        log.info("finish test");

        FileUtils.deleteDirectory(conf.getIndexDirectoryPath());
    }
     
}
