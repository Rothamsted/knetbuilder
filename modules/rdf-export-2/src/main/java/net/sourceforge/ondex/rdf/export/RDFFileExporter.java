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

import org.apache.jena.riot.Lang;
import org.apache.jena.riot.RDFFormat;
import org.apache.jena.riot.system.StreamRDFWriter;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import net.sourceforge.ondex.core.ONDEXGraph;

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
	
	public void export ( ONDEXGraph g, final OutputStream out, final String lang )
	{
		try
		{
			Lang jlang = null;
			RDFFormat jfmt = null;
			try {
				jlang = (Lang) Lang.class.getField ( lang ).get ( null );
			}
			catch ( NoSuchFieldException ex ) {
				jfmt = (RDFFormat) RDFFormat.class.getField ( lang ).get ( null );
			}
			final Lang jlangf = jlang; 
			final RDFFormat jfmtf = jfmt;
			
			
			RDFExporter xport = new RDFExporter ();
			
			// There's no point in true parallelism here, because the output stream below is not written
			// in a thread-safe way, so it would need synchronization at model level (going more fine-grained
			// is too complicated) making the true processing single-thread anyway
			//
			xport.setExecutor ( new ThreadPoolExecutor (
				1, 
				Integer.MAX_VALUE, 
				0L, TimeUnit.MILLISECONDS, 
				new LinkedBlockingQueue<> ( 100 ) 
			));
			
			xport.setExportHandler ( m -> 
			{ 
				log.trace ( "BEGIN RDF writing thread {}", Thread.currentThread ().getName () );
				if ( jlangf != null )
					StreamRDFWriter.write ( out, m.getGraph (), jlangf );
				else
					StreamRDFWriter.write ( out, m.getGraph (), jfmtf );
				log.trace ( "END RDF writing thread {}", Thread.currentThread ().getName () );
			});
			
			xport.export ( g ); 
			out.close ();
		}
		catch ( NoSuchFieldException | IllegalArgumentException | IllegalAccessException | SecurityException ex )
		{
			throw new IllegalArgumentException ( String.format (  
				"Got error: %s. Probably RDF language '%s' is invalid/unsupported", ex.getMessage (), lang ), 
			ex );
		}
		catch ( IOException ex ) {
			throw new UncheckedIOException ( "I/O Error while exporting RDF: " + ex.getMessage (), ex );
		}
	}
	
	public void export ( ONDEXGraph g, File file, String lang )
	{
		try
		{
			export ( g, new BufferedOutputStream ( new FileOutputStream ( file ) ), lang );
		}
		catch ( FileNotFoundException ex ) {
			throw new UncheckedIOException ( "RDF export file '" + file.getAbsolutePath () + "' not found" , ex );
		}
	}
	
	public void export ( ONDEXGraph g, String path, String lang )
	{
		export ( g, new File ( path ), lang );
	}

}
