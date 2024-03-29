package net.sourceforge.ondex.algorithm.graphquery;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.net.URL;
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
import net.sourceforge.ondex.algorithm.graphquery.flatfile.StateMachineFlatFileParser2;
import net.sourceforge.ondex.algorithm.graphquery.nodepath.EvidencePathNode;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationKey;
import net.sourceforge.ondex.core.util.BitSetFunctions;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * A graph traverser to extract multiple paths based rules defined in a graph of states and transitions.
 * 
 * <p>This traverser requires the {@code "ONDEXGraph"} {@link #getOptions() option}, but that's used by the 
 * {@link StateMachine} {@link #loadSemanticMotifs(String) loader}, to know the metadata to be used to validate 
 * the state machine being loaded. So that could be an empty graph, though typically it will be the same graph you use 
 * for the traverse methods</p>.
 *
 * @author hindlem
 */
public class GraphTraverser extends AbstractGraphTraverser {

    private static ExecutorService EXECUTOR;

    // transition => all relations of the required type for the transition
    private final Map<Transition, Set<ONDEXRelation>> transitionViewCache = new HashMap<>();

    private StateMachine sm;

    private int maxLengthOfAnyRoute = -1;
            
    /**
     * @param sm the state machine to traverse
     * @param maxLengthOfAnyStateDerivedRoute
     *           the max length of any given StateDerivedRoute
     */
    public GraphTraverser(StateMachine sm, int maxLengthOfAnyStateDerivedRoute) {
        this.sm = sm;
        this.maxLengthOfAnyRoute = maxLengthOfAnyStateDerivedRoute;
        init ( null );
    }

    /**
     * @param sm the state machine to traverse
     */
    public GraphTraverser(StateMachine sm) {
        this(sm, Integer.MAX_VALUE);
    }
      
    
    /**
     * This is for component managers like Spring or initalisation via class name + options 
     * (see {@link AbstractGraphTraverser#getInstance(Map)}.
     * 
     * This traverser requires a {@link StateMachine} before invoking traverse() methods. When invoked with this
     * empty constructor, you need to setup the "StateMachineFilePath" {@link #getOptions() option}, either after 
     * instantiation, or in the options passed to {@link AbstractGraphTraverser#getInstance(Map)}.
     * 
     * We also accept the "MaxLengthOfAnyStateDerivedRoute" option.
     * 
     */
    public GraphTraverser ()
    {
    }
    
    
    /**
     * For methods that have a graph to be give to the SM parser (in {@link #loadSemanticMotifs(String, ONDEXGraph)},
     * use that graph as parameter, else use null and the graph will be taken from the ONDEXGraph option.
     * 
     */
    private synchronized void init ( ONDEXGraph graph )
    {
    	if ( EXECUTOR == null )
    	{
				EXECUTOR = Executors.newFixedThreadPool(Runtime.getRuntime().availableProcessors());
				//EXECUTOR = Executors.newFixedThreadPool ( 1 );
				Runtime.getRuntime ().addShutdownHook ( new Thread () {
					public void run () {
						EXECUTOR.shutdownNow ();
					}
				});
    	}
    	
    	if ( this.maxLengthOfAnyRoute == -1 ) 
    		this.maxLengthOfAnyRoute = this.getOption ( "MaxLengthOfAnyStateDerivedRoute", Integer.MAX_VALUE );
    	
    	if ( this.sm != null ) return;
    	    	
    	String stateMachineFilePath = this.getOption ( "StateMachineFilePath" );
    	if ( stateMachineFilePath == null ) throw new IllegalArgumentException (
	  			"Cannot initialise the StateMachine graph traverser: "
	  		+ "you must configure a path for the StateMachine File (usually in config.xml)"
    	);
    	
    	this.sm = loadSemanticMotifs ( stateMachineFilePath, graph  ); 
    }
    
    
    /**
     * @see #init(ONDEXGraph).
     * 
     */
  	private StateMachine loadSemanticMotifs ( String smFile, ONDEXGraph graph  ) 
  	{
  		StateMachineFlatFileParser2 smp = null;
  		try 
  		{
  			if ( graph == null ) graph = this.getOption ( "ONDEXGraph", null );
				if ( graph == null ) throw new IllegalArgumentException (
					"ONDEXGraph option not set" 
				);

				URL motifsUrl = smFile.startsWith ( "file://" )
  					? new URL ( smFile )
  					: Thread.currentThread().getContextClassLoader().getResource ( smFile );
  				
  			log.info ( "Loading State Machine Traverser description from '{}'", motifsUrl.toString () );
  			
				smp = new StateMachineFlatFileParser2 ();
				smp.parseReader ( new BufferedReader ( new InputStreamReader ( motifsUrl.openStream () ) ), graph );
	
				StateMachine sm = smp.getStateMachine ();
				if ( sm == null ) throw new NullPointerException (
					"State Machine parser failed, result is null" 
				);
				
				log.info ( 
					"State Machine Loaded, {} states and {} transitions",
					sm.getAllStates ().size (), sm.getAllTransitions ().size ()
				);
				return sm;
  		} 
  		catch (Exception e) {
  			throw new IllegalArgumentException ( String.format ( 
					"Error while initialising State Machine Graph Traverser: %s", e.getMessage () ),
					e
				);
  		}
  	}
    
    
    
    /**
     * Single-seed, single-thread traversal.
     * 
     * @param concept the concepts that form the start points for this synchronous search
     * @param filter  allows method caller to filter results as they are returned (can be null)
     * 
     * @return the list found paths.
     */
    @Override
		public List<EvidencePathNode> traverseGraph(ONDEXGraph aog, ONDEXConcept concept, FilterPaths<EvidencePathNode> filter) {

        init ( aog );
        
    		Set<Transition> transitions = sm.getAllTransitions();
        for (Transition transition : transitions) {
            transitionViewCache.put(transition,
                    aog.getRelationsOfRelationType(transition.getValidRelationType()));
        }

        Traverser traverser = new Traverser(aog, concept, sm, filter);

        try
        {
        	List<EvidencePathNode> future = traverser.call();
          if (filter != null) future = filter.filterPaths(future);
          return future;
        } 
        catch ( Exception ex ) {
          throw ExceptionUtils.buildEx (
          	RuntimeException.class, ex, "Error while running the traverser tasks: $cause" 
          );
        }
        finally {
          transitionViewCache.clear();
        }
    }

    /**
     * Multi-thread traversal from a set of seeds.
     * 
     * Essentially, this invokes {@link Traverser} instances in parallel.
     *
     * @param concepts the concepts that form the start points for this synchronous search
     * @param filter   allows method caller to filter results as they are returned (can be null)
     * @return a map of starting point => paths found using the state machine from that starting point
     *  
     */
		@Override
    @SuppressWarnings ( "rawtypes" )
		public Map<ONDEXConcept, List<EvidencePathNode>> traverseGraph(ONDEXGraph aog, Set<ONDEXConcept> concepts, FilterPaths<EvidencePathNode> filter) {

    		init ( aog );
    	
    		log.info ( "Graph Traverser, beginning parallel traversing of {} concept(s)", concepts.size () );
    		
    		// Populate the transition=>relations cache
        Set<Transition> transitions = sm.getAllTransitions();
        for (Transition transition : transitions) {
            transitionViewCache.put(transition,
                    aog.getRelationsOfRelationType(transition.getValidRelationType()));
        }

        // Final and partial results
        Map<ONDEXConcept, List<EvidencePathNode>> completeStateDerivedRoutes = new HashMap<>();
        Map<ONDEXConcept, Future<List<EvidencePathNode>>> resultFutures = new LinkedHashMap<>();

        // Let's go, in parallel
        for (ONDEXConcept concept : concepts) {
            Traverser traverser = new Traverser(aog, concept, sm, filter);
            resultFutures.put(concept, EXECUTOR.submit(traverser));
        }

        int totalFutureNumber = resultFutures.keySet().size();

        PercentProgressLogger progressLogger = new PercentProgressLogger (
        	"{}% of traversal tasks completed", totalFutureNumber 
        );
        
        for (ONDEXConcept concept : resultFutures.keySet()) {
            Future<List<EvidencePathNode>> future = resultFutures.get(concept);
            try {
                List<EvidencePathNode> results = future.get();
                if (results.size() > 0) completeStateDerivedRoutes.put(concept, results);
                progressLogger.updateWithIncrement ();
            } 
            catch ( InterruptedException | ExecutionException ex ) {
              ExceptionUtils.throwEx (
              	RuntimeException.class, ex, "Error while running the traverser tasks: $cause" 
              );
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
         * The main body of work. Walks the graph from the tail of path, that is, 
         * the {@link EvidencePathNode#getEntity() path end node} and the 
         * {@link EvidencePathNode#getStateMachineComponent() state machine component that led to that node}.
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
                          
                            EvidencePathNode newPathRE =
                                    new EvidencePathNode.EvidenceRelationNode(relation, transition, path);
                            EvidencePathNode newPathCE =
                                    new EvidencePathNode.EvidenceConceptNode(targetConcept, targetState, newPathRE);

                            if (sm.isFinish(targetState)) {
                                completeStateDerivedPaths.add(newPathCE);
                            } else if (newPathCE.getConceptLength() < maxLengthOfAnyRoute) {
                                incompleteStateDerivedPaths.add(newPathCE);
                            }
                        }
                    }
                }
            }
        }

				@Override
        @SuppressWarnings ( { "rawtypes", "unchecked" } )
        public List<EvidencePathNode> call() throws CloneNotSupportedException {
            State startingState = sm.getStart();
            EvidencePathNode startingEmptyRoute //a new blank route to start the process off
                    = new EvidencePathNode.FirstEvidenceConceptNode(startingConcept, startingState);

            try {
                findPaths(startingEmptyRoute); //Recursively find all paths for this starting node

                while (!incompleteStateDerivedPaths.isEmpty())
                    findPaths(incompleteStateDerivedPaths.poll());

            }
            catch ( StateDoesNotExistException | StateMachineInvalidException | TransitionDoesNotExistException ex ) {
              ExceptionUtils.throwEx (
                	IllegalStateException.class, ex, "Error while running the traverser tasks: $cause" 
                );
            }

            return filter != null 
            	? filter.filterPaths(completeStateDerivedPaths)
            	: completeStateDerivedPaths;
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
