package net.sourceforge.ondex.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import net.sourceforge.ondex.emolecules.graph.Configuration;
import net.sourceforge.ondex.emolecules.graph.GraphLoadingService;
import net.sourceforge.ondex.emolecules.graph.GraphManager;
import net.sourceforge.ondex.test.utils.DefaultConfiguration;
import org.apache.commons.io.FileUtils;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.junit.runners.Parameterized.Parameters;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.index.Index;
import org.neo4j.graphdb.index.IndexHits;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class IndexTest {

    private static Logger log = LoggerFactory.getLogger(IndexTest.class);
    private static Configuration conf;
    private static GraphManager gm;
    private static String ID_INDEX_NAME = "chemicalId";
    private static String SMILE_INDEX_NAME = "chemicalSmile";
    private Long id;
    // id based index
    private Index<Node> idx;
    // smiles based index
    private Index<Node> chx;

    public IndexTest(Long id) {
        this.id = id;
    }

    @BeforeClass
    public static void init() throws Exception {
        log.info("initialise test");

        conf = DefaultConfiguration.instantiate();

        GraphLoadingService gls = new GraphLoadingService(conf);
        gls.run();

        gm = gls.getGraphManager();
    }

    @Parameters
    public static Collection<Long[]> params() {
        //Long[][] data = new Long[][]{{14298L}, {15421L}, {13388L}, {10029L}};
        Long[][] data = new Long[][]{{15421L}, {13388L}, {10029L}, {14298L}, {14643L}};

        return Arrays.asList(data);
    }

    @Before
    public void initTest() throws Exception {
        log.info("prepare to test");

        idx = gm.getDatabase().index().forNodes(ID_INDEX_NAME);
        chx = gm.getDatabase().index().forNodes(SMILE_INDEX_NAME);

    }

    @Test
    public void testIdIndex() throws Exception {
        log.info("test node id: " + id);

        /*
         List<Node> out = getAllNodes(gm.getDatabase());
         for (Node n : out) {
         log.debug("out: " + n.getProperty("id", 0));
         }*/
        IndexHits<Node> res = idx.get("id", id);
        log.debug("number of hits: " + res.size());
        try {
            Node n = res.getSingle();
            res.close();
            Assert.assertNotNull("node can't be null. Null node for id: " + id, n);
            Long nId = (Long) n.getProperty("id");
            Assert.assertEquals("ids must be equal", id, nId);
        } finally {
            res.close();
        }
    }

    protected List<Node> getAllNodes(GraphDatabaseService gs) {
        log.debug("get all nodes");

        String q = "START n=node(*) RETURN n;";
        ExecutionEngine ee = new ExecutionEngine(gs);
        ExecutionResult result = ee.execute(q);

        Iterator<Node> resIter = result.columnAs("n");
        log.debug("list of columns: " + result.columns());

        ArrayList<Node> toReturn = new ArrayList<Node>(0);
        IteratorUtil.addToCollection(resIter, toReturn);

        for (Node node : toReturn) {
            log.debug(String.format("\tnode: %s\t id: %d", node, node.getProperty("id", 0)));
        }

        return toReturn;
    }

    @AfterClass
    public static void finish() throws Exception {
        log.info("finish test");
        gm.getDatabase().shutdown();

        FileUtils.deleteDirectory(conf.getIndexDirectoryPath());
    }
}
