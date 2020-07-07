package net.sourceforge.ondex.test;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import net.sourceforge.ondex.emolecules.graph.Configuration;
import net.sourceforge.ondex.emolecules.graph.GraphLoadingService;
import net.sourceforge.ondex.parser.emolecules.TanimotoService;
import net.sourceforge.ondex.test.utils.DefaultConfiguration;
import org.apache.commons.io.FileUtils;
import org.apache.log4j.Logger;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.junit.runner.RunWith;
import org.junit.runners.Parameterized;
import org.neo4j.graphdb.Node;

/**
 *
 * @author grzebyta
 */
@RunWith(Parameterized.class)
public class TanimotoCoefTest {

    private static Logger log = Logger.getLogger(TanimotoCoefTest.class);
    private static Configuration config;
    private String smiles;

    @Parameterized.Parameters
    public static Collection<String[]> getParameters() {

        String[][] parms = new String[][]{{"CC(=O)C(=O)c1ccccc1"},
            {"[H]C(=O)C(=O)[C@@H](O)[C@H](O)[C@H](O)CO"},
            {"OC[C@@H](O)[C@@H](O)[C@H](O)C(=O)CO"}, 
            {"C[C@H](O)[C@@H](O)[C@@H](O)C(=O)CO"}};

        //String[][] parms = new String[][]{{"CC(=O)C(=O)c1ccccc1"}};

        return Arrays.asList(parms);
    }

    public TanimotoCoefTest(String smiles) {
        this.smiles = smiles;
    }

    @BeforeClass
    public static void preload() throws Exception {
        log.info("init");
        config = DefaultConfiguration.instantiate();
        config.setInputFilePath("D:/Downloads/version.smi.gz");

        // loading data
        log.debug("load data ...");
        GraphLoadingService loading = new GraphLoadingService(config);
        loading.run();
        log.debug("data loaded");
        loading.getGraphManager().getDatabase().shutdown();
    }

    @Test
    public void tanimotoCoefTest() throws Exception {
        log.info("*** run tanimoto test");

        log.info("target: " + smiles);
        long t1 = System.currentTimeMillis();
        TanimotoService tSrv = new TanimotoService(config);
        Map<Node, Float> resut = tSrv.run(smiles);
        long t2 = System.currentTimeMillis();
        log.info("results: " + resut.size());
        log.info("\t &&&& time: " + (t2-t1));

        Assert.assertTrue("results are empty", !resut.isEmpty());

        List<Long> ids = new ArrayList<Long>(0);
        for (Node n : resut.keySet()) {
            ids.add(n.getId());
        }
        Collections.sort(ids);
        log.info("nodes: " + ids);
        tSrv.getGraphManager().getDatabase().shutdown();
    }

    @AfterClass
    public static void finish() throws Exception {
        log.info("finish test");

        FileUtils.deleteDirectory(config.getIndexDirectoryPath());
    }
}
