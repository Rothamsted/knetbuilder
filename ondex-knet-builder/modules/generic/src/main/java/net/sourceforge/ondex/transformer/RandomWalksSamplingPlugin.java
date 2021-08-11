package net.sourceforge.ondex.transformer;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;
import java.util.stream.IntStream;
import java.util.stream.Stream;

import org.apache.commons.collections4.ComparatorUtils;
import org.apache.commons.lang.mutable.MutableDouble;
import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
import net.sourceforge.ondex.args.IntegerRangeArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.memory.MemoryONDEXGraph;
import net.sourceforge.ondex.core.util.ONDEXGraphUtils;
import net.sourceforge.ondex.tools.ondex.ONDEXGraphCloner;
import uk.ac.ebi.utils.runcontrol.ProgressLogger;

/**
 * A random subgraph selector based on random walks.
 * 
 * With respect to {@link GraphSamplingPlugin}, this preserve many relevan paths, being based
 * on taking a set of initial seed nodes and walking the graph randomly, starting from them.
 * 
 * TODO: startConceptClassIds to be tested yet!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Jul 2021</dd></dl>
 *
 */
public class RandomWalksSamplingPlugin extends ONDEXTransformer
{
	private static Logger slog = LoggerFactory.getLogger ( RandomWalksSamplingPlugin.class );
	private Logger log = LoggerFactory.getLogger ( this.getClass () );

	public RandomWalksSamplingPlugin ()
	{
	}

	@Override
	public String getId ()
	{
		return StringUtils.uncapitalize ( this.getClass ().getSimpleName () );
	}

	@Override
	public String getName ()
	{
		return "Graph Sample Selector based on Random Walks";
	}

	@Override
	public String getVersion ()
	{
		return "1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition<?>[]
		{
			new FloatRangeArgumentDefinition (
				"startConceptsSamplingRatio", 
				"How many initial concepts are randomly picked to seed the random walks (wrt the total)",
				false, // required 
				0.1f  // default
			),
			new IntegerRangeArgumentDefinition (
				"maxWalkLen", 
				"Max no. of random steps done during the path sampling",
				false, // required 
				10  // default
			),
			new IntegerRangeArgumentDefinition (
				"maxWalksPerSeed", 
				"Max no. of walks to start from every seed concept",
				false, // required 
				10  // default
			),
			new StringArgumentDefinition (
				"startConceptClassIds",
				"Concept class IDs to start from for seeding the random walks. If not specified, selects randomly from all concepts",
				false, // required
				null, // default
				true // multiple
			)
		};
	}

	@Override
	public void start () throws Exception
	{
		var args = this.getArguments ();
		double startConceptsSamplingRatio = (Float) args.getUniqueValue ( "startConceptsSamplingRatio" );
		int maxWalkLen = (int) args.getUniqueValue ( "maxWalkLen" );
		int maxWalksPerSeed = (int) args.getUniqueValue ( "maxWalksPerSeed" );
		var startConceptClassIds = args.getObjectValueList ( "startConceptClassIds", String.class );
		
		var newGraph = sample (
			graph, startConceptsSamplingRatio, maxWalkLen, maxWalksPerSeed, startConceptClassIds.toArray ( new String[0] )
		);
		
		log.info ( "Replacing the old graph" );
		ONDEXGraphCloner.replaceGraph ( graph, newGraph );

		log.debug ( 
			"Replaced graph has {} concept(s) and {} relation(s)",
			graph.getConcepts ().size (),
			graph.getRelations ().size ()
		);
		log.info ( "Sampling finished" );
	}

	@Override
	public boolean requiresIndexedGraph ()
	{
		return false;
	}

	@Override
	public String[] requiresValidators ()
	{
		return null;
	}

	
	public static ONDEXGraph sample ( 
		ONDEXGraph graph, double startConceptsSamplingRatio, int maxWalkLen, int maxWalksPerSeed, String ...startConceptClassIds )
	{
		slog.info ( 
			"Sampling {} concepts and {} relations", 
			graph.getConcepts ().size (), graph.getRelations ().size () 
		);
		
		var graphCloner = new ONDEXGraphCloner ( graph, new MemoryONDEXGraph ( graph.getName () ) );
		
		// Seed concepts, based either on specified types or all the concepts in the graph
		Stream<ONDEXConcept> startConcepts = startConceptClassIds != null && startConceptClassIds.length != 0
			? Stream.of ( (String[]) startConceptClassIds )
				.peek ( ccid -> slog.info ( "Sampling from the class {}", ccid ) )
				.map ( ccid -> ONDEXGraphUtils.getConceptClass ( graph, ccid ) )
				.flatMap ( cc -> graph.getConceptsOfConceptClass ( cc ).parallelStream () )
			: graph.getConcepts ().parallelStream ();
				
		// Now, take a random sample of these concepts
		var startConceptsFinal = startConcepts.filter ( c -> Math.random () < startConceptsSamplingRatio );
		
		// Use it to decrease the likelihood to re-visit nodes.
		Map<Integer, Integer> relationVisits = new ConcurrentHashMap<> ();
		
		var progress = new ProgressLogger ( "{} concepts walked", 1000 );
		
		// And walk the graph randomly starting from each concept
		startConceptsFinal.forEach ( startConcept -> 
		{
			var repeats = RandomUtils.nextInt ( 0, maxWalksPerSeed );
			for ( int repeat = 0; repeat < repeats; repeat++ )
			{
				var rndLen = RandomUtils.nextInt ( 1, maxWalkLen );
				ONDEXConcept pathConcept = startConcept;
				for ( var step = 1; step <= rndLen; step+=2 )
				{
					synchronized ( graphCloner ) {
					 graphCloner.cloneConcept ( pathConcept );
					}
	
					// Now, pick a random relation, giving more chances to the ones having fewer visits
					//
					var crels = new ArrayList<> ( graph.getRelationsOfConcept ( pathConcept ) );
					if ( crels.isEmpty () ) break;
					
					var visitsCount = crels.stream ()
					.map ( ONDEXRelation::getId )
					.mapToInt ( rid -> relationVisits.getOrDefault ( rid, 0 ) )
					.sum ();
	
					// This is how we do it: we get the cumulative distribution function using the dual relative frequencies,
					// and then we toss a 0-1 number over it
					Collections.shuffle ( crels ); // allows for random selection of items with same frequency (in particular, 0)
					var rnd = Math.random ();
					var cdf = 0d;
					ONDEXRelation selectedRel = null;
					for ( var r: crels )
					{
						selectedRel = r; // Cause, if never breaks, the last one is selected anyway.
						cdf += 1 - 1d * relationVisits.getOrDefault ( r.getId (), 0 ) / visitsCount;					
						if ( rnd < cdf ) break;
					}
					
					// Shouldn't happen, but anyway...
					if ( selectedRel == null ) throw new IllegalStateException ( 
						"Internal error: no random relation selected during the random walk for the concept: "
						+ pathConcept.getPID ()
					);
					
					synchronized ( graphCloner ) {
					 graphCloner.cloneRelation ( selectedRel );
					}
					relationVisits.merge ( selectedRel.getId (), 1, (vold, vnew) -> vold + vnew );
					
					// And eventually use the relation to move to another node
					// This might be the same, so that multiple walks can be started from seed nodes and hubs
					pathConcept = RandomUtils.nextBoolean () ? selectedRel.getFromConcept () : selectedRel.getToConcept ();
	
				} // for step
			} // for repeat
			progress.updateWithIncrement ();
		}); // forEach ( startConcept )
		
		var sampledGraph = graphCloner.getNewGraph ();
		slog.info ( 
			"Sampling done. New graph has {} concepts and {} relations", 
			sampledGraph.getConcepts ().size (), sampledGraph.getRelations ().size () 
		);
		
		return sampledGraph;
		
	} // sample()
}
