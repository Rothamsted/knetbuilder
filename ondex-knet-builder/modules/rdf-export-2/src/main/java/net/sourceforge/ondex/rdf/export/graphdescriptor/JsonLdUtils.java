package net.sourceforge.ondex.rdf.export.graphdescriptor;

import static uk.ac.ebi.utils.exceptions.ExceptionUtils.buildEx;

import java.io.IOException;
import java.io.Reader;
import java.io.StringReader;
import java.io.StringWriter;
import java.io.UncheckedIOException;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.core.JsonLdProcessor;
import com.github.jsonldjava.utils.JsonUtils;

import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * Various utilities to manage JSON and its Java representation as {@link Map}.
 * 
 * TODO: move it to jutils. 
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Mar 2021</dd></dl>
 *
 */
public class JsonLdUtils
{
	private static Logger slog = LoggerFactory.getLogger ( JsonLdUtils.class ); 
	
	private JsonLdUtils () {}
	
	/**
	 * Gets an RDF template, populates its placeholders with graphMetadata and read it into a Jena model.
	 */
	public static Model getRdfFromTemplate ( Map<String, Object> context, String rdfTemplate, String rdfLang )
	{
		var rdfStr = StrSubstitutor.replace ( rdfTemplate, context );
		Model result = ModelFactory.createDefaultModel ();
		Reader rdfReader = new StringReader ( rdfStr );
		result.read ( rdfReader, null, rdfLang );
		return result;
	}
	
	public static Model getRdfFromUris ( String contextUri, String rdfTemplateUri, String rdfLang )
	{
		return null; // TODO
	}
	
	
	public static Map<String, Object> getProperties ( String uri )
	{
		return null; // TODO
	}
	
	
	/**
	 * Turns a Jena model into JSON-LD. if doSimplify is on, transforms the original JSON-LD (a JSON object 
	 * containing @graph and @context at its root) into a map of URI -> object, with a special
	 * object having the '@context' key. 
	 */
	public static Map<String, Object> rdf2JsonLd ( Model model, boolean doSimplify )
	{
		try
		{
			StringWriter sw = new StringWriter ();
			model.write ( sw, "JSON-LD" );
			
			String json = sw.toString ();
			slog.trace ( "Extracted JSON: {}", json );
			
			// We need some post-processing of what Jena returns
			@SuppressWarnings ( "unchecked" )
			var js = (Map<String, Object>) JsonUtils.fromString ( json );
						
			if ( !doSimplify ) return js;
			
			Map<String, Object> result = new HashMap<> ();
			
			@SuppressWarnings ( "unchecked" )
			var jsObjects = (List<Map<String, Object>>) js.get ( "@graph" );
			jsObjects.forEach ( obj -> result.put ( (String) obj.get ( "@id" ), obj ) );
						
			result.put ( "@context", js.get ( "@context" ) );
			
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
	 * Defaults to doSimplify = false 
	 */
	public static Map<String, Object> rdf2JsonLd ( Model model )
	{
		return rdf2JsonLd ( model, false );
	}

	/**
	 * Creates an index for a map of JSON-LD objects (URI->Object), by grouping them based on their 
	 * @type. This is mostly useful when you expect given types to be contained in your RDF.
	 * 
	 */
	public static Map<String, List<Map<String, Object>>> indexByType ( Map<String, Object> jsonLd )
	{
		@SuppressWarnings ( "unchecked" )
		var typeIndex = jsonLd.values ()
		.stream ()
		.filter ( obj -> obj instanceof Map )
		.map ( obj -> (Map<String, Object>) obj )
		.filter ( obj -> obj.get ( "@type" ) != null )
		.collect ( Collectors.groupingBy ( obj -> (String) obj.get ( "@type" ) ) );
		
		return typeIndex;
	}
	
		
	/**
	 * Simple helper to serialise a {@link Map} object into JSON. 
	 */
	public static String serialize ( Map<String, Object> json )
	{
		try {
			return JsonUtils.toPrettyString ( json );
		}
		catch ( IOException ex )
		{
			throw buildEx ( 
				UncheckedIOException.class, ex,
				"Error while turning JSON to string: ", ex.getMessage () 
			);
		}
	}
	 
	/**
	 * Simple helper to get the single property value for an object.
	 * The object can possibly be multi-valued, if it is and failIfMany is false, returns the first
	 * value (ie, an undetermined value), else throws {@link TooManyValuesException}.
	 *  
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> T asValue ( Map<String, Object> jsobj, String property, boolean failIfMany )
	{
		Object value = jsobj.get ( property );
		if ( value == null ) return null;
		if ( !(value instanceof List) ) return (T) value;

		var list = (List<?>) value;
		if ( list.isEmpty () ) return null;
		
		if ( list.size () > 1 )
		{
			var msg = String.format ( "Too many values (%d) for the property: %s", list.size (), property );
			if ( failIfMany ) throw new TooManyValuesException ( msg );
			else
				slog.trace ( msg );
		}
		
		return (T) list.iterator ().next ();
	}
	
	/** 
	 * Defaults to failIfMany = false
	 */
	public static <T> T asValue ( Map<String, Object> jsobj, String property )
	{
		return asValue ( jsobj, property, false );
	}

	public static <T> T asValue ( Map<String, Object> jsobj, String property, Function<Object, T> strConverter, boolean failIfMany )
	{
		Object v = asValue ( jsobj, property, failIfMany );
		
		return strConverter.apply ( v );
	}

	public static <T> T asValue ( Map<String, Object> jsobj, String property, Function<Object, T> strConverter )
	{
		return asValue ( jsobj, property, strConverter, false );
	}

	public static Integer asInt ( Map<String, Object> jsobj, String property )
	{
		return (Integer) asValue ( jsobj, property, v -> {
			if ( v == null ) return null;
			if ( v instanceof Number ) return ((Number) v).intValue ();
			return Integer.valueOf ( v.toString () );
		});
	}

	
	/**
	 * Simple helper to get a list from a JSON object property.
	 * If the property contains a single value and failIfSingle is false, it wraps the value into a list, else throws
	 * {@link IllegalArgumentException}.
	 * 
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> List<T> asList ( Map<String, Object> jsobj, String property, boolean failIfSingle )
	{
		Object value = jsobj.get ( property );
		if ( value == null ) return List.of();
		if ( (value instanceof List) ) return (List<T>) value;

		var msg = String.format ( "The property: %s isn't a list", property );
		if ( failIfSingle ) throw new IllegalArgumentException ( msg );
		else
			slog.trace ( msg );

		return List.of ( (T) value );
	}
	
	/**
	 * Defaults to failIfSingle = false
	 */
	public static <T> List<T> asList ( Map<String, Object> jsobj, String property )
	{
		return asList ( jsobj, property, false );
	}
	
}