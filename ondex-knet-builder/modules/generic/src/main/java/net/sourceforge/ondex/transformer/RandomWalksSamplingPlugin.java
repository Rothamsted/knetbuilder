package net.sourceforge.ondex.transformer;

import java.util.stream.Stream;

import org.apache.commons.lang3.RandomUtils;
import org.apache.commons.lang3.StringUtils;
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
				"The maximum no. of random steps done during the path sampling",
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
		var startConceptClassIds = args.getObjectValueList ( "startConceptClassIds", String.class );
		
		var newGraph = sample (
			graph, startConceptsSamplingRatio, maxWalkLen, startConceptClassIds.toArray ( new String[0] )
		);
		ONDEXGraphCloner.replaceGraph ( graph, newGraph );
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
		ONDEXGraph graph, double startConceptsSamplingRatio, int maxWalkLen, String ...startConceptClassIds )
	{
		slog.info ( 
			"Sampling {} concepts and {} relations", 
			graph.getConcepts ().size (), graph.getRelations ().size () 
		);
		
		var graphCloner = new ONDEXGraphCloner ( graph, new MemoryONDEXGraph ( graph.getName () ) );
		
		// Seed concepts, based either on specified types or all the concepts in the graph
		Stream<ONDEXConcept> startConcepts = startConceptClassIds != null && startConceptClassIds.length != 0
			? Stream.of ( (String[]) startConceptClassIds )
				.map ( ccid -> ONDEXGraphUtils.getConceptClass ( graph, ccid ) )
				.flatMap ( cc -> graph.getConceptsOfConceptClass ( cc ).parallelStream () )
			: graph.getConcepts ().parallelStream ();
				
		// Now, take a random sample of these concepts
		startConcepts = startConcepts.filter ( c -> RandomUtils.nextDouble ( 0, 1 ) < startConceptsSamplingRatio );
		
		var progress = new ProgressLogger ( "{} concepts walked", 10000 );
		progress.setIsThreadSafe ( true );
		
		// And walk the graph randomly starting from each concept
		startConcepts.forEach ( c -> 
		{
			var rndLen = RandomUtils.nextInt ( 0, maxWalkLen );
			ONDEXEntity pathEl = c;
			for ( var step = 0; step < rndLen; step++ )
			{
				boolean isConcept = pathEl instanceof ONDEXConcept;
				synchronized ( graphCloner )
				{
					if ( isConcept ) graphCloner.cloneConcept ( (ONDEXConcept) pathEl );
					else graphCloner.cloneRelation ( (ONDEXRelation) pathEl );
				}
								
				if ( isConcept )
				{
					// Draw a random concept relation
					var rels = graph.getRelationsOfConcept ( (ONDEXConcept) pathEl );
					if ( !rels.isEmpty () )
					{
						var relsItr = rels.iterator ();
						var rndSteps = RandomUtils.nextInt ( 1, rels.size () );
						for ( int conceptRelStep = 1; conceptRelStep <= rndSteps; conceptRelStep++ )
							pathEl = relsItr.next ();
					}
				}
				else
				{
					// Just take one of the end points
					var rel = ((ONDEXRelation) pathEl);
					pathEl = RandomUtils.nextBoolean () ? rel.getFromConcept () : rel.getToConcept ();
				}
			} // for steps
			progress.updateWithIncrement ();
		}); // forEach ( concept )
		
		var sampledGraph = graphCloner.getNewGraph ();
		slog.info ( 
			"Sampling done. New graph has {} concepts and {} relations", 
			sampledGraph.getConcepts ().size (), sampledGraph.getRelations ().size () 
		);
		
		return sampledGraph;
		
	} // sample()
	
}
