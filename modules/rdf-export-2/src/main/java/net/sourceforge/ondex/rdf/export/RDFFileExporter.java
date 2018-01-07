package net.sourceforge.ondex.rdf.export;

import java.io.BufferedOutputStream;
import java.io.File;
import java.io.FileNotFoundException;
import java.io.FileOutputStream;
import java.io.IOException;
import java.io.OutputStream;
import java.io.UncheckedIOException;
import java.util.concurrent.LinkedBlockingQueue;
import java.util.concurrent.ThreadPoolExecutor;
import java.util.concurrent.TimeUnit;

import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import info.marcobrandizi.rdfutils.jena.elt.JenaIoUtils;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ebi.utils.threading.HackedBlockingQueue;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Nov 2017</dd></dl>
 *
 */
public class RDFFileExporter
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	public void export ( ONDEXGraph g, final OutputStream out, final String langOrFormat )
	{
		try
		{
			final Pair<RDFFormat, Lang> jlang = JenaIoUtils.getLangOrFormat ( langOrFormat );
			
			RDFExporter xport = new RDFExporter ();
			
			// There's no point in true parallelism here, because the output stream below is not written
			// in a thread-safe way, so it would need synchronization at model level (going more fine-grained
			// is too complicated) making the true processing single-thread anyway
			//
			ThreadPoolExecutor executor = (ThreadPoolExecutor) xport.getExecutor ();
			executor.setCorePoolSize ( 1 );
			executor.setMaximumPoolSize ( 1 );
			
			xport.setConsumer ( m -> 
			{ 
				log.trace ( "BEGIN RDF writing thread {}", Thread.currentThread ().getName () );
				if ( jlang.getLeft () != null )
					StreamRDFWriter.write ( out, m.getGraph (), jlang.getLeft () );
				else
					StreamRDFWriter.write ( out, m.getGraph (), jlang.getRight () );
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
	}
	
	public void export ( ONDEXGraph g, File file, String lang )
	{
		try ( OutputStream out = new BufferedOutputStream ( new FileOutputStream ( file ) ) )
		{
			export ( g, out, lang );
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
	
	public void export ( ONDEXGraph g, String path, String lang )
	{
		export ( g, new File ( path ), lang );
	}

}
