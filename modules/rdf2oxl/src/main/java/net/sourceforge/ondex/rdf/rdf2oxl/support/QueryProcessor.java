package net.sourceforge.ondex.rdf.rdf2oxl.support;

import static java.lang.String.format;

import java.io.IOException;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.LinkedList;
import java.util.List;
import java.util.function.Consumer;

import org.apache.jena.query.QuerySolution;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

import info.marcobrandizi.rdfutils.jena.SparqlEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.Rdf2OxlConverter;
import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;
import uk.ac.ebi.utils.threading.SizeBasedBatchProcessor;

/**
 * TODO: comment me!
 *
 * The {@link #setConsumer(Consumer) consumer} for this processor is set by {@link Rdf2OxlConverter}, which has its own
 * Spring-coming default and also values taken from {@link ItemConfiguration}. 
 * 
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@Component ( "resourceProcessor" )
public class QueryProcessor extends SizeBasedBatchProcessor<String, List<QuerySolution>>
{
	private String header, trailer;
	
	private SparqlEndPointHelper sparqlHelper;
	private FreeMarkerHelper templateHelper;
		
	private String logPrefix = "[RDF Processor]";
	
	public QueryProcessor ()
	{
		super ();
		this.setDestinationMaxSize ( 1000 );
		this.setDestinationSupplier ( () -> new LinkedList<> () );
	}	
	
	@Override
	protected long getDestinationSize ( List<QuerySolution> dest ) {
		return dest.size ();
	}

	@Override
	public void process ( String resourcesQuery, Object... opts )
	{		
		try
		{
			log.info ( "{}: starting Reading RDF", logPrefix );
						
			QuerySolutionHandler handler = (QuerySolutionHandler) getConsumer ();

			Writer outWriter = handler.getOutWriter ();
			if ( this.header != null ) outWriter.write ( this.header );
			
			@SuppressWarnings ( "unchecked" )
			List<QuerySolution> chunk[] = new List[] { this.getDestinationSupplier ().get () };
			
			sparqlHelper.processSelect ( logPrefix, resourcesQuery, sol -> 
			{
				chunk [ 0 ].add ( sol );

				// As usually, this triggers a new chunk-processing task when we have enough items to process.
				chunk [ 0 ] = handleNewTask ( chunk [ 0 ] );
			});
				
			// Process last chunk
			handleNewTask ( chunk [ 0 ], true );			
			this.waitExecutor ( logPrefix + ": waiting for RDF resource processing tasks to finish" );

			if ( this.trailer != null ) outWriter.write ( this.trailer );
			
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

	
	public SparqlEndPointHelper getSparqlHelper ()
	{
		return sparqlHelper;
	}

	@Autowired
	public void setSparqlHelper ( SparqlEndPointHelper sparqlHelper )
	{
		this.sparqlHelper = sparqlHelper;
	}
	
	public FreeMarkerHelper getTemplateHelper ()
	{
		return templateHelper;
	}
	
	public String getHeader ()
	{
		return header;
	}

	public void setHeader ( String header )
	{
		this.header = header;
	}

	public String getTrailer ()
	{
		return trailer;
	}

	public void setTrailer ( String trailer )
	{
		this.trailer = trailer;
	}

	@Autowired
	public void setTemplateHelper ( FreeMarkerHelper templateHelper )
	{
		this.templateHelper = templateHelper;
	}

	public String getLogPrefix ()
	{
		return logPrefix;
	}

	@Autowired ( required = false ) @Qualifier ( "logPrefix" )	
	public void setLogPrefix ( String logPrefix )
	{
		this.logPrefix = logPrefix;
	}
}
