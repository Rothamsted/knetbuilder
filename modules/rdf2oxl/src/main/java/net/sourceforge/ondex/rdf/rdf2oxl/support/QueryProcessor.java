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

import com.google.common.util.concurrent.UncheckedExecutionException;

import info.marcobrandizi.rdfutils.jena.SparqlEndPointHelper;
import net.sourceforge.ondex.rdf.rdf2oxl.Rdf2OxlConverter;
import net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker.FreeMarkerHelper;
import uk.ac.ebi.utils.threading.SizeBasedBatchProcessor;

/**
 * # The SPARQL query processor
 * 
 * Each instance gets resource URIs for a given type (e.g., concepts, relations) and pass them to a 
 * {@link QuerySolutionHandler}, for getting resource details and passing them to the OXL renderer.  
 * 
 * See the [package description](package-summary.html) for details.  
 *
 * The {@link #setBatchJob(Consumer) consumer} for this processor is set by {@link Rdf2OxlConverter}, which has its own
 * Spring-coming defaults and also values taken from {@link ItemConfiguration}.  
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
	
	private Exception executionException = null;

	protected long lastExecutionCount = -1;
	
	
	public QueryProcessor ()
	{
		super ();
		this.setBatchMaxSize ( 1000 );
		this.setBatchFactory ( () -> new LinkedList<> () );
	}	
	
	@Override
	protected long getCurrentBatchSize ( List<QuerySolution> batch ) {
		return batch.size ();
	}

	@Override
	public void process ( String resourcesQuery, Object... opts )
	{		
		try
		{
			log.info ( "{}: starting Reading RDF", logPrefix );
						
			QuerySolutionHandler handler = (QuerySolutionHandler) getBatchJob ();

			Writer outWriter = handler.getOutWriter ();
			if ( this.header != null ) outWriter.write ( this.header );
			
			@SuppressWarnings ( "unchecked" )
			List<QuerySolution> batch[] = new List[] { this.getBatchFactory ().get () };
			
			lastExecutionCount = sparqlHelper.processSelect ( logPrefix, resourcesQuery, sol -> 
			{
				batch [ 0 ].add ( sol );

				// As usually, this triggers a new chunk-processing task when we have enough items to process.
				batch [ 0 ] = handleNewBatch ( batch [ 0 ] );
			});
				
			// Process last chunk
			handleNewBatch ( batch [ 0 ], true );			
			this.waitExecutor ( logPrefix + ": waiting for RDF resource processing tasks to finish" );

			// Did everything go fine?
			Exception lastEx = getExecutionException ();
			if ( lastEx != null ) throw new UncheckedExecutionException ( 
				"Error while processing RDF in parallel, everything stops here", 
				lastEx 
			);
			
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


	@Override
	protected List<QuerySolution> handleNewBatch ( List<QuerySolution> currentBatch, boolean forceFlush )
	{
		Exception lastEx = this.getExecutionException ();
		if ( lastEx != null ) throw new UncheckedExecutionException ( 
			"Error while processing RDF in parallel, everything stops here", 
			lastEx 
		);
		
		return super.handleNewBatch ( currentBatch, forceFlush );
	}
	
	
	protected synchronized void setExecutionException ( Exception ex ) 
	{
		if ( this.executionException != null ) return;
		this.executionException = ex;
	}
	
	/**
	 * This is used by the {@link QueryProcessor#handleNewBatch(List, boolean) processor}, to stop further processing
	 * when any running thread terminates with an exception.
	 */
	public synchronized Exception getExecutionException () {
		return this.executionException;
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

	
	@Override
	protected Runnable wrapBatchJob ( Runnable task )
	{
		return super.wrapBatchJob ( () -> 
		{
			try {
				task.run ();
			}
			catch ( Exception ex ) 
			{
				this.setExecutionException ( ex );
				String msg = format ( 
					"Error while running RDF processing thread %s: %s", 
					Thread.currentThread ().getName (), ex.getMessage () 
				);
				log.error ( msg, ex ); 
			}
		});
	}	
}
