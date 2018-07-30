package net.sourceforge.ondex.rdf.convert.support.freemarker;

import static java.lang.String.format;
import static java.lang.System.out;

import java.io.IOException;
import java.io.OutputStreamWriter;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;

import com.machinezoo.noexception.Exceptions;

import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import info.marcobrandizi.rdfutils.jena.SparqlUtils;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
public class FreeMarkerHelper
{
	private Configuration templateConfig;

	public void processJenaModel ( Model model, String templateName, Writer outWriter )
	{
		processJenaModel ( model, templateName, outWriter, null );
	}

	public void processJenaModel ( Model model, String templateName, Writer outWriter, Map<String, Object> initialData )
	{
		try
		{
			StringWriter sw = new StringWriter ();
			model.write ( sw, "JSON-LD" );
						
			if ( initialData == null ) initialData = new HashMap<> ();
			initialData.put ( "json", sw.toString () );
			
			Template tpl = templateConfig.getTemplate ( templateName );
			tpl.process ( initialData, new OutputStreamWriter ( out ) );
		}
		catch ( IOException ex ) 
		{
			throw new UncheckedIOException ( 
				format ( "I/O error while processing OXL template '%s': %s", templateName, ex.getMessage () ),
				ex
			);
		}
		catch ( TemplateException ex ) {
			throw new UncheckedTemplateException ( 
				format ( "I/O error while processing OXL template '%s': %s", templateName, ex.getMessage () ),
				ex
			);
		}	
	}

	public Configuration getTemplateConfig ()
	{
		return templateConfig;
	}

	public void setTemplateConfig ( Configuration templateConfig )
	{
		this.templateConfig = templateConfig;
	}
}
