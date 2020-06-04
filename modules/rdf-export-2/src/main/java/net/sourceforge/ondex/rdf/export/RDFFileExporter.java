package net.sourceforge.ondex.rdf.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.Optional;
import java.util.concurrent.ExecutorService;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.commons.rdf.api.Graph;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFDataMgr;
import org.apache.jena.riot.RDFFormat;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.jena.elt.JenaIoUtils;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ebi.utils.threading.HackedBlockingQueue;

/**
 * A wrapper to invoke the {@link RDFExporter} and save its results to file.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class RDFFileExporter
{
	public static final String DEFAULT_X_LANG = "TURTLE_BLOCKS"; 
	
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public void export ( ONDEXGraph g, final OutputStream out, final String langOrFormat )
	{
		RDFExporter xport = null;
		try
		{
			xport = new RDFExporter (); 			
			final Pair<RDFFormat, Lang> jlang = JenaIoUtils.getLangOrFormat ( langOrFormat );
						
			// There's no point in true parallelism here, because the output stream below is not written
			// in a thread-safe way, so it would need synchronisation at model level (going more fine-grained
			// is too complicated) making the true processing single-thread anyway
			//
			xport.setExecutor ( HackedBlockingQueue.createExecutor ( 1, 1 ) );
			xport.getBatchCollector ().setMaxBatchSize ( 50000l );
			
			xport.setBatchJob ( xfact -> 
			{ 
				log.trace ( "BEGIN RDF writing thread {}", Thread.currentThread ().getName () );
				
				 Model m = xfact.getJenaModel ();
				
				// This should be the way to ensure output streaming in Jena.
				// However, this recognises a few languages/formats only (those registered by StreamRDFWriter)
				// and non-streaming formats are not recognised (e.g. RDFXML).
				//
				// The good news is formats like TURTLE_BLOCKS are still streamed by RDFDataMgr
				// TODO: clarify this with Jena community.
				
				/*
				StreamRDF writer = jlang.getLeft () != null 
					? StreamRDFWriter.getWriterStream ( out, jlang.getLeft () )
					: StreamRDFWriter.getWriterStream ( out, jlang.getRight () );

				StreamOps.graphToStream ( m.getGraph (), writer );
				*/

				if ( jlang.getLeft () != null ) RDFDataMgr.write ( out, m, jlang.getLeft () );
				else RDFDataMgr.write ( out, m, jlang.getRight () );
				
				log.trace ( "END RDF writing thread {}", Thread.currentThread ().getName () );
			});
			
			xport.export ( g ); 
		}
		catch ( IllegalArgumentException ex )
		{
			throw new IllegalArgumentException ( String.format (  
				"Got error: %s. Probably RDF language '%s' is invalid/unsupported", ex.getMessage (), langOrFormat ), 
			ex );
		}
		catch ( Exception ex ) {
			throw new RuntimeException ( "Internal error while closing the RDFExporter: " + ex.getMessage (), ex );
		}
		finally {
			Optional.ofNullable ( xport )
				.map ( RDFExporter::getExecutor )
				.ifPresent ( ExecutorService::shutdownNow );
		}
	}
	
	public void export ( ONDEXGraph g, File file, String langOrFormat )
	{
		try ( OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) ) ) {
			log.info ( "Exporting to '{}'", file.getAbsolutePath () );
			export ( g, out, langOrFormat );
		}
		catch ( FileNotFoundException ex ) {
			throw new UncheckedIOException ( "RDF export file '" + file.getAbsolutePath () + "' not found" , ex );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( 
				"Error while RDF-exporting to '" + file.getAbsolutePath () + "': " + ex.getMessage () , ex 
			);
		}
	}
	
	public void export ( ONDEXGraph g, String path, String langOrFormat )
	{
		export ( g, new File ( path ), langOrFormat );
	}

}
