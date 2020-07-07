package net.sourceforge.ondex.transformer.subnetwork;

import static net.sourceforge.ondex.tools.functions.ControledVocabularyHelper.createAttName;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.BitSet;
import java.util.Collections;
import java.util.HashMap;
import java.util.HashSet;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.util.ArrayKey;
import net.sourceforge.ondex.tools.functions.StandardFunctions;
import net.sourceforge.ondex.transformer.ONDEXTransformer;

/**
 * Subnetwork transformer - identifies subnetworks in the PPI network based on the microarray data.
 *
 * @author lysenkoa
 */
public class Transformer extends ONDEXTransformer implements ArgumentNames {
    private final Set<ArrayKey<Integer>> processedNetworksUnSync = new HashSet<ArrayKey<Integer>>();
    private final Set<ArrayKey<Integer>> processedNetworksSync = new HashSet<ArrayKey<Integer>>();
    private static final int operationsTillSync = 80;
    private MicroarryDataHolder mdh = null;
    private final Random rnd = new Random(System.currentTimeMillis());
    private List<Exception> threadExceptions = Collections.synchronizedList(new ArrayList<Exception>());

    @Override
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return new ArgumentDefinition<?>[]{
                new StringArgumentDefinition(DATA_ATT_ARG, DATA_ATT_ARG_DESC, true, null, false),
                new StringArgumentDefinition(MARKER_ARG, MARKER_ARG_DESC, true, null, false),
                new StringArgumentDefinition(DISTANCE_ARG, DISTANCE_ARG_DESC, true, "2", false),
                new StringArgumentDefinition(INFO_ARG, INFO_ARG_DESC, true, "0.05", false),
                new StringArgumentDefinition(P1_ARG, P1_ARG_DESC, true, "0.05", false),
                new StringArgumentDefinition(P2_ARG, P2_ARG_DESC, true, "0.05", false),
                new StringArgumentDefinition(P3_ARG, P3_ARG_DESC, true, "0.00005", false),
                new StringArgumentDefinition(GROUP_NAME_ARG, GROUP_NAME_ARG_DESC, true, "Subnetwork", false)
        };
    }

    @Override
    public String getName() {
        return "Subnetwork transformer";
    }

    @Override
    public String getVersion() {
        return "v1.0";
    }

    @Override
    public String getId() {
        return "subnetwork";
    }

    @Override
    public boolean requiresIndexedGraph() {
        return false;
    }

    @Override
    public String[] requiresValidators() {
        return new String[0];
    }

    @Override
    public void start() throws Exception {
        String startsWith = convertToString(DATA_ATT_ARG);
        String marker = convertToString(MARKER_ARG);
        int distance = convertToInteger(DISTANCE_ARG);
        double r = convertToDouble(INFO_ARG);
        String groupname = convertToString(GROUP_NAME_ARG);
        AttributeName gr = createAttName(graph, groupname, String.class);
        double p1 = convertToDouble(P1_ARG);
        double p2 = convertToDouble(P2_ARG);
        double p3 = convertToDouble(P3_ARG);

        int threads = Runtime.getRuntime().availableProcessors() * 3;
        List<List<Integer>> tasks = new ArrayList<List<Integer>>(threads);
        for (int i = 0; i < threads; i++) {
            tasks.add(new ArrayList<Integer>());
        }

        mdh = new MicroarryDataHolder(startsWith, marker, graph, threads);

        for (ONDEXConcept c : graph.getConcepts()) {
            mdh.processConcept(c);
        }

        List<Searcher> searchers = new ArrayList<Searcher>(threads);
        for (List<Integer> task : tasks) {
            Searcher s = new Searcher(task, r, distance, operationsTillSync);
            searchers.add(s);
        }

        ThreadPoolExecutor threadPool = new ThreadPoolExecutor(threads, threads, Long.MAX_VALUE, TimeUnit.DAYS, new ArrayBlockingQueue<Runnable>(threads));
        for (int remaining = tasks.get(0).size(); remaining > 0; remaining = remaining - operationsTillSync) {
            for (Searcher searcher : searchers) {
                threadPool.execute(searcher);
            }
            threadPool.awaitTermination(Long.MAX_VALUE, TimeUnit.DAYS);
            if (threadExceptions.size() > 0) {
                throw threadExceptions.get(0);
            }
            processedNetworksUnSync.addAll(processedNetworksSync);
            processedNetworksSync.clear();
        }

        threadPool.shutdown();
        threadPool = null;
        processedNetworksUnSync.clear();
        processedNetworksSync.clear();

        Set<ScoredMISubnet> subnets = new HashSet<ScoredMISubnet>();
        for (Searcher searcher : searchers) {
            subnets.addAll(searcher.getSubnets());
        }


        int id = 1;
        for (ScoredMISubnet subnet : subnets) {
            for (Integer member : subnet.getMembers()) {
                if (graph.getConcept(member).getAttribute(gr) == null)
                    graph.getConcept(member).createAttribute(gr, id, false);
            }
            id++;
        }
    }

    private class Searcher implements Runnable {
        private final List<Integer> ids;
        private final int returnAfter;
        private final Set<ScoredMISubnet> subnetworks = new HashSet<ScoredMISubnet>();
        private final double infoGain;
        private final int neighbourhoodSize;
        int current = 0;
        private final Set<Integer> processed = new HashSet<Integer>();

        public Searcher(List<Integer> ids, double r, int distance, int returnAfter) {
            this.ids = ids;
            this.infoGain = r;
            this.neighbourhoodSize = distance;
            this.returnAfter = returnAfter;
        }

        @Override
        public void run() {
            int i = current;
            for (; i < (current + returnAfter); i++) {
                if (i >= ids.size())
                    return;
                ONDEXConcept seed = graph.getConcept(ids.get(i));
                try {
                    ScoredMISubnet subnet = new ScoredMISubnet(seed, processed, neighbourhoodSize, infoGain);
                    BitSet neighbours = StandardFunctions.getNeighboursAtLevel(seed, graph, neighbourhoodSize)[0];
                    List<Double> random100 = new ArrayList<Double>(100);

                    for (int j = 0; j < 100; j++) {
                        //random100.add(mdh.getMIForSubnetwork(drawRandomSubset(seed, graph, 2, rnd)));
                    }
                    subnetworks.add(subnet);
                }
                catch (Exception e) {
                    threadExceptions.add(e);
                    return;
                }
            }
            current = i;
            processed.clear();
        }

        public Set<ScoredMISubnet> getSubnets() {
            return subnetworks;
        }
    }

    public boolean isProcessed(final Integer[] array) {
        final Integer[] sorted = Arrays.copyOf(array, array.length);
        Arrays.sort(sorted);
        final ArrayKey<Integer> key = new ArrayKey<Integer>(sorted);
        if (processedNetworksUnSync.contains(key)) {
            return true;
        }
        synchronized (processedNetworksSync) {
            if (processedNetworksSync.contains(key)) {
                return true;
            }
            processedNetworksSync.add(key);
            if (sorted.length > 2) {
                for (int i = sorted.length - 2; i > 2; i--) {
                    final ArrayKey<Integer> temp = new ArrayKey<Integer>(Arrays.copyOfRange(sorted, 0, i));
                    if (processedNetworksSync.contains(temp))
                        break;
                    processedNetworksSync.add(temp);
                }
            }
            return false;
        }
    }

    public String convertToString(String arg) throws Exception {
        Object o = args.getUniqueValue(arg);
        if (o == null) {
            throw new IllegalArgumentException("Argument " + arg + " is required, but wass not supplied");
        }
        String value = o.toString();
        return value;
    }

    public double convertToDouble(String arg) throws Exception {
        Object o = args.getUniqueValue(arg);
        if (o == null) {
            throw new IllegalArgumentException("Argument " + arg + " is required, but wass not supplied");
        }
        double value = 0.0;
        try {
            value = Double.valueOf(o.toString());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Argument " + arg + " should be a valid double number");
        }
        return value;
    }

    public int convertToInteger(String arg) throws Exception {
        Object o = args.getUniqueValue(arg);
        if (o == null) {
            throw new IllegalArgumentException("Argument " + arg + " is required, but wass not supplied");
        }
        int value = 0;
        try {
            value = Integer.valueOf(o.toString());
        }
        catch (NumberFormatException e) {
            throw new IllegalArgumentException("Argument " + arg + " should be a valid integer number");
        }
        return value;
    }

    private class ScoredMISubnet {
        private List<Integer> ids = new LinkedList<Integer>();
        private final Integer[] network;
        private Set<Integer> processed;
        private final int maxLevel;
        private final double r;
        private double networkMI;

        public ScoredMISubnet(ONDEXConcept seed, Set<Integer> processed, int maxLevel, double r) throws Exception {
            this.maxLevel = maxLevel;
            this.r = r;
            this.processed = processed;
            processedAddiotons(processSeed(seed), 1);
            this.network = ids.toArray(new Integer[ids.size()]);
            this.processed = null;
            ids = null;
        }

        public Integer[] getMembers() {
            return network;
        }

        public double getMIScore() {
            return networkMI;
        }

        private void processedAddiotons(List<Integer> additions, int currentlevel) throws Exception {
            if (currentlevel > maxLevel)
                return;
            ids.addAll(additions);
            List<Integer> newAdditions = new LinkedList<Integer>();
            for (Integer addition : additions) {
                newAdditions.addAll(processSeed(graph.getConcept(addition)));
            }
            if (newAdditions.size() > 0)
                processedAddiotons(newAdditions, ++currentlevel);
        }

        private List<Integer> processSeed(final ONDEXConcept seed) throws Exception {
            final Set<ONDEXConcept> neighbours = StandardFunctions.getOtherNodes(graph, seed);
            final List<Integer> result = new LinkedList<Integer>();
            final Integer[] candidateSubnet = new Integer[ids.size() + 1];
            for (int i = 0; i < ids.size(); i++) {
                candidateSubnet[i] = ids.get(i);
            }
            for (ONDEXConcept neighbour : neighbours) {
                final int id = neighbour.getId();
                candidateSubnet[candidateSubnet.length - 2] = id;
                if (processed.contains(id))
                    continue;
                processed.add(id);
                if (isProcessed(candidateSubnet))
                    continue;
                double newMI = mdh.getMIForSubnetwork(candidateSubnet);

                if ((networkMI - newMI) > r) {
                    result.add(id);
                }
            }
            return result;
        }

        @Override
        public int hashCode() {
            return Arrays.hashCode(network);
        }

        public int size() {
            return network.length;
        }

        @Override
        public boolean equals(Object key) {
            if (key instanceof ScoredMISubnet)
                return Arrays.equals(this.network, ((ScoredMISubnet) key).network);
            return false;
        }
    }

    /**
     * Draws a random sized subset from the source array, of size at least min.
     *
     * @param source
     * @param min
     * @param rnd
     * @return
     */
    private static Integer[] drawRandomSubset(final int seed, final int[] source, final int min, final Random rnd) {
        int size = min + (int) (rnd.nextFloat() * (source.length - min));
        Integer[] result = new Integer[size];
        int[] shuffled = Arrays.copyOf(source, source.length);
        shuffle(shuffled, rnd);

        for (int i = 1; i < size; i++) {
            result[i] = shuffled[i - 1];
        }
        return result;
    }

    /**
     * @param array
     * @param rnd
     * @author toxiclibs - code snippet from toxi.util.datatypes
     * Rearranges the array items in random order using the given RNG. Operation
     * is in-place, no copy is created.
     */
    private static void shuffle(final int[] array, final Random rnd) {
        final int N = array.length;
        for (int i = 0; i < N; i++) {
            int r = i + (int) (rnd.nextFloat() * (N - i)); // between i and N-1
            int swap = array[i];
            array[i] = array[r];
            array[r] = swap;
        }
    }

    public static void getRandomSubnet(final int seed, final ONDEXGraph graph, final int level) {
        List<Integer> result = new ArrayList<Integer>();

    }

    private class RandomSubgraph {
        private final Random rnd = new Random(System.currentTimeMillis());
        private final List<RandomSubgraph> branches = new ArrayList<RandomSubgraph>();
        private final ONDEXConcept seed;
        private final int max;
        private Map<Integer, Set<Integer>> levels = new HashMap<Integer, Set<Integer>>();

        public RandomSubgraph(ONDEXConcept seed, int maxDepth) {
            this.seed = seed;
            this.max = StandardFunctions.getNeighboursAtLevel(seed, graph, maxDepth)[0].length();

        }

        public int[] getSubnet(int distance, int min) {
            int subset = 2 + (int) (rnd.nextFloat() * (max - 1));
            int[] result = new int[subset];

            for (int i = 0; i < subset; i++) {
				
			}
			return result;
		}
	}
}

