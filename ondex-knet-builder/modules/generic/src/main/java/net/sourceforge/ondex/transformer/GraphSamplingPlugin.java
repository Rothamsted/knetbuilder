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
import net.sourceforge.ondex.args.FloatRangeArgumentDefinition;
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
			new FloatRangeArgumentDefinition (
				"relativeSize", 
				"The relative (0-1) final size you want",
				false, // required 
				0.1f  // default
			)
		};
	}

	@Override
	public void start () throws Exception
	{	
		double relativeSize = (Float) this.getArguments ().getUniqueValue ( "relativeSize" );
		sample ( null, relativeSize );
	}
	
	public void sample ( ONDEXGraph inGraph, double relativeSize )
	{
		if ( inGraph != null ) this.setONDEXGraph ( inGraph );
		if ( this.graph == null ) throw new IllegalStateException ( "Input graph is null" );

		// First the relations, since a graph with too many isolated concepts isn't very
		// significant
		//		
		Set<ONDEXConcept> concepts = this.graph.getConcepts ();
		Set<ONDEXRelation> relations = this.graph.getRelations ();

		int conceptsNo0 = concepts.size ();
		int relationsNo0 = relations.size ();

		if ( conceptsNo0 == 0 )
		{
			log.warn ( "Empty graph, sampler is quitting without doing anything" );
			return;
		}
		
		log.info ( "Reducing {} concepts and {} relations to {}", conceptsNo0, relationsNo0, pcent ( relativeSize ) );
		
		log.info ( "Reducing concepts" );
		deleteEntities ( concepts, relativeSize, this.graph::deleteConcept );
		
		if ( relationsNo0 == 0 )
			log.info ( "Unconnected graph, sampling nodes only" );
		else
		{
			var curRelSize = (0d + concepts.size () + relations.size () ) / (conceptsNo0 + relationsNo0 );
			if ( curRelSize - relativeSize <= 0 )
				log.info ( "Graph is already small enough, skipping relations pruning" );
			else
			{
				log.info ( "Reducing relations" );

				// We want: (r1 + c1 ) / (r0 + c0 ) = R0  R0 is relativeSize
				// => r1 = R0 * ( r0 + c0 ) - c1
				var newRelationsNo = relativeSize * ( conceptsNo0 + relationsNo0 ) - concepts.size ();
				var newRelativeSize = newRelationsNo / relations.size (); 
				
				deleteEntities ( relations, newRelativeSize, this.graph::deleteRelation );
			}
		}
		
		log.info ( "All done. New graph has {} concepts and {} relations", concepts.size (), relations.size () );
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
		
		int newSize = (int) round ( entities.size () * ( 1 - relativeSize ) );
		if ( newSize == entities.size () ) 
			log.info ( "Too few {}s to select a sample, taking them all", etype );
		
		List<Integer> entityIds = entities
			.parallelStream ()
			.map ( ONDEXEntity::getId )
			.collect ( Collectors.toList () );
		
		Collections.shuffle ( entityIds ); // shuffle the initial list of IDs
		
		// Then consider a quota of them (hence, a random quota)
		IntStream
		.range ( 0, newSize )
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
