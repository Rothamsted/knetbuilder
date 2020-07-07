package net.sourceforge.ondex.parser.emolecules;

import java.util.ArrayList;
import java.util.BitSet;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import net.sourceforge.ondex.emolecules.cdk.CdkCalculator;
import net.sourceforge.ondex.emolecules.graph.Configuration;
import net.sourceforge.ondex.emolecules.graph.GraphManager;
import net.sourceforge.ondex.emolecules.graph.ServiceI;
import net.sourceforge.ondex.parser.emolecules.TanimotoRunnable.Result;
import org.apache.log4j.Logger;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.openscience.cdk.exception.CDKException;

/**
 *
 * @author grzebyta
 */
public class TanimotoService implements ServiceI {

    private GraphManager gm;
    private static Logger log = Logger.getLogger(TanimotoService.class);
    private List<Node> allNodes;
    private int numberOfThreads = 0;
    private List<Thread> threads = new ArrayList<Thread>(0);
    private Map<Node, Float> results;
    private CdkCalculator calc = new CdkCalculator();

    public TanimotoService(Configuration conf) throws Exception {
        gm = GraphManager.instantiate(conf);
    }

    /**
     * Give a task to threads
     *
     * @return
     */
    public Node[] getTask() {
        if (allNodes == null) {
            allNodes = gm.getAllNodes();

            // instantiate results in proper size
            if (results == null) {
                results = new HashMap<Node, Float>(allNodes.size());
            }
        }

        return allNodes.toArray(new Node[]{});
    }

    /**
     * Get results from threads
     *
     * @param nodeId
     * @param result
     */
    public synchronized void registerResults(List<TanimotoRunnable.Result> data) {
        log.debug("have data from thread");
        log.debug("data size: " + data.size());

        GraphDatabaseService dbServ = gm.getDatabase();

        for (Result record : data) {
            Node node = dbServ.getNodeById(record.getId());
            results.put(node, record.getResult());
        }
    }

    /**
     * Calculate the number of threads.
     *
     * @return
     */
    private int numberOfThreads() {

        if (numberOfThreads == 0) {
            numberOfThreads = Runtime.getRuntime().availableProcessors() * 2;
        }

        return numberOfThreads;

        //return 1;
    }

    public Map<Node, Float> run(String target) throws CDKException, InterruptedException {
        log.info("do main action");

        BitSet trgt = calc.getFingerprint(target);

        // create and start threads
        for (int i = 0; i < numberOfThreads(); i++) {
            log.info("start thread: " + i);
            TanimotoRunnable tanRunn =
                    new TanimotoRunnable(this, trgt, i, numberOfThreads());
            Thread t = new Thread(tanRunn, "thread: " + i);
            t.start();

            threads.add(t);
        }

        // do test until all threads gone from list

        for (Thread t : threads) {
            t.join();
        }

        return results;
    }

    public GraphManager getGraphManager() {
        return gm;
    }
}
