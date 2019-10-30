package net.sourceforge.ondex.core.util;

import static java.lang.String.format;
import static org.apache.commons.lang3.StringUtils.abbreviate;

import java.util.Collection;
import java.util.HashMap;
import java.util.Map;
import java.util.Optional;
import java.util.function.Function;
import java.util.stream.Stream;

import javax.crypto.KeyGenerator;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.sourceforge.ondex.core.Attribute;
import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ONDEXEntity;
import net.sourceforge.ondex.core.ONDEXGraph;
import uk.ac.ebi.utils.runcontrol.PercentProgressLogger;

/**
 * TODO: comment me!
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>Oct 25, 2019</dd></dl>
 *
 */
public class GraphMemIndex
{
	private final ONDEXGraph graph; 

	/**
	 * The indexed and cached objects that we maintain for the current graph. Essentially it is a dynamic table of
	 * key name, key object =&gt; object.
	 * 
	 */
	private Table<String, Object, Object> cache;
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	
	
	private static Map<ONDEXGraph, GraphMemIndex> instances = new HashMap<> ();

	/**
	 * Similarly to {@link CachedGraphWrapper#getInstance(ONDEXGraph)}, we recommend to use this to get a wrapper that 
	 * caches this graph.
	 *  
	 * @param graph
	 */
	public static synchronized GraphMemIndex getInstance ( ONDEXGraph graph ) 
	{
		return instances.computeIfAbsent ( graph, g -> new GraphMemIndex ( g ) );
	}
	
	
	/**
	 * This will create the index using {@link #createIndex()}, so it will take time (and might need synchronisation).
	 * 
	 */
	public GraphMemIndex ( ONDEXGraph graph )
	{
		this.graph = graph;
		this.updateIndex ();
	}

	/**
	 * Get an entry for the index, null if it doesn't exist. 
	 * @see #createIndex()
	 */
	@SuppressWarnings ( "unchecked" )
	public <OO, KV> OO get ( String fieldName, KV keyValue )
	{
		if ( this.cache == null ) throw new IllegalStateException ( 
			"Index was clear()-ed, call updateIndex() again to keep using its methods"
		);
		return (OO) this.cache.get ( fieldName, (KV) keyValue );
	}
	
		
	
	/**
	 * <p>Does all the job of creating the index.</p>
	 * 
	 * <p><b>NOTE</b>: this is written so that a fixed number of things are indexed here (at the moment, just the 'iri' 
	 * attribute. Should you need more, change this method or even introduce some configuration (eg, list of attributes
	 * to index).</p>
	 */
	private void createIndex ()
	{
		log.info ( "Ondex Memory Index, starting indexing operations" );

		this.indexStringAttribute ( this.graph.getConcepts (), "iri", "iri" );
		this.indexStringAttribute ( this.graph.getRelations (), "iri", "iri" );
	}
	
	/**
	 * <p>Deletes the index, useful to free the memory.</p>
	 * 
	 * <p><b>WARNING</b>: You'll get errors after having invoked this method, unless you call {@link #updateIndex()} 
	 * (in which case you typically won't need to clear me explicitly since the update implies that).</p>
	 * 
	 */
	public void clear ()
	{
		this.cache = HashBasedTable.create ();
	}
	
	/**
	 * Updates the index after the graph has changed.
	 */
	public void updateIndex ()
	{
		this.clear ();
		this.createIndex ();
	}
	
		
	/**
	 * Indexes ONDEX objects, creating entries in the cache, which of field name is fieldName and field value is 
	 * given by keyGenerator. In other words, if {@code oo} is in {@code odxObjects}, then 
	 * {@link #get(String, Object) get ( fieldName, keyGenerator ( oo ) ) } will return {@code oo} after having called
	 * this method.
	 *  
	 * @param <OO> the type of objects being indexed 
	 * @param <KV> the type of key
	 * @param odxObjects the objects being indexed
	 * @param fieldName the field name used for this index
	 * @param keyGenerator a function that generates a key for every object in odxObjects. If this returns null for 
	 * a value, a warning is logged and the corresponding object is ignored.
	 *   
	 */
	@SuppressWarnings ( "unchecked" )
	private <OO, KV> void indexObjects ( 
		PercentProgressLogger progressLogger, Stream<OO> odxObjects, String fieldName, Function<OO, KV> keyGenerator 
	)
	{
		odxObjects.forEach ( odxObjs ->
		{
			try
			{
				// Get the key via the generator and perform several checks about null
				//
				Optional<Object> keyValOpt = Optional.ofNullable ( keyGenerator.apply ( odxObjs ) );
				
				KV keyVal = (KV) keyValOpt.orElseGet ( () -> 
				{
					if ( log.isWarnEnabled () ) log.warn 
					(
						"=== NO VALUE FOR THE ONDEX MEMORY INDEX, field: \"{}\", entity: {}", 
						fieldName, 
						abbreviate ( odxObjs.toString (), 30 ) 
					);
					return null;
				});
				
				if ( keyVal == null ) return;

				// OK, we have a key, let's see if it has duplicates and possibly store the new value
				//
				KV oldEntry = (KV) this.cache.get ( fieldName, keyVal );
				if ( oldEntry != null )
				{
					if ( log.isWarnEnabled () ) log.warn 
					(
						"=== DUPED VALUES FOR THE ONDEX MEMORY INDEX, field: \"{}\", key: \"{}\", entity: {}", 
						fieldName,					
						abbreviate ( keyVal.toString (), 1000 ), 
						abbreviate ( odxObjs.toString (), 30 ) 
					);
					return;
				}
				
				this.cache.put ( fieldName, keyVal, odxObjs );
				
			} // indexObjects() body
			finally {
				if ( progressLogger == null ) return;
				progressLogger.updateWithIncrement ();
			}
		}); // indexObjects() foreach
	} // indexObjects()
	
	
	/**
	 * <p>A wrapper of {@link #indexObjects(Stream, String, Function)}, which uses 
	 * {@link ONDEXEntity#getAttribute(AttributeName) ONDEX attributes} as index keys.</p>
	 * 
	 * <p>if the {@link ONDEXGraph#getAttributeName attribute type} doesn't existin in the current graph, it reports a 
	 * warning and doesn't index anything.</p>
	 * 
	 * <p>You have to take care of null cases in {@link KeyGenerator}, ie, when the an entity doesn't return an
	 * attribute for {@code attribName}, or when the {@link Attribute#getValue() value} returned by an entity
	 * is null.</p> 
	 * 
	 */
	private <OE extends ONDEXEntity, AV, KV> void indexAttribute ( 
		Collection<OE> odxEntities, String attribName, String fieldName, Function<Attribute, KV> keyGenerator 
	)
	{
		AttributeName attrType = this.graph.getMetaData ().getAttributeName ( attribName );
		if ( attrType == null ) {
			log.warn ( "==== NO ATTRIBUTE TYPE \"{}\" AVAILABLE FOR THE ONDEX MEMORY INDEX, skipping ===", attribName );
			return;
		}
		
		// Setup the progress reporter and do initial logging
		String type = ONDEXGraphUtils.getEntityType ( odxEntities ); // "concept" or "relation"

		log.info ( 
			"indexing {} {}(s) against the field/attribute: \"{}\"/\"{}\"",
			odxEntities.size (),
			type,
			fieldName,
			attribName
		);
		PercentProgressLogger progressLogger = new PercentProgressLogger ( "{}% of " + type + "s indexed", odxEntities.size () ); 

		// The attribute extractor
		Function<OE, Attribute> attrbConverter = odxEntity -> odxEntity.getAttribute ( attrType );
		Function<OE, KV> finalKeyGenerator = attrbConverter.andThen ( keyGenerator );
		
		// Here we go
		this.indexObjects ( progressLogger, odxEntities.stream (), fieldName, finalKeyGenerator );
	}

	
	/**
	 * A wrapper of {@link #indexAttribute(Stream, String, String, Function)} that consider the simplified case where a
	 * given attribute is expected to store string values.
	 * 
	 */
	private <OE extends ONDEXEntity> void indexStringAttribute ( 
		Collection<OE> odxEntities, String attribName, String fieldName 
	)
	{
		// Define an attribute converter that extract its string value, after having checked the type.
		//
		Function<Attribute, String> finalKeyGenerator = attr ->
		( 
			Optional.ofNullable ( attr )
			 .map ( attr1 -> 
			 {
				 Object av = attr1.getValue ();
				 if ( ! ( av instanceof String ) ) throw new IllegalArgumentException ( 
						format ( "The attribute \"{}\" has a non-string value: {}",	attribName, abbreviate ( av.toString (), 30 ) )
				 );
				 return (String) av;
			 })
			 .orElse ( null )
		);

		this.indexAttribute ( odxEntities, attribName, fieldName, finalKeyGenerator );
	}
}
