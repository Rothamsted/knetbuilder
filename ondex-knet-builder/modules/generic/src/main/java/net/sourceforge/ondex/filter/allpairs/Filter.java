package net.sourceforge.ondex.filter.allpairs;

import java.util.BitSet;
import java.util.HashSet;
import java.util.Hashtable;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.InvalidPluginArgumentException;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;
import net.sourceforge.ondex.tools.threading.monitoring.Monitorable;
import net.sourceforge.ondex.tools.threading.monitoring.MonitoringToolKit;

/**
 * The all pairs shortest path filter is an implementation of the Floyd-Warshall
 * algorithm. It constructs a connection matrices from the graph and then runs on
 * the matrices. See Cormen's "Introduction to Algorithms" p.629 for details.
 * The matrices can be either represented as full 2D-arrays or as sparse
 * implementations based on Hashtables. The algorithm automatically decides whether
 * it is more memory efficient to use the one or the other matrix representation.
 * This can be overridden by setting the constant <code>ALWAYS_USE_SPARSE</code>
 * to <code>true</code>.
 *
 * @author Jochen Weile, B.Sc.
 */
@Authors(authors = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames, Monitorable {

    //####FIELDS####

    /**
     * The AttributeName that holds the Attribute containing the edge weights.
     */
    private AttributeName weightAttributeName;

//	/**
//	 * Determines whether the edge weights are taken from the Attribute or not.
//	 * If not, all edges are considered to be of weight 1.
//	 */
//	private boolean useWeights;

    /**
     * Determines whether the graph is considered directed or undirected.
     */
    private boolean onlyDirected;

    /**
     * inverse weights?
     */
    private boolean inverse;

    /**
     * This is the output set containing all relations that are still visible after
     * the use of the filter.
     */
    private Set<Integer> relationBitSet;

    /**
     * States if the algorithm is done working yet.
     */
    private boolean ready = false;

    /**
     * contains the argument definitions that determine the needed input for this algorithm.
     */
    private ArgumentDefinition<?>[] argdefs;

    /**
     * This Hashtable encodes the internal indices for the used matrices.
     */
    private Hashtable<Integer, Integer> concept2index;

    /**
     * This Hashtable decodes the internal indices for the used matrices.
     */
    //private Hashtable<Integer,Integer> index2concept;

    /**
     * This is a lookup matrix. It represents all relations encoded by
     * their from- and to-concepts
     */
    private Matrix<Integer> simpleRelations = null;

    /**
     * This is the input matrix. it contains all edge weights for edges between given
     * from- and to-concepts.
     */
    private Matrix<Double> simpleWeights = null;

    /**
     * This is the computation- and output-matrix. It is rewritten in each phase of
     * the algorithm. It contains all path lengths between all pairs of concepts.
     */
    private Matrix<Double> lastWeights = null;

    /**
     * This is the backtracing initiation matrix it is necessary to support backtracing.
     */
    private Matrix<Integer> simplePredecessors = null;

    /**
     * This is the backtracing matrix. It is rewritten in each phase of the algorithm.
     * It contains for each pair (i,j) the predecessor of j on a path from i.
     */
    private Matrix<Integer> lastPredecessors = null;

    /**
     * The number of concepts contained in the graph. This determines the matrix sizes.
     * Each matrix has the size <code>num_concepts ^ 2</code>.
     */
    private int num_concepts;

    /**
     * This constant can be used to override the automatic matrix representation handling.
     * By setting it to true, all matrices will be initialised as sparse matrices.
     */
    public static final boolean ALWAYS_USE_SPARSE = false;

    //THE FOLLOWING VARIABLES ARE FOR SUPPORT OF net.sourceforge.ondex.tools.Monitorable
    /**
     * current progress of the algorithm.
     */
    private int progress = 0;

    /**
     * The maximal progress of the algorithm.
     * This value is determined properly after the start() method has been called.
     * Before that it is provisionally set to Integer.MAX_VALUE .
     */
    private int max_progress = Integer.MAX_VALUE;

    /**
     * The minimal progress.
     */
    private int min_progress = 0;

    /**
     * The current state of computation.
     */
    private String state;

    /**
     * determines whether the user has decided to cancel the filter computation.
     */
    private boolean cancelled = false;

    /**
     * uncaught throwable.
     */
    private Throwable uncaught = null;


    //####CONSTRUCTOR####

    /**
     * NB: Overrides previous instance logging
     */
    public Filter() {
        argdefs = new ArgumentDefinition[]{
//					new BooleanArgumentDefinition(USEWEIGHTS_ARG, USEWEIGHTS_ARG_DESC, false, false),
                new StringArgumentDefinition(WEIGHTATTRIBUTENAME_ARG, WEIGHTATTRIBUTENAME_ARG_DESC, true, null, false),
                new BooleanArgumentDefinition(ONLYDIRECTED_ARG, ONLYDIRECTED_ARG_DESC, false, false),
                new BooleanArgumentDefinition(INVERSE_WEIGHT_ARG, INVERSE_WEIGHT_ARG_DESC, false, false)
        };

        state = Monitorable.STATE_IDLE;
    }


    //####METHODS####

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#start()
     */
    public void start() {

        try {
            state = "fetching arguments...";
            fetchArguments();

            state = "preprocessing...";
            buildMatrices();

            state = "running algorithm...";
            runFloydWarshall();

            if (!cancelled) {
                state = "postprocessing...";
                applyToGraph();
            }

            ready = true;
            state = "done";
        } catch (Throwable t) {
            uncaught = t;
        }
    }

    /**
     * constructs all input matrices and lookup tables necessary for the
     * algorithm to perform.
     */
    private void buildMatrices() {

        int lastID = 0;

        concept2index = new Hashtable<Integer, Integer>();

        for (ONDEXConcept c : graph.getConcepts()) {
            concept2index.put(c.getId(), lastID);
            lastID++;
        }

        assert (num_concepts == lastID);

        long num_relations = graph.getRelations().size();

        boolean useSparse = (num_concepts > (long) Integer.MAX_VALUE)
                || (num_relations < (long) (Math.pow((double) num_concepts, 2) * 0.09))
                || ALWAYS_USE_SPARSE;

        if (useSparse) {
            simpleWeights = new SparseMatrix<Double>(num_concepts, onlyDirected, Double.POSITIVE_INFINITY);
            simpleRelations = new SparseMatrix<Integer>(num_concepts, onlyDirected, null);
            simplePredecessors = new SparseMatrix<Integer>(num_concepts, true, null);
        } else {
            simpleWeights = new FullMatrix<Double>(num_concepts, onlyDirected, Double.POSITIVE_INFINITY);
            simpleRelations = new FullMatrix<Integer>(num_concepts, onlyDirected, null);
            simplePredecessors = new FullMatrix<Integer>(num_concepts, true, null);
        }

        for (ONDEXRelation r_curr : graph.getRelations()) {
            int rid = r_curr.getId();
            ONDEXConcept from = r_curr.getFromConcept();
            int fcid = from.getId();
            int myfcid = concept2index.get(fcid);
            ONDEXConcept to = r_curr.getToConcept();
            int tcid = to.getId();
            int mytcid = concept2index.get(tcid);
            double weight = getWeight(r_curr);

            simpleWeights.set(myfcid, mytcid, weight);
            simpleRelations.set(myfcid, mytcid, rid);
            simplePredecessors.set(myfcid, mytcid, myfcid);
        }
    }

    /**
     * retrieves the edge weight for a given relation. This method is used
     * by the <code>buildMatrices()</code> method.
     *
     * @param r_curr The relation from which to extract the edge weight.
     * @return The edge weight.
     */
    private double getWeight(ONDEXRelation r_curr) {
        Attribute weightAttribute = r_curr.getAttribute(weightAttributeName);
        double w;
        if ((weightAttribute != null) && (weightAttribute.getValue() instanceof Number))
            w = ((Number) weightAttribute.getValue()).doubleValue();
        else
            w = 1.0;
        if (inverse)
            w = 1.0 - w;
        return w;
    }

    /**
     * runs the actual algorithm. see Cormen's "Introduction to Algorithm" p.269
     * for details. The memory usage is reduced to O(n^2) by only memorising the last
     * used matrix instead of saving all matrices.
     */
    private void runFloydWarshall() {

        lastWeights = simpleWeights;
        lastPredecessors = simplePredecessors;

        Matrix<Double> currWeights = null;
        Matrix<Integer> currPredecessors = null;

        double min, d_ij, d_ik, d_kj;
        Integer pre, pre_ij, pre_kj;

        long before = System.currentTimeMillis();

        for (int k = 0; k < num_concepts; k++) {//phase
            if (lastWeights.suggestNextInstance().equals("sparse") || ALWAYS_USE_SPARSE) {
                currWeights = new SparseMatrix<Double>(num_concepts, onlyDirected, Double.POSITIVE_INFINITY);
                currPredecessors = new SparseMatrix<Integer>(num_concepts, true, null);
            } else {
                currWeights = new FullMatrix<Double>(num_concepts, onlyDirected, Double.POSITIVE_INFINITY);
                currPredecessors = new FullMatrix<Integer>(num_concepts, true, null);
            }
            for (int i = 0; i < num_concepts; i++) {//row
                progress = k * num_concepts + i;
                for (int j = 0; j < num_concepts; j++) {//column
                    d_ij = lastWeights.get(i, j);
                    d_ik = lastWeights.get(i, k);
                    d_kj = lastWeights.get(k, j);

                    min = Math.min(d_ij, d_ik + d_kj);
                    currWeights.set(i, j, min);

                    pre_ij = lastPredecessors.get(i, j);
                    pre_kj = lastPredecessors.get(k, j);

                    pre = (d_ij <= d_ik + d_kj) ? pre_ij : pre_kj;
                    currPredecessors.set(i, j, pre);
                    if (cancelled) break;
                }
                if (progress % 100 == 0)
                    state = MonitoringToolKit.calculateWaitingTime(before, this);
                if (cancelled) break;
            }
//			printPredecessorMatrix();
            lastWeights = currWeights;
            lastPredecessors = currPredecessors;
            if (cancelled) break;
        }
        if (!cancelled)
            progress = max_progress;
    }


    /**
     * constructs the set of visible relations out of the backtracing matrix.
     */
    private void applyToGraph() {
        relationBitSet = new HashSet<Integer>();
        Integer pre;
        int rid;
        for (int i = 0; i < num_concepts; i++) {
            for (int j = 0; j < num_concepts; j++) {
                pre = lastPredecessors.get(i, j);
                if (pre != null) {
                    //System.out.println(i+" -> "+j+" via ("+pre+","+j+")");
                    rid = simpleRelations.get(pre, j);
                    relationBitSet.add(rid);
                }
            }
        }

    }

    /**
     * convenience method for debugging.
     */
    @SuppressWarnings("unused")
    private void printPredecessorMatrix() {
        Integer v;
        String vString;
        System.out.println();
        for (int i = 0; i < num_concepts; i++) {
            for (int j = 0; j < num_concepts; j++) {
                v = lastPredecessors.get(i, j);
                vString = (v != null) ? v + "" : "n";
                System.out.print(vString + " ");
            }
            System.out.println();
        }
        System.out.println();
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getArgumentDefinitions()
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return argdefs;
    }

    /**
     * fetches all given arguments and checks their validity.
     */
    private void fetchArguments() throws InvalidPluginArgumentException {
//		Object wArg = args.getUniqueValue(USEWEIGHTS_ARG);
//		useWeights = false;
//		if (wArg != null && (wArg instanceof Boolean)) {
//			useWeights = (Boolean)wArg;
//		}
        boolean error = false;
        String anArg = (String) args.getUniqueValue(WEIGHTATTRIBUTENAME_ARG);
        weightAttributeName = null;
        if (anArg != null) {
            weightAttributeName = graph.getMetaData().getAttributeName(anArg);
            if (weightAttributeName == null) {
                fireEventOccurred(new WrongParameterEvent("Invalid attribute name for edge weight : " + anArg, getCurrentMethodName()));
                error = true;
            }
            if (!Number.class.isAssignableFrom(weightAttributeName.getDataType())) {
                fireEventOccurred(new WrongParameterEvent("Attribute name for edge weight " + anArg + " does support datatype double!", getCurrentMethodName()));
                error = true;
            }
        }
        if (error) {
//			useWeights = false;
            weightAttributeName = null;
        }
        Object dArg = args.getUniqueValue(ONLYDIRECTED_ARG);
        onlyDirected = false;
        if (dArg != null && (dArg instanceof Boolean)) {
            onlyDirected = (Boolean) dArg;
        }

        dArg = args.getUniqueValue(INVERSE_WEIGHT_ARG);
        inverse = false;
        if (dArg != null && (dArg instanceof Boolean)) {
            inverse = (Boolean) dArg;
        }

        num_concepts = graph.getConcepts().size();
        max_progress = num_concepts * num_concepts;

    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#requiresIndexedGraph()
     */
    public boolean requiresIndexedGraph() {
        return false;
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#copyResultsToNewGraph(net.sourceforge.ondex.core.base.AbstractONDEXGraph)
     */
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        for (ONDEXConcept c : graph.getConcepts()) {
            graphCloner.cloneConcept(c);
        }
        Iterator<Integer> it = relationBitSet.iterator();
        while (it.hasNext()) {
            graphCloner.cloneRelation(it.next());
        }
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleConcepts()
     */
    public Set<ONDEXConcept> getVisibleConcepts() {
        return graph.getConcepts();
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleRelations()
     */
    public Set<ONDEXRelation> getVisibleRelations() {
        while (!ready) {
            try {
                Thread.sleep(100);
            } catch (InterruptedException e) {
            }
        }
        if (!cancelled) {
            BitSet BitSet = new BitSet(relationBitSet.size());
            for (int rid : relationBitSet) {
                BitSet.set(rid);
            }
            return BitSetFunctions.create(graph, ONDEXRelation.class, BitSet);
        } else {
            return graph.getRelations();
        }
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getName()
     */
    public String getName() {
        return "All pairs shortest path filter";
    }

    /**
     * @see net.sourceforge.ondex.ONDEXPlugin#getVersion()
     */
    public String getVersion() {
        return "16.04.2008";
    }

    @Override
    public String getId() {
        return "allpairs";
    }


    /**
     * Convenience method for outputing the current method name in a dynamic way
     *
     * @return the calling method name
     */
    public static String getCurrentMethodName() {
        Exception e = new Exception();
        StackTraceElement trace = e.fillInStackTrace().getStackTrace()[1];
        String name = trace.getMethodName();
        String className = trace.getClassName();
        int line = trace.getLineNumber();
        return "[CLASS:" + className + " - METHOD:" + name + " LINE:" + line + "]";
    }

    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getState()
     */
    public String getState() {
        return state;
    }

    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getProgress()
     */
    public int getProgress() {
        return progress;
    }

    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#setCancelled(boolean)
     */
    public void setCancelled(boolean c) {
        cancelled = c;
    }

    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getMaxProgress()
     */
    public int getMaxProgress() {
        return max_progress;
    }

    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#getMinProgress()
     */
    public int getMinProgress() {
        return min_progress;
    }


    /**
     * @see net.sourceforge.ondex.tools.monitoring.Monitorable#isIndeterminate()
     */
    @Override
    public boolean isIndeterminate() {
        return false;
    }

    public String[] requiresValidators() {
        return new String[0];
    }

    public Throwable getUncaughtException() {
        return uncaught;
    }

    public boolean isAbortable() {
        return true;
    }
}
