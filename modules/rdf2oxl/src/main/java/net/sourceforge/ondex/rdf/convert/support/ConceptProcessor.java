package net.sourceforge.ondex.rdf.convert.support;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.Map;
import java.util.Set;

import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.apache.jena.rdf.model.Resource;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@Component ( "conceptProcessor" )
public class ConceptProcessor extends ResourceProcessor
{	
	@Autowired @Qualifier ( "conceptIds" )
	private Map<String, Integer> conceptIds;
	
	@Override
	public void process ( String foo, Object... opts )
	{		
		String logPrefix = this.getLogPrefix ();
		try
		{
			log.info ( "{}: Reusing previous IRIs", logPrefix );
						
			ResourceHandler handler = (ResourceHandler) getConsumer ();
			Writer outWriter = handler.getOutWriter ();
			
			if ( this.getHeader () != null ) outWriter.write ( this.getHeader () );
			
			@SuppressWarnings ( "unchecked" )
			Set<Resource> chunk[] = new Set[] { this.getDestinationSupplier ().get () };

			Model model = ModelFactory.createDefaultModel ();
			
			this.conceptIds.forEach ( (iri, id) -> 
			{
				chunk [ 0 ].add ( model.createResource ( iri ) );
			
				// As usually, this triggers a new chunk-processing task when we have enough items to process.
				chunk [ 0 ] = handleNewTask ( chunk [ 0 ] );
			});
			
			// Process last chunk
			handleNewTask ( chunk [ 0 ], true );		
			this.waitExecutor ( this.getLogPrefix () + ": waiting for RDF resource processing tasks to finish" );
	
			if ( this.getTrailer () != null ) outWriter.write ( this.getTrailer () );
			
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
