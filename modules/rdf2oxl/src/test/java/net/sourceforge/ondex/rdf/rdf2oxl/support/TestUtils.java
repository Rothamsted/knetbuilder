package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.io.FileWriter;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.Writer;
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
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>16 Oct 2018</dd></dl>
 *
 */
public class TestUtils
{
	private static Logger log = LoggerFactory.getLogger ( TestUtils.class );
	
	public static class OutputCollectorWriter extends FilterWriter
	{
		private StringWriter sw = new StringWriter ();
		
		public OutputCollectorWriter ( Writer destinationWriter )
		{
			super ( new StringWriter () );
			this.lock = this.out = new OutputStreamWriter ( new TeeOutputStream (
					new WriterOutputStream ( sw , "UTF-8"),
					new WriterOutputStream ( destinationWriter, "UTF-8" )
			));
		}
		
		public OutputCollectorWriter ( OutputStream destinationStream )
		{
			this ( new OutputStreamWriter ( destinationStream ) );
		}
		
		public OutputCollectorWriter ()
		{
			this ( System.out );
		}

		public OutputCollectorWriter ( String outPath ) throws IOException
		{
			this ( new FileWriter ( outPath ) );
		}
		
		
		@Override
		public String toString () {
			return this.sw.toString ();
		}
	}

	public static String processTemplate ( Template tpl, Map<String, Object> data )
		throws IOException, TemplateException
	{
		String out = getOutput ( w -> tpl.process ( data, w ) );
		log.info ( "\n\nProcessed Template:\n{}\n", out );
		return out;
	}
	
	public static String getOutput ( ThrowingConsumer<Writer> action )
	{
		OutputCollectorWriter w = new OutputCollectorWriter ();
		Exceptions.sneak ().run ( () -> action.accept ( w ) ); 
		return w.toString ();
	}
	
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

	@SafeVarargs	
	public static void generateTDB ( String tdbPath, Pair<InputStream, String>... rdfInputs ) throws IOException
	{
		try ( ConfigurableApplicationContext springContext = new ClassPathXmlApplicationContext ( "default_beans.xml" ) )
		{
			generateTDB ( springContext, tdbPath, rdfInputs );
		}
	}
}
