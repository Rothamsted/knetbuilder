package net.sourceforge.ondex.rdf.convert.support.freemarker;

import static java.lang.String.format;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.throwEx;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.jsonldjava.core.JsonLdError;
import com.github.jsonldjava.core.JsonLdOptions;
import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import freemarker.template.Version;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import uk.ac.ebi.utils.exceptions.ExceptionUtils;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@Component
public class FreeMarkerHelper
{
	private Configuration templateConfig;

	public void processTemplate ( String templateName, Writer outWriter ) {
		processTemplate ( templateName, outWriter, null );
	}

	public void processTemplate ( String templateName, Writer outWriter, Map<String, Object> data )
	{
		try 
		{
			Template tpl = templateConfig.getTemplate ( templateName );
			tpl.process ( data, outWriter );
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
	
	
	public Map<String, Object> getTemplateData ( Model model )
	{
		try
		{
			Map<String, Object> result = new HashMap<> ();

			StringWriter sw = new StringWriter ();
			model.write ( sw, "JSON-LD" );
						
			// We need some post-processing of what Jena returns
			@SuppressWarnings ( "unchecked" )
			Map<String, Object> js = (Map<String, Object>) JsonUtils.fromString ( sw.toString () );
						
			result.put ( "js", js.get ( "@graph" ) );

			// This might be useful in several cases
			result.put ( NamespaceUtils.class.getSimpleName (), getStaticClassWrapper ( NamespaceUtils.class ) );

			return result;
		}
		catch ( IOException ex )
		{
			throw buildEx ( 
				UncheckedIOException.class, ex,
				"Error while getting JSON-LD data from Jena: %s", ex.getMessage () 
			);
		}
	}
	
	
	public Configuration getTemplateConfig ()
	{
		return templateConfig;
	}

	@Autowired
	public void setTemplateConfig ( Configuration templateConfig )
	{
		this.templateConfig = templateConfig;
	}
		
	/**
	 * A static class wrapper, which allows to refer static methods in FTL. If you send the wrapper with a 
	 * key in an hash map data model, then you can do {@code <name>.<staticMethod>(...)} in the template.
	 */
	public TemplateHashModel getStaticClassWrapper ( Class<?> clazz )
	{
		String className = clazz.getName ();
		
		try
		{
			BeansWrapper wrapper = new BeansWrapperBuilder ( this.templateConfig.getIncompatibleImprovements () )
				.build ();
			TemplateHashModel staticModels = wrapper.getStaticModels ();
			return (TemplateHashModel) staticModels.get ( className );
		}
		catch ( TemplateModelException ex )
		{
			throw new UncheckedTemplateException ( 
				"Error while getting template wrapper for '" + className + "': " + ex.getMessage (), ex 
			);
		}				
	}
}
