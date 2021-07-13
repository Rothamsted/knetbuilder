package net.sourceforge.ondex.transformer;

import java.util.Collections;
import java.util.List;
import java.util.Set;
import java.util.function.Consumer;
import java.util.stream.Collectors;
import java.util.stream.IntStream;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;

import static java.lang.Math.round;

/**
 * <h2>The Graph Sampler Plugin</h2>
 *
 * A very simple plugin that reduces the input graph to a random sample of nodes and relations.
 * 
 * Initially, it cuts nodes away, keeping a quota equal to the {@code relativeSize} parameter. Then, if the no. of 
 * relations haven't become under that threshold too, it cuts them randomly, until reaching the same quota.
 * 
 * Beware that this process might leave isolated nodes around.
 *
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>7 Mar 2019</dd></dl>
 *
 */
public class GraphSamplingPlugin extends ONDEXTransformer
{	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	@Override
	public String getId ()
	{
		return "graphSampler";
	}

	@Override
	public String getName ()
	{
		return "Graph Random Sample Selector";
	}

	@Override
	public String getVersion ()
	{
		return "1.0";
	}

	@Override
	public ArgumentDefinition<?>[] getArgumentDefinitions ()
	{
		return new ArgumentDefinition[] {
			new StringArgumentDefinition (
				"relativeSize", 
				"The relative (0-1) final size you want",
				true, // required 
				"0.1",  // default
				false // multi-value
			)
		};
	}

	@Override
	public void start () throws Exception
	{	
		String relativeSizeStr = (String) this.getArguments ().getUniqueValue ( "relativeSize" );
		sample ( null, relativeSizeStr );
	}
	
	public void sample ( ONDEXGraph graph, String relativeSizeStr ) 
	{
		try
		{
			double relativeSize = Double.parseDouble ( relativeSizeStr );
			sample ( graph, relativeSize );
		}
		catch ( NumberFormatException ex ) {
			throw new IllegalArgumentException ( 
				"relativeSize parameter wrong value '" + relativeSizeStr + "': " + ex.getMessage (), ex 
			);
		}		
	}


	public void sample ( ONDEXGraph inGraph, double relativeSize )
	{
		if ( inGraph != null ) this.setONDEXGraph ( inGraph );
		if ( this.graph == null ) throw new IllegalStateException ( "Input graph is null" );
		
		Set<ONDEXConcept> concepts = this.graph.getConcepts ();

		int originalConceptCount = concepts.size ();
		if ( originalConceptCount == 0 ) {
			log.info ( "Empty graph, quitting the random sampler" );
			return;
		}
		
		// First the concepts, a number of relations will be dragged away with them
		
		log.info ( 
			"Reducing {} concepts to {}% => {}", 
			originalConceptCount, relativeSize * 100, round ( originalConceptCount * relativeSize ) 
		);

		Set<ONDEXRelation> relations = this.graph.getRelations ();
		int originalRelCount = relations.size ();

		deleteEntities ( concepts, relativeSize, this.graph::deleteConcept );
		
		log.info ( "{} concepts remaining", concepts.size () );

		// So, how many relations are we left with?
		//
		
		if ( originalRelCount == 0 ) {
			log.info ( "No relation in the graph, all done");
			return;
		}
		
		// eg, if we want a 10% away and the size is still 30%, excess = 20%
		double relQuota = 1d * relations.size () / originalRelCount;
		double excess = relQuota - relativeSize; 

		log.info ( 
			"Relations reduced from {} to {}, which is {}%", 
			originalRelCount, relations.size (), pcent ( relQuota ) 
		);
		
		if ( excess <= 0 ) {
			log.info ( "So, all done" );
			return;
		}

		// eg, we still need to cut 20% away, so remove anything under this threshold
		log.info ( 
			"Further reducing of {}% => -{} relations", 
			pcent ( excess ), 
			round ( relations.size () * excess ) 
		);
		deleteEntities ( relations, 1 - excess, this.graph::deleteRelation );

		log.info ( "{} relations remaining, all done", relations.size () );
	}
	
	
	/**
	 * Helper to delete a quota of entities. We generate a support array of IDs, do the removals and quit, giving 
	 * the GC the opportunity to cleanup the IDs.
	 */
	private void deleteEntities ( Set<? extends ONDEXEntity> entities, double relativeSize, Consumer<Integer> eraser )
	{
		String etype = entities.iterator ().next () instanceof ONDEXConcept 
			? "concept" 
			: "relation";
		
		final List<Integer> entityIds = entities
			.parallelStream ()
			.map ( ONDEXEntity::getId )
			.collect ( Collectors.toList () );

		Collections.shuffle ( entityIds ); // shuffle the initial list of IDs
		
		// Then consider a quota of them (hence, a random quota)
		IntStream
			.range ( 0, (int) round ( entityIds.size () * ( 1 - relativeSize ) ) )
			.forEach ( i ->  
			{
				eraser.accept ( entityIds.get ( i ) );
				if ( ( i + 1 ) % 10000 == 0 ) log.info ( "Removed {} {}s", i + 1, etype );
			}); 
	}
	
	/** 
	 * Small utility to convert a 0-1 ratio into a percentage string value. Used during logging. 
	 */
	private static String pcent ( double ratio ) {
		return String.format ( "%.2f%%", ratio * 100 );
	}
	
	@Override
	public boolean requiresIndexedGraph () {
		return false;
	}

	@Override
	public String[] requiresValidators () {
		return null;
	}

}
