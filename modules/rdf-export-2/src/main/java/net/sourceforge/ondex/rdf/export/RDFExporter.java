package net.sourceforge.ondex.rdf.export;

import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;

import org.apache.commons.rdf.api.Graph;
import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.commons.rdf.jena.JenaRDF;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.commonsrdf.CommonsRDFUtils;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.rdf.export.mappers.RDFXFactory;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>21 Nov 2017</dd></dl>
 *
 */
public class RDFExporter
{
	private Consumer<Model> exportHandler;
	private Supplier<Model> jenaModelSupplier = () -> 
	{
		Model m = ModelFactory.createDefaultModel ();
		m.setNsPrefixes ( NamespaceUtils.getNamespaces () );
		return m;
	};
	
	private long chunkSize = 100000;
	private ExecutorService executor;
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	public RDFExporter ()
	{
		int poolSize = Runtime.getRuntime().availableProcessors();
		
		this.executor = new ThreadPoolExecutor (
			poolSize, 
			Integer.MAX_VALUE, 
			0L, TimeUnit.MILLISECONDS, 
			new LinkedBlockingQueue<> ( poolSize * 2 ) 
		);
	}
	
	public void export ( ONDEXGraph graph )
	{
		RDFXFactory xfact = new RDFXFactory ( jenaModelSupplier.get () );
		
		// TODO: Graph
		
		for ( ConceptClass cc: graph.getMetaData ().getConceptClasses () )
			xfact.map ( cc );

		for ( RelationType rt: graph.getMetaData ().getRelationTypes () )
			xfact.map ( rt );
		
		// We export all metadata in one chunk. This is typically small at this point and flushing it out 
		// allows a client to handle the whole T-Box first.
		xfact = this.handleChunkSwitching ( xfact, true );
		
		for ( ONDEXConcept concept: graph.getConcepts () )
		{
			xfact.map ( concept );
			
			xfact = this.handleChunkSwitching ( xfact );
		}

		for ( ONDEXRelation relation: graph.getRelations () )
		{
			// do the export
			
			xfact = this.handleChunkSwitching ( xfact );
		}
		
		this.handleChunkSwitching ( xfact, true );
		
		executor.shutdown ();

		// Wait to finish
		try
		{
			while ( !executor.isTerminated () ) 
			{
				log.info ( "Waiting for RDF export completion, please wait..." ); 
				this.executor.awaitTermination ( 5, TimeUnit.MINUTES );
			}
		}
		catch ( InterruptedException ex ) {
			throw new RuntimeException ( "Internal error: " + ex.getMessage (), ex );
		}
		log.info ( "...RDF export finished" );
	}
	
	private RDFXFactory handleChunkSwitching ( final RDFXFactory xfact ){
		return handleChunkSwitching ( xfact, false );
	}

	private RDFXFactory handleChunkSwitching ( final RDFXFactory xfact, boolean forceFlush )
	{
		final Model model = ( (JenaGraph) xfact.getGraphModel () ).asJenaModel ();
		
		if ( !forceFlush && model.size () < chunkSize ) return xfact;
		executor.submit ( new Runnable() 
		{
			@Override
			public void run () {
				exportHandler.accept ( model );
			}
		});
		
		return  new RDFXFactory ( jenaModelSupplier.get () );
	}

	
	
	public Consumer<Model> getExportHandler ()
	{
		return exportHandler;
	}

	public RDFExporter setExportHandler ( Consumer<Model> exportHandler )
	{
		this.exportHandler = exportHandler;
		return this;
	}

	public Supplier<Model> getJenaModelSupplier ()
	{
		return jenaModelSupplier;
	}

	public RDFExporter setJenaModelSupplier ( Supplier<Model> jenaModelSupplier )
	{
		this.jenaModelSupplier = jenaModelSupplier;
		return this;
	}

	public long getChunkSize ()
	{
		return chunkSize;
	}

	public RDFExporter setChunkSize ( long chunkSize )
	{
		this.chunkSize = chunkSize;
		return this;
	}

	public ExecutorService getExecutor ()
	{
		return executor;
	}

	public RDFExporter setExecutor ( ExecutorService executor )
	{
		this.executor = executor;
		return this;
	}
		
}
