package net.sourceforge.ondex.rdf.convert.support;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.List;

import org.apache.jena.query.QuerySolution;
import org.springframework.stereotype.Component;

/**
 * This is a {@link QueryProcessor} specific to Ondex straight relations.
 * It expects to read queries returning ?from ?typeId ?to, which it then
 * passes to TODO. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>28 Sep 2018</dd></dl>
 *
 */
@Component ( "straightRelationProcessor" )
public class StraightRelationProcessor extends QueryProcessor
{
	@Override
	public void process ( String resourcesQuery, Object... opts )
	{		
		String logPrefix = this.getLogPrefix ();

		try
		{
			log.info ( "{}: starting Reading RDF", logPrefix );
						
			QuerySolutionHandler handler = (QuerySolutionHandler) getConsumer ();
			Writer outWriter = handler.getOutWriter ();
						
			String header = this.getHeader ();
			if ( header != null ) outWriter.write ( header );
			
			@SuppressWarnings ( "unchecked" )
			List<QuerySolution> chunk[] = new List[] { this.getDestinationSupplier ().get () };
			
			this.getSparqlHelper ().processSelect ( logPrefix, resourcesQuery, sol -> 
			{
				chunk [ 0 ].add ( sol );

				// As usually, this triggers a new chunk-processing task when we have enough items to process.
				chunk [ 0 ] = handleNewTask ( chunk [ 0 ] );
			});
				
			// Process last chunk
			handleNewTask ( chunk [ 0 ], true );			
			this.waitExecutor ( logPrefix + ": waiting for RDF resource processing tasks to finish" );

			String trailer = this.getTrailer ();
			if ( trailer != null ) outWriter.write ( trailer );
			
			log.info ( "{}: all RDF resources processed", logPrefix );
		}
		catch ( IOException ex )
		{
			throw new UncheckedIOException ( 
				format ( "%s: I/O error while processing RDF data: %s", logPrefix, ex.getMessage () ),
				ex
			);
		}
	}
}
