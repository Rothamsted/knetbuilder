package net.sourceforge.ondex.rdf.rdf2oxl.support;

import java.io.FileWriter;
import java.io.FilterOutputStream;
import java.io.FilterWriter;
import java.io.IOException;
import java.io.OutputStream;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UnsupportedEncodingException;
import java.io.Writer;
import java.util.Map;
import java.util.function.Consumer;

import org.apache.commons.io.output.TeeOutputStream;
import org.apache.commons.io.output.WriterOutputStream;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.machinezoo.noexception.Exceptions;
import com.machinezoo.noexception.throwing.ThrowingConsumer;

import freemarker.template.Template;
import freemarker.template.TemplateException;

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
}
