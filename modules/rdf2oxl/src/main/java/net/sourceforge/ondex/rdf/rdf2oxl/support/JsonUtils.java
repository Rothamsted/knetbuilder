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
 * TODO: Never used yet, maybe to be removed.
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>29 Aug 2018</dd></dl>
 *
 */
public class JsonUtils
{
	public static Map<String, Object> indexJsArray ( List<Map<String, Object>> jsArray, String key )
	{
		return indexJsArray ( jsArray.parallelStream (), key );
	}

	public static Map<String, Object> indexJsArray ( Stream<Map<String, Object>> jsArray, String key ) 
	{
		return jsArray
			.collect ( Collectors.toMap ( e -> (String) e.get ( key ), e -> e ) );
	}

	public static Stream<Map<String, Object>> filterOnProp ( 
		Stream<Map<String, Object>> jsArray, String prop, Function<Object, Boolean> predicate 
	)
	{
		return jsArray.filter ( obj -> predicate.apply ( obj.get ( prop ) ) );
	}

	public static Stream<Map<String, Object>> filterOnProp ( 
		Stream<Map<String, Object>> jsArray, String prop, Object... values 
	)
	{
		return filterOnProp ( jsArray, prop, jsObj -> containsAny ( toList ( jsObj ), asList ( values ) ) );
	}
	
	public static Map<String, Object> indexJsonLdTypes ( List<Map<String, Object>> jsonLdGraph, String...types )
	{
  	return JsonUtils.indexJsArray (
  		JsonUtils.filterOnProp ( jsonLdGraph.stream (), "@type", (Object[]) types ), 
  		"@id"
  	);
	}
	
	@SuppressWarnings ( "unchecked" )
	public static <T> List<T> toList ( Object jsObj )
	{
		if ( jsObj == null ) return 
			new ArrayList<T> ();
		if ( jsObj instanceof List )
			return (List<T>) jsObj;
		return new ArrayList<T> ( Collections.singleton ( (T) jsObj ) );
	}
}
