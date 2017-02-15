package net.sourceforge.ondex.algorithm.graphquery;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutionException;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateDoesNotExistException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.StateMachineInvalidException;
import net.sourceforge.ondex.algorithm.graphquery.exceptions.TransitionDoesNotExistException;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationKey;
import net.sourceforge.ondex.core.util.BitSetFunctions;

/**
 * A graph traverser to extract multiple paths based rules defined in a graph of states and transitions
 *
 * @author hindlem
 */
public class GraphTraverser {

    private static ExecutorService EXECUTOR;

    private final Map<Transition, Set<ONDEXRelation>> transitionViewCache = new HashMap<Transition, Set<ONDEXRelation>>();

    private final StateMachine sm;

    private int maxLengthOfAnyRoute;

    /**
     * @param sm the state machine to traverse
     * @param maxLengthOfAnyStateDerivedRoute
     *           the max length of any given StateDerivedRoute
     */
    public GraphTraverser(StateMachine sm, int maxLengthOfAnyStateDerivedRoute) {
        this.sm = sm;
        this.maxLengthOfAnyRoute = maxLengthOfAnyStateDerivedRoute;
       
        if (EXECUTOR == null) {
            //EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
            EXECUTOR = Executors.newFixedThreadPool(1);
            Runtime.getRuntime().addShutdownHook(new Thread() {
                public void run() {
                    EXECUTOR.shutdownNow();
                }
            });
        }
    }

    /**
     * @param sm the state machine to traverse
     */
    public GraphTraverser(StateMachine sm) {
        this(sm, Integer.MAX_VALUE);
    }

    /**
     * No multi threading here this implementation threads on start states
     * Only concept ids with valid routes out are returned
     *
     * @param aog     the graph to extract StateDerivedRoutes from (these all become start points --> be careful)
     * @param concept the concepts that form the start points for this synchronous search
     * @param filter  allows method caller to filter results as they are returned (can be null)
     * @return StateDerivedPaths extracted
     */
    public List<EvidencePathNode> traverseGraph(ONDEXGraph aog, ONDEXConcept concept, FilterPaths<EvidencePathNode> filter) {

        Set<Transition> transitions = sm.getAllTransitions();
        for (Transition transition : transitions) {
            transitionViewCache.put(transition,
                    aog.getRelationsOfRelationType(transition.getValidRelationType()));
        }

        Traverser traverser = new Traverser(aog, concept, sm, filter);

        try {
            try {
                List<EvidencePathNode> future = traverser.call();
                if (filter != null) {
                    future = filter.filterPaths(future);
                }
                return future;
            } catch (Exception e) {
                e.printStackTrace();
            }
        } finally {
            transitionViewCache.clear();
        }
        throw new RuntimeException("operation unsuccessful");
    }

    /**
     * extract StateDerivedRoutes from the given graph (multi-threaded returns on completion)
     *
     * @param aog      the graph to extract StateDerivedRoutes from (these all become start points --> be careful)
     * @param concepts the concepts that form the start points for this synchronous search
     * @param filter   allows method caller to filter results as they are returned (can be null)
     * @return a map of starting points to StateDerivedPaths extracted
     */
    public Map<ONDEXConcept, List<EvidencePathNode>> traverseGraph(ONDEXGraph aog, Set<ONDEXConcept> concepts, FilterPaths<EvidencePathNode> filter) {

//        for (Transition transition : sm.getAllTransitions()) {
//            Set<ONDEXRelation> rv = BitSetFunctions.copy(aog.getRelations());
//            for (ONDEXRelation r : rv) {
//                if (!transition.isValid(r))
//                    rv.remove(r.getId());
//            }
//            transitionViewCache.put(transition, rv);
//        }
    	
    	//above code seems to be very slow and this code
    	//seems to do the job much faster (passes all junit test)
        Set<Transition> transitions = sm.getAllTransitions();
        for (Transition transition : transitions) {
            transitionViewCache.put(transition,
                    aog.getRelationsOfRelationType(transition.getValidRelationType()));
        }

        Map<ONDEXConcept, List<EvidencePathNode>> completeStateDerivedRoutes
                = new HashMap<ONDEXConcept, List<EvidencePathNode>>();

        Map<ONDEXConcept, Future<List<EvidencePathNode>>>
                futures = new LinkedHashMap<ONDEXConcept, Future<List<EvidencePathNode>>>();

        for (ONDEXConcept concept : concepts) {
            Traverser traverser = new Traverser(aog, concept, sm, filter);
            futures.put(concept, EXECUTOR.submit(traverser));
        }

        System.out.println(futures.size() + " futures submitted");

        int previouslyComplete = 0;
        int futureNumber = 0;
        int totalFutureNumber = futures.keySet().size();
        for (ONDEXConcept concept : futures.keySet()) {
            Future<List<EvidencePathNode>> future = futures.get(concept);
            try {
                List<EvidencePathNode> results = future.get();
                futureNumber++;
                if (results.size() > 0) {
                    completeStateDerivedRoutes.put(concept, results);
                }

                int complete = Math.round(((float) futureNumber / (float) totalFutureNumber) * 100f);
                if (complete > previouslyComplete) {
                    System.out.println("Traversal done " + complete + "%");
                    previouslyComplete = complete;
                }
            } catch (InterruptedException e) {
                e.printStackTrace();
            } catch (ExecutionException e) {
                e.printStackTrace();
            }
        }

        transitionViewCache.clear();

        return completeStateDerivedRoutes;
    }

    /**
     * The work of the state machine from state to state transition in the graph
     *
     * @author hindlem
     */
    private class Traverser implements Callable<List<EvidencePathNode>> {

        private final ONDEXGraph aog;

        private final StateMachine sm;

        private final ONDEXConcept startingConcept;

        private final List<EvidencePathNode> completeStateDerivedPaths = new ArrayList<EvidencePathNode>(30);

        private final LinkedList<EvidencePathNode> incompleteStateDerivedPaths = new LinkedList<EvidencePathNode>();

        private FilterPaths<EvidencePathNode> filter;

        /**
         * The traverser object constructor that does all the work
         *
         * @param aog             the graph we are traversing
         * @param startingConcept the concept we are at
         * @param sm              the all important state machine
         * @param filter
         */
        public Traverser(ONDEXGraph aog,
                         ONDEXConcept startingConcept,
                         StateMachine sm,
                         FilterPaths<EvidencePathNode> filter) {
            this.aog = aog;
            this.startingConcept = startingConcept;
            this.sm = sm;
            this.filter = filter;
        }

        /**
         * The main body of work around here!
         *
         * @param path the graph instance path that has so far been traversed
         */
        private void findPaths(EvidencePathNode<ONDEXConcept, ONDEXRelation, State> path)
                throws StateDoesNotExistException,
                StateMachineInvalidException,
                TransitionDoesNotExistException, CloneNotSupportedException {

            ONDEXConcept currentConcept = path.getEntity();
            State currentState = path.getStateMachineComponent();

            //get the transitions
            Set<Transition> transitions = sm.getOutgoingTransitions(currentState);
//            System.err.println("From: " + currentState + " following transitions: " + transitions);

            for (Transition transition : transitions) {
                State sourceState = sm.getTransitionSource(transition);
                if (path.getLength() == 1 && !sourceState.isValid(currentConcept, path))
                    continue;

                Set<ONDEXRelation> relationsOfConcept = BitSetFunctions.copy(aog.getRelationsOfConcept(currentConcept));
                relationsOfConcept.retainAll(transitionViewCache.get(transition));

                for (ONDEXRelation relation : relationsOfConcept) {
                    RelationKey key = relation.getKey();

                    if (key.getFromID() == key.getToID())  //self loop
                        continue;///ignore these

                    if (!path.containsEntity(relation) && transition.isValid(relation, path)) {

                        //the target concept is the concept on the relation that is not the same as the last concept traversed (i.e not the trailing concept)
                        int targetConceptId = (key.getToID() != currentConcept.getId()) ? key.getToID() : key.getFromID();

                        if (!path.containsEntityId(targetConceptId, ONDEXConcept.class)) { //loop in the StateDerivedRoute

                            ONDEXConcept targetConcept = aog.getConcept(targetConceptId);

                            State targetState = sm.getTransitionTarget(transition);
                            if (!targetState.isValid(targetConcept, path))
                                continue;

                            //System.out.println("TO==> "+targetConcept.getPID()+" "+targetConcept.getOfType().getId());

                            EvidencePathNode newPathRE =
                                    new EvidencePathNode.EvidenceRelationNode(relation, transition, path);
                            EvidencePathNode newPathCE =
                                    new EvidencePathNode.EvidenceConceptNode(targetConcept, targetState, newPathRE);

                            if (sm.isFinish(targetState)) {
                                completeStateDerivedPaths.add(newPathCE);
                            } else if (newPathCE.getConceptLength() < maxLengthOfAnyRoute) {
                                //System.out.println("part: "+newStateDerivedRoute.toString());
                                incompleteStateDerivedPaths.add(newPathCE);
                            }
                        }
                    }
                }
            }
        }

        @Override
        public List<EvidencePathNode> call() throws CloneNotSupportedException {
            State startingState = sm.getStart();
            EvidencePathNode startingEmptyRoute //a new blank route to start the process off
                    = new EvidencePathNode.FirstEvidenceConceptNode(startingConcept, startingState);

            try {
                findPaths(startingEmptyRoute); //Recursively find all paths for this starting node

                while (!incompleteStateDerivedPaths.isEmpty())
                    findPaths(incompleteStateDerivedPaths.poll());

            } catch (StateDoesNotExistException e) {
                e.printStackTrace();
            } catch (StateMachineInvalidException e) {
                e.printStackTrace();
            } catch (TransitionDoesNotExistException e) {
                e.printStackTrace();
            }

            if (filter != null) {
                return filter.filterPaths(completeStateDerivedPaths);
            } else {
                return completeStateDerivedPaths;
            }
        }
    }

    /**
     * @return maxLengthOfAnyStateDerivedRoute in states
     */
    public int getMaxLengthOfAnyStateDerivedRoute() {
        return maxLengthOfAnyRoute;
    }

    /**
     * @param maxLengthOfAnyRoute in states
     */
    public void setMaxLengthOfAnyRoute(int maxLengthOfAnyRoute) {
        this.maxLengthOfAnyRoute = maxLengthOfAnyRoute;
    }

}
