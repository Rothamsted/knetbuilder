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
import java.util.stream.Collectors;

import org.apache.commons.lang3.text.StrSubstitutor;
import org.apache.jena.rdf.model.Model;
import org.apache.jena.rdf.model.ModelFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.github.jsonldjava.utils.JsonUtils;

import uk.ac.ebi.utils.exceptions.TooManyValuesException;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>15 Mar 2021</dd></dl>
 *
 */
public class GraphDescriptorExporter
{
	private Logger log = LoggerFactory.getLogger ( this.getClass () ); 
	
	public String instantiateTemplate ( Map<String, Object> graphMetadata, String rdfTemplate )
	{
		return StrSubstitutor.replace ( rdfTemplate, graphMetadata );
	}
	
	public Model getDescriptor ( Map<String, Object> graphMetadata, String rdfTemplate, String rdfLang )
	{
		var rdfStr = instantiateTemplate ( graphMetadata, rdfTemplate );
		Model result = ModelFactory.createDefaultModel ();
		Reader rdfReader = new StringReader ( rdfStr );
		result.read ( rdfReader, null, rdfLang );
		return result;
	}
	
	public Map<String, Object> getDescriptorAsJsonLd ( Model descriptor, boolean doSimplify )
	{
		try
		{
			StringWriter sw = new StringWriter ();
			descriptor.write ( sw, "JSON-LD" );
			
			String json = sw.toString ();
			log.trace ( "Extracted JSON: {}", json );
			
			// We need some post-processing of what Jena returns
			@SuppressWarnings ( "unchecked" )
			Map<String, Object> js = (Map<String, Object>) JsonUtils.fromString ( json );
			
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

	public Map<String, List<Map<String, Object>>> getJsonLdTypeIndex ( Map<String, Object> jsonLd )
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
	
	
	public String toJson ( Map<String, Object> json )
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
		
		//return new JSONObject ( json ).toString ( 2 );		
	}
	 
	@SuppressWarnings ( "unchecked" )
	public <T> T getSingleJsonValue ( Map<String, Object> jsobj, String property, boolean failIfMany )
	{
		Object value = jsobj.get ( property );
		if ( value == null ) return null;
		if ( !(value instanceof List) ) return (T) value;

		var list = (List<?>) value;
		if ( list.size () == 0 ) return null;
		
		if ( list.size () > 1 )
		{
			var msg = String.format ( "Too many values (%d) for the property: %s", list.size (), property );
			if ( failIfMany ) throw new TooManyValuesException ( msg );
			else
				log.trace ( msg );
		}
		
		return (T) list.get ( 0 );
	}
}
