package net.sourceforge.ondex.rdf.rdf2oxl.support.freemarker;

import static java.lang.String.format;
import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;

import java.io.IOException;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.io.Writer;
import java.util.HashMap;
import java.util.Map;

import org.apache.jena.rdf.model.Model;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;

import com.github.jsonldjava.utils.JsonUtils;

import freemarker.ext.beans.BeansWrapper;
import freemarker.ext.beans.BeansWrapperBuilder;
import freemarker.template.Configuration;
import freemarker.template.Template;
import freemarker.template.TemplateException;
import freemarker.template.TemplateHashModel;
import freemarker.template.TemplateModelException;
import info.marcobrandizi.rdfutils.XsdMapper;
import info.marcobrandizi.rdfutils.namespaces.NamespaceUtils;
import net.sourceforge.ondex.rdf.rdf2oxl.support.Rdf2OxlConfiguration;

/**
 * A few utilities to use the FreeMarker template engine.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>25 Jul 2018</dd></dl>
 *
 */
@Component
public class FreeMarkerHelper
{
	private Configuration templateConfig;

	/**
	 * Defaults to `data = null`
	 */
	public void processTemplate ( String templateName, Writer outWriter ) {
		processTemplate ( templateName, outWriter, null );
	}

	/**
	 * Invokes a template with some data.
	 * 
	 */
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
	
	/**
	 * Gets a map of data from a Jena RDF {@link Model}. In order to do so, it converts the `model` into `JSON-LD`
	 * (using the Jena's internals) and then extracts the `@graph` object from the result, to put it into the a `js`
	 * key in the resulting map.  
	 * 
	 * Moreover, it adds some utilities to the same resulting map, so that they can be invoked from within FreeMarker
	 * templates (see the implementation).  
	 * 
	 */
	public Map<String, Object> getTemplateData ( Model model )
	{
		try
		{
			Map<String, Object> result = new HashMap<> ();

			if ( model != null )
			{
				StringWriter sw = new StringWriter ();
				model.write ( sw, "JSON-LD" );
							
				// We need some post-processing of what Jena returns
				@SuppressWarnings ( "unchecked" )
				Map<String, Object> js = (Map<String, Object>) JsonUtils.fromString ( sw.toString () );
							
				result.put ( "js", js.get ( "@graph" ) );
				
				// We also need to let our namespace manager know the namespaces used in this model
				NamespaceUtils.registerNs ( model.getNsPrefixMap () );
			}

			// These might be useful in several cases
			addStaticClassWrapper ( result, NamespaceUtils.class );
			addStaticClassWrapper ( result, net.sourceforge.ondex.rdf.rdf2oxl.support.JsonUtils.class );
			addStaticClassWrapper ( result, XsdMapper.class );
	
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
	
	/**
	 * See {@link Rdf2OxlConfiguration#getTemplateEngineConfiguration()}
	 */
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
			BeansWrapper wrapper = new BeansWrapperBuilder ( this.templateConfig.getIncompatibleImprovements () ).build ();
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
	
	/**
	 * See {@link #getStaticClassWrapper(Class)}
	 */
	public void addStaticClassWrapper ( Map<String, Object> data, String name, Class<?> clazz )
	{
		data.put ( name, getStaticClassWrapper ( clazz ) );
	}

	/**
	 * Gets the name to be seen inside FreeMarker from the {@link Class#getSimpleName() class's simple name}.  
	 */
	public void addStaticClassWrapper ( Map<String, Object> data, Class<?> clazz )
	{
		addStaticClassWrapper ( data, clazz.getSimpleName (), clazz );
	}
}
