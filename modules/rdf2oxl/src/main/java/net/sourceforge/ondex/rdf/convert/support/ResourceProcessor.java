package net.sourceforge.ondex.rdf.convert.support;

import java.util.HashSet;
import java.util.Set;

import org.apache.jena.rdf.model.Resource;

import info.marcobrandizi.rdfutils.jena.SparqlEndPointHelper;
import uk.ac.ebi.utils.threading.SizeBasedBatchProcessor;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
public class ResourceProcessor extends SizeBasedBatchProcessor<String, Set<Resource>>
{
	private SparqlEndPointHelper sparqlHelper;
	private String logPrefix = "RDF Processor";
	
	public ResourceProcessor ()
	{
		super ();
		this.setDestinationMaxSize ( 1000 );
		this.setDestinationSupplier ( () -> new HashSet<> () );
	}	
	
	@Override
	protected long getDestinationSize ( Set<Resource> dest ) {
		return dest.size ();
	}

	@Override
	public void process ( String resourcesQuery, Object... opts )
	{		
		@SuppressWarnings ( "unchecked" )
		Set<Resource> chunk[] = new Set[] { this.getDestinationSupplier ().get () };
		
		sparqlHelper.processSelect ( logPrefix, resourcesQuery, sol -> 
		{
			chunk [ 0 ].add ( sol.getResource ( "resourceIri" ) );

			// As usually, this triggers a new chunk-processing task when we have enough items to process.
			chunk [ 0 ] = handleNewTask ( chunk [ 0 ] );
		});
			
		// Process last chunk
		handleNewTask ( chunk [ 0 ], true );
		
		this.waitExecutor ( logPrefix + ": waiting for RDF resource processing tasks to finish" );
		log.info ( logPrefix + ": all RDF resources processed" );
	}

	
	public SparqlEndPointHelper getSparqlHelper ()
	{
		return sparqlHelper;
	}

	public void setSparqlHelper ( SparqlEndPointHelper sparqlHelper )
	{
		this.sparqlHelper = sparqlHelper;
	}

	public String getLogPrefix ()
	{
		return logPrefix;
	}

	public void setLogPrefix ( String logPrefix )
	{
		this.logPrefix = logPrefix;
	}
}
