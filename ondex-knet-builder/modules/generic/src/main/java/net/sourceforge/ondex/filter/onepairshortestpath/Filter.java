package net.sourceforge.ondex.filter.onepairshortestpath;

import java.util.ConcurrentModificationException;
import java.util.HashSet;
import java.util.Iterator;
import java.util.Set;

import net.sourceforge.ondex.algorithm.dijkstra.DijkstraQueue;
import net.sourceforge.ondex.algorithm.dijkstra.PathNode;
import net.sourceforge.ondex.annotations.Authors;
import net.sourceforge.ondex.annotations.Custodians;
import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.BooleanArgumentDefinition;
import net.sourceforge.ondex.args.RangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.base.util.BitSetFunctions;
import net.sourceforge.ondex.event.type.GeneralOutputEvent;
import net.sourceforge.ondex.event.type.WrongParameterEvent;
import net.sourceforge.ondex.exception.type.PluginException;
import net.sourceforge.ondex.exception.type.WrongArgumentException;
import net.sourceforge.ondex.filter.ONDEXFilter;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;
import net.sourceforge.ondex.workflow.InvalidPluginArgumentException;

/**
 * This filter performs a shortest path search between a given root concept
 * and all other nodes in the graph. The search can either
 * be performed as a Dijkstra search (working with edge weights retrieved from
 * relation GDSs) or as a BFS search (considering each edge with weight 1).
 * This is controlled by the boolean argument <code>USEWEIGHTS_ARG</code>.
 * If it is set to true, another argument <code>WEIGHTATTRIBUTENAME_ARG</code>,
 * determining the attribute name containing the edge weights, must be specified.
 * The value corresponding to this attribute name <b>must</b> be of
 * type <code>Double</code>.
 *
 * @author Jochen Weile
 */
@Authors(authors = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
@Custodians(custodians = {"Jochen Weile"}, emails = {"jweile at users.sourceforge.net"})
public class Filter extends ONDEXFilter implements ArgumentNames {

    //####FIELDS####

    /**
     * The dijkstra queue is the central datastructure supporting the work of the algorithm.
     */
    private DijkstraQueue queue;

    /**
     * The initial concept from which the search starts.
     */
    private ONDEXConcept startConcept, stopConcept;

    /**
     * The attribute name which represents the Attribute that contains the edge weights.
     */
    private AttributeName weightAttributeName;

    /**
     * This switches between Dijkstra and BFS algorithm.
     */
    private boolean useWeights;

    /**
     * Whether or not to consider the directions of the edges.
     */
    private boolean onlyDirected;

    /**
     * use inverse weights.
     */
    private boolean inverse;

    /**
     * Contains the outcome of concepts of the algorithm after backtracing.
     */
    private Set<Integer> conceptDefaultBitSet;

    /**
     * contains the outcome of relations of the algorithm after backtracing.
     */
    private Set<Integer> relationDefaultBitSet;

    /**
     * states if the algorithm is done already.
     */
    private boolean ready = false;

    /**
     * contains the argument definitions for the filter.
     */
    private ArgumentDefinition<?>[] argdefs;


    //####CONSTRUCTOR####


    /**
     * NB: Overrides previous instance logging
     */
    public Filter() {
        argdefs = new ArgumentDefinition[]{
                new RangeArgumentDefinition<Integer>(STARTCONCEPT_ARG, STARTCONCEPT_ARG_DESC, true, null, 1, Integer.MAX_VALUE, Integer.class),
                new RangeArgumentDefinition<Integer>(STOPCONCEPT_ARG, STOPCONCEPT_ARG_DESC, true, null, 1, Integer.MAX_VALUE, Integer.class),
                new BooleanArgumentDefinition(USEWEIGHTS_ARG, USEWEIGHTS_ARG_DESC, false, false),
                new StringArgumentDefinition(WEIGHTATTRIBUTENAME_ARG, WEIGHTATTRIBUTENAME_ARG_DESC, false, null, false),
                new BooleanArgumentDefinition(ONLYDIRECTED_ARG, ONLYDIRECTED_ARG_DESC, false, false),
                new BooleanArgumentDefinition(INVERSE_WEIGHT_ARG, INVERSE_WEIGHT_ARG_DESC, false, false)
        };
    }


    //####METHODS####


    /**
     * @see net.sourceforge.ondex.workflow.ONDEXPlugin#getArgumentDefinitions()
     */
    public ArgumentDefinition<?>[] getArgumentDefinitions() {
        return argdefs;
    }


    /**
     * @see net.sourceforge.ondex.workflow.ONDEXPlugin#getName()
     */
    public String getName() {
        return "Shortest Path Filter (for pair of nodes)";
    }


    /**
     * @see net.sourceforge.ondex.workflow.ONDEXPlugin#getVersion()
     */
    public String getVersion() {
        return "03.04.2008";
    }

    @Override
    public String getId() {
        return "onepairshortestpath";
    }


    /**
     * @see net.sourceforge.ondex.workflow.ONDEXPlugin#start()
     */
    public void start() throws PluginException {

        fetchArguments();

        PathNode result = search();

        conceptDefaultBitSet = new HashSet<Integer>();
        relationDefaultBitSet = new HashSet<Integer>();

        if (result != null) {
            conceptDefaultBitSet.add(result.getCid());
            traceBack(result);
            fireEventOccurred(new GeneralOutputEvent("Finished: Found " + conceptDefaultBitSet.size() +
                    " Concepts and " + relationDefaultBitSet.size() + " Relations", getCurrentMethodName()));
        } else {
            ready = true;
            throw new PluginException("No path exists!");
        }

        ready = true;

    }

    /**
     * fetches all given arguments and checks their validity.
     */
    private void fetchArguments() throws WrongArgumentException, InvalidPluginArgumentException {
        Integer cArg = (Integer) args.getUniqueValue(STARTCONCEPT_ARG);
        startConcept = null;
        if (cArg != null) {
            startConcept = graph.getConcept(cArg);
            if (startConcept == null) {
                fireEventOccurred(new WrongParameterEvent("Invalid start concept : " + cArg, getCurrentMethodName()));
                throw new WrongArgumentException("Invalid start concept : " + cArg);
            }
        }
        cArg = (Integer) args.getUniqueValue(STOPCONCEPT_ARG);
        stopConcept = null;
        if (cArg != null) {
            stopConcept = graph.getConcept(cArg);
            if (stopConcept == null) {
                fireEventOccurred(new WrongParameterEvent("Invalid stop concept : " + cArg, getCurrentMethodName()));
                throw new WrongArgumentException("Invalid stop concept : " + cArg);
            }
        }

        Object wArg = args.getUniqueValue(USEWEIGHTS_ARG);
        useWeights = false;
        if (wArg != null && (wArg instanceof Boolean)) {
            useWeights = (Boolean) wArg;
        }

        if (useWeights) {
            Object anArg = args.getUniqueValue(WEIGHTATTRIBUTENAME_ARG);
            weightAttributeName = null;
            if (anArg != null) {
                weightAttributeName = graph.getMetaData().getAttributeName((String) anArg);
                if (weightAttributeName == null) {
                    fireEventOccurred(new WrongParameterEvent("Invalid attribute name for edge weight : " + anArg, getCurrentMethodName()));
                    throw new WrongArgumentException("Invalid attribute name for edge weight : " + anArg);
                }
                if (!Number.class.isAssignableFrom(weightAttributeName.getDataType())) {
                    fireEventOccurred(new WrongParameterEvent("Attribute name for edge weight " + anArg + " does support datatype double!", getCurrentMethodName()));
                    throw new WrongArgumentException("Attribute name for edge weight " + anArg + " does support datatype double!");
                }
            }
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
    }

    /**
     * This method contains the actual implementation of the algorithm.
     * it performs the BFS/Dijkstra search and returns a result set.
     *
     * @return the result set.
     */
    private PathNode search() {

        PathNode node_curr, node_succ;

        ONDEXConcept c_curr, c_succ;

        Set<ONDEXRelation> relations;

        PathNode node_root = new PathNode(startConcept.getId());
        queue = new DijkstraQueue(node_root);

        while (queue.moreOpenElements()) {
            //get next
            node_curr = queue.dequeue();
            c_curr = graph.getConcept(node_curr.getCid());
            //check if goal reached
            if (c_curr.getId() == stopConcept.getId())
                return node_curr;
            if ((relations = graph.getRelationsOfConcept(c_curr)) != null) {
                for (ONDEXRelation r_curr : relations) {
                    if (onlyDirected && !r_curr.getFromConcept().equals(c_curr))
                        continue;
                    if (r_curr.getFromConcept().equals(r_curr.getToConcept()))
                        continue; //loops of size one are evil ;)
                    c_succ = getOppositeConcept(c_curr, r_curr);

                    node_succ = new PathNode(c_succ.getId());
                    node_succ.setParent(node_curr, r_curr.getId());
                    node_succ.setG(node_curr.getG() + getWeight(r_curr));

                    queue.enqueueIfBetterOrNew(node_succ);
                }
            }
            queue.considerClosed(node_curr);
        }
        return null;
    }

    /**
     * Returns a concept's opposite one on a relation.
     *
     * @param c_curr the concept on the one end.
     * @param r_curr the relation.
     * @return the concept on the other end.
     */
    private ONDEXConcept getOppositeConcept(ONDEXConcept c_curr, ONDEXRelation r_curr) {
        return (r_curr.getFromConcept().equals(c_curr)) ?
                r_curr.getToConcept() :
                r_curr.getFromConcept();
    }

    /**
     * returns the weight of the given edge.
     *
     * @param r_curr the relation for which to retrieve the edge weight.
     * @return the edge weight of the given relation.
     */
    private double getWeight(ONDEXRelation r_curr) {
        double out;
        if (useWeights) {
            Attribute weightAttribute = r_curr.getAttribute(weightAttributeName);
            if ((weightAttribute != null) && (weightAttribute.getValue() instanceof Number))
                out = ((Number) weightAttribute.getValue()).doubleValue();
            else
                out = 1.0;
        } else
            out = 1.0;

        if (inverse)
            out = 1.0 - out;
        return out;
    }

    /**
     * recursive method for backtracing inside the result set of the algorithm.
     *
     * @param n the current node.
     */
    private void traceBack(PathNode n) {
        if (n.getParent() != null) {
            relationDefaultBitSet.add(n.getRid());
            if (!conceptDefaultBitSet.contains(n.getParent().getCid())) {
                conceptDefaultBitSet.add(n.getParent().getCid());
                traceBack(n.getParent());
            }
        }
    }


    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#copyResultsToNewGraph(net.sourceforge.ondex.core.base.AbstractONDEXGraph)
     */
    @Override
    public void copyResultsToNewGraph(ONDEXGraph exportGraph) {
        ONDEXGraphCloner graphCloner = new ONDEXGraphCloner(graph, exportGraph);
        Iterator<Integer> it = conceptDefaultBitSet.iterator();
        while (it.hasNext()) {
            graphCloner.cloneConcept(it.next());
        }
        it = relationDefaultBitSet.iterator();
        while (it.hasNext()) {
            graphCloner.cloneRelation(it.next());
        }
    }


    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleConcepts()
     */
    @Override
    public Set<ONDEXConcept> getVisibleConcepts() {
        if (ready)
            return BitSetFunctions.create(graph, ONDEXConcept.class, conceptDefaultBitSet);
        else
            throw new ConcurrentModificationException("Plugin still running!");
    }

    /**
     * @see net.sourceforge.ondex.filter.ONDEXFilter#getVisibleRelations()
     */
    @Override
    public Set<ONDEXRelation> getVisibleRelations() {
        if (ready)
            return BitSetFunctions.create(graph, ONDEXRelation.class, relationDefaultBitSet);
        else
            throw new ConcurrentModificationException("Plugin still running!");
    }


    /**
     * @see net.sourceforge.ondex.workflow.ONDEXPlugin#requiresIndexedGraph()
     */
    public boolean requiresIndexedGraph() {
        return false;
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

    public String[] requiresValidators() {
        return new String[0];
    }
}
