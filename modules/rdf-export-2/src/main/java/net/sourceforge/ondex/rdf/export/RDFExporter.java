package net.sourceforge.ondex.rdf.export;

import java.util.Collection;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;
import java.util.function.Consumer;
import java.util.function.Supplier;
import java.util.stream.Stream;

import org.apache.commons.rdf.jena.JenaGraph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXGraphMetaData;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.rdf.export.mappers.RDFXFactory;

/**
 * The RDFExporter machinery. 
 * 
 * This triggers the exporting of an {@link ONDEXGraph}, passing it to the {@link RDFXFactory java2rdf-based RDF mappers}
 * and calling an handler parameter for the RDF chunks generated during the process.
 * 
 * The handler is supposed to do some concrete job, such as saving to a file or sending the RDF to a
 * triple store. 
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
		final RDFXFactory xfactf = xfact; 
		ONDEXGraphMetaData metaData = graph.getMetaData ();
		Stream.of ( 
			metaData.getConceptClasses (), 
			metaData.getRelationTypes (), 
			metaData.getAttributeNames (), 
			metaData.getEvidenceTypes (),
			metaData.getUnits ()
		)
		.flatMap ( Collection::stream )
		.forEach ( meta -> xfactf.map ( meta ) );
								
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
			xfact.map ( relation );
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

	/**
	 * Checks if the graph being built in xfact is {@link #getChunkSize() big enough} and, if yes, sends it 
	 * to the {@link #getExportHandler()} and returns a new {@link RDFXFactory}, equipped with a 
	 * {@link #getJenaModelSupplier() new graph}. 
	 * 
	 * @param forceFlush if true it flushes the data independently of {@link #getChunkSize()}.	 * @return
	 */
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

	
	/**
	 * This is invoked from time to time, with subsets of ONDEX/RDF data. This is assumed to do something
	 * concrete with that (see above). Note that handlers are invoked in parallel and they must take care of 
	 * possibly necessary synchronisation (e.g., writing triples to a shared file or database).
	 */
	public Consumer<Model> getExportHandler ()
	{
		return exportHandler;
	}

	public RDFExporter setExportHandler ( Consumer<Model> exportHandler )
	{
		this.exportHandler = exportHandler;
		return this;
	}

	/**
	 * Used every time a new graph has to be generated for a new chunk of exported data. Here you can decide
	 * if you want to deal the export with a shared single graph (just make this supplier to always return 
	 * the same model).
	 * 
	 * The default version uses the {@link ModelFactory jena model factory} and initialises it with
	 * {@link NamespaceUtils#getNamespaces() our default namespaces}.
	 * 
	 */
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

	/**
	 * The thread pool manager used by {@link #export(ONDEXGraph)}. By default this is 
	 * {@link ThreadPoolExecutor 
	 * ThreadPoolExecutor( &lt;available processors&gt;, Integer.MAX_VALUE, ..., LinkedBlockingQueue (processors*2) )},
	 * that is, a pool where a fixed number of threads is running at any time (up to the 
	 * {@link Runtime#availableProcessors() number of processors available}) and where the  
	 * {@link ExecutorService#submit(Runnable) task submission operation} is also put on hold if the 
	 * pool is full, waiting for some thread to finish its job.
	 * 
	 * Normally you shouldn't need to change this parameter, except, maybe, where parallelism isn't such 
	 * worth and hence you prefer a fixed pool of size 1 ({@link RDFFileExporter} does so).
	 * 
	 */
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
