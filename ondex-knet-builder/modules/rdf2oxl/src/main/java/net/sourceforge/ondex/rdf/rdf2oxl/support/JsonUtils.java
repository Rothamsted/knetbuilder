package net.sourceforge.ondex.rdf.rdf2oxl.support;

import static java.util.Arrays.asList;
import static org.apache.commons.collections15.CollectionUtils.containsAny;

import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;
import java.util.stream.Stream;

/**
 * A few utilities to work with JSON in OXL2RDF.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Aug 2018</dd></dl>
 *
 */
public class JsonUtils
{
	/**
	 * A variant of {@link #indexJsArray(Stream, String)}. 
	 */
	public static Map<String, Object> indexJsArray ( List<Map<String, Object>> jsArray, String key )
	{
		return indexJsArray ( jsArray.parallelStream (), key );
	}

	/**
	 * Indexes an array of maps, by building a map of key -> object pairs. This assumes that for each 
	 * array's element there is a key field, to which one object only is associated.  
	 */
	public static Map<String, Object> indexJsArray ( Stream<Map<String, Object>> jsArray, String key ) 
	{
		return jsArray
			.collect ( Collectors.toMap ( e -> (String) e.get ( key ), e -> e ) );
	}

	/**
	 * Filters an array of maps by first extracting the property `prop` from each of the map in the array
	 * and then passing it to the `predicate` parameter. 
	 */
	public static Stream<Map<String, Object>> filterOnProp ( 
		Stream<Map<String, Object>> jsArray, String prop, Function<Object, Boolean> predicate 
	)
	{
		return jsArray.filter ( obj -> predicate.apply ( obj.get ( prop ) ) );
	}

	/**
	 * Returns any map in the array of maps `jsArray` that has some intersection between the value(s) returned 
	 * by the property `prop` and the `values` parameter.
	 */
	public static Stream<Map<String, Object>> filterOnProp ( 
		Stream<Map<String, Object>> jsArray, String prop, Object... values 
	)
	{
		return filterOnProp ( jsArray, prop, jsObj -> containsAny ( toList ( jsObj ), asList ( values ) ) );
	}
	
	/**
	 * Assumes `jsonLdGraph` is a `JSON-LD` structure, ie, containing objects that have the `@type` key, and filters
	 * those objects that are instances of any value in `types` (ie, having `@type` in `types`).  
	 * 
	 * It also indexes the results over the `@id` property (using {@link #indexJsArray(Stream, String)}). 
	 */
	public static Map<String, Object> indexJsonLdTypes ( List<Map<String, Object>> jsonLdGraph, String...types )
	{
  	return JsonUtils.indexJsArray (
  		JsonUtils.filterOnProp ( jsonLdGraph.stream (), "@type", (Object[]) types ), 
  		"@id"
  	);
	}
	
	/**
	 * Simple facility to turn a value to a {@link List}, whether it is initially already a list or a single value of
	 * another type.
	 */
	@SuppressWarnings ( "unchecked" )
	public static <T> List<T> toList ( Object jsObj )
	{
		if ( jsObj == null ) return new ArrayList<> ();
		if ( jsObj instanceof List ) return (List<T>) jsObj;
		return new ArrayList<> ( Collections.singleton ( (T) jsObj ) );
	}
}
