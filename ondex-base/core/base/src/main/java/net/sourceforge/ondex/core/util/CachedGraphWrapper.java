package net.sourceforge.ondex.core.util;

import java.util.HashMap;
import java.util.Map;
import java.util.function.Supplier;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import com.google.common.collect.HashBasedTable;
import com.google.common.collect.Table;

import net.sourceforge.ondex.core.AttributeName;
import net.sourceforge.ondex.core.ConceptAccession;
import net.sourceforge.ondex.core.ConceptClass;
import net.sourceforge.ondex.core.DataSource;
import net.sourceforge.ondex.core.EvidenceType;
import net.sourceforge.ondex.core.ONDEXConcept;
import net.sourceforge.ondex.core.ONDEXGraph;
import net.sourceforge.ondex.core.ONDEXRelation;
import net.sourceforge.ondex.core.RelationType;
import net.sourceforge.ondex.core.Unit;
import net.sourceforge.ondex.core.util.prototypes.AccessionPrototype;
import net.sourceforge.ondex.core.util.prototypes.ConceptClassPrototype;
import net.sourceforge.ondex.core.util.prototypes.DataSourcePrototype;
import net.sourceforge.ondex.core.util.prototypes.EvidenceTypePrototype;
import net.sourceforge.ondex.core.util.prototypes.RelationTypePrototype;

/**
 * <p>A wrapper for {@link ONDEXGraph} that has the main scope of avoiding to create duplicates when creating new
 * ONDEX entities. Methods are similar to createXXX methods in the {@link ONDEXGraph} interface, each method
 * calls the corresponding underlining creation method, but only if the required object hasn't been created yet.</p>
 * 
 * <p>The version of getXXX() that receives only the type/key parameters simply return the corresponding object in the
 * cache, if it is present, null otherwise</p>
 * 
 * <p>Clearly, this is based on an internal static set of caches, that can be on a per-graph basis 
 * (see {@link #getInstance(ONDEXGraph)}).</p>
 * 
 * <p>Note that this class would normally be a <a href = "https://en.wikipedia.org/wiki/Decorator_pattern">decorator</a>, 
 * but we prefer not to implement this way here (for the time being), for it would require too much review of 
 * existing code.</p>
 * 
 * <p><b>WARNING</>: the getXXX() methods below work well ONLY for empty graphs, which are being created. For instance,
 * {@link #getConcept(String, String, String, DataSource, ConceptClass, EvidenceType)} assumes that NO CONCEPT like the 
 * parameter has been created yet when you call the method for the first time. Then it creates a new concept and caches
 * it for subsequent calls to the same method or to {@link #getConcept(String)}.</p>
 *
 * @author brandizi
 * <dl><dt>Date:</dt><dd>12 Apr 2017</dd></dl>
 *
 * TODO: must be reviewed, the cached elements should be retrievable from either the graph or its metadata.
 *
 *
 */
public class CachedGraphWrapper
{
	protected final ONDEXGraph graph; 
	
	/**
	 * The indexed and cached objects that we maintain for the current graph. Essentially it is a dynamic table of
	 * object type (identified by its class), object key as string =&gt; object.
	 *    
	 */
	private final Table<Class<Object>, String, Object> cache = HashBasedTable.create ();
	private Logger log = LoggerFactory.getLogger ( this.getClass () );
	
	private static Map<ONDEXGraph, CachedGraphWrapper> instances = new HashMap<> ();
	
	/**
	 * We recommend to use this to get a wrapper that caches this graph.
	 *  
	 * @param graph
	 */
	public static synchronized CachedGraphWrapper getInstance ( ONDEXGraph graph ) 
	{
		return instances.computeIfAbsent ( graph, g -> new CachedGraphWrapper ( g ) );
	}

	
	public CachedGraphWrapper ( ONDEXGraph graph )
	{
		this.graph = graph;
	}

		
	public synchronized ConceptClass getConceptClass ( String id, String fullName, String description, ConceptClass specialisationOf )
	{
		return this.cacheGet ( 
			ConceptClass.class, id, 
			() -> this.graph.getMetaData ().createConceptClass ( id, fullName, description, specialisationOf )
		);
	}

	public synchronized ConceptClass getConceptClass ( ConceptClassPrototype proto )
	{
		try 
		{
			// Let's see if it has a parent
			if ( proto.getParent () == null )
			{
				// Or a prototype to build it. In case of loops, this will lead to stack overflow
				ConceptClassPrototype parentProto = proto.getParentPrototype ();
				if ( parentProto != null ) proto.setParent ( this.getConceptClass ( parentProto ) );
			}
			
			return this.cacheGet ( 
				ConceptClass.class, proto.getId (), 
				() -> this.graph.getMetaData ().getFactory ().createConceptClass ( 
				proto.getId (), proto.getFullName (), proto.getDescription (), 
				proto.getParent () 
			));
		}
		catch ( StackOverflowError ex ) 
		{
			log.error ( "Stackoverflow error while creating the concept class '{}'. Do you have circular references?", proto.getId () );
			throw ex;
		}
	}

	public synchronized ONDEXConcept getConcept (
		String id, String annotation, String description, DataSource ds, ConceptClass conceptClass, EvidenceType evidence
	)
	{
		return this.cacheGet ( 
			ONDEXConcept.class, id, 
			() -> this.graph.getFactory ().createConcept ( id, annotation, description, ds, conceptClass, evidence )
		);
	}
	
	public synchronized ONDEXConcept getConcept ( String id ) 
	{
		return this.cacheGet ( ONDEXConcept.class, id );
	}
	
	public synchronized RelationType getRelationType ( 
		String id, boolean isAntisymmetric, boolean isReflexive, boolean isSymmetric, boolean isTransitive 
	)
	{
		return this.cacheGet ( 
			RelationType.class, id, 
			() -> this.graph.getMetaData ().getFactory ().createRelationType (
				id, isAntisymmetric, isReflexive, isSymmetric, isTransitive 
			)
		);
	}

	public synchronized RelationType getRelationType ( RelationTypePrototype proto )
	{
		try 
		{
			// Let's see if it has a parent
			if ( proto.getParent () == null )
			{
				// Or a prototype to build it. In case of loops, this will lead to stack overflow
				RelationTypePrototype parentProto = proto.getParentPrototype ();
				if ( parentProto != null )
						proto.setParent ( this.getRelationType ( parentProto ) );
			}
			
			return this.cacheGet ( 
				RelationType.class, proto.getId (), 
				() -> this.graph.getMetaData ().getFactory ().createRelationType ( 
					proto.getId (), proto.getFullName (), proto.getDescription (), 
					proto.isAntisymmetric (), proto.isReflexive (), proto.isSymmetric (), proto.isTransitive (), 
					proto.getParent () )
			);
		}
		catch ( StackOverflowError ex ) 
		{
			log.error ( "Stack Overflow error while creating relation '{}'. Do you have circular references?", proto.getId () );
			throw ex;
		}
	}
	
	
	public synchronized ONDEXRelation getRelation ( ONDEXConcept from, ONDEXConcept to, RelationType type, EvidenceType evidence )
	{
		String id = from.getPID () + to.getPID () + type.getId () + evidence.getId ();
		return this.cacheGet ( 
				ONDEXRelation.class, id, 
			() -> this.graph.getFactory ().createRelation ( from, to, type, evidence )
		);
	}
	
	public synchronized EvidenceType getEvidenceType ( String id, String fullName, String description )
	{
		return this.cacheGet ( 
			EvidenceType.class, id, 
			() -> this.graph.getMetaData ().createEvidenceType ( id, fullName, description ) 
		);
	}
	
	public synchronized EvidenceType getEvidenceType ( EvidenceTypePrototype proto )
	{
		return this.getEvidenceType ( proto.getId (), proto.getFullName (), proto.getDescription () );
	}
	
	
	public synchronized DataSource getDataSource ( String id, String fullName, String description )
	{
		return this.cacheGet ( 
			DataSource.class, id, 
			() -> this.graph.getMetaData ().createDataSource ( id, fullName, description ) 
		);
	}

	public synchronized DataSource getDataSource ( DataSourcePrototype proto )
	{
		return this.getDataSource ( proto.getId (), proto.getFullName (), proto.getDescription () );
	}
	
	public synchronized ConceptAccession getAccession ( String accession, DataSource dataSrc, boolean isAmbiguous, ONDEXConcept concept )
	{
		// TODO: is the ID unique? Is it concept-unique? 
		return this.cacheGet ( 
			ConceptAccession.class, accession,
			() -> concept.createConceptAccession ( accession, dataSrc, isAmbiguous )
		);		
	}
	
	public synchronized ConceptAccession getAccession ( AccessionPrototype proto, ONDEXConcept concept )
	{
		// Let's see if it has a parent
		if ( proto.getDataSource () == null )
		{
			// Or a prototype to build it. In case of loops, this will lead to stack overflow
			DataSourcePrototype dsProto = proto.getDataSourcePrototype ();
			if ( dsProto != null ) proto.setDataSource ( this.getDataSource ( dsProto ) );
		}
		
		return this.getAccession ( proto.getAccession (), proto.getDataSource (), proto.isAmbiguous (), concept );
	}	
	
	/**
	 * @Deprecated use {@link ONDEXGraphUtils#getOrCreateAttributeName(ONDEXGraph, String, String, String, Class, Unit, AttributeName)} 
	 * instead
	 */
	@Deprecated( forRemoval = true )
	public synchronized AttributeName getAttributeName ( 
		String id, String fullName, String description, Unit unit, Class<?> datatype, AttributeName parent 
	)
	{
		return ONDEXGraphUtils.getOrCreateAttributeName ( 
			graph, id, fullName, description, datatype, unit, parent 
		);
	}

	@Deprecated( forRemoval = true )
	public synchronized AttributeName getAttributeName ( 
		String id, String fullName, String description, Class<?> datatype 
	)
	{
		return this.getAttributeName ( id, fullName, description, null, datatype, null );
	}
		
	/**
	 * Facility to return cached objects, or, create and return them, if not already in the cache. 
	 */	
	@SuppressWarnings ( "unchecked" )
	private <V> V cacheGet ( Class<? super V> type, String key, Supplier<V> newValueGenerator )
	{
		V result = (V) this.cache.get ( type, key );
		if ( result != null ) return result;
		
		this.cache.put ( (Class<Object>) type, key, result = newValueGenerator.get () );
		return result;
	}

	/**
	 * Like {@link #cacheGet(Class, String, Supplier)}, but just returns null if the type/key is not in the cache.  
	 */
	@SuppressWarnings ( "unchecked" )	
	private <V> V cacheGet ( Class<? super V> type, String key )
	{
		return (V) this.cache.get ( type, key );
	}
}
