package net.sourceforge.ondex.emolecules.graph;

import java.io.IOException;
import java.io.Serializable;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.Iterator;
import java.util.List;
import java.util.Map;
import org.neo4j.cypher.javacompat.ExecutionEngine;
import org.neo4j.cypher.javacompat.ExecutionResult;
import org.neo4j.graphdb.GraphDatabaseService;
import org.neo4j.graphdb.Node;
import org.neo4j.graphdb.factory.GraphDatabaseFactory;
import org.neo4j.helpers.collection.IteratorUtil;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/**
 *
 * @author grzebyta
 */
public class GraphManager implements Serializable {

    private static final long serialVersionUID = 2742110805153162647L;
    private Logger log = LoggerFactory.getLogger(getClass());
    private Map<Long, Node> maps = new HashMap<Long, Node>();
    private GraphDatabaseService graphDb;
    private Configuration config;

    private GraphManager(Configuration c) throws IllegalArgumentException, IOException {
        this.config = c;
    }

    /**
     * Main factory method. It calls {@link #init() } method.
     *
     * @param conf
     * @return
     * @throws IllegalArgumentException
     * @throws IOException
     */
    public static GraphManager instantiate(Configuration conf)
            throws IllegalArgumentException, IOException {
        GraphManager toReturn = new GraphManager(conf);
        toReturn.init();
        return toReturn;
    }

    /**
     * Builds graph database based on configuration
     */
    protected void init() throws IllegalArgumentException, IOException {
        log.debug("initialise database");
        if (config == null) {
            throw new IllegalArgumentException("configuration can't be null");
        }

        // create index directory if it's necessary
        createDbPath();

        // build database interface
        graphDb = new GraphDatabaseFactory()
                .newEmbeddedDatabase(config.getIndexDirectoryPath().getAbsolutePath());
        registerShutdownHook(graphDb);
    }

    /**
     * Creates database directory
     *
     * @throws IOException
     * @throws IllegalArgumentException
     */
    private void createDbPath() throws IOException, IllegalArgumentException {
        if (!config.isValid()) {
            throw new IllegalArgumentException("configuration object is not valid");
        }

        if (!config.getIndexDirectoryPath().exists()) {
            config.getIndexDirectoryPath().mkdirs();
        } else {
            if (!config.getIndexDirectoryPath().isDirectory()) {
                throw new IOException("index can't be build cos found a file with the same name : "
                        + config.getIndexDirectoryPath());
            }
        }

    }

    /**
     * Method took directly from <a
     * href="http://docs.neo4j.org/chunked/milestone/tutorials-java-embedded-setup.html#tutorials-java-embedded-setup-startstop">Neo4J
     * manual</a>.
     *
     * @param graphDb
     */
    // START SNIPPET: shutdownHook
    private static void registerShutdownHook(final GraphDatabaseService graphDb) {
        // Registers a shutdown hook for the Neo4j instance so that it
        // shuts down nicely when the VM exits (even if you "Ctrl-C" the
        // running example before it's completed)
        Runtime.getRuntime().addShutdownHook(new Thread() {
            @Override
            public void run() {
                graphDb.shutdown();
            }
        });
    }
    // END SNIPPET: shutdownHook

    public GraphDatabaseService getDatabase() {
        return graphDb;
    }

    /**
     * Creates a new node with set id. Also puts a node to local memory index or
     * returns from it if was created earlier.
     *
     * @param id
     * @return
     */
    public Node createChemicalNode(Long id) {
        log.debug("create a node with id: " + id);
        if (maps.containsKey(id)) {
            return maps.get(id);
        } else {
            Node node = graphDb.createNode();
            node.setProperty("id", id);

            // set node type
            node.setProperty("type", "chemical");

            maps.put(id, node);
            return node;
        }
    }

    /**
     * Return all nodes from the graph.
     * @param gs
     * @return 
     */
    public List<Node> getAllNodes() {
        GraphDatabaseService gs = getDatabase();
        log.debug("get all nodes");

        String q = "START n=node(*) RETURN n;";
        ExecutionEngine ee = new ExecutionEngine(gs);
        ExecutionResult result = ee.execute(q);

        Iterator<Node> resIter = result.columnAs("n");
        log.debug("list of columns: " + result.columns());

        ArrayList<Node> toReturn = new ArrayList<Node>(0);
        IteratorUtil.addToCollection(resIter, toReturn);

        for (Node node : toReturn) {
            if (node.getId() != 0) {
                log.debug(String.format("\tnode: %s\t id: %d\tm: %s", node, node.getProperty("id", 0), node.getProperty("m", 0)));
            } else {
                log.debug(String.format("\tnode: %s\t id: %d\t", node, node.getProperty("id", 0)));
            }
        }

        return toReturn;
    }
    
    public void shutdown() {
        graphDb.shutdown();
    }
}
