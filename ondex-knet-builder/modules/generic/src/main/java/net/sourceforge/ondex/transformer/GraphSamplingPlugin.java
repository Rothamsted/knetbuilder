package net.sourceforge.ondex.transformer;

import static java.lang.Math.round;

import java.util.Collections;
import java.util.stream.Collectors;
import java.util.stream.Stream;

import org.apache.commons.lang3.tuple.Pair;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.args.ArgumentDefinition;
import net.sourceforge.ondex.args.StringArgumentDefinition;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ebi.utils.runcontrol.ProgressLogger;

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
		
		int nentities0 = graph.getConcepts ().size () + graph.getRelations ().size ();
		if ( nentities0 == 0 ) {
			log.warn ( "The graph is empty, no sampling to do, quitting the plug-in" );
			return;
		}

		log.info (
			"Sampling plug-in, reducing {} concepts and {} entities",
			graph.getConcepts ().size (),
			graph.getRelations ().size ()
		);

		Stream<Pair<Boolean, Integer>> conceptIdx = this.graph.getConcepts ()
		.parallelStream ()
		.map ( c -> Pair.of ( true, c.getId () ) ); // true for concepts
		
		Stream<Pair<Boolean, Integer>> relationIdx = this.graph.getRelations ()
		.parallelStream ()
		.map ( r -> Pair.of ( false, r.getId () ) );
		
		var entityIdx = Stream.concat ( conceptIdx, relationIdx )
		  .collect ( Collectors.toList () );
		
		Collections.shuffle ( entityIdx );
		
		var progress = new ProgressLogger ( "{} entities removed", 10000 );
		int ncut = (int) round ( entityIdx.size () * ( 1 - relativeSize ) );
		for ( int i = 0; i < ncut; i++ )
		{
			var idx = entityIdx.get ( i );
			if ( idx.getLeft () )
				graph.deleteConcept ( idx.getRight () );
			else
				graph.deleteRelation ( idx.getRight () );

			int newSize = graph.getConcepts ().size () + graph.getRelations ().size ();
			progress.update ( nentities0 - newSize );

			// We need to check the size here, since concepts might drag relations with them
			if ( newSize <= nentities0 * relativeSize )
				break;
		}
		
		log.info (
			"Done. The reduced graph has {} concepts and {} relations",
			graph.getConcepts ().size (),
			graph.getRelations ().size ()
		);	
	}

	
	/** 
	 * Small utility to convert a 0-1 ratio into a percentage string value. Used during logging.
	 * TODO: remove. 
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
