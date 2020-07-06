package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.io.FileOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.util.Map;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.apache.commons.lang3.tuple.Pair;
import org.apache.jena.query.Dataset;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.system.Txn;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.context.ApplicationContext;
import org.springframework.context.ConfigurableApplicationContext;
import org.springframework.context.support.ClassPathXmlApplicationContext;

import com.machinezoo.noexception.Exceptions;
import com.machinezoo.noexception.throwing.ThrowingConsumer;

import freemarker.template.Template;
import freemarker.template.TemplateException;
import info.marcobrandizi.rdfutils.jena.TDBEndPointHelper;

/**
 * A few test utilities used with RDF2OXL tests.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Oct 2018</dd></dl>
 *
 */
public class TestUtils
{
	private static Logger log = LoggerFactory.getLogger ( TestUtils.class );
	
	/**
	 * 
	 * An {@link OutputStream} that collects the flow coming from upstream into a string. Useful for testing.
	 *
	 * TODO: Move to jutils.
	 *
	 */
	public static class CollectingOutputStream extends OutputStream
	{
		private StringWriter sw = new StringWriter ();
		private TeeOutputStream splitOut;
		
		public CollectingOutputStream ( OutputStream out )
		{
			this.splitOut = new TeeOutputStream ( out, new WriterOutputStream ( sw, "UTF-8" ) );
		}
				
		public CollectingOutputStream ()
		{
			this ( System.out );
		}

		public CollectingOutputStream ( String outPath ) throws IOException
		{
			this ( new FileOutputStream ( outPath ) );
		}
		
		@Override
		public String toString () {
			return this.sw.toString ();
		}

		public void write ( byte[] b ) throws IOException
		{
			splitOut.write ( b );
		}

		public void write ( byte[] b, int off, int len ) throws IOException
		{
			splitOut.write ( b, off, len );
		}

		public void write ( int b ) throws IOException
		{
			splitOut.write ( b );
		}

		public void flush () throws IOException
		{
			splitOut.flush ();
		}

		public void close () throws IOException
		{
			splitOut.close ();
		}
	}

	/** A simple wrapper that processes a FreeMarker template and returns its output */
	public static String processTemplate ( Template tpl, Map<String, Object> data )
		throws IOException, TemplateException
	{
		String out = getOutput ( w -> tpl.process ( data, new OutputStreamWriter ( w ) ));
		log.info ( "\n\nProcessed Template:\n{}\n", out );
		return out;
	}
		
  /**
   * Uses {@link CollectingOutputStream} to collect the `action` output into a string.
   */
	public static String getOutput ( ThrowingConsumer<OutputStream> action )
	{
		CollectingOutputStream w = new CollectingOutputStream ();
		Exceptions.sneak ().run ( () -> action.accept ( w ) ); 
		return w.toString ();
	}
	
	/**
	 * A simple wrapper that uses the RDF2OXL configuration to load RDF files into a TDB triple store.
	 */
	@SafeVarargs	
	public static void generateTDB ( ApplicationContext springContext, String tdbPath, Pair<InputStream, String>... rdfInputs )
		throws IOException
	{
		TDBEndPointHelper sparqlHelper = springContext.getBean ( TDBEndPointHelper.class );
		sparqlHelper.open ( tdbPath );
		
		Dataset ds = sparqlHelper.getDataSet ();
		Txn.executeWrite ( ds, Exceptions.sneak ().runnable ( () ->
		{
			Model m = ds.getDefaultModel ();
			for ( Pair<InputStream, String> rdfInput: rdfInputs )
				m.read ( rdfInput.getLeft (),  null, rdfInput.getRight () );
		}));
	}		

	/** Uses the RDF2OXL default Spring context (`default_beans.xml` in the classpath) */
	@SafeVarargs	
	public static void generateTDB ( String tdbPath, Pair<InputStream, String>... rdfInputs ) throws IOException
	{
		try ( ConfigurableApplicationContext springContext = new ClassPathXmlApplicationContext ( "default_beans.xml" ) )
		{
			generateTDB ( springContext, tdbPath, rdfInputs );
		}
	}
}
